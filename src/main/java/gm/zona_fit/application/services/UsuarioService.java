package gm.zona_fit.application.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import gm.zona_fit.application.dto.UserDTO;
import gm.zona_fit.application.dto.UserRegisterDTO;
import gm.zona_fit.domain.Entities.Cliente;
import gm.zona_fit.domain.Entities.RolUsuario;
import gm.zona_fit.domain.Entities.Usuario;
import gm.zona_fit.infrastructure.IClienteRepository;
import gm.zona_fit.infrastructure.IUsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UsuarioService implements IUsuarioService {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDTO register(UserRegisterDTO dto) {
        if (usuarioRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException("Username already exists.");
        }

        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        var cliente = clienteRepository.save(new Cliente(
            null,
            dto.username(),
            "SIN_APELLIDO",
            null,
            null));

        var usuario = usuarioRepository.save(new Usuario(
                null,
                dto.username(),
                dto.email(),
                passwordEncoder.encode(dto.password()),
            RolUsuario.CLIENTE,
                cliente));

        return toDto(usuario);
    }

    @Override
    public List<UserDTO> getAll() {
        return usuarioRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public UserDTO getById(Integer id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("%s with id %s not found.", "Usuario", id)));
        return toDto(usuario);
    }

    private UserDTO toDto(Usuario usuario) {
        return new UserDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getCliente().getId());
    }
}

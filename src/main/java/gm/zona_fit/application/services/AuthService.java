package gm.zona_fit.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import gm.zona_fit.application.dto.AuthResponseDTO;
import gm.zona_fit.application.dto.UserLoginDTO;
import gm.zona_fit.application.dto.UserRegisterDTO;
import gm.zona_fit.infrastructure.IUsuarioRepository;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Override
    public AuthResponseDTO register(UserRegisterDTO dto) {
        var created = usuarioService.register(dto);

        var userDetails = User.withUsername(created.username())
                .password("N/A")
                .authorities("ROLE_" + created.rol())
                .build();

        var token = jwtService.generateToken(userDetails, created.rol(), created.id());

        return new AuthResponseDTO(token, created.username(), created.rol());
    }

    @Override
    public AuthResponseDTO login(UserLoginDTO dto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));

        var usuario = usuarioRepository.findByUsername(dto.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        var userDetails = User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities("ROLE_" + usuario.getRol().name())
                .build();

        var token = jwtService.generateToken(userDetails, usuario.getRol().name(), usuario.getId());
        return new AuthResponseDTO(token, usuario.getUsername(), usuario.getRol().name());
    }

    @Override
    public AuthResponseDTO oauthLogin(String email, String oauthName) {
        var maybeUsuario = usuarioRepository.findByEmail(email);

        if (maybeUsuario.isEmpty()) {
            throw new IllegalArgumentException("OAuth user is not registered. Please register first.");
        }

        var usuario = maybeUsuario.get();
        var userDetails = User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities("ROLE_" + usuario.getRol().name())
                .build();

        var token = jwtService.generateToken(userDetails, usuario.getRol().name(), usuario.getId());
        return new AuthResponseDTO(token, usuario.getUsername(), usuario.getRol().name());
    }
}

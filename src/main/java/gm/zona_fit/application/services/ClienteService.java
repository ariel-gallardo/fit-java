package gm.zona_fit.application.services;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gm.zona_fit.application.dto.ClientDTO;
import gm.zona_fit.domain.Entities.Cliente;
import gm.zona_fit.domain.Entities.Membresia;
import gm.zona_fit.infrastructure.IClienteRepository;
import gm.zona_fit.infrastructure.IMembresiaRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ClienteService implements IClienteService {

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private IMembresiaRepository membresiaRepository;

    @Override
    public List<Cliente> getAll() {
        return clienteRepository.findAll();
    }

    @Override
    public List<Cliente> getWithExpiredMembresia() {
        return clienteRepository.findByMembresiaIsNotNullAndMembresiaExpiraEnIsNotNullAndMembresiaExpiraEnBefore(LocalDateTime.now());
    }

    @Override
    public Cliente getById(Integer id) {
    var current = clienteRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(
            String.format("%s with id %s not found.", "Cliente", id)
        ));
        return current;
    }

    @Override
    public void delete(Integer id) {
        var exists = clienteRepository.existsById(id);
        if(!exists) {
            throw new EntityNotFoundException(
                String.format("%s with id %s not found.", "Cliente", id)
            );
        }
        clienteRepository.deleteById(id);
    }

    @Override
    public Integer create(ClientDTO cliente) {
        var newCliente = clienteRepository.save(new Cliente(
                null,
                cliente.nombre(),
                cliente.apellido(),
                resolveMembresia(cliente.membresia()),
                cliente.membresia() == null ? null : cliente.membresiaExpiraEn()));
        return newCliente.id();
    }

    @Override
    public void update(Integer id, ClientDTO cliente) {
    var current = clienteRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(
            String.format("%s with id %s not found.", "Cliente", id)
        ));
        clienteRepository.save(new Cliente(
            id,
            cliente.nombre(),
            cliente.apellido(),
            resolveMembresia(cliente.membresia()),
            cliente.membresia() == null ? null : cliente.membresiaExpiraEn()
        ));
    }

    @Override
    public void patchMembresia(Integer id, Integer membresiaId, LocalDateTime membresiaExpiraEn) {
        var current = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("%s with id %s not found.", "Cliente", id)
                ));

        current.setMembresia(resolveMembresia(membresiaId));
        current.setMembresiaExpiraEn(membresiaId == null ? null : membresiaExpiraEn);
        clienteRepository.save(current);
    }

    private Membresia resolveMembresia(Integer membresiaId) {
        if (membresiaId == null) {
            return null;
        }

        return membresiaRepository.findById(membresiaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("%s with id %s not found.", "Membresia", membresiaId)
                ));
    }
    
}

package gm.zona_fit.application.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gm.zona_fit.application.dto.ClientDTO;
import gm.zona_fit.domain.Entities.Cliente;
import gm.zona_fit.infrastructure.IClienteRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ClienteService implements IClienteService {

    @Autowired
    private IClienteRepository clienteRepository;

    @Override
    public List<Cliente> getAll() {
        return clienteRepository.findAll();
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
        var newCliente = clienteRepository.save(new Cliente(null, cliente.nombre(), cliente.apellido(), cliente.membresia()));
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
            cliente.membresia()
        ));
    }
    
}

package gm.zona_fit.unit.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import gm.zona_fit.application.dto.ClientDTO;
import gm.zona_fit.application.services.ClienteService;
import gm.zona_fit.domain.Entities.Cliente;
import gm.zona_fit.infrastructure.IClienteRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@Tag("unit/service")
class ClienteServiceUnitTest {

    @Mock
    private IClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void getAll_shouldReturnAllClientes() {
        var clientes = List.of(
                new Cliente(1, "Ana", "Gomez", 101),
                new Cliente(2, "Luis", "Perez", 102));

        when(clienteRepository.findAll()).thenReturn(clientes);

        var result = clienteService.getAll();

        assertEquals(2, result.size());
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    void getById_shouldReturnCliente_whenExists() {
        var cliente = new Cliente(1, "Ana", "Gomez", 101);
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));

        var result = clienteService.getById(1);

        assertNotNull(result);
        assertEquals("Ana", result.nombre());
        verify(clienteRepository).findById(1);
    }

    @Test
    void getById_shouldThrow_whenNotExists() {
        when(clienteRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clienteService.getById(99));
        verify(clienteRepository).findById(99);
    }

    @Test
    void create_shouldPersistAndReturnId() {
        var dto = new ClientDTO("Carlos", "Lopez", 200);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(new Cliente(10, "Carlos", "Lopez", 200));

        var id = clienteService.create(dto);

        assertEquals(10, id);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void update_shouldPersistChanges_whenExists() {
        var dto = new ClientDTO("Lucia", "Suarez", 300);
        when(clienteRepository.findById(2)).thenReturn(Optional.of(new Cliente(2, "Old", "Name", 111)));

        clienteService.update(2, dto);

        verify(clienteRepository).findById(2);
        verify(clienteRepository).save(new Cliente(2, "Lucia", "Suarez", 300));
    }

    @Test
    void update_shouldThrow_whenNotExists() {
        var dto = new ClientDTO("Lucia", "Suarez", 300);
        when(clienteRepository.findById(200)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clienteService.update(200, dto));

        verify(clienteRepository).findById(200);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void delete_shouldDelete_whenExists() {
        when(clienteRepository.existsById(1)).thenReturn(true);

        clienteService.delete(1);

        verify(clienteRepository).existsById(1);
        verify(clienteRepository).deleteById(1);
    }

    @Test
    void delete_shouldThrow_whenNotExists() {
        when(clienteRepository.existsById(404)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> clienteService.delete(404));

        verify(clienteRepository).existsById(404);
        verify(clienteRepository, never()).deleteById(any());
    }
}

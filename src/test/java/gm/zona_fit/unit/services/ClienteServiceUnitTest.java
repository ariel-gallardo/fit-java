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
import java.time.LocalDateTime;
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
import gm.zona_fit.domain.Entities.Membresia;
import gm.zona_fit.domain.Entities.MembresiaTipo;
import gm.zona_fit.infrastructure.IClienteRepository;
import gm.zona_fit.infrastructure.IMembresiaRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@Tag("unit/service")
class ClienteServiceUnitTest {

    @Mock
    private IClienteRepository clienteRepository;

    @Mock
    private IMembresiaRepository membresiaRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void getAll_shouldReturnAllClientes() {
        var basica = new Membresia(1, MembresiaTipo.SILVER, 100);
        var premium = new Membresia(2, MembresiaTipo.GOLD, 200);
        var clientes = List.of(
            new Cliente(1, "Ana", "Gomez", basica, LocalDateTime.parse("2030-01-01T00:00:00")),
            new Cliente(2, "Luis", "Perez", premium, LocalDateTime.parse("2030-06-01T00:00:00")));

        when(clienteRepository.findAll()).thenReturn(clientes);

        var result = clienteService.getAll();

        assertEquals(2, result.size());
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    void getWithExpiredMembresia_shouldReturnExpiredClientes() {
        var expired = List.of(
            new Cliente(7, "Pedro", "Ruiz", new Membresia(1, MembresiaTipo.SILVER, 100), LocalDateTime.parse("2020-01-01T00:00:00"))
        );

        when(clienteRepository.findByMembresiaIsNotNullAndMembresiaExpiraEnIsNotNullAndMembresiaExpiraEnBefore(any(LocalDateTime.class)))
            .thenReturn(expired);

        var result = clienteService.getWithExpiredMembresia();

        assertEquals(1, result.size());
        assertEquals("Pedro", result.get(0).getNombre());
        verify(clienteRepository).findByMembresiaIsNotNullAndMembresiaExpiraEnIsNotNullAndMembresiaExpiraEnBefore(any(LocalDateTime.class));
    }

    @Test
    void getById_shouldReturnCliente_whenExists() {
        var cliente = new Cliente(1, "Ana", "Gomez", new Membresia(1, MembresiaTipo.SILVER, 100), LocalDateTime.parse("2030-01-01T00:00:00"));
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
        var dto = new ClientDTO("Carlos", "Lopez", 2, LocalDateTime.parse("2031-01-01T10:00:00"));
        var premium = new Membresia(2, MembresiaTipo.GOLD, 200);
        when(membresiaRepository.findById(2)).thenReturn(Optional.of(premium));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(new Cliente(10, "Carlos", "Lopez", premium, LocalDateTime.parse("2031-01-01T10:00:00")));

        var id = clienteService.create(dto);

        assertEquals(10, id);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void update_shouldPersistChanges_whenExists() {
        var dto = new ClientDTO("Lucia", "Suarez", 3, LocalDateTime.parse("2031-08-01T12:00:00"));
        var vip = new Membresia(3, MembresiaTipo.BRONZE, 300);
        when(membresiaRepository.findById(3)).thenReturn(Optional.of(vip));
        when(clienteRepository.findById(2)).thenReturn(Optional.of(new Cliente(2, "Old", "Name", null)));

        clienteService.update(2, dto);

        verify(clienteRepository).findById(2);
        verify(clienteRepository).save(new Cliente(2, "Lucia", "Suarez", vip, LocalDateTime.parse("2031-08-01T12:00:00")));
    }

    @Test
    void update_shouldThrow_whenNotExists() {
        var dto = new ClientDTO("Lucia", "Suarez", 3, LocalDateTime.parse("2031-08-01T12:00:00"));
        when(clienteRepository.findById(200)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clienteService.update(200, dto));

        verify(clienteRepository).findById(200);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void patchMembresia_shouldAssignMembresia_whenExists() {
        var cliente = new Cliente(1, "Ana", "Gomez", null);
        var membresia = new Membresia(2, MembresiaTipo.GOLD, 200);

        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(membresiaRepository.findById(2)).thenReturn(Optional.of(membresia));

        var expiracion = LocalDateTime.parse("2032-01-01T00:00:00");
        clienteService.patchMembresia(1, 2, expiracion);

        assertEquals(2, cliente.getMembresia().getId());
        assertEquals(expiracion, cliente.getMembresiaExpiraEn());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void patchMembresia_shouldRemoveMembresia_whenNull() {
        var cliente = new Cliente(1, "Ana", "Gomez", new Membresia(1, MembresiaTipo.SILVER, 100), LocalDateTime.parse("2031-01-01T00:00:00"));
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));

        clienteService.patchMembresia(1, null, LocalDateTime.parse("2032-01-01T00:00:00"));

        assertEquals(null, cliente.getMembresia());
        assertEquals(null, cliente.getMembresiaExpiraEn());
        verify(clienteRepository).save(cliente);
        verify(membresiaRepository, never()).findById(any());
    }

    @Test
    void patchMembresia_shouldThrow_whenMembresiaNotExists() {
        var cliente = new Cliente(1, "Ana", "Gomez", null);
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(membresiaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clienteService.patchMembresia(1, 99, LocalDateTime.parse("2032-01-01T00:00:00")));

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

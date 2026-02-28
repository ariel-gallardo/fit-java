package gm.zona_fit.unit.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import gm.zona_fit.domain.Entities.Cliente;
import gm.zona_fit.infrastructure.IClienteRepository;

@DataJpaTest
@ActiveProfiles("tests")
@Tag("unit/repository")
@Sql(scripts = "/fake-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ClienteRepositoryUnitTest {

    @Autowired
    private IClienteRepository clienteRepository;

    @Test
    void findAll_shouldReturnSeededData() {
        var clientes = clienteRepository.findAll();

        assertEquals(3, clientes.size());
    }

    @Test
    void findById_shouldReturnCliente_whenExists() {
        var result = clienteRepository.findById(2);

        assertTrue(result.isPresent());
        assertEquals("Luis", result.get().nombre());
    }

    @Test
    void save_shouldPersistCliente() {
        var nuevo = new Cliente(null, "Carla", "Ruiz", 220);

        var saved = clienteRepository.save(nuevo);

        assertNotNull(saved.id());
        assertEquals("Carla", saved.nombre());
    }

    @Test
    void deleteById_shouldRemoveCliente() {
        clienteRepository.deleteById(1);

        var result = clienteRepository.findById(1);
        assertTrue(result.isEmpty());
    }
}

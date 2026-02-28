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

import gm.zona_fit.domain.Entities.Membresia;
import gm.zona_fit.domain.Entities.MembresiaTipo;
import gm.zona_fit.infrastructure.IMembresiaRepository;

@DataJpaTest
@ActiveProfiles("tests")
@Tag("unit/repository")
@Sql(scripts = "/fake-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MembresiaRepositoryUnitTest {

    @Autowired
    private IMembresiaRepository membresiaRepository;

    @Test
    void findAll_shouldReturnSeededData() {
        var membresias = membresiaRepository.findAll();

        assertEquals(3, membresias.size());
    }

    @Test
    void findById_shouldReturnMembresia_whenExists() {
        var result = membresiaRepository.findById(2);

        assertTrue(result.isPresent());
        assertEquals(MembresiaTipo.GOLD, result.get().getTipo());
    }

    @Test
    void save_shouldPersistMembresia() {
        var nueva = new Membresia(null, MembresiaTipo.GOLD, 450);

        var saved = membresiaRepository.save(nueva);

        assertNotNull(saved.getId());
        assertEquals(MembresiaTipo.GOLD, saved.getTipo());
    }
}

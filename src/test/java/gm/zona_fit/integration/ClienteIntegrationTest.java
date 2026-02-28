package gm.zona_fit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("tests")
@TestPropertySource(properties = "spring.main.web-application-type=servlet")
@Sql(scripts = "/fake-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Tag("integration")
class ClienteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAll_shouldReturnSeededClientes() throws Exception {
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                                .andExpect(jsonPath("$[0].nombre").value("Ana"))
                                                                .andExpect(jsonPath("$[0].membresia.id").value(1))
                                                                .andExpect(jsonPath("$[0].membresia.tipo").value("SILVER"))
                                                                .andExpect(jsonPath("$[0].membresiaExpiraEn").value("2030-01-01T00:00:00"));
    }

    @Test
    void getById_shouldReturnOneCliente() throws Exception {
        mockMvc.perform(get("/clientes").param("id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Luis"));
    }

    @Test
    void getById_shouldReturn404_whenMissing() throws Exception {
        mockMvc.perform(get("/clientes").param("id", "999"))
                .andExpect(status().isNotFound())
                                .andExpect(content().string("Cliente with id 999 not found."));
    }

    @Test
    void create_shouldPersistAndReturnLocation() throws Exception {
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                                                                .content("{\"nombre\":\"Carlos\",\"apellido\":\"Lopez\",\"membresia\":2,\"membresiaExpiraEn\":\"2031-12-31T00:00:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/clientes?id=")));
    }

    @Test
    void update_shouldModifyExistingCliente() throws Exception {
        mockMvc.perform(put("/clientes/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"MartaUpdated\",\"apellido\":\"DiazUpdated\",\"membresia\":3,\"membresiaExpiraEn\":\"2032-08-15T10:00:00\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clientes").param("id", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("MartaUpdated"))
                .andExpect(jsonPath("$[0].membresia.id").value(3))
                .andExpect(jsonPath("$[0].membresia.tipo").value("BRONZE"))
                .andExpect(jsonPath("$[0].membresiaExpiraEn").value("2032-08-15T10:00:00"));
    }

    @Test
    void patchMembresia_shouldAssignMembresiaToCliente() throws Exception {
        mockMvc.perform(patch("/clientes/3/membresia")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"membresiaId\":1,\"membresiaExpiraEn\":\"2033-01-01T00:00:00\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clientes").param("id", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].membresia.id").value(1))
                .andExpect(jsonPath("$[0].membresiaExpiraEn").value("2033-01-01T00:00:00"));
    }

    @Test
    void patchMembresia_shouldRemoveMembresia_whenNull() throws Exception {
        mockMvc.perform(patch("/clientes/2/membresia")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"membresiaId\":null,\"membresiaExpiraEn\":null}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clientes").param("id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].membresia").isEmpty())
                .andExpect(jsonPath("$[0].membresiaExpiraEn").isEmpty());
    }

    @Test
    void getMembresiasVencidas_shouldReturnExpiredClientes() throws Exception {
        mockMvc.perform(patch("/clientes/1/membresia")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"membresiaId\":1,\"membresiaExpiraEn\":\"2020-01-01T00:00:00\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clientes/membresias-vencidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void delete_shouldRemoveCliente() throws Exception {
        mockMvc.perform(delete("/clientes/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/clientes").param("id", "1"))
                .andExpect(status().isNotFound());
    }
}

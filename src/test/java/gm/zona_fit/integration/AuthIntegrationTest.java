package gm.zona_fit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_shouldReturnJwt_whenCredentialsAreValid() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void register_shouldCreateUserAndReturnJwt() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                                                .content("{\"username\":\"nuevo\",\"email\":\"nuevo@zona.com\",\"password\":\"clave123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("nuevo"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));

                var adminToken = loginAndGetToken("admin", "admin123");

                var usuariosResult = mockMvc.perform(get("/usuarios")
                                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode usuarios = objectMapper.readTree(usuariosResult.getResponse().getContentAsString());
                JsonNode usuarioNuevo = null;
                for (JsonNode usuario : usuarios) {
                        if ("nuevo".equals(usuario.get("username").asText())) {
                                usuarioNuevo = usuario;
                                break;
                        }
                }

                Integer clienteId = usuarioNuevo.get("clienteId").asInt();

                mockMvc.perform(get("/clientes").param("id", String.valueOf(clienteId))
                                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].membresia").isEmpty())
                                .andExpect(jsonPath("$[0].membresiaExpiraEn").isEmpty());
    }

    @Test
    void protectedEndpoints_shouldRequireJwt_andRespectRole() throws Exception {
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isUnauthorized());

        var clienteToken = loginAndGetToken("cliente", "cliente123");

        mockMvc.perform(get("/clientes")
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("cliente"));

        var adminToken = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void usuarioGlobalFilter_shouldScopeClienteByTokenUserId_andNotRestrictAdmin() throws Exception {
        var clienteToken = loginAndGetToken("cliente", "cliente123");

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].username").value("cliente"));

        mockMvc.perform(get("/usuarios/1")
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isNotFound());

        var adminToken = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/usuarios/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        var result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }
}

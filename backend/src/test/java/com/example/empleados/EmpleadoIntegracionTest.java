package com.example.empleados;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.empleados.repository.EmpleadoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EmpleadoIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpleadoRepository repository;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    private String cuerpo(String email, String dni) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", "Ana");
        body.put("apellido", "Diaz");
        body.put("email", email);
        body.put("dni", dni);
        body.put("fechaNacimiento", "1990-05-20");
        body.put("fechaIngreso", "2024-01-15");
        body.put("salario", 150000);
        body.put("departamento", "IT");
        body.put("telefono", "+541112345678");
        return objectMapper.writeValueAsString(body);
    }

    @Test
    void altaValidaPersisteYDevuelve201() throws Exception {
        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("ana.diaz@example.com", "12345678")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.activo").value(true));

        assertThat(repository.existsByEmail("ana.diaz@example.com")).isTrue();
    }

    @Test
    void datosInvalidosDevuelve400() throws Exception {
        String invalido = objectMapper.writeValueAsString(Map.of(
                "nombre", "",
                "apellido", "Diaz",
                "email", "no-es-email",
                "dni", "ABC",
                "fechaIngreso", "2024-01-15",
                "salario", 150000,
                "departamento", "IT"));

        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void emailDuplicadoDevuelve409() throws Exception {
        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("ana.diaz@example.com", "12345678")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("ana.diaz@example.com", "87654321")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.email").value("Ya existe un empleado con ese email"));
    }
}

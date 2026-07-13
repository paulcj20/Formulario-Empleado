package com.example.empleados.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.empleados.domain.Departamento;
import com.example.empleados.domain.TipoContrato;
import com.example.empleados.dto.EmpleadoResponse;
import com.example.empleados.exception.EmpleadoDuplicadoException;
import com.example.empleados.service.EmpleadoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmpleadoController.class)
class EmpleadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmpleadoService service;

    private String jsonValido() throws Exception {
        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("nombre", "Ana");
        body.put("apellido", "Diaz");
        body.put("email", "ana.diaz@example.com");
        body.put("dni", "12345678");
        body.put("fechaNacimiento", "1990-05-20");
        body.put("fechaIngreso", "2024-01-15");
        body.put("salario", 150000);
        body.put("departamento", "IT");
        body.put("telefono", "+541112345678");
        body.put("tipoContrato", "EMPLEADO");
        body.put("porcentajeAportes", 17);
        return objectMapper.writeValueAsString(body);
    }

    @Test
    void altaValidaDevuelve201YResponse() throws Exception {
        EmpleadoResponse response = new EmpleadoResponse(1L, "Ana", "Diaz",
                "ana.diaz@example.com", "12345678", LocalDate.of(1990, 5, 20),
                LocalDate.of(2024, 1, 15), new BigDecimal("150000"), Departamento.IT,
                "+541112345678", true, TipoContrato.EMPLEADO, new BigDecimal("17"),
                null, null);
        when(service.altaEmpleado(any())).thenReturn(response);

        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ana.diaz@example.com"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void datosInvalidosDevuelve400ConErroresPorCampo() throws Exception {
        String jsonInvalido = objectMapper.writeValueAsString(java.util.Map.of(
                "nombre", "",
                "apellido", "Diaz",
                "email", "no-es-email",
                "dni", "ABC",
                "fechaIngreso", "2024-01-15",
                "salario", 150000,
                "departamento", "IT"));

        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.dni").exists());
    }

    @Test
    void emailDuplicadoDevuelve409() throws Exception {
        when(service.altaEmpleado(any()))
                .thenThrow(new EmpleadoDuplicadoException("email", "Ya existe un empleado con ese email"));

        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.email").value("Ya existe un empleado con ese email"));
    }
}

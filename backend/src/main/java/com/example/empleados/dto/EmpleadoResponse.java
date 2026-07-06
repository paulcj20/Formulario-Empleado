package com.example.empleados.dto;

import com.example.empleados.domain.Departamento;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmpleadoResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        String dni,
        LocalDate fechaNacimiento,
        LocalDate fechaIngreso,
        BigDecimal salario,
        Departamento departamento,
        String telefono,
        boolean activo) {
}

package com.example.empleados.dto;

import com.example.empleados.domain.Departamento;
import com.example.empleados.domain.TipoContrato;
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
        boolean activo,
        TipoContrato tipoContrato,
        BigDecimal porcentajeAportes,
        BigDecimal montoFactura,
        LocalDate fechaServicio) {
}

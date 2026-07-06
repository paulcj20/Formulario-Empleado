package com.example.empleados.dto;

import com.example.empleados.domain.Departamento;
import com.example.empleados.validation.MayorDeEdad;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmpleadoRequest(
        @NotBlank(message = "El nombre debe tener entre 2 y 50 caracteres")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "El nombre solo puede contener letras")
        String nombre,

        @NotBlank(message = "El apellido debe tener entre 2 y 50 caracteres")
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "El apellido solo puede contener letras")
        String apellido,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @NotBlank(message = "El DNI debe tener 7 u 8 dígitos")
        @Pattern(regexp = "^\\d{7,8}$", message = "El DNI debe tener 7 u 8 dígitos")
        String dni,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @MayorDeEdad
        LocalDate fechaNacimiento,

        @NotNull(message = "La fecha de ingreso es obligatoria")
        @PastOrPresent(message = "La fecha de ingreso no puede ser futura")
        LocalDate fechaIngreso,

        @NotNull(message = "El salario es obligatorio")
        @Positive(message = "El salario debe ser positivo")
        @DecimalMax(value = "1000000", message = "El salario no puede superar 1.000.000")
        BigDecimal salario,

        @NotNull(message = "Seleccione un departamento")
        Departamento departamento,

        @Pattern(regexp = "^\\+?\\d{8,15}$", message = "El teléfono debe tener entre 8 y 15 dígitos")
        String telefono) {
}

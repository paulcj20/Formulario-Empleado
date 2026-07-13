package com.example.empleados.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.empleados.domain.Departamento;
import com.example.empleados.domain.TipoContrato;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EmpleadoRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private EmpleadoRequest empleadoValido() {
        return new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com", "12345678",
                LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678",
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
    }

    private EmpleadoRequest terciarizadoValido() {
        return new EmpleadoRequest("Luis", "Perez", "luis.perez@example.com", "87654321",
                null, null, null, Departamento.IT, null,
                TipoContrato.TERCIARIZADO, null, new BigDecimal("250000"),
                LocalDate.of(2026, 6, 30));
    }

    private Set<String> camposConError(EmpleadoRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void empleadoValidoNoTieneViolaciones() {
        assertThat(validator.validate(empleadoValido())).isEmpty();
    }

    @Test
    void terciarizadoValidoNoTieneViolaciones() {
        assertThat(validator.validate(terciarizadoValido())).isEmpty();
    }

    @Test
    void tipoContratoNuloEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                null, new BigDecimal("17"), null, null);
        assertThat(camposConError(r)).contains("tipoContrato");
    }

    @Test
    void empleadoSinFechaNacimientoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", null, LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
        assertThat(camposConError(r)).contains("fechaNacimiento");
    }

    @Test
    void empleadoMenorDeEdadEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.now().minusYears(15), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
        assertThat(camposConError(r)).contains("fechaNacimiento");
    }

    @Test
    void empleadoSinSalarioEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                null, Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
        assertThat(camposConError(r)).contains("salario");
    }

    @Test
    void empleadoSinPorcentajeAportesEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, null, null, null);
        assertThat(camposConError(r)).contains("porcentajeAportes");
    }

    @Test
    void porcentajeAportesMayorA30EsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("31"), null, null);
        assertThat(camposConError(r)).contains("porcentajeAportes");
    }

    @Test
    void porcentajeAportesNegativoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("-1"), null, null);
        assertThat(camposConError(r)).contains("porcentajeAportes");
    }

    @Test
    void empleadoConMontoFacturaEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("17"), new BigDecimal("250000"), null);
        assertThat(camposConError(r)).contains("montoFactura");
    }

    @Test
    void terciarizadoSinMontoFacturaEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Luis", "Perez", "luis.perez@example.com",
                "87654321", null, null, null, Departamento.IT, null,
                TipoContrato.TERCIARIZADO, null, null, LocalDate.of(2026, 6, 30));
        assertThat(camposConError(r)).contains("montoFactura");
    }

    @Test
    void terciarizadoSinFechaServicioEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Luis", "Perez", "luis.perez@example.com",
                "87654321", null, null, null, Departamento.IT, null,
                TipoContrato.TERCIARIZADO, null, new BigDecimal("250000"), null);
        assertThat(camposConError(r)).contains("fechaServicio");
    }

    @Test
    void fechaServicioFuturaEsInvalida() {
        EmpleadoRequest r = new EmpleadoRequest("Luis", "Perez", "luis.perez@example.com",
                "87654321", null, null, null, Departamento.IT, null,
                TipoContrato.TERCIARIZADO, null, new BigDecimal("250000"),
                LocalDate.now().plusDays(1));
        assertThat(camposConError(r)).contains("fechaServicio");
    }

    @Test
    void terciarizadoConSalarioEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Luis", "Perez", "luis.perez@example.com",
                "87654321", null, null, new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.TERCIARIZADO, null, new BigDecimal("250000"),
                LocalDate.of(2026, 6, 30));
        assertThat(camposConError(r)).contains("salario");
    }

    @Test
    void terciarizadoConFechaNacimientoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Luis", "Perez", "luis.perez@example.com",
                "87654321", LocalDate.of(1990, 5, 20), null, null, Departamento.IT, null,
                TipoContrato.TERCIARIZADO, null, new BigDecimal("250000"),
                LocalDate.of(2026, 6, 30));
        assertThat(camposConError(r)).contains("fechaNacimiento");
    }

    @Test
    void nombreVacioEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
        assertThat(camposConError(r)).contains("nombre");
    }

    @Test
    void emailInvalidoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "no-es-email",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
        assertThat(camposConError(r)).contains("email");
    }
}

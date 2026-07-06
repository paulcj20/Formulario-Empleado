package com.example.empleados.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.empleados.domain.Departamento;
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

    private EmpleadoRequest valido() {
        return new EmpleadoRequest(
                "Ana",
                "Diaz",
                "ana.diaz@example.com",
                "12345678",
                LocalDate.of(1990, 5, 20),
                LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"),
                Departamento.IT,
                "+541112345678");
    }

    private Set<String> camposConError(EmpleadoRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void requestValidoNoTieneViolaciones() {
        assertThat(validator.validate(valido())).isEmpty();
    }

    @Test
    void telefonoNuloEsValido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null);
        assertThat(validator.validate(r)).isEmpty();
    }

    @Test
    void nombreVacioEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678");
        assertThat(camposConError(r)).contains("nombre");
    }

    @Test
    void emailInvalidoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "no-es-email",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678");
        assertThat(camposConError(r)).contains("email");
    }

    @Test
    void dniNoNumericoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "ABC123", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678");
        assertThat(camposConError(r)).contains("dni");
    }

    @Test
    void menorDeEdadEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.now().minusYears(15), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678");
        assertThat(camposConError(r)).contains("fechaNacimiento");
    }

    @Test
    void fechaIngresoFuturaEsInvalida() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.now().plusDays(1),
                new BigDecimal("150000"), Departamento.IT, "+541112345678");
        assertThat(camposConError(r)).contains("fechaIngreso");
    }

    @Test
    void salarioNegativoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("-1"), Departamento.IT, "+541112345678");
        assertThat(camposConError(r)).contains("salario");
    }

    @Test
    void salarioSuperiorAlMaximoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("1000001"), Departamento.IT, "+541112345678");
        assertThat(camposConError(r)).contains("salario");
    }

    @Test
    void departamentoNuloEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), null, "+541112345678");
        assertThat(camposConError(r)).contains("departamento");
    }
}

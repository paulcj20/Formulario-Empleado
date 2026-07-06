package com.example.empleados.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MayorDeEdadValidatorTest {

    private final MayorDeEdadValidator validator = new MayorDeEdadValidator();

    @Test
    void nullEsValido() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void mayorDe18EsValido() {
        assertThat(validator.isValid(LocalDate.now().minusYears(25), null)).isTrue();
    }

    @Test
    void exactamente18EsValido() {
        assertThat(validator.isValid(LocalDate.now().minusYears(18), null)).isTrue();
    }

    @Test
    void menorDe18EsInvalido() {
        assertThat(validator.isValid(LocalDate.now().minusYears(17), null)).isFalse();
    }
}

package com.example.empleados.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.empleados.domain.Departamento;
import com.example.empleados.domain.Empleado;
import com.example.empleados.domain.TipoContrato;
import com.example.empleados.dto.EmpleadoRequest;
import com.example.empleados.dto.EmpleadoResponse;
import com.example.empleados.exception.EmpleadoDuplicadoException;
import com.example.empleados.mapper.EmpleadoMapper;
import com.example.empleados.repository.EmpleadoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmpleadoServiceImplTest {

    @Mock
    private EmpleadoRepository repository;

    private EmpleadoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmpleadoServiceImpl(repository, new EmpleadoMapper());
    }

    private EmpleadoRequest request() {
        return new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com", "12345678",
                LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678",
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
    }

    @Test
    void altaGuardaYDevuelveResponse() {
        when(repository.existsByEmail("ana.diaz@example.com")).thenReturn(false);
        when(repository.existsByDni("12345678")).thenReturn(false);
        when(repository.save(any(Empleado.class))).thenAnswer(invocation -> {
            Empleado e = invocation.getArgument(0);
            e.setId(99L);
            return e;
        });

        EmpleadoResponse response = service.altaEmpleado(request());

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.email()).isEqualTo("ana.diaz@example.com");
        assertThat(response.activo()).isTrue();
        verify(repository).save(any(Empleado.class));
    }

    @Test
    void emailDuplicadoLanzaExcepcion() {
        when(repository.existsByEmail("ana.diaz@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.altaEmpleado(request()))
                .isInstanceOf(EmpleadoDuplicadoException.class)
                .satisfies(ex -> assertThat(((EmpleadoDuplicadoException) ex).getCampo()).isEqualTo("email"));

        verify(repository, never()).save(any());
    }

    @Test
    void dniDuplicadoLanzaExcepcion() {
        when(repository.existsByEmail("ana.diaz@example.com")).thenReturn(false);
        when(repository.existsByDni("12345678")).thenReturn(true);

        assertThatThrownBy(() -> service.altaEmpleado(request()))
                .isInstanceOf(EmpleadoDuplicadoException.class)
                .satisfies(ex -> assertThat(((EmpleadoDuplicadoException) ex).getCampo()).isEqualTo("dni"));

        verify(repository, never()).save(any());
    }
}

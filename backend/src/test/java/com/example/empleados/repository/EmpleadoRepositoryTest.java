package com.example.empleados.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.empleados.domain.Departamento;
import com.example.empleados.domain.Empleado;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class EmpleadoRepositoryTest {

    @Autowired
    private EmpleadoRepository repository;

    private Empleado nuevoEmpleado() {
        Empleado e = new Empleado();
        e.setNombre("Ana");
        e.setApellido("Diaz");
        e.setEmail("ana.diaz@example.com");
        e.setDni("12345678");
        e.setFechaNacimiento(LocalDate.of(1990, 5, 20));
        e.setFechaIngreso(LocalDate.of(2024, 1, 15));
        e.setSalario(new BigDecimal("150000"));
        e.setDepartamento(Departamento.IT);
        e.setTelefono("+541112345678");
        e.setActivo(true);
        return e;
    }

    @Test
    void existsByEmailDevuelveTrueCuandoExiste() {
        repository.save(nuevoEmpleado());

        assertThat(repository.existsByEmail("ana.diaz@example.com")).isTrue();
        assertThat(repository.existsByEmail("otro@example.com")).isFalse();
    }

    @Test
    void existsByDniDevuelveTrueCuandoExiste() {
        repository.save(nuevoEmpleado());

        assertThat(repository.existsByDni("12345678")).isTrue();
        assertThat(repository.existsByDni("87654321")).isFalse();
    }

    @Test
    void guardaAsignaId() {
        Empleado guardado = repository.save(nuevoEmpleado());

        assertThat(guardado.getId()).isNotNull();
    }
}

package com.example.empleados.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.empleados.domain.Departamento;
import com.example.empleados.domain.Empleado;
import com.example.empleados.domain.TipoContrato;
import com.example.empleados.dto.EmpleadoRequest;
import com.example.empleados.dto.EmpleadoResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EmpleadoMapperTest {

    private final EmpleadoMapper mapper = new EmpleadoMapper();

    @Test
    void toEntityMapeaTodosLosCamposYActivoTrue() {
        EmpleadoRequest request = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678",
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);

        Empleado entidad = mapper.toEntity(request);

        assertThat(entidad.getId()).isNull();
        assertThat(entidad.getNombre()).isEqualTo("Ana");
        assertThat(entidad.getApellido()).isEqualTo("Diaz");
        assertThat(entidad.getEmail()).isEqualTo("ana.diaz@example.com");
        assertThat(entidad.getDni()).isEqualTo("12345678");
        assertThat(entidad.getFechaNacimiento()).isEqualTo(LocalDate.of(1990, 5, 20));
        assertThat(entidad.getFechaIngreso()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(entidad.getSalario()).isEqualByComparingTo("150000");
        assertThat(entidad.getDepartamento()).isEqualTo(Departamento.IT);
        assertThat(entidad.getTelefono()).isEqualTo("+541112345678");
        assertThat(entidad.isActivo()).isTrue();
    }

    @Test
    void toResponseMapeaTodosLosCamposIncluyendoId() {
        Empleado entidad = new Empleado();
        entidad.setId(7L);
        entidad.setNombre("Ana");
        entidad.setApellido("Diaz");
        entidad.setEmail("ana.diaz@example.com");
        entidad.setDni("12345678");
        entidad.setFechaNacimiento(LocalDate.of(1990, 5, 20));
        entidad.setFechaIngreso(LocalDate.of(2024, 1, 15));
        entidad.setSalario(new BigDecimal("150000"));
        entidad.setDepartamento(Departamento.IT);
        entidad.setTelefono("+541112345678");
        entidad.setActivo(true);

        EmpleadoResponse response = mapper.toResponse(entidad);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.nombre()).isEqualTo("Ana");
        assertThat(response.email()).isEqualTo("ana.diaz@example.com");
        assertThat(response.dni()).isEqualTo("12345678");
        assertThat(response.departamento()).isEqualTo(Departamento.IT);
        assertThat(response.activo()).isTrue();
    }
}

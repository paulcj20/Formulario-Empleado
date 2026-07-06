package com.example.empleados.mapper;

import com.example.empleados.domain.Empleado;
import com.example.empleados.dto.EmpleadoRequest;
import com.example.empleados.dto.EmpleadoResponse;
import org.springframework.stereotype.Component;

@Component
public class EmpleadoMapper {

    public Empleado toEntity(EmpleadoRequest request) {
        Empleado empleado = new Empleado();
        empleado.setNombre(request.nombre());
        empleado.setApellido(request.apellido());
        empleado.setEmail(request.email());
        empleado.setDni(request.dni());
        empleado.setFechaNacimiento(request.fechaNacimiento());
        empleado.setFechaIngreso(request.fechaIngreso());
        empleado.setSalario(request.salario());
        empleado.setDepartamento(request.departamento());
        empleado.setTelefono(request.telefono());
        empleado.setActivo(true);
        return empleado;
    }

    public EmpleadoResponse toResponse(Empleado empleado) {
        return new EmpleadoResponse(
                empleado.getId(),
                empleado.getNombre(),
                empleado.getApellido(),
                empleado.getEmail(),
                empleado.getDni(),
                empleado.getFechaNacimiento(),
                empleado.getFechaIngreso(),
                empleado.getSalario(),
                empleado.getDepartamento(),
                empleado.getTelefono(),
                empleado.isActivo());
    }
}

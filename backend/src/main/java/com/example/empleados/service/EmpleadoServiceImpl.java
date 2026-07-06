package com.example.empleados.service;

import com.example.empleados.domain.Empleado;
import com.example.empleados.dto.EmpleadoRequest;
import com.example.empleados.dto.EmpleadoResponse;
import com.example.empleados.exception.EmpleadoDuplicadoException;
import com.example.empleados.mapper.EmpleadoMapper;
import com.example.empleados.repository.EmpleadoRepository;
import org.springframework.stereotype.Service;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository repository;
    private final EmpleadoMapper mapper;

    public EmpleadoServiceImpl(EmpleadoRepository repository, EmpleadoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public EmpleadoResponse altaEmpleado(EmpleadoRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new EmpleadoDuplicadoException("email", "Ya existe un empleado con ese email");
        }
        if (repository.existsByDni(request.dni())) {
            throw new EmpleadoDuplicadoException("dni", "Ya existe un empleado con ese DNI");
        }
        Empleado guardado = repository.save(mapper.toEntity(request));
        return mapper.toResponse(guardado);
    }
}

package com.example.empleados.service;

import com.example.empleados.dto.EmpleadoRequest;
import com.example.empleados.dto.EmpleadoResponse;

public interface EmpleadoService {

    EmpleadoResponse altaEmpleado(EmpleadoRequest request);
}

package com.example.empleados.controller;

import com.example.empleados.dto.EmpleadoRequest;
import com.example.empleados.dto.EmpleadoResponse;
import com.example.empleados.service.EmpleadoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService service;

    public EmpleadoController(EmpleadoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EmpleadoResponse> alta(@Valid @RequestBody EmpleadoRequest request) {
        EmpleadoResponse response = service.altaEmpleado(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

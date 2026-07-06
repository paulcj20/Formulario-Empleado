package com.example.empleados.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errores.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errores));
    }

    @ExceptionHandler(EmpleadoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> manejarDuplicado(EmpleadoDuplicadoException ex) {
        Map<String, String> errores = Map.of(ex.getCampo(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("errors", errores));
    }
}

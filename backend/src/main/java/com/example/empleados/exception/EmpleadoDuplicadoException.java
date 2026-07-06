package com.example.empleados.exception;

public class EmpleadoDuplicadoException extends RuntimeException {

    private final String campo;

    public EmpleadoDuplicadoException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}

package com.example.empleados.validation;

import com.example.empleados.domain.TipoContrato;
import com.example.empleados.dto.EmpleadoRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ConsistenteConTipoContratoValidator
        implements ConstraintValidator<ConsistenteConTipoContrato, EmpleadoRequest> {

    @Override
    public boolean isValid(EmpleadoRequest request, ConstraintValidatorContext context) {
        if (request == null || request.tipoContrato() == null) {
            // @NotNull sobre tipoContrato reporta ese caso
            return true;
        }
        context.disableDefaultConstraintViolation();
        boolean valido = true;
        if (request.tipoContrato() == TipoContrato.EMPLEADO) {
            valido &= regla(context, request.fechaNacimiento() != null,
                    "fechaNacimiento", "La fecha de nacimiento es obligatoria");
            valido &= regla(context, request.fechaIngreso() != null,
                    "fechaIngreso", "La fecha de ingreso es obligatoria");
            valido &= regla(context, request.salario() != null,
                    "salario", "El salario es obligatorio");
            valido &= regla(context, request.porcentajeAportes() != null,
                    "porcentajeAportes", "El porcentaje de aportes es obligatorio");
            valido &= regla(context, request.montoFactura() == null,
                    "montoFactura", "El monto de factura no corresponde a un empleado");
            valido &= regla(context, request.fechaServicio() == null,
                    "fechaServicio", "La fecha de servicio no corresponde a un empleado");
        } else {
            valido &= regla(context, request.montoFactura() != null,
                    "montoFactura", "El monto de factura es obligatorio");
            valido &= regla(context, request.fechaServicio() != null,
                    "fechaServicio", "La fecha de servicio es obligatoria");
            valido &= regla(context, request.fechaNacimiento() == null,
                    "fechaNacimiento", "La fecha de nacimiento no corresponde a un terciarizado");
            valido &= regla(context, request.fechaIngreso() == null,
                    "fechaIngreso", "La fecha de ingreso no corresponde a un terciarizado");
            valido &= regla(context, request.salario() == null,
                    "salario", "El salario no corresponde a un terciarizado");
            valido &= regla(context, request.porcentajeAportes() == null,
                    "porcentajeAportes", "El porcentaje de aportes no corresponde a un terciarizado");
        }
        return valido;
    }

    private boolean regla(ConstraintValidatorContext context, boolean cumple,
            String campo, String mensaje) {
        if (cumple) {
            return true;
        }
        context.buildConstraintViolationWithTemplate(mensaje)
                .addPropertyNode(campo)
                .addConstraintViolation();
        return false;
    }
}

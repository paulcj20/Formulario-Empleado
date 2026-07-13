package com.example.empleados.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConsistenteConTipoContratoValidator.class)
public @interface ConsistenteConTipoContrato {

    String message() default "Datos inconsistentes con el tipo de contrato";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

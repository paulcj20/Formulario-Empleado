# Tipo de Contrato con Campos Condicionales — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Agregar el campo obligatorio "Tipo de contrato" (`EMPLEADO` | `TERCIARIZADO`) al alta de empleado, con campos condicionales por tipo, validados en frontend (zod) y backend (Bean Validation), y persistidos.

**Architecture:** Frontend React con react-hook-form + `z.discriminatedUnion` sobre `tipoContrato`; los campos condicionales se montan/desmontan con `watch()` y `shouldUnregister: true`. Backend Spring Boot: enum `TipoContrato`, DTO ampliado con validador class-level `@ConsistenteConTipoContrato` (mismo patrón que `@MayorDeEdad`), entidad single-table con columnas nullables.

**Tech Stack:** React 18, react-hook-form 7, zod 3, @hookform/resolvers, react-bootstrap, Testing Library, Jest (react-scripts). Spring Boot 3, Jakarta Bean Validation, JPA/H2 (tests), JUnit 5, AssertJ, MockMvc, Maven wrapper.

**Spec:** `docs/superpowers/specs/2026-07-12-tipo-contrato-campos-condicionales-design.md`

## Global Constraints

- Valores del discriminante: `EMPLEADO`, `TERCIARIZADO` (strings exactos, mayúsculas).
- Solo EMPLEADO: `fechaNacimiento` (mayor de 18), `fechaIngreso` (no futura), `salario` (positivo ≤ 1.000.000), `porcentajeAportes` (0–30 ambos inclusive, obligatorio).
- Solo TERCIARIZADO: `montoFactura` (positivo ≤ 1.000.000, obligatorio), `fechaServicio` (no futura, obligatoria).
- El payload `POST /api/empleados` lleva SOLO los campos del tipo elegido; el backend devuelve 400 si vienen campos del tipo contrario.
- Mensajes de error en castellano, mismo estilo que los existentes.
- SIN tests E2E de Playwright (decisión explícita del usuario).
- Comandos backend se corren desde `backend/` con `.\mvnw.cmd` (Windows PowerShell). Comandos frontend desde la raíz del repo.
- Commits con sufijo `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>`.

---

### Task 1: Backend — enum `TipoContrato` y entidad `Empleado` con campos condicionales

**Files:**
- Create: `backend/src/main/java/com/example/empleados/domain/TipoContrato.java`
- Modify: `backend/src/main/java/com/example/empleados/domain/Empleado.java`
- Test: `backend/src/test/java/com/example/empleados/repository/EmpleadoRepositoryTest.java`

**Interfaces:**
- Consumes: entidad `Empleado` y `EmpleadoRepository` existentes.
- Produces: enum `com.example.empleados.domain.TipoContrato { EMPLEADO, TERCIARIZADO }`; en `Empleado`: `get/setTipoContrato(TipoContrato)`, `get/setPorcentajeAportes(BigDecimal)`, `get/setMontoFactura(BigDecimal)`, `get/setFechaServicio(LocalDate)`; `fechaNacimiento`, `fechaIngreso` y `salario` pasan a columnas nullables.

- [ ] **Step 1: Actualizar el fixture y agregar el test que falla**

En `EmpleadoRepositoryTest.java`, agregar el import `com.example.empleados.domain.TipoContrato`, completar `nuevoEmpleado()` y agregar un test de terciarizado:

```java
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
        e.setTipoContrato(TipoContrato.EMPLEADO);
        e.setPorcentajeAportes(new BigDecimal("17"));
        e.setActivo(true);
        return e;
    }

    @Test
    void guardaTerciarizadoSinCamposDeEmpleado() {
        Empleado t = new Empleado();
        t.setNombre("Luis");
        t.setApellido("Perez");
        t.setEmail("luis.perez@example.com");
        t.setDni("87654321");
        t.setDepartamento(Departamento.IT);
        t.setTipoContrato(TipoContrato.TERCIARIZADO);
        t.setMontoFactura(new BigDecimal("250000"));
        t.setFechaServicio(LocalDate.of(2026, 6, 30));
        t.setActivo(true);

        Empleado guardado = repository.save(t);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getSalario()).isNull();
        assertThat(guardado.getFechaNacimiento()).isNull();
        assertThat(guardado.getTipoContrato()).isEqualTo(TipoContrato.TERCIARIZADO);
    }
```

- [ ] **Step 2: Verificar que falla**

```powershell
cd backend; .\mvnw.cmd -Dtest=EmpleadoRepositoryTest test
```

Expected: FAIL de compilación — `cannot find symbol: class TipoContrato` / `method setTipoContrato`.

- [ ] **Step 3: Crear el enum**

`backend/src/main/java/com/example/empleados/domain/TipoContrato.java`:

```java
package com.example.empleados.domain;

public enum TipoContrato {
    EMPLEADO,
    TERCIARIZADO
}
```

- [ ] **Step 4: Modificar la entidad**

En `Empleado.java`:

1. Quitar `@Column(nullable = false)` de `fechaNacimiento`, `fechaIngreso` y `salario` (quedan sin anotación `@Column`).
2. Agregar después del campo `activo`:

```java
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoContrato tipoContrato;

    private BigDecimal porcentajeAportes;

    private BigDecimal montoFactura;

    private LocalDate fechaServicio;
```

3. Agregar al final de la clase los getters/setters:

```java
    public TipoContrato getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(TipoContrato tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    public BigDecimal getPorcentajeAportes() {
        return porcentajeAportes;
    }

    public void setPorcentajeAportes(BigDecimal porcentajeAportes) {
        this.porcentajeAportes = porcentajeAportes;
    }

    public BigDecimal getMontoFactura() {
        return montoFactura;
    }

    public void setMontoFactura(BigDecimal montoFactura) {
        this.montoFactura = montoFactura;
    }

    public LocalDate getFechaServicio() {
        return fechaServicio;
    }

    public void setFechaServicio(LocalDate fechaServicio) {
        this.fechaServicio = fechaServicio;
    }
```

- [ ] **Step 5: Verificar que pasa**

```powershell
cd backend; .\mvnw.cmd -Dtest=EmpleadoRepositoryTest test
```

Expected: PASS (4 tests).

- [ ] **Step 6: Commit**

```powershell
git add backend/src/main/java/com/example/empleados/domain/ backend/src/test/java/com/example/empleados/repository/
git commit -m "feat(backend): enum TipoContrato y campos condicionales en la entidad (TDD)"
```

---

### Task 2: Backend — `EmpleadoRequest` ampliado + validador `@ConsistenteConTipoContrato`

**Files:**
- Create: `backend/src/main/java/com/example/empleados/validation/ConsistenteConTipoContrato.java`
- Create: `backend/src/main/java/com/example/empleados/validation/ConsistenteConTipoContratoValidator.java`
- Modify: `backend/src/main/java/com/example/empleados/dto/EmpleadoRequest.java`
- Test: `backend/src/test/java/com/example/empleados/dto/EmpleadoRequestValidationTest.java`
- Modify (solo fixtures para que compile todo): `backend/src/test/java/com/example/empleados/mapper/EmpleadoMapperTest.java`, `backend/src/test/java/com/example/empleados/service/EmpleadoServiceImplTest.java`, `backend/src/test/java/com/example/empleados/controller/EmpleadoControllerTest.java`, `backend/src/test/java/com/example/empleados/EmpleadoIntegracionTest.java`

**Interfaces:**
- Consumes: `TipoContrato` (Task 1).
- Produces: nuevo constructor del record `EmpleadoRequest(String nombre, String apellido, String email, String dni, LocalDate fechaNacimiento, LocalDate fechaIngreso, BigDecimal salario, Departamento departamento, String telefono, TipoContrato tipoContrato, BigDecimal porcentajeAportes, BigDecimal montoFactura, LocalDate fechaServicio)`. Anotación class-level `@ConsistenteConTipoContrato`.

- [ ] **Step 1: Reescribir el test de validación del request**

Reemplazar el contenido completo de `EmpleadoRequestValidationTest.java` por:

```java
package com.example.empleados.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.empleados.domain.Departamento;
import com.example.empleados.domain.TipoContrato;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EmpleadoRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private EmpleadoRequest empleadoValido() {
        return new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com", "12345678",
                LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678",
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
    }

    private EmpleadoRequest terciarizadoValido() {
        return new EmpleadoRequest("Luis", "Perez", "luis.perez@example.com", "87654321",
                null, null, null, Departamento.IT, null,
                TipoContrato.TERCIARIZADO, null, new BigDecimal("250000"),
                LocalDate.of(2026, 6, 30));
    }

    private Set<String> camposConError(EmpleadoRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void empleadoValidoNoTieneViolaciones() {
        assertThat(validator.validate(empleadoValido())).isEmpty();
    }

    @Test
    void terciarizadoValidoNoTieneViolaciones() {
        assertThat(validator.validate(terciarizadoValido())).isEmpty();
    }

    @Test
    void tipoContratoNuloEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                null, new BigDecimal("17"), null, null);
        assertThat(camposConError(r)).contains("tipoContrato");
    }

    @Test
    void empleadoSinFechaNacimientoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", null, LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
        assertThat(camposConError(r)).contains("fechaNacimiento");
    }

    @Test
    void empleadoMenorDeEdadEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.now().minusYears(15), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
        assertThat(camposConError(r)).contains("fechaNacimiento");
    }

    @Test
    void empleadoSinSalarioEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                null, Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
        assertThat(camposConError(r)).contains("salario");
    }

    @Test
    void empleadoSinPorcentajeAportesEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, null, null, null);
        assertThat(camposConError(r)).contains("porcentajeAportes");
    }

    @Test
    void porcentajeAportesMayorA30EsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("31"), null, null);
        assertThat(camposConError(r)).contains("porcentajeAportes");
    }

    @Test
    void porcentajeAportesNegativoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("-1"), null, null);
        assertThat(camposConError(r)).contains("porcentajeAportes");
    }

    @Test
    void empleadoConMontoFacturaEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("17"), new BigDecimal("250000"), null);
        assertThat(camposConError(r)).contains("montoFactura");
    }

    @Test
    void terciarizadoSinMontoFacturaEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Luis", "Perez", "luis.perez@example.com",
                "87654321", null, null, null, Departamento.IT, null,
                TipoContrato.TERCIARIZADO, null, null, LocalDate.of(2026, 6, 30));
        assertThat(camposConError(r)).contains("montoFactura");
    }

    @Test
    void terciarizadoSinFechaServicioEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Luis", "Perez", "luis.perez@example.com",
                "87654321", null, null, null, Departamento.IT, null,
                TipoContrato.TERCIARIZADO, null, new BigDecimal("250000"), null);
        assertThat(camposConError(r)).contains("fechaServicio");
    }

    @Test
    void fechaServicioFuturaEsInvalida() {
        EmpleadoRequest r = new EmpleadoRequest("Luis", "Perez", "luis.perez@example.com",
                "87654321", null, null, null, Departamento.IT, null,
                TipoContrato.TERCIARIZADO, null, new BigDecimal("250000"),
                LocalDate.now().plusDays(1));
        assertThat(camposConError(r)).contains("fechaServicio");
    }

    @Test
    void terciarizadoConSalarioEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Luis", "Perez", "luis.perez@example.com",
                "87654321", null, null, new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.TERCIARIZADO, null, new BigDecimal("250000"),
                LocalDate.of(2026, 6, 30));
        assertThat(camposConError(r)).contains("salario");
    }

    @Test
    void terciarizadoConFechaNacimientoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Luis", "Perez", "luis.perez@example.com",
                "87654321", LocalDate.of(1990, 5, 20), null, null, Departamento.IT, null,
                TipoContrato.TERCIARIZADO, null, new BigDecimal("250000"),
                LocalDate.of(2026, 6, 30));
        assertThat(camposConError(r)).contains("fechaNacimiento");
    }

    @Test
    void nombreVacioEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
        assertThat(camposConError(r)).contains("nombre");
    }

    @Test
    void emailInvalidoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "no-es-email",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null,
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
        assertThat(camposConError(r)).contains("email");
    }
}
```

- [ ] **Step 2: Verificar que falla**

```powershell
cd backend; .\mvnw.cmd -Dtest=EmpleadoRequestValidationTest test
```

Expected: FAIL de compilación — el constructor del record no acepta 13 argumentos.

- [ ] **Step 3: Crear la anotación**

`backend/src/main/java/com/example/empleados/validation/ConsistenteConTipoContrato.java`:

```java
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
```

- [ ] **Step 4: Crear el validador**

`backend/src/main/java/com/example/empleados/validation/ConsistenteConTipoContratoValidator.java`:

```java
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
```

- [ ] **Step 5: Reescribir el record `EmpleadoRequest`**

Reemplazar el contenido completo de `EmpleadoRequest.java` por:

```java
package com.example.empleados.dto;

import com.example.empleados.domain.Departamento;
import com.example.empleados.domain.TipoContrato;
import com.example.empleados.validation.ConsistenteConTipoContrato;
import com.example.empleados.validation.MayorDeEdad;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@ConsistenteConTipoContrato
public record EmpleadoRequest(
        @NotBlank(message = "El nombre debe tener entre 2 y 50 caracteres")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "El nombre solo puede contener letras")
        String nombre,

        @NotBlank(message = "El apellido debe tener entre 2 y 50 caracteres")
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "El apellido solo puede contener letras")
        String apellido,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @NotBlank(message = "El DNI debe tener 7 u 8 dígitos")
        @Pattern(regexp = "^\\d{7,8}$", message = "El DNI debe tener 7 u 8 dígitos")
        String dni,

        @MayorDeEdad
        LocalDate fechaNacimiento,

        @PastOrPresent(message = "La fecha de ingreso no puede ser futura")
        LocalDate fechaIngreso,

        @Positive(message = "El salario debe ser positivo")
        @DecimalMax(value = "1000000", message = "El salario no puede superar 1.000.000")
        BigDecimal salario,

        @NotNull(message = "Seleccione un departamento")
        Departamento departamento,

        @Pattern(regexp = "^\\+?\\d{8,15}$", message = "El teléfono debe tener entre 8 y 15 dígitos")
        String telefono,

        @NotNull(message = "Seleccione un tipo de contrato")
        TipoContrato tipoContrato,

        @DecimalMin(value = "0", message = "El porcentaje de aportes debe estar entre 0 y 30")
        @DecimalMax(value = "30", message = "El porcentaje de aportes debe estar entre 0 y 30")
        BigDecimal porcentajeAportes,

        @Positive(message = "El monto de factura debe ser positivo")
        @DecimalMax(value = "1000000", message = "El monto de factura no puede superar 1.000.000")
        BigDecimal montoFactura,

        @PastOrPresent(message = "La fecha de servicio no puede ser futura")
        LocalDate fechaServicio) {
}
```

Nota: `fechaNacimiento`, `fechaIngreso` y `salario` pierden su `@NotNull` individual (la obligatoriedad por tipo la impone el validador class-level). `@MayorDeEdad`, `@PastOrPresent`, `@Positive` y `@DecimalMax` toleran null, por eso se mantienen a nivel campo.

- [ ] **Step 6: Actualizar los fixtures de los demás tests (solo para que compilen y sigan verdes)**

En `EmpleadoMapperTest.java` (método `toEntityMapeaTodosLosCamposYActivoTrue`), agregar el import `com.example.empleados.domain.TipoContrato` y reemplazar la construcción del request por:

```java
        EmpleadoRequest request = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678",
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
```

En `EmpleadoServiceImplTest.java`, agregar el mismo import y reemplazar el helper `request()` por:

```java
    private EmpleadoRequest request() {
        return new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com", "12345678",
                LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678",
                TipoContrato.EMPLEADO, new BigDecimal("17"), null, null);
    }
```

En `EmpleadoControllerTest.java` (método `jsonValido()`) y `EmpleadoIntegracionTest.java` (método `cuerpo(...)`), agregar al body ANTES del `writeValueAsString`:

```java
        body.put("tipoContrato", "EMPLEADO");
        body.put("porcentajeAportes", 17);
```

En `EmpleadoControllerTest.datosInvalidosDevuelve400ConErroresPorCampo` y `EmpleadoIntegracionTest.datosInvalidosDevuelve400`, los `Map.of(...)` del json inválido quedan como están (sin tipoContrato: ahora además falla por "Seleccione un tipo de contrato", sigue siendo 400).

- [ ] **Step 7: Correr la suite backend completa**

```powershell
cd backend; .\mvnw.cmd test
```

Expected: PASS. `EmpleadoControllerTest.altaValidaDevuelve201YResponse` sigue verde porque la respuesta mockeada usa la firma vieja de `EmpleadoResponse` (todavía sin tocar).

- [ ] **Step 8: Commit**

```powershell
git add backend/src
git commit -m "feat(backend): EmpleadoRequest con tipoContrato y validador condicional (TDD)"
```

---

### Task 3: Backend — `EmpleadoResponse` y `EmpleadoMapper` con los campos nuevos

**Files:**
- Modify: `backend/src/main/java/com/example/empleados/dto/EmpleadoResponse.java`
- Modify: `backend/src/main/java/com/example/empleados/mapper/EmpleadoMapper.java`
- Test: `backend/src/test/java/com/example/empleados/mapper/EmpleadoMapperTest.java`
- Modify (fixture): `backend/src/test/java/com/example/empleados/controller/EmpleadoControllerTest.java`

**Interfaces:**
- Consumes: `TipoContrato`, entidad ampliada (Task 1), `EmpleadoRequest` nuevo (Task 2).
- Produces: `EmpleadoResponse(Long id, String nombre, String apellido, String email, String dni, LocalDate fechaNacimiento, LocalDate fechaIngreso, BigDecimal salario, Departamento departamento, String telefono, boolean activo, TipoContrato tipoContrato, BigDecimal porcentajeAportes, BigDecimal montoFactura, LocalDate fechaServicio)`.

- [ ] **Step 1: Ampliar los tests del mapper**

En `EmpleadoMapperTest.java`: al final de `toEntityMapeaTodosLosCamposYActivoTrue` agregar:

```java
        assertThat(entidad.getTipoContrato()).isEqualTo(TipoContrato.EMPLEADO);
        assertThat(entidad.getPorcentajeAportes()).isEqualByComparingTo("17");
        assertThat(entidad.getMontoFactura()).isNull();
        assertThat(entidad.getFechaServicio()).isNull();
```

Al final de `toResponseMapeaTodosLosCamposIncluyendoId`, antes de los asserts agregar a la entidad:

```java
        entidad.setTipoContrato(TipoContrato.EMPLEADO);
        entidad.setPorcentajeAportes(new BigDecimal("17"));
```

y al final de los asserts:

```java
        assertThat(response.tipoContrato()).isEqualTo(TipoContrato.EMPLEADO);
        assertThat(response.porcentajeAportes()).isEqualByComparingTo("17");
        assertThat(response.montoFactura()).isNull();
        assertThat(response.fechaServicio()).isNull();
```

Y agregar un test nuevo:

```java
    @Test
    void toEntityMapeaTerciarizado() {
        EmpleadoRequest request = new EmpleadoRequest("Luis", "Perez", "luis.perez@example.com",
                "87654321", null, null, null, Departamento.IT, null,
                TipoContrato.TERCIARIZADO, null, new BigDecimal("250000"),
                LocalDate.of(2026, 6, 30));

        Empleado entidad = mapper.toEntity(request);

        assertThat(entidad.getTipoContrato()).isEqualTo(TipoContrato.TERCIARIZADO);
        assertThat(entidad.getMontoFactura()).isEqualByComparingTo("250000");
        assertThat(entidad.getFechaServicio()).isEqualTo(LocalDate.of(2026, 6, 30));
        assertThat(entidad.getSalario()).isNull();
        assertThat(entidad.getFechaNacimiento()).isNull();
        assertThat(entidad.getFechaIngreso()).isNull();
        assertThat(entidad.getPorcentajeAportes()).isNull();
    }
```

- [ ] **Step 2: Verificar que falla**

```powershell
cd backend; .\mvnw.cmd -Dtest=EmpleadoMapperTest test
```

Expected: FAIL de compilación — `response.tipoContrato()` no existe.

- [ ] **Step 3: Ampliar `EmpleadoResponse`**

Reemplazar el contenido de `EmpleadoResponse.java` por:

```java
package com.example.empleados.dto;

import com.example.empleados.domain.Departamento;
import com.example.empleados.domain.TipoContrato;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmpleadoResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        String dni,
        LocalDate fechaNacimiento,
        LocalDate fechaIngreso,
        BigDecimal salario,
        Departamento departamento,
        String telefono,
        boolean activo,
        TipoContrato tipoContrato,
        BigDecimal porcentajeAportes,
        BigDecimal montoFactura,
        LocalDate fechaServicio) {
}
```

- [ ] **Step 4: Ampliar `EmpleadoMapper`**

En `toEntity`, después de `empleado.setTelefono(request.telefono());` agregar:

```java
        empleado.setTipoContrato(request.tipoContrato());
        empleado.setPorcentajeAportes(request.porcentajeAportes());
        empleado.setMontoFactura(request.montoFactura());
        empleado.setFechaServicio(request.fechaServicio());
```

En `toResponse`, agregar los argumentos nuevos al final del constructor:

```java
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
                empleado.isActivo(),
                empleado.getTipoContrato(),
                empleado.getPorcentajeAportes(),
                empleado.getMontoFactura(),
                empleado.getFechaServicio());
```

- [ ] **Step 5: Actualizar el fixture del controller test**

En `EmpleadoControllerTest.altaValidaDevuelve201YResponse`, agregar el import `com.example.empleados.domain.TipoContrato` y reemplazar la construcción del response por:

```java
        EmpleadoResponse response = new EmpleadoResponse(1L, "Ana", "Diaz",
                "ana.diaz@example.com", "12345678", LocalDate.of(1990, 5, 20),
                LocalDate.of(2024, 1, 15), new BigDecimal("150000"), Departamento.IT,
                "+541112345678", true, TipoContrato.EMPLEADO, new BigDecimal("17"),
                null, null);
```

- [ ] **Step 6: Correr la suite backend completa**

```powershell
cd backend; .\mvnw.cmd test
```

Expected: PASS (todas las clases de test).

- [ ] **Step 7: Commit**

```powershell
git add backend/src
git commit -m "feat(backend): mapper y response con campos de tipo de contrato (TDD)"
```

---

### Task 4: Backend — tests de integración end-to-end del terciarizado

**Files:**
- Test: `backend/src/test/java/com/example/empleados/EmpleadoIntegracionTest.java`

**Interfaces:**
- Consumes: stack completo de Tasks 1–3 vía `POST /api/empleados`.
- Produces: nada nuevo — verificación e2e del contrato.

- [ ] **Step 1: Agregar helper y tests al final de la clase**

```java
    private java.util.Map<String, Object> cuerpoTerciarizado(String email, String dni) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", "Luis");
        body.put("apellido", "Perez");
        body.put("email", email);
        body.put("dni", dni);
        body.put("departamento", "IT");
        body.put("tipoContrato", "TERCIARIZADO");
        body.put("montoFactura", 250000);
        body.put("fechaServicio", "2026-06-30");
        return body;
    }

    @Test
    void altaTerciarizadoValidaDevuelve201() throws Exception {
        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                cuerpoTerciarizado("luis.perez@example.com", "87654321"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tipoContrato").value("TERCIARIZADO"))
                .andExpect(jsonPath("$.montoFactura").value(250000))
                .andExpect(jsonPath("$.salario").isEmpty());

        assertThat(repository.existsByEmail("luis.perez@example.com")).isTrue();
    }

    @Test
    void terciarizadoConSalarioDevuelve400() throws Exception {
        Map<String, Object> body = cuerpoTerciarizado("luis.perez@example.com", "87654321");
        body.put("salario", 150000);

        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.salario")
                        .value("El salario no corresponde a un terciarizado"));
    }
```

- [ ] **Step 2: Correr la suite backend completa**

```powershell
cd backend; .\mvnw.cmd test
```

Expected: PASS. Estos tests son verificación e2e de lo construido en Tasks 1–3; si `altaTerciarizadoValidaDevuelve201` falla, el bug está en el validador o el mapper — arreglarlo antes de seguir. (Nota: `fechaServicio` "2026-06-30" es pasada respecto de hoy, 2026-07-12; si esto se ejecuta en otra fecha usar cualquier fecha pasada.)

- [ ] **Step 3: Commit**

```powershell
git add backend/src/test
git commit -m "test(backend): integracion end-to-end del alta de terciarizado"
```

---

### Task 5: Frontend — schema con unión discriminada por `tipoContrato`

**Files:**
- Modify: `src/schemas/empleadoSchema.js`
- Test: Create `src/schemas/empleadoSchema.test.js`

**Interfaces:**
- Consumes: nada (módulo hoja).
- Produces: `export const TIPOS_CONTRATO = ['EMPLEADO', 'TERCIARIZADO']`; `export const empleadoSchema` (discriminated union, misma export usada por el form); `export const DEPARTAMENTOS` (sin cambios).

- [ ] **Step 1: Escribir los tests del schema**

`src/schemas/empleadoSchema.test.js`:

```js
import { empleadoSchema, TIPOS_CONTRATO } from './empleadoSchema';

const comunes = {
  nombre: 'Ana',
  apellido: 'Diaz',
  dni: '12345678',
  email: 'ana.diaz@example.com',
  telefono: '',
  departamento: 'IT',
};

const empleadoValido = {
  ...comunes,
  tipoContrato: 'EMPLEADO',
  fechaNacimiento: '1990-05-20',
  fechaIngreso: '2024-01-15',
  salario: '150000',
  porcentajeAportes: '17',
};

const terciarizadoValido = {
  ...comunes,
  tipoContrato: 'TERCIARIZADO',
  montoFactura: '250000',
  fechaServicio: '2024-06-30',
};

function erroresDe(datos) {
  const resultado = empleadoSchema.safeParse(datos);
  if (resultado.success) return {};
  const errores = {};
  resultado.error.issues.forEach((issue) => {
    errores[issue.path[0]] = issue.message;
  });
  return errores;
}

test('exporta los dos tipos de contrato', () => {
  expect(TIPOS_CONTRATO).toEqual(['EMPLEADO', 'TERCIARIZADO']);
});

test('empleado valido pasa', () => {
  expect(empleadoSchema.safeParse(empleadoValido).success).toBe(true);
});

test('terciarizado valido pasa', () => {
  expect(empleadoSchema.safeParse(terciarizadoValido).success).toBe(true);
});

test('sin tipo de contrato falla en tipoContrato', () => {
  const errores = erroresDe({ ...empleadoValido, tipoContrato: '' });
  expect(errores.tipoContrato).toBe('Seleccione un tipo de contrato');
});

test('empleado sin porcentaje de aportes falla', () => {
  const errores = erroresDe({ ...empleadoValido, porcentajeAportes: '' });
  expect(errores.porcentajeAportes).toBe('El porcentaje de aportes es obligatorio');
});

test('porcentaje de aportes mayor a 30 falla', () => {
  const errores = erroresDe({ ...empleadoValido, porcentajeAportes: '31' });
  expect(errores.porcentajeAportes).toBe('El porcentaje de aportes debe estar entre 0 y 30');
});

test('porcentaje de aportes negativo falla', () => {
  const errores = erroresDe({ ...empleadoValido, porcentajeAportes: '-1' });
  expect(errores.porcentajeAportes).toBe('El porcentaje de aportes debe estar entre 0 y 30');
});

test('terciarizado sin monto de factura falla', () => {
  const errores = erroresDe({ ...terciarizadoValido, montoFactura: '' });
  expect(errores.montoFactura).toBe('El monto de factura es obligatorio');
});

test('monto de factura negativo falla', () => {
  const errores = erroresDe({ ...terciarizadoValido, montoFactura: '-5' });
  expect(errores.montoFactura).toBe('El monto de factura debe ser positivo');
});

test('fecha de servicio futura falla', () => {
  const errores = erroresDe({ ...terciarizadoValido, fechaServicio: '2099-01-01' });
  expect(errores.fechaServicio).toBe('La fecha de servicio no puede ser futura');
});

test('terciarizado no valida campos de empleado', () => {
  // el schema de terciarizado ni siquiera define salario/fechaNacimiento
  const resultado = empleadoSchema.safeParse({
    ...terciarizadoValido,
    salario: 'no-numero',
    fechaNacimiento: 'invalida',
  });
  expect(resultado.success).toBe(true);
  expect(resultado.data.salario).toBeUndefined();
  expect(resultado.data.fechaNacimiento).toBeUndefined();
});
```

- [ ] **Step 2: Verificar que falla**

```powershell
npx react-scripts test --watchAll=false empleadoSchema
```

Expected: FAIL — `TIPOS_CONTRATO` no está exportado y los payloads de empleado fallan porque el schema actual no conoce `tipoContrato`.

- [ ] **Step 3: Reescribir el schema**

Reemplazar el contenido de `src/schemas/empleadoSchema.js` por:

```js
import { z } from 'zod';

export const DEPARTAMENTOS = ['VENTAS', 'IT', 'RRHH', 'ADMINISTRACION', 'PRODUCCION'];
export const TIPOS_CONTRATO = ['EMPLEADO', 'TERCIARIZADO'];

const soloLetras = /^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$/;
const dniRegex = /^\d{7,8}$/;
const telefonoRegex = /^\+?\d{8,15}$/;

function esMayorDeEdad(fechaStr) {
  if (!fechaStr) return false;
  const nac = new Date(fechaStr);
  if (Number.isNaN(nac.getTime())) return false;
  const hoy = new Date();
  let edad = hoy.getFullYear() - nac.getFullYear();
  const m = hoy.getMonth() - nac.getMonth();
  if (m < 0 || (m === 0 && hoy.getDate() < nac.getDate())) {
    edad -= 1;
  }
  return edad >= 18;
}

function noEsFutura(fechaStr) {
  if (!fechaStr) return false;
  const f = new Date(fechaStr);
  if (Number.isNaN(f.getTime())) return false;
  const hoy = new Date();
  hoy.setHours(23, 59, 59, 999);
  return f.getTime() <= hoy.getTime();
}

const numeroDesdeInput = (v) =>
  v === '' || v === null || v === undefined ? undefined : Number(v);

const camposComunes = {
  nombre: z
    .string()
    .trim()
    .min(2, 'El nombre debe tener entre 2 y 50 caracteres')
    .max(50, 'El nombre debe tener entre 2 y 50 caracteres')
    .regex(soloLetras, 'El nombre solo puede contener letras'),
  apellido: z
    .string()
    .trim()
    .min(2, 'El apellido debe tener entre 2 y 50 caracteres')
    .max(50, 'El apellido debe tener entre 2 y 50 caracteres')
    .regex(soloLetras, 'El apellido solo puede contener letras'),
  dni: z
    .string()
    .trim()
    .regex(dniRegex, 'El DNI debe tener 7 u 8 dígitos'),
  email: z
    .string()
    .trim()
    .min(1, 'El email es obligatorio')
    .email('El email no tiene un formato válido'),
  telefono: z
    .union([
      z.literal(''),
      z.string().regex(telefonoRegex, 'El teléfono debe tener entre 8 y 15 dígitos'),
    ])
    .optional(),
  departamento: z.enum(DEPARTAMENTOS, {
    errorMap: () => ({ message: 'Seleccione un departamento' }),
  }),
};

const schemaEmpleado = z.object({
  ...camposComunes,
  tipoContrato: z.literal('EMPLEADO'),
  fechaNacimiento: z
    .string()
    .min(1, 'La fecha de nacimiento es obligatoria')
    .refine(esMayorDeEdad, 'El empleado debe ser mayor de 18 años'),
  fechaIngreso: z
    .string()
    .min(1, 'La fecha de ingreso es obligatoria')
    .refine(noEsFutura, 'La fecha de ingreso no puede ser futura'),
  salario: z.preprocess(
    numeroDesdeInput,
    z
      .number({
        required_error: 'El salario es obligatorio',
        invalid_type_error: 'El salario debe ser un número',
      })
      .positive('El salario debe ser positivo')
      .max(1000000, 'El salario no puede superar 1.000.000')
  ),
  porcentajeAportes: z.preprocess(
    numeroDesdeInput,
    z
      .number({
        required_error: 'El porcentaje de aportes es obligatorio',
        invalid_type_error: 'El porcentaje de aportes debe ser un número',
      })
      .min(0, 'El porcentaje de aportes debe estar entre 0 y 30')
      .max(30, 'El porcentaje de aportes debe estar entre 0 y 30')
  ),
});

const schemaTerciarizado = z.object({
  ...camposComunes,
  tipoContrato: z.literal('TERCIARIZADO'),
  montoFactura: z.preprocess(
    numeroDesdeInput,
    z
      .number({
        required_error: 'El monto de factura es obligatorio',
        invalid_type_error: 'El monto de factura debe ser un número',
      })
      .positive('El monto de factura debe ser positivo')
      .max(1000000, 'El monto de factura no puede superar 1.000.000')
  ),
  fechaServicio: z
    .string()
    .min(1, 'La fecha de servicio es obligatoria')
    .refine(noEsFutura, 'La fecha de servicio no puede ser futura'),
});

export const empleadoSchema = z.discriminatedUnion(
  'tipoContrato',
  [schemaEmpleado, schemaTerciarizado],
  { errorMap: () => ({ message: 'Seleccione un tipo de contrato' }) }
);
```

- [ ] **Step 4: Verificar que pasa**

```powershell
npx react-scripts test --watchAll=false empleadoSchema
```

Expected: PASS (12 tests).

- [ ] **Step 5: Commit**

```powershell
git add src/schemas/
git commit -m "feat(frontend): schema con union discriminada por tipo de contrato (TDD)"
```

---

### Task 6: Frontend — renderizado condicional del formulario

**Files:**
- Modify: `src/components/EmpleadoForm.js`
- Modify: `src/components/steps/DatosPersonalesStep.js`
- Modify: `src/components/steps/DatosLaboralesStep.js`
- Test: Create `src/components/EmpleadoForm.test.js`

**Interfaces:**
- Consumes: `empleadoSchema`, `TIPOS_CONTRATO`, `DEPARTAMENTOS` (Task 5); `altaEmpleado` de `src/services/empleadoService.js` (se mockea en el test).
- Produces: `DatosPersonalesStep({ mostrarFechaNacimiento })` (bool); `DatosLaboralesStep({ tipoContrato })` (string `''` | `'EMPLEADO'` | `'TERCIARIZADO'`). Labels exactos de los campos nuevos: `"Tipo de contrato"`, `"Porcentaje de aportes"`, `"Monto de Factura"`, `"Fecha de Servicio"`.

- [ ] **Step 1: Escribir los tests de interacción**

`src/components/EmpleadoForm.test.js`:

```js
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import EmpleadoForm from './EmpleadoForm';
import { altaEmpleado } from '../services/empleadoService';

jest.mock('../services/empleadoService', () => ({
  altaEmpleado: jest.fn(),
}));

function elegirTipo(tipo) {
  userEvent.selectOptions(screen.getByLabelText('Tipo de contrato'), tipo);
}

beforeEach(() => {
  jest.clearAllMocks();
});

test('sin tipo elegido solo se ven los campos comunes', () => {
  render(<EmpleadoForm onExito={jest.fn()} />);

  expect(screen.getByLabelText('Nombre')).toBeInTheDocument();
  expect(screen.getByLabelText('DNI')).toBeInTheDocument();
  expect(screen.getByLabelText('Tipo de contrato')).toBeInTheDocument();
  expect(screen.getByLabelText('Departamento')).toBeInTheDocument();

  expect(screen.queryByLabelText('Fecha de nacimiento')).not.toBeInTheDocument();
  expect(screen.queryByLabelText('Fecha de ingreso')).not.toBeInTheDocument();
  expect(screen.queryByLabelText('Salario')).not.toBeInTheDocument();
  expect(screen.queryByLabelText('Porcentaje de aportes')).not.toBeInTheDocument();
  expect(screen.queryByLabelText('Monto de Factura')).not.toBeInTheDocument();
  expect(screen.queryByLabelText('Fecha de Servicio')).not.toBeInTheDocument();
});

test('EMPLEADO muestra salario, fechas y porcentaje de aportes', () => {
  render(<EmpleadoForm onExito={jest.fn()} />);
  elegirTipo('EMPLEADO');

  expect(screen.getByLabelText('Fecha de nacimiento')).toBeInTheDocument();
  expect(screen.getByLabelText('Fecha de ingreso')).toBeInTheDocument();
  expect(screen.getByLabelText('Salario')).toBeInTheDocument();
  expect(screen.getByLabelText('Porcentaje de aportes')).toBeInTheDocument();
  expect(screen.queryByLabelText('Monto de Factura')).not.toBeInTheDocument();
  expect(screen.queryByLabelText('Fecha de Servicio')).not.toBeInTheDocument();
});

test('TERCIARIZADO oculta campos de empleado y muestra factura y servicio', () => {
  render(<EmpleadoForm onExito={jest.fn()} />);
  elegirTipo('TERCIARIZADO');

  expect(screen.queryByLabelText('Fecha de nacimiento')).not.toBeInTheDocument();
  expect(screen.queryByLabelText('Fecha de ingreso')).not.toBeInTheDocument();
  expect(screen.queryByLabelText('Salario')).not.toBeInTheDocument();
  expect(screen.queryByLabelText('Porcentaje de aportes')).not.toBeInTheDocument();
  expect(screen.getByLabelText('Monto de Factura')).toBeInTheDocument();
  expect(screen.getByLabelText('Fecha de Servicio')).toBeInTheDocument();
});

test('cambiar de tipo limpia los valores del tipo anterior', () => {
  render(<EmpleadoForm onExito={jest.fn()} />);

  elegirTipo('EMPLEADO');
  userEvent.type(screen.getByLabelText('Salario'), '150000');
  expect(screen.getByLabelText('Salario')).toHaveValue(150000);

  elegirTipo('TERCIARIZADO');
  elegirTipo('EMPLEADO');
  expect(screen.getByLabelText('Salario')).toHaveValue(null);
});

test('submit de terciarizado envia solo los campos del tipo', async () => {
  altaEmpleado.mockResolvedValue({ ok: true, empleado: { id: 1 } });
  const onExito = jest.fn();
  render(<EmpleadoForm onExito={onExito} />);

  userEvent.type(screen.getByLabelText('Nombre'), 'Luis');
  userEvent.type(screen.getByLabelText('Apellido'), 'Perez');
  userEvent.type(screen.getByLabelText('DNI'), '87654321');
  userEvent.type(screen.getByLabelText('Email'), 'luis.perez@example.com');
  userEvent.selectOptions(screen.getByLabelText('Departamento'), 'IT');
  elegirTipo('TERCIARIZADO');
  userEvent.type(screen.getByLabelText('Monto de Factura'), '250000');
  userEvent.type(screen.getByLabelText('Fecha de Servicio'), '2024-06-30');

  userEvent.click(screen.getByRole('button', { name: 'Guardar' }));

  await waitFor(() => expect(altaEmpleado).toHaveBeenCalledTimes(1));
  const enviado = altaEmpleado.mock.calls[0][0];
  expect(enviado.tipoContrato).toBe('TERCIARIZADO');
  expect(enviado.montoFactura).toBe(250000);
  expect(enviado.fechaServicio).toBe('2024-06-30');
  expect(enviado).not.toHaveProperty('salario');
  expect(enviado).not.toHaveProperty('fechaNacimiento');
  expect(enviado).not.toHaveProperty('fechaIngreso');
  expect(enviado).not.toHaveProperty('porcentajeAportes');
  await waitFor(() => expect(onExito).toHaveBeenCalled());
});
```

- [ ] **Step 2: Verificar que falla**

```powershell
npx react-scripts test --watchAll=false EmpleadoForm
```

Expected: FAIL — no existe el select "Tipo de contrato".

- [ ] **Step 3: Modificar `DatosPersonalesStep.js`**

```js
import React from 'react';
import { useFormContext } from 'react-hook-form';
import TextField from '../fields/TextField';

export default function DatosPersonalesStep({ mostrarFechaNacimiento }) {
  const {
    register,
    formState: { errors },
  } = useFormContext();

  return (
    <div>
      <TextField label="Nombre" name="nombre" register={register} error={errors.nombre} />
      <TextField label="Apellido" name="apellido" register={register} error={errors.apellido} />
      <TextField label="DNI" name="dni" register={register} error={errors.dni} />
      {mostrarFechaNacimiento && (
        <TextField
          label="Fecha de nacimiento"
          name="fechaNacimiento"
          type="date"
          register={register}
          error={errors.fechaNacimiento}
        />
      )}
    </div>
  );
}
```

- [ ] **Step 4: Modificar `DatosLaboralesStep.js`**

```js
import React from 'react';
import { useFormContext } from 'react-hook-form';
import TextField from '../fields/TextField';
import SelectField from '../fields/SelectField';
import { DEPARTAMENTOS, TIPOS_CONTRATO } from '../../schemas/empleadoSchema';

export default function DatosLaboralesStep({ tipoContrato }) {
  const {
    register,
    formState: { errors },
  } = useFormContext();

  return (
    <div>
      <SelectField
        label="Tipo de contrato"
        name="tipoContrato"
        register={register}
        error={errors.tipoContrato}
        options={TIPOS_CONTRATO}
      />
      <SelectField
        label="Departamento"
        name="departamento"
        register={register}
        error={errors.departamento}
        options={DEPARTAMENTOS}
      />
      {tipoContrato === 'EMPLEADO' && (
        <>
          <TextField
            label="Fecha de ingreso"
            name="fechaIngreso"
            type="date"
            register={register}
            error={errors.fechaIngreso}
          />
          <TextField
            label="Salario"
            name="salario"
            type="number"
            register={register}
            error={errors.salario}
          />
          <TextField
            label="Porcentaje de aportes"
            name="porcentajeAportes"
            type="number"
            register={register}
            error={errors.porcentajeAportes}
          />
        </>
      )}
      {tipoContrato === 'TERCIARIZADO' && (
        <>
          <TextField
            label="Monto de Factura"
            name="montoFactura"
            type="number"
            register={register}
            error={errors.montoFactura}
          />
          <TextField
            label="Fecha de Servicio"
            name="fechaServicio"
            type="date"
            register={register}
            error={errors.fechaServicio}
          />
        </>
      )}
    </div>
  );
}
```

- [ ] **Step 5: Modificar `EmpleadoForm.js`**

Cambios puntuales (el resto queda igual):

1. `valoresIniciales` pasa a:

```js
const valoresIniciales = {
  nombre: '',
  apellido: '',
  dni: '',
  fechaNacimiento: '',
  email: '',
  telefono: '',
  departamento: '',
  tipoContrato: '',
  fechaIngreso: '',
  salario: '',
  porcentajeAportes: '',
  montoFactura: '',
  fechaServicio: '',
};
```

2. El `useForm` agrega `shouldUnregister: true` (así los campos desmontados salen de los valores y el submit lleva solo los del tipo activo):

```js
  const methods = useForm({
    resolver: zodResolver(empleadoSchema),
    mode: 'onTouched',
    defaultValues: valoresIniciales,
    shouldUnregister: true,
  });
  const { handleSubmit, setError, watch } = methods;
  const tipoContrato = watch('tipoContrato');
```

3. Los steps reciben las props:

```js
        <h5 className="mb-3">Datos personales</h5>
        <DatosPersonalesStep mostrarFechaNacimiento={tipoContrato === 'EMPLEADO'} />

        <h5 className="mt-4 mb-3">Contacto</h5>
        <ContactoStep />

        <h5 className="mt-4 mb-3">Datos laborales</h5>
        <DatosLaboralesStep tipoContrato={tipoContrato} />
```

- [ ] **Step 6: Verificar que pasa**

```powershell
npx react-scripts test --watchAll=false EmpleadoForm
```

Expected: PASS (5 tests). Si `cambiar de tipo limpia los valores` falla porque el input conserva el valor, verificar que `shouldUnregister: true` quedó en el `useForm`.

- [ ] **Step 7: Correr toda la suite frontend**

```powershell
npx react-scripts test --watchAll=false
```

Expected: PASS (schema + form).

- [ ] **Step 8: Commit**

```powershell
git add src/components/ src/schemas/
git commit -m "feat(frontend): formulario con campos condicionales por tipo de contrato (TDD)"
```

---

### Task 7: Frontend — payload por tipo en `empleadoService`

**Files:**
- Modify: `src/services/empleadoService.js`
- Test: Create `src/services/empleadoService.test.js`

**Interfaces:**
- Consumes: datos crudos del form (Task 6); `crearEmpleado(empleado)` de la capa api.
- Produces: `altaEmpleado(datosFormulario)` envía a la api solo campos comunes + los del tipo (`tipoContrato` incluido); números convertidos con `Number()`, strings con `trim()`, `telefono` vacío como `null`.

- [ ] **Step 1: Escribir los tests del service**

`src/services/empleadoService.test.js`:

```js
jest.mock('../config', () => ({ USE_MOCK: true, API_URL: 'http://irrelevante' }));
jest.mock('../api/empleadoApiMock', () => ({ crearEmpleado: jest.fn() }));

import { altaEmpleado } from './empleadoService';
import { crearEmpleado } from '../api/empleadoApiMock';

beforeEach(() => {
  jest.clearAllMocks();
  crearEmpleado.mockResolvedValue({ ok: true, empleado: { id: 1 } });
});

test('empleado envia campos de empleado y no de terciarizado', async () => {
  await altaEmpleado({
    nombre: ' Ana ',
    apellido: 'Diaz',
    dni: '12345678',
    email: 'ana.diaz@example.com',
    telefono: '',
    departamento: 'IT',
    tipoContrato: 'EMPLEADO',
    fechaNacimiento: '1990-05-20',
    fechaIngreso: '2024-01-15',
    salario: 150000,
    porcentajeAportes: 17,
  });

  const enviado = crearEmpleado.mock.calls[0][0];
  expect(enviado).toEqual({
    nombre: 'Ana',
    apellido: 'Diaz',
    dni: '12345678',
    email: 'ana.diaz@example.com',
    telefono: null,
    departamento: 'IT',
    tipoContrato: 'EMPLEADO',
    fechaNacimiento: '1990-05-20',
    fechaIngreso: '2024-01-15',
    salario: 150000,
    porcentajeAportes: 17,
  });
});

test('terciarizado envia solo sus campos aunque vengan restos de empleado', async () => {
  await altaEmpleado({
    nombre: 'Luis',
    apellido: 'Perez',
    dni: '87654321',
    email: 'luis.perez@example.com',
    telefono: '+541112345678',
    departamento: 'IT',
    tipoContrato: 'TERCIARIZADO',
    montoFactura: 250000,
    fechaServicio: '2024-06-30',
    salario: 999,
  });

  const enviado = crearEmpleado.mock.calls[0][0];
  expect(enviado).toEqual({
    nombre: 'Luis',
    apellido: 'Perez',
    dni: '87654321',
    email: 'luis.perez@example.com',
    telefono: '+541112345678',
    departamento: 'IT',
    tipoContrato: 'TERCIARIZADO',
    montoFactura: 250000,
    fechaServicio: '2024-06-30',
  });
  expect(enviado).not.toHaveProperty('salario');
});
```

- [ ] **Step 2: Verificar que falla**

```powershell
npx react-scripts test --watchAll=false empleadoService
```

Expected: FAIL — el service actual siempre manda `salario`/`fechaNacimiento` y no manda `tipoContrato` (el primer test falla en `toEqual`, el segundo revienta en `datosFormulario.salario` → `Number(undefined)` = `NaN` incluido en el payload).

- [ ] **Step 3: Reescribir el service**

Reemplazar el contenido de `src/services/empleadoService.js` por:

```js
import * as apiReal from '../api/empleadoApi';
import * as apiMock from '../api/empleadoApiMock';
import { USE_MOCK } from '../config';

const api = USE_MOCK ? apiMock : apiReal;

// Orquesta el alta: normaliza los datos crudos del formulario y arma el
// payload solo con los campos del tipo de contrato elegido.
export async function altaEmpleado(datosFormulario) {
  const telefono = (datosFormulario.telefono || '').trim();
  const comunes = {
    nombre: datosFormulario.nombre.trim(),
    apellido: datosFormulario.apellido.trim(),
    email: datosFormulario.email.trim(),
    dni: datosFormulario.dni.trim(),
    departamento: datosFormulario.departamento,
    telefono: telefono ? telefono : null,
    tipoContrato: datosFormulario.tipoContrato,
  };

  if (datosFormulario.tipoContrato === 'TERCIARIZADO') {
    return api.crearEmpleado({
      ...comunes,
      montoFactura: Number(datosFormulario.montoFactura),
      fechaServicio: datosFormulario.fechaServicio,
    });
  }

  return api.crearEmpleado({
    ...comunes,
    fechaNacimiento: datosFormulario.fechaNacimiento,
    fechaIngreso: datosFormulario.fechaIngreso,
    salario: Number(datosFormulario.salario),
    porcentajeAportes: Number(datosFormulario.porcentajeAportes),
  });
}
```

- [ ] **Step 4: Verificar que pasa y correr toda la suite frontend**

```powershell
npx react-scripts test --watchAll=false
```

Expected: PASS (schema + form + service).

- [ ] **Step 5: Verificación manual rápida (opcional pero recomendada)**

Levantar backend (`cd backend; .\mvnw.cmd spring-boot:run`) y frontend (`npm start`), dar de alta un terciarizado real y confirmar 201.

- [ ] **Step 6: Commit**

```powershell
git add src/services/
git commit -m "feat(frontend): payload por tipo de contrato en empleadoService (TDD)"
```

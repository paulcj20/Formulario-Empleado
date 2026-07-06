# Backend de Alta de Empleado (Spring Boot) — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Backend REST Spring Boot por capas (controller/service/repository) para el alta de un empleado (`POST /api/empleados`), con validaciones y persistencia PostgreSQL, construido con TDD.

**Architecture:** Capas: `EmpleadoController` (HTTP, `@Valid`) → `EmpleadoService` (regla de unicidad email/dni) → `EmpleadoRepository` (JPA). `EmpleadoRequest`/`EmpleadoResponse` (records) separan la API de la entidad `Empleado`; `EmpleadoMapper` convierte entre ambos. Un validador custom `@MayorDeEdad` y `@RestControllerAdvice` (`GlobalExceptionHandler`) que traduce errores a `{ errors: { campo: mensaje } }`.

**Tech Stack:** Java 17 (Temurin), Spring Boot 3.x, Maven Wrapper (`mvnw`), Spring Web, Spring Data JPA, Bean Validation (Hibernate Validator), PostgreSQL (runtime), H2 (tests), JUnit 5 + Mockito + MockMvc.

## Global Constraints

- **Java 17**, **Spring Boot 3.x**, paquetes `jakarta.*` (NO `javax.*`).
- **NO hay `mvn` en el sistema.** Se usa SIEMPRE el Maven Wrapper. Comando canónico de build/test, desde `backend/`:
  `./mvnw <args>` (tras `chmod +x mvnw`). Si el wrapper no encuentra Java, prefijar
  `JAVA_HOME="C:/Program Files/Eclipse Adoptium/jdk-17.0.18.8-hotspot"`. La primera corrida descarga Maven y dependencias (puede tardar varios minutos).
- **TDD obligatorio:** en cada tarea, escribir el test PRIMERO, correrlo y verlo fallar (RED), implementar lo mínimo, correrlo y verlo pasar (GREEN), luego commit. La salida de tests debe quedar limpia.
- **Ubicación:** el backend vive en `C:\Tool Room\Development\FormularioPrueba\backend\`. El frontend React está en la raíz (no tocarlo).
- **Paquete base:** `com.example.empleados`. Coordenadas: `groupId=com.example`, `artifactId=empleados`.
- **Enum `Departamento` (valores exactos):** `VENTAS`, `IT`, `RRHH`, `ADMINISTRACION`, `PRODUCCION`.
- **Formato de error de la API (exacto):** `{ "errors": { "<campo>": "<mensaje>" } }` — el frontend lo mapea a cada input.
- **Mensajes de validación en español**, iguales a los del frontend (ver cada tarea).
- **Alcance:** solo `POST /api/empleados`. Sin GET/PUT/DELETE.

---

### Task 1: Scaffold del proyecto Spring Boot + config H2/PostgreSQL

**Files:**
- Create (generados por Spring Initializr): `backend/pom.xml`, `backend/mvnw`, `backend/mvnw.cmd`, `backend/.mvn/**`, `backend/src/main/java/com/example/empleados/EmpleadosApplication.java`, `backend/src/test/java/com/example/empleados/EmpleadosApplicationTests.java`
- Modify: `backend/pom.xml` (agregar H2 scope test)
- Create: `backend/src/main/resources/application.properties`
- Create: `backend/src/test/resources/application.properties`
- Create: `backend/.gitignore` (si Initializr no lo trae, agregar `target/`)

**Interfaces:**
- Produces: proyecto Maven compilable; `./mvnw test` corre el `contextLoads` con H2; app `com.example.empleados`.

- [ ] **Step 1: Descargar el proyecto desde Spring Initializr (incluye el Maven Wrapper) y extraerlo con `jar` (el JDK ya está instalado)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba"
curl -s -G "https://start.spring.io/starter.zip" \
  -d type=maven-project -d language=java -d javaVersion=17 \
  -d groupId=com.example -d artifactId=empleados -d name=empleados \
  -d packageName=com.example.empleados -d baseDir=backend \
  -d dependencies=web,data-jpa,validation,postgresql \
  -o backend.zip
jar xf backend.zip
rm -f backend.zip
chmod +x backend/mvnw
ls backend
```
Expected: se crea `backend/` con `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`, `src/`.

- [ ] **Step 2: Agregar H2 (scope test) al `pom.xml`**

En `backend/pom.xml`, dentro de `<dependencies>`, agregar junto a las demás dependencias:
```xml
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>test</scope>
		</dependency>
```

- [ ] **Step 3: Escribir `backend/src/main/resources/application.properties` (PostgreSQL vía variables de entorno)**

```properties
spring.application.name=empleados
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/empleados}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:postgres}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

- [ ] **Step 4: Escribir `backend/src/test/resources/application.properties` (H2 en memoria, override para tests)**

```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

- [ ] **Step 5: Correr los tests (el `contextLoads` por defecto debe pasar con H2)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba/backend"
./mvnw -q test
```
Expected: `BUILD SUCCESS`, el test `EmpleadosApplicationTests.contextLoads` pasa. (Primera corrida: descarga Maven y dependencias.)

- [ ] **Step 6: Commit**

```bash
cd "C:/Tool Room/Development/FormularioPrueba"
git add backend
git commit -m "chore(backend): scaffold Spring Boot con JPA/validation/postgres + H2 para tests"
```

---

### Task 2: Entidad `Empleado`, enum `Departamento` y `EmpleadoRepository`

**Files:**
- Create: `backend/src/main/java/com/example/empleados/domain/Departamento.java`
- Create: `backend/src/main/java/com/example/empleados/domain/Empleado.java`
- Create: `backend/src/main/java/com/example/empleados/repository/EmpleadoRepository.java`
- Test: `backend/src/test/java/com/example/empleados/repository/EmpleadoRepositoryTest.java`

**Interfaces:**
- Produces:
  - `Departamento` enum: `VENTAS, IT, RRHH, ADMINISTRACION, PRODUCCION`.
  - `Empleado` entidad con campos `id, nombre, apellido, email, dni, fechaNacimiento, fechaIngreso, salario, departamento, telefono, activo` y getters/setters + constructor vacío.
  - `EmpleadoRepository extends JpaRepository<Empleado, Long>` con `boolean existsByEmail(String)` y `boolean existsByDni(String)`.

- [ ] **Step 1: Escribir el test del repositorio (RED)**

`backend/src/test/java/com/example/empleados/repository/EmpleadoRepositoryTest.java`:
```java
package com.example.empleados.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.empleados.domain.Departamento;
import com.example.empleados.domain.Empleado;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class EmpleadoRepositoryTest {

    @Autowired
    private EmpleadoRepository repository;

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
        e.setActivo(true);
        return e;
    }

    @Test
    void existsByEmailDevuelveTrueCuandoExiste() {
        repository.save(nuevoEmpleado());

        assertThat(repository.existsByEmail("ana.diaz@example.com")).isTrue();
        assertThat(repository.existsByEmail("otro@example.com")).isFalse();
    }

    @Test
    void existsByDniDevuelveTrueCuandoExiste() {
        repository.save(nuevoEmpleado());

        assertThat(repository.existsByDni("12345678")).isTrue();
        assertThat(repository.existsByDni("87654321")).isFalse();
    }

    @Test
    void guardaAsignaId() {
        Empleado guardado = repository.save(nuevoEmpleado());

        assertThat(guardado.getId()).isNotNull();
    }
}
```

- [ ] **Step 2: Correr el test y verificar que falla (RED)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba/backend"
./mvnw -q -Dtest=EmpleadoRepositoryTest test
```
Expected: FALLA la compilación (no existen `Departamento`, `Empleado`, `EmpleadoRepository`).

- [ ] **Step 3: Crear el enum `Departamento`**

`backend/src/main/java/com/example/empleados/domain/Departamento.java`:
```java
package com.example.empleados.domain;

public enum Departamento {
    VENTAS,
    IT,
    RRHH,
    ADMINISTRACION,
    PRODUCCION
}
```

- [ ] **Step 4: Crear la entidad `Empleado`**

`backend/src/main/java/com/example/empleados/domain/Empleado.java`:
```java
package com.example.empleados.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "empleados")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String dni;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private LocalDate fechaIngreso;

    @Column(nullable = false)
    private BigDecimal salario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Departamento departamento;

    private String telefono;

    @Column(nullable = false)
    private boolean activo = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = salario;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
```

- [ ] **Step 5: Crear el repositorio**

`backend/src/main/java/com/example/empleados/repository/EmpleadoRepository.java`:
```java
package com.example.empleados.repository;

import com.example.empleados.domain.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    boolean existsByEmail(String email);

    boolean existsByDni(String dni);
}
```

- [ ] **Step 6: Correr el test y verificar que pasa (GREEN)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba/backend"
./mvnw -q -Dtest=EmpleadoRepositoryTest test
```
Expected: PASS (3 tests).

- [ ] **Step 7: Commit**

```bash
cd "C:/Tool Room/Development/FormularioPrueba"
git add backend
git commit -m "feat(backend): entidad Empleado, enum Departamento y repository (TDD)"
```

---

### Task 3: Validador custom `@MayorDeEdad`

**Files:**
- Create: `backend/src/main/java/com/example/empleados/validation/MayorDeEdad.java`
- Create: `backend/src/main/java/com/example/empleados/validation/MayorDeEdadValidator.java`
- Test: `backend/src/test/java/com/example/empleados/validation/MayorDeEdadValidatorTest.java`

**Interfaces:**
- Produces: anotación `@MayorDeEdad` (aplicable a `LocalDate`) y `MayorDeEdadValidator` que devuelve `true` para `null` (delega la obligatoriedad a `@NotNull`) y para edad ≥ 18; `false` si es menor.

- [ ] **Step 1: Escribir el test del validador (RED)**

`backend/src/test/java/com/example/empleados/validation/MayorDeEdadValidatorTest.java`:
```java
package com.example.empleados.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MayorDeEdadValidatorTest {

    private final MayorDeEdadValidator validator = new MayorDeEdadValidator();

    @Test
    void nullEsValido() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void mayorDe18EsValido() {
        assertThat(validator.isValid(LocalDate.now().minusYears(25), null)).isTrue();
    }

    @Test
    void exactamente18EsValido() {
        assertThat(validator.isValid(LocalDate.now().minusYears(18), null)).isTrue();
    }

    @Test
    void menorDe18EsInvalido() {
        assertThat(validator.isValid(LocalDate.now().minusYears(17), null)).isFalse();
    }
}
```

- [ ] **Step 2: Correr el test y verificar que falla (RED)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba/backend"
./mvnw -q -Dtest=MayorDeEdadValidatorTest test
```
Expected: FALLA la compilación (no existen `MayorDeEdad`, `MayorDeEdadValidator`).

- [ ] **Step 3: Crear la anotación `@MayorDeEdad`**

`backend/src/main/java/com/example/empleados/validation/MayorDeEdad.java`:
```java
package com.example.empleados.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MayorDeEdadValidator.class)
public @interface MayorDeEdad {

    String message() default "El empleado debe ser mayor de 18 años";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

- [ ] **Step 4: Crear el `MayorDeEdadValidator`**

`backend/src/main/java/com/example/empleados/validation/MayorDeEdadValidator.java`:
```java
package com.example.empleados.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class MayorDeEdadValidator implements ConstraintValidator<MayorDeEdad, LocalDate> {

    @Override
    public boolean isValid(LocalDate fechaNacimiento, ConstraintValidatorContext context) {
        if (fechaNacimiento == null) {
            return true;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears() >= 18;
    }
}
```

- [ ] **Step 5: Correr el test y verificar que pasa (GREEN)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba/backend"
./mvnw -q -Dtest=MayorDeEdadValidatorTest test
```
Expected: PASS (4 tests).

- [ ] **Step 6: Commit**

```bash
cd "C:/Tool Room/Development/FormularioPrueba"
git add backend
git commit -m "feat(backend): validador custom @MayorDeEdad (TDD)"
```

---

### Task 4: DTOs `EmpleadoRequest` (Bean Validation) y `EmpleadoResponse`

**Files:**
- Create: `backend/src/main/java/com/example/empleados/dto/EmpleadoRequest.java`
- Create: `backend/src/main/java/com/example/empleados/dto/EmpleadoResponse.java`
- Test: `backend/src/test/java/com/example/empleados/dto/EmpleadoRequestValidationTest.java`

**Interfaces:**
- Produces:
  - `EmpleadoRequest` (record) con componentes `nombre, apellido, email, dni, fechaNacimiento, fechaIngreso, salario, departamento, telefono` y las anotaciones de Bean Validation.
  - `EmpleadoResponse` (record) con `id, nombre, apellido, email, dni, fechaNacimiento, fechaIngreso, salario, departamento, telefono, activo`.

- [ ] **Step 1: Escribir el test de validación del DTO (RED)**

`backend/src/test/java/com/example/empleados/dto/EmpleadoRequestValidationTest.java`:
```java
package com.example.empleados.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.empleados.domain.Departamento;
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

    private EmpleadoRequest valido() {
        return new EmpleadoRequest(
                "Ana",
                "Diaz",
                "ana.diaz@example.com",
                "12345678",
                LocalDate.of(1990, 5, 20),
                LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"),
                Departamento.IT,
                "+541112345678");
    }

    private Set<String> camposConError(EmpleadoRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void requestValidoNoTieneViolaciones() {
        assertThat(validator.validate(valido())).isEmpty();
    }

    @Test
    void telefonoNuloEsValido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, null);
        assertThat(validator.validate(r)).isEmpty();
    }

    @Test
    void nombreVacioEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678");
        assertThat(camposConError(r)).contains("nombre");
    }

    @Test
    void emailInvalidoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "no-es-email",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678");
        assertThat(camposConError(r)).contains("email");
    }

    @Test
    void dniNoNumericoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "ABC123", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678");
        assertThat(camposConError(r)).contains("dni");
    }

    @Test
    void menorDeEdadEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.now().minusYears(15), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678");
        assertThat(camposConError(r)).contains("fechaNacimiento");
    }

    @Test
    void fechaIngresoFuturaEsInvalida() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.now().plusDays(1),
                new BigDecimal("150000"), Departamento.IT, "+541112345678");
        assertThat(camposConError(r)).contains("fechaIngreso");
    }

    @Test
    void salarioNegativoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("-1"), Departamento.IT, "+541112345678");
        assertThat(camposConError(r)).contains("salario");
    }

    @Test
    void salarioSuperiorAlMaximoEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("1000001"), Departamento.IT, "+541112345678");
        assertThat(camposConError(r)).contains("salario");
    }

    @Test
    void departamentoNuloEsInvalido() {
        EmpleadoRequest r = new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com",
                "12345678", LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), null, "+541112345678");
        assertThat(camposConError(r)).contains("departamento");
    }
}
```

- [ ] **Step 2: Correr el test y verificar que falla (RED)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba/backend"
./mvnw -q -Dtest=EmpleadoRequestValidationTest test
```
Expected: FALLA la compilación (no existen `EmpleadoRequest`, `EmpleadoResponse`).

- [ ] **Step 3: Crear el record `EmpleadoRequest` con Bean Validation**

`backend/src/main/java/com/example/empleados/dto/EmpleadoRequest.java`:
```java
package com.example.empleados.dto;

import com.example.empleados.domain.Departamento;
import com.example.empleados.validation.MayorDeEdad;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

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

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @MayorDeEdad
        LocalDate fechaNacimiento,

        @NotNull(message = "La fecha de ingreso es obligatoria")
        @PastOrPresent(message = "La fecha de ingreso no puede ser futura")
        LocalDate fechaIngreso,

        @NotNull(message = "El salario es obligatorio")
        @Positive(message = "El salario debe ser positivo")
        @DecimalMax(value = "1000000", message = "El salario no puede superar 1.000.000")
        BigDecimal salario,

        @NotNull(message = "Seleccione un departamento")
        Departamento departamento,

        @Pattern(regexp = "^\\+?\\d{8,15}$", message = "El teléfono debe tener entre 8 y 15 dígitos")
        String telefono) {
}
```

- [ ] **Step 4: Crear el record `EmpleadoResponse`**

`backend/src/main/java/com/example/empleados/dto/EmpleadoResponse.java`:
```java
package com.example.empleados.dto;

import com.example.empleados.domain.Departamento;
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
        boolean activo) {
}
```

- [ ] **Step 5: Correr el test y verificar que pasa (GREEN)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba/backend"
./mvnw -q -Dtest=EmpleadoRequestValidationTest test
```
Expected: PASS (10 tests).

- [ ] **Step 6: Commit**

```bash
cd "C:/Tool Room/Development/FormularioPrueba"
git add backend
git commit -m "feat(backend): DTOs EmpleadoRequest/Response con Bean Validation (TDD)"
```

---

### Task 5: `EmpleadoMapper`

**Files:**
- Create: `backend/src/main/java/com/example/empleados/mapper/EmpleadoMapper.java`
- Test: `backend/src/test/java/com/example/empleados/mapper/EmpleadoMapperTest.java`

**Interfaces:**
- Produces: `EmpleadoMapper` (clase `@Component`) con `Empleado toEntity(EmpleadoRequest)` (setea `activo=true`, `id=null`) y `EmpleadoResponse toResponse(Empleado)`.

- [ ] **Step 1: Escribir el test del mapper (RED)**

`backend/src/test/java/com/example/empleados/mapper/EmpleadoMapperTest.java`:
```java
package com.example.empleados.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.empleados.domain.Departamento;
import com.example.empleados.domain.Empleado;
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
                new BigDecimal("150000"), Departamento.IT, "+541112345678");

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
```

- [ ] **Step 2: Correr el test y verificar que falla (RED)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba/backend"
./mvnw -q -Dtest=EmpleadoMapperTest test
```
Expected: FALLA la compilación (no existe `EmpleadoMapper`).

- [ ] **Step 3: Crear el `EmpleadoMapper`**

`backend/src/main/java/com/example/empleados/mapper/EmpleadoMapper.java`:
```java
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
```

- [ ] **Step 4: Correr el test y verificar que pasa (GREEN)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba/backend"
./mvnw -q -Dtest=EmpleadoMapperTest test
```
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
cd "C:/Tool Room/Development/FormularioPrueba"
git add backend
git commit -m "feat(backend): EmpleadoMapper request<->entidad<->response (TDD)"
```

---

### Task 6: Excepción de duplicado + `EmpleadoService`

**Files:**
- Create: `backend/src/main/java/com/example/empleados/exception/EmpleadoDuplicadoException.java`
- Create: `backend/src/main/java/com/example/empleados/service/EmpleadoService.java`
- Create: `backend/src/main/java/com/example/empleados/service/EmpleadoServiceImpl.java`
- Test: `backend/src/test/java/com/example/empleados/service/EmpleadoServiceImplTest.java`

**Interfaces:**
- Consumes: `EmpleadoRepository` (`existsByEmail`, `existsByDni`, `save`), `EmpleadoMapper`.
- Produces:
  - `EmpleadoDuplicadoException extends RuntimeException` con `String getCampo()` y mensaje.
  - `EmpleadoService` (interfaz) con `EmpleadoResponse altaEmpleado(EmpleadoRequest)`.
  - `EmpleadoServiceImpl` (`@Service`), constructor `(EmpleadoRepository, EmpleadoMapper)`: lanza `EmpleadoDuplicadoException("email", "Ya existe un empleado con ese email")` si el email existe; idem `"dni"`/"Ya existe un empleado con ese DNI"; si no, mapea, guarda y devuelve el response.

- [ ] **Step 1: Escribir el test del service (RED)**

`backend/src/test/java/com/example/empleados/service/EmpleadoServiceImplTest.java`:
```java
package com.example.empleados.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.empleados.domain.Departamento;
import com.example.empleados.domain.Empleado;
import com.example.empleados.dto.EmpleadoRequest;
import com.example.empleados.dto.EmpleadoResponse;
import com.example.empleados.exception.EmpleadoDuplicadoException;
import com.example.empleados.mapper.EmpleadoMapper;
import com.example.empleados.repository.EmpleadoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmpleadoServiceImplTest {

    @Mock
    private EmpleadoRepository repository;

    private EmpleadoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmpleadoServiceImpl(repository, new EmpleadoMapper());
    }

    private EmpleadoRequest request() {
        return new EmpleadoRequest("Ana", "Diaz", "ana.diaz@example.com", "12345678",
                LocalDate.of(1990, 5, 20), LocalDate.of(2024, 1, 15),
                new BigDecimal("150000"), Departamento.IT, "+541112345678");
    }

    @Test
    void altaGuardaYDevuelveResponse() {
        when(repository.existsByEmail("ana.diaz@example.com")).thenReturn(false);
        when(repository.existsByDni("12345678")).thenReturn(false);
        when(repository.save(any(Empleado.class))).thenAnswer(invocation -> {
            Empleado e = invocation.getArgument(0);
            e.setId(99L);
            return e;
        });

        EmpleadoResponse response = service.altaEmpleado(request());

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.email()).isEqualTo("ana.diaz@example.com");
        assertThat(response.activo()).isTrue();
        verify(repository).save(any(Empleado.class));
    }

    @Test
    void emailDuplicadoLanzaExcepcion() {
        when(repository.existsByEmail("ana.diaz@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.altaEmpleado(request()))
                .isInstanceOf(EmpleadoDuplicadoException.class)
                .satisfies(ex -> assertThat(((EmpleadoDuplicadoException) ex).getCampo()).isEqualTo("email"));

        verify(repository, never()).save(any());
    }

    @Test
    void dniDuplicadoLanzaExcepcion() {
        when(repository.existsByEmail("ana.diaz@example.com")).thenReturn(false);
        when(repository.existsByDni("12345678")).thenReturn(true);

        assertThatThrownBy(() -> service.altaEmpleado(request()))
                .isInstanceOf(EmpleadoDuplicadoException.class)
                .satisfies(ex -> assertThat(((EmpleadoDuplicadoException) ex).getCampo()).isEqualTo("dni"));

        verify(repository, never()).save(any());
    }
}
```

- [ ] **Step 2: Correr el test y verificar que falla (RED)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba/backend"
./mvnw -q -Dtest=EmpleadoServiceImplTest test
```
Expected: FALLA la compilación (no existen la excepción ni el service).

- [ ] **Step 3: Crear `EmpleadoDuplicadoException`**

`backend/src/main/java/com/example/empleados/exception/EmpleadoDuplicadoException.java`:
```java
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
```

- [ ] **Step 4: Crear la interfaz `EmpleadoService`**

`backend/src/main/java/com/example/empleados/service/EmpleadoService.java`:
```java
package com.example.empleados.service;

import com.example.empleados.dto.EmpleadoRequest;
import com.example.empleados.dto.EmpleadoResponse;

public interface EmpleadoService {

    EmpleadoResponse altaEmpleado(EmpleadoRequest request);
}
```

- [ ] **Step 5: Crear `EmpleadoServiceImpl`**

`backend/src/main/java/com/example/empleados/service/EmpleadoServiceImpl.java`:
```java
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
```

- [ ] **Step 6: Correr el test y verificar que pasa (GREEN)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba/backend"
./mvnw -q -Dtest=EmpleadoServiceImplTest test
```
Expected: PASS (3 tests).

- [ ] **Step 7: Commit**

```bash
cd "C:/Tool Room/Development/FormularioPrueba"
git add backend
git commit -m "feat(backend): EmpleadoService con regla de unicidad email/dni (TDD)"
```

---

### Task 7: `EmpleadoController` + `GlobalExceptionHandler` + CORS

**Files:**
- Create: `backend/src/main/java/com/example/empleados/controller/EmpleadoController.java`
- Create: `backend/src/main/java/com/example/empleados/exception/GlobalExceptionHandler.java`
- Create: `backend/src/main/java/com/example/empleados/config/CorsConfig.java`
- Test: `backend/src/test/java/com/example/empleados/controller/EmpleadoControllerTest.java`

**Interfaces:**
- Consumes: `EmpleadoService`.
- Produces:
  - `EmpleadoController` (`@RestController`, `@RequestMapping("/api/empleados")`) con `POST` `@Valid @RequestBody EmpleadoRequest` → `201 Created` + `EmpleadoResponse`.
  - `GlobalExceptionHandler` (`@RestControllerAdvice`): `MethodArgumentNotValidException` → `400` `{ errors: {campo: mensaje} }`; `EmpleadoDuplicadoException` → `409` `{ errors: {campo: mensaje} }`.
  - `CorsConfig` (`WebMvcConfigurer`) permitiendo `http://localhost:3000` en `/api/**`.

- [ ] **Step 1: Escribir el test del controller (RED)**

`backend/src/test/java/com/example/empleados/controller/EmpleadoControllerTest.java`:
```java
package com.example.empleados.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.empleados.domain.Departamento;
import com.example.empleados.dto.EmpleadoResponse;
import com.example.empleados.exception.EmpleadoDuplicadoException;
import com.example.empleados.service.EmpleadoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmpleadoController.class)
class EmpleadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmpleadoService service;

    private String jsonValido() throws Exception {
        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("nombre", "Ana");
        body.put("apellido", "Diaz");
        body.put("email", "ana.diaz@example.com");
        body.put("dni", "12345678");
        body.put("fechaNacimiento", "1990-05-20");
        body.put("fechaIngreso", "2024-01-15");
        body.put("salario", 150000);
        body.put("departamento", "IT");
        body.put("telefono", "+541112345678");
        return objectMapper.writeValueAsString(body);
    }

    @Test
    void altaValidaDevuelve201YResponse() throws Exception {
        EmpleadoResponse response = new EmpleadoResponse(1L, "Ana", "Diaz",
                "ana.diaz@example.com", "12345678", LocalDate.of(1990, 5, 20),
                LocalDate.of(2024, 1, 15), new BigDecimal("150000"), Departamento.IT,
                "+541112345678", true);
        when(service.altaEmpleado(any())).thenReturn(response);

        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ana.diaz@example.com"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void datosInvalidosDevuelve400ConErroresPorCampo() throws Exception {
        String jsonInvalido = objectMapper.writeValueAsString(java.util.Map.of(
                "nombre", "",
                "apellido", "Diaz",
                "email", "no-es-email",
                "dni", "ABC",
                "fechaIngreso", "2024-01-15",
                "salario", 150000,
                "departamento", "IT"));

        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.dni").exists());
    }

    @Test
    void emailDuplicadoDevuelve409() throws Exception {
        when(service.altaEmpleado(any()))
                .thenThrow(new EmpleadoDuplicadoException("email", "Ya existe un empleado con ese email"));

        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.email").value("Ya existe un empleado con ese email"));
    }
}
```

- [ ] **Step 2: Correr el test y verificar que falla (RED)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba/backend"
./mvnw -q -Dtest=EmpleadoControllerTest test
```
Expected: FALLA la compilación (no existen controller, handler).

- [ ] **Step 3: Crear el `GlobalExceptionHandler`**

`backend/src/main/java/com/example/empleados/exception/GlobalExceptionHandler.java`:
```java
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
```

- [ ] **Step 4: Crear el `EmpleadoController`**

`backend/src/main/java/com/example/empleados/controller/EmpleadoController.java`:
```java
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
```

- [ ] **Step 5: Crear el `CorsConfig`**

`backend/src/main/java/com/example/empleados/config/CorsConfig.java`:
```java
package com.example.empleados.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
```

- [ ] **Step 6: Correr el test y verificar que pasa (GREEN)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba/backend"
./mvnw -q -Dtest=EmpleadoControllerTest test
```
Expected: PASS (3 tests).

- [ ] **Step 7: Commit**

```bash
cd "C:/Tool Room/Development/FormularioPrueba"
git add backend
git commit -m "feat(backend): EmpleadoController + GlobalExceptionHandler + CORS (TDD)"
```

---

### Task 8: Test de integración end-to-end (todas las capas, H2)

**Files:**
- Test: `backend/src/test/java/com/example/empleados/EmpleadoIntegracionTest.java`

**Interfaces:**
- Consumes: toda la app (`@SpringBootTest` + `@AutoConfigureMockMvc`), `EmpleadoRepository` real sobre H2.

- [ ] **Step 1: Escribir el test de integración (RED — falla por lógica de negocio real, no por compilación)**

`backend/src/test/java/com/example/empleados/EmpleadoIntegracionTest.java`:
```java
package com.example.empleados;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.empleados.repository.EmpleadoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EmpleadoIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpleadoRepository repository;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    private String cuerpo(String email, String dni) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", "Ana");
        body.put("apellido", "Diaz");
        body.put("email", email);
        body.put("dni", dni);
        body.put("fechaNacimiento", "1990-05-20");
        body.put("fechaIngreso", "2024-01-15");
        body.put("salario", 150000);
        body.put("departamento", "IT");
        body.put("telefono", "+541112345678");
        return objectMapper.writeValueAsString(body);
    }

    @Test
    void altaValidaPersisteYDevuelve201() throws Exception {
        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("ana.diaz@example.com", "12345678")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.activo").value(true));

        assertThat(repository.existsByEmail("ana.diaz@example.com")).isTrue();
    }

    @Test
    void datosInvalidosDevuelve400() throws Exception {
        String invalido = objectMapper.writeValueAsString(Map.of(
                "nombre", "",
                "apellido", "Diaz",
                "email", "no-es-email",
                "dni", "ABC",
                "fechaIngreso", "2024-01-15",
                "salario", 150000,
                "departamento", "IT"));

        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void emailDuplicadoDevuelve409() throws Exception {
        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("ana.diaz@example.com", "12345678")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("ana.diaz@example.com", "87654321")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.email").value("Ya existe un empleado con ese email"));
    }
}
```

- [ ] **Step 2: Correr toda la suite y verificar que pasa (GREEN)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba/backend"
./mvnw -q test
```
Expected: BUILD SUCCESS, toda la suite en verde (repository, validador, DTO, mapper, service, controller, integración). Salida limpia.

- [ ] **Step 3: Commit**

```bash
cd "C:/Tool Room/Development/FormularioPrueba"
git add backend
git commit -m "test(backend): integracion end-to-end del alta (POST /api/empleados)"
```

---

## Notas de ejecución (usuario)

- **Correr el backend contra PostgreSQL:** crear la base `empleados` en PostgreSQL y setear (si difieren de los defaults) `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`. Luego, desde `backend/`: `./mvnw spring-boot:run`. La API queda en `http://localhost:8080/api/empleados`.
- **Conectar el frontend:** en la raíz, poner `REACT_APP_USE_MOCK=false` en `.env` (con `REACT_APP_API_URL=http://localhost:8080`) y reiniciar `npm start`. El CORS ya permite `http://localhost:3000`.

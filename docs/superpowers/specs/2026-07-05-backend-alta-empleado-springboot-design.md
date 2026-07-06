# Diseño — Backend de Alta de Empleado (Spring Boot)

**Fecha:** 2026-07-05
**Estado:** Aprobado (decisiones tomadas en la conversación de brainstorming)
**Relacionado:** complementa el frontend React (`2026-07-05-formulario-alta-empleado-react-design.md`). Juntos forman la app full-stack.

## Objetivo

Backend REST que imita una app de producción por capas (controller → service → repository)
para el **alta de un empleado**, persistiendo en PostgreSQL. Se construye con **TDD real**
(test primero, red-green-refactor por capa). Expone el contrato que el frontend React ya
consume (`POST /api/empleados`, 201/400/409).

## Alcance

- **Solo alta:** un único endpoint `POST /api/empleados`. Sin GET/PUT/DELETE.
- Validaciones de datos (Bean Validation) + regla de negocio de unicidad (email y dni).
- Los tests los escribe el desarrollador de este proyecto (TDD): NO los escribe el usuario.

## Stack

- **Java 17** (Temurin, ya instalado), **Spring Boot 3.x**, **Maven**.
- **Maven Wrapper (`mvnw`)**: el sistema NO tiene `mvn` instalado. El proyecto se scaffoldea
  desde **Spring Initializr** (`start.spring.io`), que incluye `mvnw`/`mvnw.cmd`. Todos los
  builds y tests corren con `./mvnw`, que descarga Maven automáticamente.
- `JAVA_HOME` está sin setear en el shell → se exporta en cada comando de build:
  `JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot`.
- Dependencias: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`,
  `spring-boot-starter-validation`, `postgresql` (runtime), `spring-boot-starter-test`
  (JUnit 5 + Mockito + AssertJ + MockMvc), `com.h2database:h2` (scope test).

## Ubicación y estructura

El backend vive en el subdirectorio **`backend/`** de `C:\Tool Room\Development\FormularioPrueba`
(el frontend React está en la raíz). Coordenadas Maven: `groupId=com.example`,
`artifactId=empleados`, paquete base `com.example.empleados`.

```
backend/
  mvnw, mvnw.cmd, .mvn/            (wrapper de Maven)
  pom.xml
  src/main/java/com/example/empleados/
    EmpleadosApplication.java
    domain/
      Empleado.java               (entidad JPA)
      Departamento.java           (enum)
    dto/
      EmpleadoRequest.java        (entrada + Bean Validation)
      EmpleadoResponse.java       (salida)
    validation/
      MayorDeEdad.java            (anotación custom)
      MayorDeEdadValidator.java   (ConstraintValidator)
    mapper/
      EmpleadoMapper.java         (request→entidad, entidad→response)
    repository/
      EmpleadoRepository.java     (JpaRepository)
    service/
      EmpleadoService.java        (interfaz)
      EmpleadoServiceImpl.java
    exception/
      EmpleadoDuplicadoException.java
      GlobalExceptionHandler.java (@RestControllerAdvice)
    controller/
      EmpleadoController.java
    config/
      CorsConfig.java             (permite el origen del frontend)
  src/main/resources/
    application.properties        (datasource PostgreSQL vía env)
  src/test/java/com/example/empleados/  (tests por capa)
  src/test/resources/
    application-test.properties   (H2 en memoria)
```

## Modelo de datos

**Entidad `Empleado`** (tabla `empleados`):

| Campo | Tipo Java | Columna |
|---|---|---|
| `id` | `Long` | PK, autogenerado (IDENTITY) |
| `nombre` | `String` | not null |
| `apellido` | `String` | not null |
| `email` | `String` | not null, **unique** |
| `dni` | `String` | not null, **unique** |
| `fechaNacimiento` | `LocalDate` | not null |
| `fechaIngreso` | `LocalDate` | not null |
| `salario` | `BigDecimal` | not null |
| `departamento` | `Departamento` (enum) | not null, `@Enumerated(STRING)` |
| `telefono` | `String` | nullable |
| `activo` | `boolean` | not null, default `true` |

**Enum `Departamento`:** `VENTAS`, `IT`, `RRHH`, `ADMINISTRACION`, `PRODUCCION`.

## Validaciones (Bean Validation en `EmpleadoRequest`)

Reflejan exactamente las del frontend (zod), con los mismos mensajes en español:

| Campo | Anotaciones | Mensaje |
|---|---|---|
| `nombre` | `@NotBlank`, `@Size(min=2,max=50)`, `@Pattern(^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$)` | "El nombre debe tener entre 2 y 50 caracteres" / "El nombre solo puede contener letras" |
| `apellido` | igual que nombre | análogo |
| `email` | `@NotBlank`, `@Email` | "El email es obligatorio" / "El email no tiene un formato válido" |
| `dni` | `@NotBlank`, `@Pattern(^\d{7,8}$)` | "El DNI debe tener 7 u 8 dígitos" |
| `fechaNacimiento` | `@NotNull`, `@MayorDeEdad` (custom, ≥18 años) | "La fecha de nacimiento es obligatoria" / "El empleado debe ser mayor de 18 años" |
| `fechaIngreso` | `@NotNull`, `@PastOrPresent` | "La fecha de ingreso es obligatoria" / "La fecha de ingreso no puede ser futura" |
| `salario` | `@NotNull`, `@Positive`, `@DecimalMax("1000000")` | "El salario es obligatorio" / "El salario debe ser positivo" / "El salario no puede superar 1.000.000" |
| `departamento` | `@NotNull` | "Seleccione un departamento" |
| `telefono` | `@Pattern(^\+?\d{8,15}$)` (nullable/vacío permitido) | "El teléfono debe tener entre 8 y 15 dígitos" |

**Regla de negocio (en el service):** email y dni deben ser únicos. Si ya existen, se lanza
`EmpleadoDuplicadoException` con el campo y el mensaje ("Ya existe un empleado con ese email" /
"Ya existe un empleado con ese DNI").

## Contrato REST

### `POST /api/empleados`

- **201 Created** → cuerpo = `EmpleadoResponse` (todos los campos + `id` + `activo`).
- **400 Bad Request** (validación) → `{ "errors": { "<campo>": "<mensaje>", ... } }`.
- **409 Conflict** (email o dni duplicado) → `{ "errors": { "<campo>": "<mensaje>" } }`.

El formato `{ errors: { campo: mensaje } }` es el que el frontend ya mapea a cada input.

### CORS

`CorsConfig` permite el origen del dev server de React (`http://localhost:3000`) para
`POST`/`OPTIONS` en `/api/**`.

## Manejo de errores

`GlobalExceptionHandler` (`@RestControllerAdvice`):
- `MethodArgumentNotValidException` → 400, arma `{ errors: {campo: defaultMessage} }` a partir de
  los `FieldError`.
- `EmpleadoDuplicadoException` → 409, `{ errors: {campo: mensaje} }`.

## Persistencia

- **Producción/dev:** PostgreSQL vía `application.properties`, con URL/usuario/clave tomados de
  variables de entorno (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
  `SPRING_DATASOURCE_PASSWORD`) y `spring.jpa.hibernate.ddl-auto=update`.
- **Tests:** H2 en memoria (`application-test.properties`, perfil `test`), `ddl-auto=create-drop`.
  Así los tests corren sin PostgreSQL instalado.

## Estrategia TDD (red-green-refactor por capa)

- **Validador custom `@MayorDeEdad`** → test unitario del `ConstraintValidator` (≥18, borde, null).
- **Bean Validation del DTO** → test con `jakarta.validation.Validator` (casos válido/ inválido por campo).
- **Repository** → `@DataJpaTest` + H2: `existsByEmail`, `existsByDni`, restricción unique.
- **Mapper** → test unitario (request→entidad, entidad→response).
- **Service** → test unitario con Mockito (mockea repository): happy path guarda y mapea;
  email/dni duplicado lanza `EmpleadoDuplicadoException`.
- **Controller** → `@WebMvcTest` + MockMvc (mockea service): 201 con JSON de respuesta;
  400 con JSON de errores de validación; 409 con JSON de duplicado.

Cada tarea del plan: test primero (RED) → implementación mínima (GREEN) → refactor → commit.

## Fuera de alcance (YAGNI)

- GET/PUT/DELETE, listado, edición, borrado.
- Autenticación / autorización.
- Migraciones (Flyway/Liquibase): se usa `ddl-auto`.
- Documentación OpenAPI/Swagger.

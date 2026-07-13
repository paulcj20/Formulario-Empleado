# Diseño: campo "Tipo de contrato" con campos condicionales

**Fecha:** 2026-07-12
**Alcance:** full-stack (frontend React + backend Spring Boot)

## Objetivo

Agregar al alta de empleado un campo obligatorio **Tipo de contrato** con dos
valores — `EMPLEADO` y `TERCIARIZADO` — que condiciona qué campos se muestran,
se validan y se persisten.

## Contrato de datos

Discriminante: `tipoContrato` (`EMPLEADO` | `TERCIARIZADO`).

**Campos comunes a ambos tipos:**

| Campo | Regla |
|---|---|
| nombre | 2–50 caracteres, solo letras |
| apellido | 2–50 caracteres, solo letras |
| dni | 7 u 8 dígitos, único |
| email | formato válido, único |
| telefono | opcional, 8–15 dígitos |
| departamento | enum existente |

**Solo EMPLEADO:**

| Campo | Regla |
|---|---|
| fechaNacimiento | obligatoria, mayor de 18 años |
| fechaIngreso | obligatoria, no futura |
| salario | obligatorio, positivo, ≤ 1.000.000 |
| porcentajeAportes (nuevo) | obligatorio, número entre 0 y 100 (ambos inclusive) |

**Solo TERCIARIZADO:**

| Campo | Regla |
|---|---|
| montoFactura (nuevo) | obligatorio, positivo, ≤ 1.000.000 |
| fechaServicio (nuevo) | obligatoria, no futura |

El payload `POST /api/empleados` incluye **solo** los campos del tipo elegido:
los del otro tipo van ausentes (no `null` ni string vacío). El backend rechaza
con 400 un request que traiga campos del tipo contrario.

## Frontend

- **`src/schemas/empleadoSchema.js`:** se reestructura como
  `z.discriminatedUnion('tipoContrato', [schemaEmpleado, schemaTerciarizado])`,
  compartiendo los campos comunes mediante un objeto base (spread). Se exporta
  `TIPOS_CONTRATO = ['EMPLEADO', 'TERCIARIZADO']`.
- **`src/components/EmpleadoForm.js`:** `watch('tipoContrato')` decide el
  renderizado condicional. Al cambiar de tipo se limpian los valores y errores
  de los campos del tipo anterior (`unregister`), para que no viajen datos
  huérfanos.
- **Select "Tipo de contrato":** primer campo de la sección "Datos laborales".
  Estado inicial "Seleccione..." (sin preselección); es obligatorio. Hasta que
  se elija un tipo, solo se ven los campos comunes.
- **`DatosPersonalesStep`:** oculta "Fecha de nacimiento" salvo que el tipo sea
  EMPLEADO (queda nombre, apellido y DNI para terciarizado o sin selección).
- **`DatosLaboralesStep`:** EMPLEADO → fechaIngreso, salario,
  porcentajeAportes; TERCIARIZADO → montoFactura, fechaServicio.

## Backend

- **Enum nuevo** `TipoContrato { EMPLEADO, TERCIARIZADO }` en `domain`.
- **`EmpleadoRequest`:** agrega `tipoContrato` (`@NotNull`),
  `porcentajeAportes`, `montoFactura`, `fechaServicio`. `fechaNacimiento`,
  `fechaIngreso` y `salario` dejan de ser obligatorios a nivel campo.
- **Validador class-level `@ConsistenteConTipoContrato`** (mismo patrón que
  `@MayorDeEdad`):
  - EMPLEADO: exige fechaNacimiento (mayor de 18), fechaIngreso (no futura),
    salario (positivo ≤ 1.000.000) y porcentajeAportes (0–100); rechaza
    montoFactura y fechaServicio si vienen presentes.
  - TERCIARIZADO: exige montoFactura (positivo ≤ 1.000.000) y fechaServicio
    (no futura); rechaza fechaNacimiento, fechaIngreso, salario y
    porcentajeAportes si vienen presentes.
  - Los errores se agregan por campo (`ConstraintValidatorContext` con
    `addPropertyNode`) para que `GlobalExceptionHandler` siga devolviendo
    `errors: { campo: mensaje }`.
- **Entidad `Empleado`:** una sola tabla; `tipoContrato` con
  `@Enumerated(STRING)` no nulo; `fechaNacimiento`, `fechaIngreso`, `salario`,
  `porcentajeAportes`, `montoFactura`, `fechaServicio` nullables.
- **`EmpleadoMapper` / `EmpleadoResponse`:** copian y exponen los campos
  nuevos (nulos cuando no aplican).
- **Unicidad email/dni:** sin cambios, aplica a ambos tipos.

## Testing (TDD)

Sin tests E2E de Playwright (decisión explícita del usuario).

- **Frontend — schema:** la unión discriminada acepta payloads válidos de cada
  tipo y rechaza faltantes/inválidos por tipo.
- **Frontend — interacción (Testing Library):**
  - Sin tipo elegido solo se ven los campos comunes.
  - EMPLEADO muestra fechaNacimiento, fechaIngreso, salario y
    porcentajeAportes.
  - TERCIARIZADO oculta esos campos y muestra montoFactura y fechaServicio.
  - Cambiar de tipo limpia los valores y errores del tipo anterior.
  - El submit envía solo los campos del tipo elegido.
- **Backend:** tests unitarios del validador (válidos e inválidos por tipo),
  actualización de `EmpleadoRequestValidationTest`, e integración end-to-end:
  alta de TERCIARIZADO → 201; terciarizado con salario presente → 400.

## Fuera de alcance

- Tests E2E con Playwright.
- Migraciones de datos existentes (la tabla se regenera por JPA en dev; los
  registros previos se consideran EMPLEADO conceptualmente pero no se migran).
- Edición de empleados (el sistema solo tiene alta).

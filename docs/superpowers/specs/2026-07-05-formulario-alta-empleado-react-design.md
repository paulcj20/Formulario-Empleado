# Diseño — Formulario de Alta de Empleado (React)

**Fecha:** 2026-07-05
**Estado:** Aprobado (pendiente revisión final del spec por el usuario)

## Objetivo

Construir el **frontend** de una aplicación de ejemplo que imita el formato de una app de
producción, enfocado en un **formulario de alta de empleado**. El propósito es servir de base
para que el usuario **escriba sus propios tests manualmente** más adelante. El código se
estructura por capas y se mantiene desacoplado para facilitar esos tests.

**Alcance de este proyecto:** SOLO el frontend React. El backend (con PostgreSQL) lo construye
el usuario por separado; este frontend queda listo para conectarse a él por REST.

## Stack y dependencias

- **React 18** — framework de UI
- **JavaScript** (NO TypeScript)
- **Create React App** (`react-scripts`) — scaffolding y dev server (NO Vite)
- **React Hook Form** — manejo de estado del formulario
- **zod** + **@hookform/resolvers** — validaciones del formulario (cliente)
- **PrimeReact** — componente **Stepper** (formulario por pasos)
- **react-bootstrap** + **bootstrap** — inputs y layout del formulario
- Cliente HTTP: `fetch` nativo (sin dependencia extra) — o `axios` si se prefiere

> Nota CRA: las variables de entorno deben prefijarse con `REACT_APP_` (no `VITE_`).

## Arquitectura por capas

Se imita la separación de una app de producción, reflejada en el frontend:

```
Componente (UI / Stepper)  →  service (orquestación)  →  api / "repository" (HTTP REST)
   RHF + zod                     arma el request            fetch → backend del usuario
```

- **Componentes:** solo UI y captura de datos. No conocen HTTP.
- **service (`empleadoService`):** orquesta el alta, sin dependencias de UI. Punto ideal para
  tests unitarios.
- **api (`empleadoApi`):** capa tipo "repository". Hace el `POST` al backend. Detrás de una
  interfaz simple para poder intercambiar implementación real ↔ mock.

### Estructura de carpetas

La app React vive en la raíz del proyecto `C:\Tool Room\Development\FormularioPrueba`.
(El usuario puede ubicar su backend en otra carpeta.)

```
public/
  index.html
src/
  index.js
  App.js
  config.js                      # URL base del backend + bandera USE_MOCK
  components/
    EmpleadoStepper.js           # el wizard (orquesta los pasos)
    steps/
      DatosPersonalesStep.js     # nombre, apellido, dni, fechaNacimiento
      ContactoStep.js            # email, telefono
      DatosLaboralesStep.js      # departamento, fechaIngreso, salario
      ConfirmacionStep.js        # resumen + botón Guardar
    fields/                      # inputs reutilizables (react-bootstrap + RHF)
  schemas/
    empleadoSchema.js            # esquemas zod (por paso + total) + enum Departamento
  services/
    empleadoService.js           # capa service
  api/
    empleadoApi.js               # capa "repository": HTTP real
    empleadoApiMock.js           # adaptador mock (in-memory)
  types/
    empleado.js                  # shape/constantes del empleado (JSDoc)
package.json
.env                             # REACT_APP_API_URL, REACT_APP_USE_MOCK
```

## El formulario (Stepper, 4 pasos)

Un único Stepper de PrimeReact. La validación es **por paso**: no se avanza si el paso actual
tiene errores. En el último paso se muestra un resumen y se envía.

1. **Datos personales** — nombre, apellido, dni, fechaNacimiento
2. **Contacto** — email, telefono
3. **Datos laborales** — departamento, fechaIngreso, salario
4. **Confirmación** — resumen de todo lo cargado + botón "Guardar"

Inputs con **react-bootstrap**, estado con **React Hook Form**, validación con **zod** vía
`@hookform/resolvers`.

## Modelo de datos y validaciones (zod)

| Campo | Tipo | Validaciones |
|---|---|---|
| `nombre` | string | Obligatorio, 2–50 caracteres, solo letras (incl. acentos y espacios) |
| `apellido` | string | Obligatorio, 2–50 caracteres, solo letras |
| `email` | string | Obligatorio, formato email válido |
| `dni` | string | Obligatorio, 7 u 8 dígitos numéricos |
| `fechaNacimiento` | string (fecha) | Obligatorio, debe corresponder a **mayor de 18 años** |
| `fechaIngreso` | string (fecha) | Obligatorio, **no puede ser futura** |
| `salario` | number | Obligatorio, positivo, rango 1–1.000.000 |
| `departamento` | enum | Obligatorio: `VENTAS`, `IT`, `RRHH`, `ADMINISTRACION`, `PRODUCCION` |
| `telefono` | string | Opcional; si viene, 8–15 dígitos, permite `+` inicial |

Regex de referencia:
- nombre/apellido: `/^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,50}$/`
- dni: `/^\d{7,8}$/`
- telefono: `/^\+?\d{8,15}$/`

Validaciones custom (zod `.refine`):
- `fechaNacimiento`: edad calculada `>= 18` años a la fecha actual.
- `fechaIngreso`: fecha `<=` hoy.

El schema se organiza para poder validar cada paso por separado (subconjuntos de campos) y el
total antes de enviar.

## Conexión con el backend (contrato REST)

El backend lo construye el usuario. El frontend asume este contrato:

### `POST /api/empleados`

Request body:
```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan.perez@example.com",
  "dni": "12345678",
  "fechaNacimiento": "1990-05-20",
  "fechaIngreso": "2024-01-15",
  "salario": 150000.00,
  "departamento": "IT",
  "telefono": "+541112345678"
}
```

Respuestas:

- **201 Created** — empleado creado:
  ```json
  { "id": 1, "nombre": "Juan", "apellido": "Pérez", "email": "...", "dni": "...",
    "fechaNacimiento": "1990-05-20", "fechaIngreso": "2024-01-15",
    "salario": 150000.00, "departamento": "IT", "telefono": "...", "activo": true }
  ```
- **400 Bad Request** — errores de validación por campo:
  ```json
  { "errors": { "email": "El email no tiene un formato válido",
                "dni": "El DNI debe tener 7 u 8 dígitos" } }
  ```
- **409 Conflict** — email o dni duplicado:
  ```json
  { "errors": { "email": "Ya existe un empleado con ese email" } }
  ```

El frontend mapea las claves de `errors` a los campos del formulario usando `setError` de
React Hook Form, mostrando el mensaje del backend junto al campo correspondiente.

### Configuración

- `config.js` lee:
  - `REACT_APP_API_URL` — URL base del backend (ej. `http://localhost:8080`).
  - `REACT_APP_USE_MOCK` — bandera (`"true"`/`"false"`) para elegir mock vs. real.
- `empleadoService` recibe la implementación de `api` según la bandera, así el resto del código
  no cambia al conectar el backend real.

### Adaptador mock

`empleadoApiMock.js` implementa la misma interfaz que `empleadoApi.js` pero guarda en memoria y
simula respuestas (incluye un caso de duplicado para poder ejercitar el flujo 409). Permite
correr y probar el formulario **sin backend**. Se apaga cambiando `REACT_APP_USE_MOCK=false`.

## Manejo de errores en la UI

- **Errores de validación de campo (zod):** se muestran junto a cada input, bloquean el avance
  del paso.
- **Errores del backend (400/409):** se mapean por campo (`setError`) o, si son generales, se
  muestran en un banner en el paso de Confirmación.
- **Errores de red / 5xx:** banner genérico "No se pudo guardar, intentá de nuevo".
- **Éxito (201):** mensaje de éxito / pantalla de confirmación con el id devuelto.

## Tests

Los tests los escribe el usuario más adelante. Este proyecto los facilita:

- **schemas zod** son funciones puras → fáciles de testear (válido/ inválido por campo).
- **`empleadoService`** no depende de la UI → testeable con un mock de `api`.
- **componentes** desacoplados → testeables con React Testing Library (incluido por CRA).

Se deja la configuración de test que trae CRA (Jest + React Testing Library) lista, **sin
escribir tests** (salvo que el usuario pida un par de ejemplos).

## Fuera de alcance (YAGNI)

- Backend real, base de datos, persistencia (los hace el usuario).
- CRUD completo: listado, edición, borrado. Solo **alta**.
- Autenticación / autorización.
- TypeScript, Vite.
- Internacionalización (la UI va en español).
```
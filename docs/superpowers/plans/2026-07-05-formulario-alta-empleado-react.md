# Formulario de Alta de Empleado (React) — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Construir una SPA React de una sola pantalla para el alta de un empleado, con un formulario por pasos (Stepper), validaciones de cliente y una capa de servicio/API lista para conectarse a un backend REST.

**Architecture:** App React (Create React App, JavaScript) estructurada por capas: componentes de UI (Stepper + pasos) → `empleadoService` (orquestación) → `empleadoApi` (HTTP REST) con un adaptador mock conmutable para correr sin backend. El estado del formulario lo maneja React Hook Form y las validaciones se definen con esquemas zod.

**Tech Stack:** React 18, JavaScript, Create React App (`react-scripts`), React Hook Form, zod, `@hookform/resolvers`, PrimeReact (Stepper), react-bootstrap + bootstrap.

## Global Constraints

- **Lenguaje:** JavaScript únicamente. NO TypeScript, NO Vite.
- **Scaffolding:** Create React App (`react-scripts`).
- **Los tests los escribe el usuario.** Este plan NO escribe tests; deja el código desacoplado y verifica cada tarea compilando (`npm run build`) o corriendo la app (`npm start`).
- **Idioma de la UI:** español.
- **Ubicación:** la app React vive en la raíz `C:\Tool Room\Development\FormularioPrueba` (junto a `docs/` y el repo git ya inicializado).
- **Alcance:** solo el alta (un formulario). Sin listado, edición ni borrado.
- **Enum Departamento (valores exactos):** `VENTAS`, `IT`, `RRHH`, `ADMINISTRACION`, `PRODUCCION`.
- **Contrato REST del backend (lo construye el usuario):** `POST /api/empleados` → 201 (creado), 400 `{ "errors": { campo: mensaje } }` (validación), 409 `{ "errors": {...} }` (email/dni duplicado).

---

### Task 1: Scaffold de la app React + dependencias + estilos base

**Files:**
- Create: `package.json`, `public/`, `src/index.js` (generados por CRA y ajustados)
- Modify: `src/index.js`
- Create: `src/App.js` (placeholder), `.env`

**Interfaces:**
- Produces: proyecto CRA ejecutable; `PrimeReactProvider` y hojas de estilo (bootstrap, tema PrimeReact, primeicons) cargadas globalmente en `src/index.js`; componente `App` exportado por defecto desde `src/App.js`.

- [ ] **Step 1: Scaffold de CRA en una carpeta temporal (la raíz ya tiene `docs/`, que a CRA le da conflicto) y mover el contenido a la raíz**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba"
npx --yes create-react-app@5.0.1 .cra-tmp
mv .cra-tmp/package.json .cra-tmp/package-lock.json .cra-tmp/public .cra-tmp/src .cra-tmp/.gitignore .
rm -rf .cra-tmp
```
Expected: quedan en la raíz `package.json`, `package-lock.json`, `public/`, `src/`, `.gitignore`.

- [ ] **Step 2: Instalar dependencias del proyecto**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba"
npm install react-hook-form@^7 zod@^3 @hookform/resolvers@^3 primereact@^10 primeicons@^7 react-bootstrap@^2 bootstrap@^5
```
Expected: se agregan las dependencias a `package.json` sin errores de peer-deps que rompan la instalación.

- [ ] **Step 3: Borrar archivos de ejemplo que no usamos**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba"
rm -f src/App.css src/App.test.js src/logo.svg src/reportWebVitals.js src/setupTests.js
```
Expected: esos archivos ya no existen.

- [ ] **Step 4: Reescribir `src/index.js` con los estilos globales y el provider de PrimeReact**

```javascript
import React from 'react';
import ReactDOM from 'react-dom/client';
import { PrimeReactProvider } from 'primereact/api';

import 'bootstrap/dist/css/bootstrap.min.css';
import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';

import App from './App';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <PrimeReactProvider>
      <App />
    </PrimeReactProvider>
  </React.StrictMode>
);
```

- [ ] **Step 5: Crear `src/App.js` como placeholder**

```javascript
import React from 'react';
import { Container } from 'react-bootstrap';

export default function App() {
  return (
    <Container className="py-4" style={{ maxWidth: 720 }}>
      <h1>Alta de empleado</h1>
      <p>La aplicación está en construcción.</p>
    </Container>
  );
}
```

- [ ] **Step 6: Crear `.env` con la configuración del backend y el mock**

```bash
REACT_APP_API_URL=http://localhost:8080
REACT_APP_USE_MOCK=true
```

- [ ] **Step 7: Verificar que la app arranca**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba"
npm start
```
Expected: compila sin errores y en `http://localhost:3000` se ve el título "Alta de empleado". Cerrar con Ctrl+C.

> Si `npm start` falla con un error de OpenSSL (`ERR_OSSL_EVP_UNSUPPORTED`), correr antes: `export NODE_OPTIONS=--openssl-legacy-provider`.

- [ ] **Step 8: Commit**

```bash
cd "C:/Tool Room/Development/FormularioPrueba"
git add -A
git commit -m "chore: scaffold app React (CRA) con dependencias y estilos base"
```

---

### Task 2: Configuración (`config.js`)

**Files:**
- Create: `src/config.js`

**Interfaces:**
- Produces: `API_URL` (string) y `USE_MOCK` (boolean) exportados desde `src/config.js`.

- [ ] **Step 1: Crear `src/config.js`**

```javascript
// Lee la configuración desde variables de entorno de CRA (prefijo REACT_APP_).
export const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

// Bandera para usar el adaptador mock (true) o el backend real (false).
export const USE_MOCK = (process.env.REACT_APP_USE_MOCK || 'true') === 'true';
```

- [ ] **Step 2: Verificar que compila**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba"
npm run build
```
Expected: `Compiled successfully.`

- [ ] **Step 3: Commit**

```bash
git add src/config.js
git commit -m "feat: capa de configuracion (API_URL, USE_MOCK)"
```

---

### Task 3: Esquema de validación zod (`empleadoSchema.js`)

**Files:**
- Create: `src/schemas/empleadoSchema.js`

**Interfaces:**
- Produces:
  - `DEPARTAMENTOS` — array de strings con los valores del enum.
  - `empleadoSchema` — objeto zod que valida todos los campos.
  - `camposPorPaso` — array de arrays con los nombres de campo de cada paso, en orden: `[['nombre','apellido','dni','fechaNacimiento'], ['email','telefono'], ['departamento','fechaIngreso','salario'], []]`.

- [ ] **Step 1: Crear `src/schemas/empleadoSchema.js`**

```javascript
import { z } from 'zod';

export const DEPARTAMENTOS = ['VENTAS', 'IT', 'RRHH', 'ADMINISTRACION', 'PRODUCCION'];

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

export const empleadoSchema = z.object({
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
  fechaNacimiento: z
    .string()
    .min(1, 'La fecha de nacimiento es obligatoria')
    .refine(esMayorDeEdad, 'El empleado debe ser mayor de 18 años'),
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
  fechaIngreso: z
    .string()
    .min(1, 'La fecha de ingreso es obligatoria')
    .refine(noEsFutura, 'La fecha de ingreso no puede ser futura'),
  salario: z.preprocess(
    (v) => (v === '' || v === null || v === undefined ? undefined : Number(v)),
    z
      .number({
        required_error: 'El salario es obligatorio',
        invalid_type_error: 'El salario debe ser un número',
      })
      .positive('El salario debe ser positivo')
      .max(1000000, 'El salario no puede superar 1.000.000')
  ),
});

// Campos que se validan en cada paso del Stepper (mismo orden que los pasos).
export const camposPorPaso = [
  ['nombre', 'apellido', 'dni', 'fechaNacimiento'],
  ['email', 'telefono'],
  ['departamento', 'fechaIngreso', 'salario'],
  [],
];
```

- [ ] **Step 2: Verificar que compila**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba"
npm run build
```
Expected: `Compiled successfully.`

- [ ] **Step 3: Commit**

```bash
git add src/schemas/empleadoSchema.js
git commit -m "feat: esquema de validacion zod del empleado"
```

---

### Task 4: Capa API — adaptador real y mock

**Files:**
- Create: `src/api/empleadoApi.js`
- Create: `src/api/empleadoApiMock.js`

**Interfaces:**
- Consumes: `API_URL` de `src/config.js`.
- Produces: en ambos módulos, `crearEmpleado(empleado)` async que devuelve:
  - éxito: `{ ok: true, empleado }` (empleado con `id` y `activo`)
  - validación/duplicado: `{ ok: false, status: 400|409, errors: { campo: mensaje } }`
  - error de red/5xx (solo el real): lanza `Error`.

- [ ] **Step 1: Crear `src/api/empleadoApi.js` (backend real)**

```javascript
import { API_URL } from '../config';

// Envía el alta al backend REST. Interpreta el contrato:
// 201 -> creado; 400/409 -> errores por campo; otro -> excepción.
export async function crearEmpleado(empleado) {
  const resp = await fetch(`${API_URL}/api/empleados`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(empleado),
  });

  const data = await resp.json().catch(() => ({}));

  if (resp.status === 201) {
    return { ok: true, empleado: data };
  }
  if (resp.status === 400 || resp.status === 409) {
    return { ok: false, status: resp.status, errors: data.errors || {} };
  }
  throw new Error(`Error inesperado del servidor (${resp.status})`);
}
```

- [ ] **Step 2: Crear `src/api/empleadoApiMock.js` (sin backend, en memoria)**

```javascript
// Adaptador mock: guarda en memoria y simula el contrato del backend,
// incluyendo el caso 409 por email/dni duplicado.
const empleados = [];
let secuencia = 1;

export async function crearEmpleado(empleado) {
  await new Promise((resolve) => setTimeout(resolve, 300));

  const duplicado = empleados.find(
    (e) => e.email === empleado.email || e.dni === empleado.dni
  );
  if (duplicado) {
    const errors = {};
    if (duplicado.email === empleado.email) {
      errors.email = 'Ya existe un empleado con ese email';
    }
    if (duplicado.dni === empleado.dni) {
      errors.dni = 'Ya existe un empleado con ese DNI';
    }
    return { ok: false, status: 409, errors };
  }

  const creado = { id: secuencia, activo: true, ...empleado };
  secuencia += 1;
  empleados.push(creado);
  return { ok: true, empleado: creado };
}
```

- [ ] **Step 3: Verificar que compila**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba"
npm run build
```
Expected: `Compiled successfully.`

- [ ] **Step 4: Commit**

```bash
git add src/api/empleadoApi.js src/api/empleadoApiMock.js
git commit -m "feat: capa api con adaptador real y mock conmutable"
```

---

### Task 5: Capa de servicio (`empleadoService.js`)

**Files:**
- Create: `src/services/empleadoService.js`

**Interfaces:**
- Consumes: `crearEmpleado` de `empleadoApi`/`empleadoApiMock`; `USE_MOCK` de `config`.
- Produces: `altaEmpleado(datosFormulario)` async → devuelve el mismo shape que `crearEmpleado` (`{ ok, empleado }` o `{ ok:false, status, errors }`). Normaliza los datos del formulario antes de enviarlos (telefono vacío → `null`, salario → número).

- [ ] **Step 1: Crear `src/services/empleadoService.js`**

```javascript
import * as apiReal from '../api/empleadoApi';
import * as apiMock from '../api/empleadoApiMock';
import { USE_MOCK } from '../config';

const api = USE_MOCK ? apiMock : apiReal;

// Orquesta el alta: normaliza los datos crudos del formulario y delega en la capa api.
export async function altaEmpleado(datosFormulario) {
  const telefono = (datosFormulario.telefono || '').trim();
  const empleado = {
    nombre: datosFormulario.nombre.trim(),
    apellido: datosFormulario.apellido.trim(),
    email: datosFormulario.email.trim(),
    dni: datosFormulario.dni.trim(),
    fechaNacimiento: datosFormulario.fechaNacimiento,
    fechaIngreso: datosFormulario.fechaIngreso,
    salario: Number(datosFormulario.salario),
    departamento: datosFormulario.departamento,
    telefono: telefono ? telefono : null,
  };
  return api.crearEmpleado(empleado);
}
```

- [ ] **Step 2: Verificar que compila**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba"
npm run build
```
Expected: `Compiled successfully.`

- [ ] **Step 3: Commit**

```bash
git add src/services/empleadoService.js
git commit -m "feat: capa service que orquesta el alta de empleado"
```

---

### Task 6: Inputs reutilizables (`TextField`, `SelectField`)

**Files:**
- Create: `src/components/fields/TextField.js`
- Create: `src/components/fields/SelectField.js`

**Interfaces:**
- Produces:
  - `TextField({ label, name, register, error, type })` — input de react-bootstrap conectado a RHF vía `register`.
  - `SelectField({ label, name, register, error, options, placeholder })` — select de react-bootstrap conectado a RHF.

- [ ] **Step 1: Crear `src/components/fields/TextField.js`**

```javascript
import React from 'react';
import { Form } from 'react-bootstrap';

export default function TextField({ label, name, register, error, type = 'text', ...rest }) {
  return (
    <Form.Group className="mb-3" controlId={name}>
      <Form.Label>{label}</Form.Label>
      <Form.Control type={type} isInvalid={!!error} {...register(name)} {...rest} />
      <Form.Control.Feedback type="invalid">{error?.message}</Form.Control.Feedback>
    </Form.Group>
  );
}
```

- [ ] **Step 2: Crear `src/components/fields/SelectField.js`**

```javascript
import React from 'react';
import { Form } from 'react-bootstrap';

export default function SelectField({
  label,
  name,
  register,
  error,
  options,
  placeholder = 'Seleccione...',
}) {
  return (
    <Form.Group className="mb-3" controlId={name}>
      <Form.Label>{label}</Form.Label>
      <Form.Select isInvalid={!!error} {...register(name)}>
        <option value="">{placeholder}</option>
        {options.map((opt) => (
          <option key={opt} value={opt}>
            {opt}
          </option>
        ))}
      </Form.Select>
      <Form.Control.Feedback type="invalid">{error?.message}</Form.Control.Feedback>
    </Form.Group>
  );
}
```

- [ ] **Step 3: Verificar que compila**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba"
npm run build
```
Expected: `Compiled successfully.`

- [ ] **Step 4: Commit**

```bash
git add src/components/fields
git commit -m "feat: inputs reutilizables TextField y SelectField"
```

---

### Task 7: Componentes de cada paso

**Files:**
- Create: `src/components/steps/DatosPersonalesStep.js`
- Create: `src/components/steps/ContactoStep.js`
- Create: `src/components/steps/DatosLaboralesStep.js`
- Create: `src/components/steps/ConfirmacionStep.js`

**Interfaces:**
- Consumes: `useFormContext` de react-hook-form (register, errors, getValues); `TextField`, `SelectField`; `DEPARTAMENTOS` del schema.
- Produces: cuatro componentes sin props que renderizan los campos de su paso.

- [ ] **Step 1: Crear `src/components/steps/DatosPersonalesStep.js`**

```javascript
import React from 'react';
import { useFormContext } from 'react-hook-form';
import TextField from '../fields/TextField';

export default function DatosPersonalesStep() {
  const {
    register,
    formState: { errors },
  } = useFormContext();

  return (
    <div>
      <TextField label="Nombre" name="nombre" register={register} error={errors.nombre} />
      <TextField label="Apellido" name="apellido" register={register} error={errors.apellido} />
      <TextField label="DNI" name="dni" register={register} error={errors.dni} />
      <TextField
        label="Fecha de nacimiento"
        name="fechaNacimiento"
        type="date"
        register={register}
        error={errors.fechaNacimiento}
      />
    </div>
  );
}
```

- [ ] **Step 2: Crear `src/components/steps/ContactoStep.js`**

```javascript
import React from 'react';
import { useFormContext } from 'react-hook-form';
import TextField from '../fields/TextField';

export default function ContactoStep() {
  const {
    register,
    formState: { errors },
  } = useFormContext();

  return (
    <div>
      <TextField label="Email" name="email" type="email" register={register} error={errors.email} />
      <TextField
        label="Teléfono (opcional)"
        name="telefono"
        register={register}
        error={errors.telefono}
      />
    </div>
  );
}
```

- [ ] **Step 3: Crear `src/components/steps/DatosLaboralesStep.js`**

```javascript
import React from 'react';
import { useFormContext } from 'react-hook-form';
import TextField from '../fields/TextField';
import SelectField from '../fields/SelectField';
import { DEPARTAMENTOS } from '../../schemas/empleadoSchema';

export default function DatosLaboralesStep() {
  const {
    register,
    formState: { errors },
  } = useFormContext();

  return (
    <div>
      <SelectField
        label="Departamento"
        name="departamento"
        register={register}
        error={errors.departamento}
        options={DEPARTAMENTOS}
      />
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
    </div>
  );
}
```

- [ ] **Step 4: Crear `src/components/steps/ConfirmacionStep.js`**

```javascript
import React from 'react';
import { useFormContext } from 'react-hook-form';
import { ListGroup } from 'react-bootstrap';

export default function ConfirmacionStep() {
  const { getValues } = useFormContext();
  const v = getValues();

  const filas = [
    ['Nombre', v.nombre],
    ['Apellido', v.apellido],
    ['DNI', v.dni],
    ['Fecha de nacimiento', v.fechaNacimiento],
    ['Email', v.email],
    ['Teléfono', v.telefono ? v.telefono : '—'],
    ['Departamento', v.departamento],
    ['Fecha de ingreso', v.fechaIngreso],
    ['Salario', v.salario],
  ];

  return (
    <ListGroup className="mb-2">
      {filas.map(([etiqueta, valor]) => (
        <ListGroup.Item key={etiqueta} className="d-flex justify-content-between">
          <strong>{etiqueta}</strong>
          <span>{valor}</span>
        </ListGroup.Item>
      ))}
    </ListGroup>
  );
}
```

- [ ] **Step 5: Verificar que compila**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba"
npm run build
```
Expected: `Compiled successfully.`

- [ ] **Step 6: Commit**

```bash
git add src/components/steps
git commit -m "feat: componentes de los 4 pasos del formulario"
```

---

### Task 8: Orquestador `EmpleadoStepper`

**Files:**
- Create: `src/components/EmpleadoStepper.js`

**Interfaces:**
- Consumes: `useForm`, `FormProvider` (RHF); `zodResolver`; `Stepper`, `StepperPanel`, `Button` (PrimeReact); `empleadoSchema`, `camposPorPaso`; `altaEmpleado`; los 4 componentes de paso.
- Produces: `EmpleadoStepper({ onExito })` — renderiza el wizard completo. Valida cada paso antes de avanzar y, al confirmar, llama a `altaEmpleado`; en éxito invoca `onExito(empleado)`; ante errores 400/409 los mapea a los campos con `setError`.

- [ ] **Step 1: Crear `src/components/EmpleadoStepper.js`**

```javascript
import React, { useRef, useState } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Stepper } from 'primereact/stepper';
import { StepperPanel } from 'primereact/stepperpanel';
import { Button } from 'primereact/button';
import { Alert } from 'react-bootstrap';

import { empleadoSchema, camposPorPaso } from '../schemas/empleadoSchema';
import { altaEmpleado } from '../services/empleadoService';
import DatosPersonalesStep from './steps/DatosPersonalesStep';
import ContactoStep from './steps/ContactoStep';
import DatosLaboralesStep from './steps/DatosLaboralesStep';
import ConfirmacionStep from './steps/ConfirmacionStep';

const valoresIniciales = {
  nombre: '',
  apellido: '',
  dni: '',
  fechaNacimiento: '',
  email: '',
  telefono: '',
  departamento: '',
  fechaIngreso: '',
  salario: '',
};

export default function EmpleadoStepper({ onExito }) {
  const stepperRef = useRef(null);
  const [pasoActual, setPasoActual] = useState(0);
  const [enviando, setEnviando] = useState(false);
  const [errorGeneral, setErrorGeneral] = useState(null);

  const methods = useForm({
    resolver: zodResolver(empleadoSchema),
    mode: 'onTouched',
    defaultValues: valoresIniciales,
  });
  const { handleSubmit, trigger, setError } = methods;

  const avanzar = async () => {
    const valido = await trigger(camposPorPaso[pasoActual]);
    if (valido) {
      stepperRef.current.nextCallback();
      setPasoActual((p) => p + 1);
    }
  };

  const retroceder = () => {
    stepperRef.current.prevCallback();
    setPasoActual((p) => p - 1);
  };

  const onSubmit = async (datos) => {
    setEnviando(true);
    setErrorGeneral(null);
    try {
      const resultado = await altaEmpleado(datos);
      if (resultado.ok) {
        onExito(resultado.empleado);
        return;
      }
      Object.entries(resultado.errors).forEach(([campo, mensaje]) => {
        setError(campo, { type: 'server', message: mensaje });
      });
      setErrorGeneral('Corregí los campos marcados e intentá nuevamente.');
    } catch (e) {
      setErrorGeneral(e.message || 'No se pudo guardar, intentá de nuevo.');
    } finally {
      setEnviando(false);
    }
  };

  return (
    <FormProvider {...methods}>
      <form onSubmit={handleSubmit(onSubmit)}>
        {errorGeneral && <Alert variant="danger">{errorGeneral}</Alert>}

        <Stepper ref={stepperRef} linear>
          <StepperPanel header="Datos personales">
            <DatosPersonalesStep />
            <div className="d-flex justify-content-end mt-3">
              <Button type="button" label="Siguiente" onClick={avanzar} />
            </div>
          </StepperPanel>

          <StepperPanel header="Contacto">
            <ContactoStep />
            <div className="d-flex justify-content-between mt-3">
              <Button type="button" label="Atrás" severity="secondary" onClick={retroceder} />
              <Button type="button" label="Siguiente" onClick={avanzar} />
            </div>
          </StepperPanel>

          <StepperPanel header="Datos laborales">
            <DatosLaboralesStep />
            <div className="d-flex justify-content-between mt-3">
              <Button type="button" label="Atrás" severity="secondary" onClick={retroceder} />
              <Button type="button" label="Siguiente" onClick={avanzar} />
            </div>
          </StepperPanel>

          <StepperPanel header="Confirmación">
            <ConfirmacionStep />
            <div className="d-flex justify-content-between mt-3">
              <Button type="button" label="Atrás" severity="secondary" onClick={retroceder} />
              <Button type="submit" label="Guardar" loading={enviando} />
            </div>
          </StepperPanel>
        </Stepper>
      </form>
    </FormProvider>
  );
}
```

- [ ] **Step 2: Verificar que compila**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba"
npm run build
```
Expected: `Compiled successfully.`

- [ ] **Step 3: Commit**

```bash
git add src/components/EmpleadoStepper.js
git commit -m "feat: orquestador EmpleadoStepper con validacion por paso"
```

---

### Task 9: Integración en `App.js` y pantalla de éxito

**Files:**
- Modify: `src/App.js`

**Interfaces:**
- Consumes: `EmpleadoStepper`.
- Produces: pantalla única que muestra el Stepper y, tras un alta exitosa, un mensaje de éxito con el `id` y un botón para cargar otro empleado.

- [ ] **Step 1: Reescribir `src/App.js`**

```javascript
import React, { useState } from 'react';
import { Container, Card, Alert, Button } from 'react-bootstrap';
import EmpleadoStepper from './components/EmpleadoStepper';

export default function App() {
  const [creado, setCreado] = useState(null);

  return (
    <Container className="py-4" style={{ maxWidth: 720 }}>
      <h1 className="mb-4">Alta de empleado</h1>

      {creado ? (
        <Card body>
          <Alert variant="success" className="mb-3">
            Empleado creado con éxito (ID {creado.id}).
          </Alert>
          <Button variant="primary" onClick={() => setCreado(null)}>
            Cargar otro empleado
          </Button>
        </Card>
      ) : (
        <Card body>
          <EmpleadoStepper onExito={setCreado} />
        </Card>
      )}
    </Container>
  );
}
```

- [ ] **Step 2: Verificación funcional completa (con mock)**

Run:
```bash
cd "C:/Tool Room/Development/FormularioPrueba"
npm start
```
En `http://localhost:3000` verificar el camino feliz y las validaciones:
- Intentar avanzar el paso 1 vacío → aparecen errores y NO avanza.
- Cargar datos válidos (ej. nombre "Ana", apellido "Diaz", dni "12345678", nacimiento con +18 años) → avanza.
- En Datos laborales, elegir departamento, fecha de ingreso no futura, salario 150000.
- En Confirmación, revisar el resumen y "Guardar" → aparece "Empleado creado con éxito (ID 1)".
- "Cargar otro empleado" con el mismo email/dni → al guardar muestra el error 409 mapeado al campo.

Cerrar con Ctrl+C.

- [ ] **Step 3: Commit**

```bash
git add src/App.js
git commit -m "feat: integracion del stepper y pantalla de exito"
```

---

## Notas para conectar el backend real (usuario)

- Poner `REACT_APP_USE_MOCK=false` y `REACT_APP_API_URL=<url del backend>` en `.env`, y reiniciar `npm start`.
- El backend debe exponer `POST /api/empleados` respetando el contrato (201/400/409) y habilitar **CORS** para `http://localhost:3000`.
- El JSON enviado usa las claves: `nombre, apellido, email, dni, fechaNacimiento, fechaIngreso, salario, departamento, telefono` (fechas en formato `YYYY-MM-DD`, `telefono` puede ser `null`).

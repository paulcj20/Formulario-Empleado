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

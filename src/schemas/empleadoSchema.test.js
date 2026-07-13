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

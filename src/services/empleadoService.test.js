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

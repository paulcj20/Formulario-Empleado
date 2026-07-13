import React from 'react';
import '@testing-library/jest-dom';
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

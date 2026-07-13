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

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

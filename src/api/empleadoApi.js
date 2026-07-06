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

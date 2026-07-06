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

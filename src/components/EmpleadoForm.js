import React, { useState } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Alert, Button } from 'react-bootstrap';

import { empleadoSchema } from '../schemas/empleadoSchema';
import { altaEmpleado } from '../services/empleadoService';
import DatosPersonalesStep from './steps/DatosPersonalesStep';
import ContactoStep from './steps/ContactoStep';
import DatosLaboralesStep from './steps/DatosLaboralesStep';

const valoresIniciales = {
  nombre: '',
  apellido: '',
  dni: '',
  fechaNacimiento: '',
  email: '',
  telefono: '',
  departamento: '',
  tipoContrato: '',
  fechaIngreso: '',
  salario: '',
  porcentajeAportes: '',
  montoFactura: '',
  fechaServicio: '',
};

export default function EmpleadoForm({ onExito }) {
  const [enviando, setEnviando] = useState(false);
  const [errorGeneral, setErrorGeneral] = useState(null);

  const methods = useForm({
    resolver: zodResolver(empleadoSchema),
    mode: 'onTouched',
    defaultValues: valoresIniciales,
    shouldUnregister: true,
  });
  const { handleSubmit, setError, watch } = methods;
  const tipoContrato = watch('tipoContrato');

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

        <h5 className="mb-3">Datos personales</h5>
        <DatosPersonalesStep mostrarFechaNacimiento={tipoContrato === 'EMPLEADO'} />

        <h5 className="mt-4 mb-3">Contacto</h5>
        <ContactoStep />

        <h5 className="mt-4 mb-3">Datos laborales</h5>
        <DatosLaboralesStep tipoContrato={tipoContrato} />

        <div className="d-flex justify-content-end mt-4">
          <Button type="submit" variant="primary" disabled={enviando}>
            {enviando ? 'Guardando...' : 'Guardar'}
          </Button>
        </div>
      </form>
    </FormProvider>
  );
}

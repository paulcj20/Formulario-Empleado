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

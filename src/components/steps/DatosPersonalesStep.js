import React from 'react';
import { useFormContext } from 'react-hook-form';
import TextField from '../fields/TextField';

export default function DatosPersonalesStep({ mostrarFechaNacimiento }) {
  const {
    register,
    formState: { errors },
  } = useFormContext();

  return (
    <div>
      <TextField label="Nombre" name="nombre" register={register} error={errors.nombre} />
      <TextField label="Apellido" name="apellido" register={register} error={errors.apellido} />
      <TextField label="DNI" name="dni" register={register} error={errors.dni} />
      {mostrarFechaNacimiento && (
        <TextField
          label="Fecha de nacimiento"
          name="fechaNacimiento"
          type="date"
          register={register}
          error={errors.fechaNacimiento}
        />
      )}
    </div>
  );
}

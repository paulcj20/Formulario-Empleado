import React from 'react';
import { useFormContext } from 'react-hook-form';
import TextField from '../fields/TextField';

export default function ContactoStep() {
  const {
    register,
    formState: { errors },
  } = useFormContext();

  return (
    <div>
      <TextField label="Email" name="email" type="email" register={register} error={errors.email} />
      <TextField
        label="Teléfono (opcional)"
        name="telefono"
        register={register}
        error={errors.telefono}
      />
    </div>
  );
}

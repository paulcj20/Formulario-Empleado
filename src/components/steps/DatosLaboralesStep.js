import React from 'react';
import { useFormContext } from 'react-hook-form';
import TextField from '../fields/TextField';
import SelectField from '../fields/SelectField';
import { DEPARTAMENTOS } from '../../schemas/empleadoSchema';

export default function DatosLaboralesStep() {
  const {
    register,
    formState: { errors },
  } = useFormContext();

  return (
    <div>
      <SelectField
        label="Departamento"
        name="departamento"
        register={register}
        error={errors.departamento}
        options={DEPARTAMENTOS}
      />
      <TextField
        label="Fecha de ingreso"
        name="fechaIngreso"
        type="date"
        register={register}
        error={errors.fechaIngreso}
      />
      <TextField
        label="Salario"
        name="salario"
        type="number"
        register={register}
        error={errors.salario}
      />
    </div>
  );
}

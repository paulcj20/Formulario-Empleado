import React from 'react';
import { useFormContext } from 'react-hook-form';
import TextField from '../fields/TextField';
import SelectField from '../fields/SelectField';
import { DEPARTAMENTOS } from '../../schemas/empleadoSchema';

export default function DatosLaboralesStep({ tipoContrato }) {
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
      {tipoContrato === 'EMPLEADO' && (
        <>
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
          <TextField
            label="Porcentaje de aportes"
            name="porcentajeAportes"
            type="number"
            register={register}
            error={errors.porcentajeAportes}
          />
        </>
      )}
      {tipoContrato === 'TERCIARIZADO' && (
        <>
          <TextField
            label="Monto de Factura"
            name="montoFactura"
            type="number"
            register={register}
            error={errors.montoFactura}
          />
          <TextField
            label="Fecha de Servicio"
            name="fechaServicio"
            type="date"
            register={register}
            error={errors.fechaServicio}
          />
        </>
      )}
    </div>
  );
}

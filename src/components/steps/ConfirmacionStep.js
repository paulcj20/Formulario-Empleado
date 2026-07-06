import React from 'react';
import { useFormContext } from 'react-hook-form';
import { ListGroup } from 'react-bootstrap';

export default function ConfirmacionStep() {
  const { getValues } = useFormContext();
  const v = getValues();

  const filas = [
    ['Nombre', v.nombre],
    ['Apellido', v.apellido],
    ['DNI', v.dni],
    ['Fecha de nacimiento', v.fechaNacimiento],
    ['Email', v.email],
    ['Teléfono', v.telefono ? v.telefono : '—'],
    ['Departamento', v.departamento],
    ['Fecha de ingreso', v.fechaIngreso],
    ['Salario', v.salario],
  ];

  return (
    <ListGroup className="mb-2">
      {filas.map(([etiqueta, valor]) => (
        <ListGroup.Item key={etiqueta} className="d-flex justify-content-between">
          <strong>{etiqueta}</strong>
          <span>{valor}</span>
        </ListGroup.Item>
      ))}
    </ListGroup>
  );
}

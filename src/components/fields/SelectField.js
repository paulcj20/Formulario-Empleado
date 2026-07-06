import React from 'react';
import { Form } from 'react-bootstrap';

export default function SelectField({
  label,
  name,
  register,
  error,
  options,
  placeholder = 'Seleccione...',
}) {
  return (
    <Form.Group className="mb-3" controlId={name}>
      <Form.Label>{label}</Form.Label>
      <Form.Select isInvalid={!!error} {...register(name)}>
        <option value="">{placeholder}</option>
        {options.map((opt) => (
          <option key={opt} value={opt}>
            {opt}
          </option>
        ))}
      </Form.Select>
      <Form.Control.Feedback type="invalid">{error?.message}</Form.Control.Feedback>
    </Form.Group>
  );
}

import React from 'react';
import { Form } from 'react-bootstrap';

export default function TextField({ label, name, register, error, type = 'text', ...rest }) {
  return (
    <Form.Group className="mb-3" controlId={name}>
      <Form.Label>{label}</Form.Label>
      <Form.Control type={type} isInvalid={!!error} {...register(name)} {...rest} />
      <Form.Control.Feedback type="invalid">{error?.message}</Form.Control.Feedback>
    </Form.Group>
  );
}

import React, { useState } from 'react';
import { Container, Card, Alert, Button } from 'react-bootstrap';
import EmpleadoStepper from './components/EmpleadoStepper';

export default function App() {
  const [creado, setCreado] = useState(null);

  return (
    <Container className="py-4" style={{ maxWidth: 720 }}>
      <h1 className="mb-4">Alta de empleado</h1>

      {creado ? (
        <Card body>
          <Alert variant="success" className="mb-3">
            Empleado creado con éxito (ID {creado.id}).
          </Alert>
          <Button variant="primary" onClick={() => setCreado(null)}>
            Cargar otro empleado
          </Button>
        </Card>
      ) : (
        <Card body>
          <EmpleadoStepper onExito={setCreado} />
        </Card>
      )}
    </Container>
  );
}

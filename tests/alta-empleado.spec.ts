import { test, expect } from '@playwright/test';

test('Caso feliz de Alta de Empleado', async ({ page }) => {
    await page.goto('http://localhost:3001');
    await page.getByLabel('Tipo de contrato').selectOption('EMPLEADO');
    await page.getByRole('textbox', { name: 'Nombre' }).fill('Juan');
    await page.getByRole('textbox', { name: 'Apellido' }).fill('Pérez');
    await page.getByRole('textbox', { name: 'DNI' }).fill('30123456');
    await page.getByRole('textbox', { name: 'Fecha de nacimiento' }).fill('2001-01-25');
    await page.getByRole('textbox', { name: 'Email' }).fill('juan.perez@example.com');
    await page.getByRole('textbox', { name: 'Teléfono (opcional)' }).fill('1122334455');
    await page.getByLabel('Departamento').selectOption('PRODUCCION');
    await page.getByRole('textbox', { name: 'Fecha de ingreso' }).fill('2026-07-12');
    await page.getByRole('spinbutton', { name: 'Salario' }).fill('500000');
    await page.getByRole('spinbutton', { name: 'Porcentaje de aportes' }).fill('17');
    await page.getByRole('button', { name: 'Guardar' }).click();
    await expect(page.getByRole('alert')).toContainText('hola');
});

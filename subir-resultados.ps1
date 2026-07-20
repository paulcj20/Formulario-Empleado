# Corre los tests de Playwright y sube los resultados a Kiwi TCMS
# Uso: .\subir-resultados.ps1
Set-Location $PSScriptRoot

# Para que tcms-api encuentre el archivo de credenciales ~\.tcms.conf
$env:HOME = $env:USERPROFILE

# Datos del producto en Kiwi (deben coincidir exacto con lo creado en Kiwi)
$env:TCMS_PRODUCT = "Formulario de Alta Empleado / Servicio"
$env:TCMS_PRODUCT_VERSION = "1.0"
$env:TCMS_BUILD = "local-" + (Get-Date -Format "yyyyMMdd-HHmmss")

# ID de TU Test Plan en Kiwi (el numero que aparece en la URL del plan, ej. /plan/1/... -> 1)
# Los Test Runs nuevos se crearan dentro de este plan....
$env:TCMS_PLAN_ID = "1"

# 1) Correr los tests (solo chromium)
npx playwright test --project=chromium

# 2) Subir el junit.xml a Kiwi
#    (bypass de SSL porque Kiwi local usa certificado autofirmado)
$xml = Join-Path $PSScriptRoot "results\junit.xml"
python subir_kiwi.py "$xml"

# 3) Archivar los artefactos de esta corrida (Playwright borra test-results/ en cada corrida)
$dest = Join-Path $PSScriptRoot "artifacts\$($env:TCMS_BUILD)"
New-Item -ItemType Directory -Path $dest -Force | Out-Null
foreach ($src in @("test-results", "playwright-report", "results")) {
    if (Test-Path $src) { Copy-Item $src -Destination $dest -Recurse -Force }
}
Write-Host "Artefactos archivados en $dest"

Write-Host ""
Write-Host "Listo. Mira el Test Run en https://localhost:8443 -> TESTING -> Search Test Runs" -ForegroundColor Green

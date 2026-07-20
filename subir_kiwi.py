# Sube resultados junit.xml a Kiwi TCMS.
# - Desactiva verificacion SSL (para Kiwi local con certificado autofirmado)
# - Arregla los timestamps de Playwright (terminan en 'Z' y el plugin no los parsea)
# - Usa solo el titulo del test como summary para matchear con el caso en Kiwi
# - Adjunta los screenshots de Playwright a las ejecuciones que fallaron
import base64
import os
import pathlib
import re
import ssl
import sys

ssl._create_default_https_context = ssl._create_unverified_context

from tcms_junit_plugin import Plugin  # noqa: E402

_original = Plugin.parse_timestamp


def _parse_timestamp(self, value):
    value = value.strip().replace("Z", "").replace("+00:00", "")
    try:
        return _original(self, value)
    except ValueError:
        return None  # sin timestamp es mejor que abortar la subida


Plugin.parse_timestamp = _parse_timestamp

plugin = Plugin(verbose=True, summary_template="${name}")
plugin.parse(sys.argv[1:])

# ── Enlazar el reporte HTML de Playwright en las notas del TestRun ──
report_url = os.environ.get("REPORT_URL")
if report_url:
    plugin.backend.rpc.TestRun.update(
        plugin.backend.run_id, {"notes": f"Reporte Playwright: {report_url}"}
    )
    print(f"[reporte] Enlazado en TR-{plugin.backend.run_id}: {report_url}")


# ── Adjuntar screenshots de fallos ───────────────────────────────
def _norm(text):
    return re.sub(r"[^a-z0-9]", "", text.lower())


backend = plugin.backend
rpc = backend.rpc
test_results = pathlib.Path(__file__).parent / "test-results"

print(f"[adjuntos] Buscando screenshots en: {test_results}")
if not test_results.is_dir():
    print("[adjuntos] No existe la carpeta test-results (no hubo fallos o no se corrieron tests)")
else:
    folders = [f for f in test_results.iterdir() if f.is_dir()]
    print(f"[adjuntos] Carpetas de fallos encontradas: {[f.name for f in folders]}")

    executions = rpc.TestExecution.filter({"run": backend.run_id})
    print(f"[adjuntos] Ejecuciones en TR-{backend.run_id}: {len(executions)}")

    for execution in executions:
        try:
            case = rpc.TestCase.filter({"pk": execution["case"]})[0]
            case_key = _norm(case["summary"])
            print(f"[adjuntos] TE-{execution['id']} caso='{case['summary']}'")

            # Playwright crea una carpeta por test fallido, con el titulo slugificado
            for folder in folders:
                if case_key not in _norm(folder.name):
                    continue
                images = sorted(folder.glob("*.png"))
                if not images:
                    print(f"[adjuntos]   {folder.name}: sin .png")
                for image in images:
                    content = base64.b64encode(image.read_bytes()).decode()
                    rpc.TestExecution.add_attachment(
                        execution["id"], f"{folder.name}-{image.name}", content
                    )
                    print(f"[adjuntos]   Adjuntado {image.name} -> TE-{execution['id']}")
        except Exception as error:  # noqa: BLE001
            print(f"[adjuntos] ERROR en TE-{execution.get('id')}: {error!r}")

print("Subida a Kiwi completada.")

# Sube resultados junit.xml a Kiwi TCMS.
# - Desactiva verificacion SSL (para Kiwi local con certificado autofirmado)
# - Arregla los timestamps de Playwright (terminan en 'Z' y el plugin no los parsea)
# - Usa solo el titulo del test como summary para matchear con el caso en Kiwi
# - Adjunta los screenshots de Playwright a las ejecuciones que fallaron
import base64
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


# ── Adjuntar screenshots de fallos ───────────────────────────────
def _norm(text):
    return re.sub(r"[^a-z0-9]", "", text.lower())


backend = plugin.backend
rpc = backend.rpc
test_results = pathlib.Path(__file__).parent / "test-results"

if test_results.is_dir():
    executions = rpc.TestExecution.filter({"run": backend.run_id})
    for execution in executions:
        case = rpc.TestCase.filter({"pk": execution["case"]})[0]
        case_key = _norm(case["summary"])

        # Playwright crea una carpeta por test fallido, con el titulo slugificado
        for folder in test_results.iterdir():
            if not folder.is_dir() or case_key not in _norm(folder.name):
                continue
            for image in sorted(folder.glob("*.png")):
                content = base64.b64encode(image.read_bytes()).decode()
                rpc.TestExecution.add_attachment(
                    execution["id"], f"{folder.name}-{image.name}", content
                )
                print(f"Adjuntado {image.name} -> TE-{execution['id']}")

print("Subida a Kiwi completada.")

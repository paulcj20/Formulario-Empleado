# Sube resultados junit.xml a Kiwi TCMS.
# - Desactiva verificacion SSL (Kiwi local con certificado autofirmado)
# - Arregla los timestamps de Playwright (terminan en 'Z' y el plugin no los parsea)
# - Usa solo el titulo del test como summary para matchear con el caso en Kiwi
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

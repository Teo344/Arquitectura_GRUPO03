#!/usr/bin/env bash
# Diagramas de secuencia completos de un TG: los .oom (sin PNG), 03_SECUENCIA.prj y su entrada en el .sws.
# uso: bash gen_ds_tg.sh <carpeta_TG> <espacio_sws_sin_ext> <clave_specs> [java|dotnet]
set -u
S="$(cd "$(dirname "$0")" && pwd)"
RAIZ="$(cd "$S/../../.." && pwd)"
TPL="$RAIZ/TI.1.1. PAO202651_SOAP_JAVA_SINBDD_NRC36884_GR03/01. UML/03 DIAGRAMAS/03_SECUENCIA/03_SECUENCIA.prj"
TG="$(cd "$1" && pwd)"; SWS="$2"; CLAVE="$3"
D="$TG/01. UML/03 DIAGRAMAS/03_SECUENCIA"
python -I "$S/generar_specs_ds.py" "$CLAVE" "${4:-java}" >/dev/null
bash "$S/gen_ds.sh" "$TG" "$CLAVE"
modelos=(); for f in "$S/specs/secuencia/$CLAVE"/DS_*.txt; do modelos+=("$D/$(basename "$f" .txt).oom"); done
PID=$(python -I "$S/crear_prj.py" "$TPL" "$D/03_SECUENCIA.prj" "03_SECUENCIA" "${modelos[@]}")
python -I "$S/agregar_sws.py" "$TG/$SWS.sws" 03_SECUENCIA "$PID" "01. UML/03 DIAGRAMAS/03_SECUENCIA/03_SECUENCIA.prj"
for k in $(seq 1 90); do tasklist | grep -qi pdshell || break; sleep 2; done; sleep 4
C:/Windows/SysWOW64/cscript.exe //nologo "$(cygpath -w "$S/verificar_prj.vbs")" "$(cygpath -w "$D/03_SECUENCIA.prj")" "$(cygpath -w "$TG/$SWS.sws")" 2>&1 | iconv -f CP850 -t UTF-8 | grep -vE "^ - "

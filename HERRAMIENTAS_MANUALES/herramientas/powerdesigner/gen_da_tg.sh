#!/usr/bin/env bash
# Diagramas de actividad completos de un TG: los .oom (la imagen de revisión queda en tmp/revision), 02_ACTIVIDAD.prj con
# todos los specs de la clave y su entrada en el .sws.
# uso: bash gen_da_tg.sh <carpeta_TG> <espacio_sws_sin_ext> <clave_specs> <servidor> <bd> [java|dotnet|rest|restnet]
#   con servidor "-" no se regeneran los specs (por ejemplo, los de generar_specs_da_ex.py).
set -u
S="$(cd "$(dirname "$0")" && pwd)"
RAIZ="$(cd "$S/../../.." && pwd)"
TPL="$RAIZ/TI.1.1. PAO202651_SOAP_JAVA_SINBDD_NRC36884_GR03/01. UML/03 DIAGRAMAS/02_ACTIVIDAD/02_ACTIVIDAD.prj"
TG="$(cd "$1" && pwd)"; SWS="$2"; CLAVE="$3"
D="$TG/01. UML/03 DIAGRAMAS/02_ACTIVIDAD"
[ "$4" != "-" ] && python -I "$S/generar_specs_da.py" "$CLAVE" "$4" "$5" "${6:-java}" >/dev/null
bash "$S/gen_da.sh" "$TG" "$CLAVE"
modelos=(); for f in "$S/specs/actividad/$CLAVE"/DA_*.txt; do modelos+=("$D/$(basename "$f" .txt).oom"); done
PID=$(python -I "$S/crear_prj.py" "$TPL" "$D/02_ACTIVIDAD.prj" "02_ACTIVIDAD" "${modelos[@]}")
python -I "$S/agregar_sws.py" "$TG/$SWS.sws" 02_ACTIVIDAD "$PID" "01. UML/03 DIAGRAMAS/02_ACTIVIDAD/02_ACTIVIDAD.prj"
for k in $(seq 1 90); do tasklist | grep -qi pdshell || break; sleep 2; done; sleep 4
C:/Windows/SysWOW64/cscript.exe //nologo "$(cygpath -w "$S/verificar_prj.vbs")" "$(cygpath -w "$D/02_ACTIVIDAD.prj")" "$(cygpath -w "$TG/$SWS.sws")" 2>&1 | iconv -f CP850 -t UTF-8

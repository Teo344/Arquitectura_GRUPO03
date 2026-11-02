#!/usr/bin/env bash
# Diagramas de actividad completos de un TG: los 7 .oom/.png, 02_ACTIVIDAD.prj y su entrada en el .sws.
# uso: bash gen_da_tg.sh <carpeta_TG> <espacio_sws_sin_ext> <clave_specs> <servidor> <bd> [java|dotnet]
set -u
S="$(cd "$(dirname "$0")" && pwd)"
RAIZ="$(cd "$S/../../.." && pwd)"
TPL="$RAIZ/TI.1.1. PAO202651_SOAP_JAVA_SINBDD_NRC36884_GR03/01. UML/03 DIAGRAMAS/02_ACTIVIDAD/02_ACTIVIDAD.prj"
TG="$(cd "$1" && pwd)"; SWS="$2"; CLAVE="$3"
D="$TG/01. UML/03 DIAGRAMAS/02_ACTIVIDAD"
python -I "$S/generar_specs_da.py" "$CLAVE" "$4" "$5" "${6:-java}" >/dev/null
bash "$S/gen_da.sh" "$TG" "$CLAVE"
modelos=(); for n in Iniciar_Sesion Cerrar_Sesion Consultar_Cuenta Consultar_Movimientos Registrar_Deposito Registrar_Retiro Registrar_Transferencia; do modelos+=("$D/DA_$n.oom"); done
PID=$(python -I "$S/crear_prj.py" "$TPL" "$D/02_ACTIVIDAD.prj" "02_ACTIVIDAD" "${modelos[@]}")
python -I "$S/agregar_sws.py" "$TG/$SWS.sws" 02_ACTIVIDAD "$PID" "01. UML/03 DIAGRAMAS/02_ACTIVIDAD/02_ACTIVIDAD.prj"
for k in $(seq 1 90); do tasklist | grep -qi pdshell || break; sleep 2; done; sleep 4
C:/Windows/SysWOW64/cscript.exe //nologo "$(cygpath -w "$S/verificar_prj.vbs")" "$(cygpath -w "$D/02_ACTIVIDAD.prj")" "$(cygpath -w "$TG/$SWS.sws")" 2>&1 | iconv -f CP850 -t UTF-8

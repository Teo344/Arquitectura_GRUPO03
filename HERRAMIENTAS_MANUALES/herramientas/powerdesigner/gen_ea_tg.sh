#!/usr/bin/env bash
# Diagrama de arquitectura (EAM) de un TG a partir de specs/arquitectura/<clave>.txt: el .eam (sin PNG; la imagen de
# revisión queda en tmp/revision), 05_ARQUITECTURA.prj y su entrada en el .sws.
# uso: bash gen_ea_tg.sh <carpeta_TG> <espacio_sws_sin_ext> <clave_spec> <nombre_modelo>
set -u
S="$(cd "$(dirname "$0")" && pwd)"
RAIZ="$(cd "$S/../../.." && pwd)"
TPLD="$RAIZ/TI.1.1. PAO202651_SOAP_JAVA_SINBDD_NRC36884_GR03/01. UML/03 DIAGRAMAS/05_ARQUITECTURA"
TMP="$S/tmp"; REV="$TMP/revision"; mkdir -p "$REV"
TG="$(cd "$1" && pwd)"; SWS="$2"; CLAVE="$3"; A="$4"
D="$TG/01. UML/03 DIAGRAMAS/05_ARQUITECTURA"; mkdir -p "$D"
SPEC="$S/specs/arquitectura/$CLAVE.txt"
esperar(){ for k in $(seq 1 90); do tasklist | grep -qi pdshell || { sleep 4; return 0; }; sleep 2; done; echo "PowerDesigner sigue abierto"; }
w(){ cygpath -w "$1"; }
CSCRIPT=C:/Windows/SysWOW64/cscript.exe   # PowerDesigner 16 es de 32 bits

iconv -f UTF-8 -t UTF-16LE "$SPEC" | { printf '\xff\xfe'; cat; } > "$TMP/spec_ea.txt"
for intento in 1 2 3; do
  esperar; cp "$TPLD/Arquitectura.eam" "$TMP/base_ea.eam"; rm -f "${D:?}/${A:?}.eam"
  "$CSCRIPT" //nologo "$(w "$S/ea_detalle.vbs")" "$(w "$TMP/base_ea.eam")" "$(w "$D/$A.eam")" "$(w "$TMP/spec_ea.txt")" "$A"
  [ -f "$D/$A.eam" ] && [ "$(stat -c %s "$D/$A.eam")" -gt 20000 ] && break
done
python -I "$S/finalizar_ea.py" "$D/$A.eam" "$TMP/base_ea.eam" "$SPEC"
rm -f "${REV:?}/${A:?}.png"
for intento in 1 2 3; do esperar; "$CSCRIPT" //nologo "$(w "$S/exportar_diagrama.vbs")" "$(w "$D/$A.eam")" "$(w "$REV/$A.png")" >/dev/null 2>&1; [ -f "$REV/$A.png" ] && break; done
PID=$(python -I "$S/crear_prj.py" "$TPLD/05_ARQUITECTURA.prj" "$D/05_ARQUITECTURA.prj" "05_ARQUITECTURA" "$D/$A.eam")
python -I "$S/agregar_sws.py" "$TG/$SWS.sws" 05_ARQUITECTURA "$PID" "01. UML/03 DIAGRAMAS/05_ARQUITECTURA/05_ARQUITECTURA.prj" >/dev/null
esperar
"$CSCRIPT" //nologo "$(w "$S/verificar_prj.vbs")" "$(w "$D/05_ARQUITECTURA.prj")" "$(w "$TG/$SWS.sws")" 2>&1 | iconv -f CP850 -t UTF-8 | grep -vE "^ - "

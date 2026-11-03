#!/usr/bin/env bash
# Diagrama de clases del servidor de un TG (Java o C#): extrae las clases del código, genera el .oom (sin PNG; la imagen de
# revisión queda en tmp/revision), crea 04_CLASES.prj y lo agrega al .sws.
# uso: bash gen_dc_tg.sh <carpeta_TG> <espacio_sws_sin_ext> <carpeta del código del servidor> <nombre_modelo> [java|cs]
#   java: la carpeta es src/main/java; cs: la carpeta del proyecto .csproj
set -u
S="$(cd "$(dirname "$0")" && pwd)"
RAIZ="$(cd "$S/../../.." && pwd)"
TPLD="$RAIZ/TI.1.1. PAO202651_SOAP_JAVA_SINBDD_NRC36884_GR03/01. UML/03 DIAGRAMAS/04_CLASES"
TMP="$S/tmp"; REV="$TMP/revision"; mkdir -p "$REV" "$S/specs/clases"
TG="$(cd "$1" && pwd)"; SWS="$2"; SRC="$(cd "$3" && pwd)"; A="$4"
D="$TG/01. UML/03 DIAGRAMAS/04_CLASES"; mkdir -p "$D"
esperar(){ for k in $(seq 1 90); do tasklist | grep -qi pdshell || { sleep 4; return 0; }; sleep 2; done; echo "PowerDesigner sigue abierto"; }
w(){ cygpath -w "$1"; }
CSCRIPT=C:/Windows/SysWOW64/cscript.exe   # PowerDesigner 16 es de 32 bits

python -I "$S/extraer_clases_${5:-java}.py" "$SRC" "$S/specs/clases/$A.txt"
iconv -f UTF-8 -t UTF-16LE "$S/specs/clases/$A.txt" | { printf '\xff\xfe'; cat; } > "$TMP/spec_dc.txt"
for intento in 1 2 3; do
  esperar; cp "$TPLD/WS_ConUni_DC.oom" "$TMP/base_dc.oom"; rm -f "${D:?}/${A:?}.oom" "$TMP/paquetes.txt"
  "$CSCRIPT" //nologo "$(w "$S/dc_detalle.vbs")" "$(w "$TMP/base_dc.oom")" "$(w "$D/$A.oom")" "$(w "$TMP/spec_dc.txt")" "$A" "$(w "$TMP/paquetes.txt")"
  [ -f "$D/$A.oom" ] && [ "$(stat -c %s "$D/$A.oom")" -gt 20000 ] && break
done
python -I "$S/finalizar_dc.py" "$D/$A.oom" "$TMP/base_dc.oom" "$TMP/paquetes.txt"
rm -f "${REV:?}/${A:?}.png"
for intento in 1 2 3; do esperar; "$CSCRIPT" //nologo "$(w "$S/exportar_diagrama.vbs")" "$(w "$D/$A.oom")" "$(w "$REV/$A.png")" >/dev/null 2>&1; [ -f "$REV/$A.png" ] && break; done
# el proyecto incluye todos los modelos de la carpeta (EX.1.1 tiene un diagrama por servidor)
PID=$(python -I "$S/crear_prj.py" "$TPLD/04_CLASES.prj" "$D/04_CLASES.prj" "04_CLASES" "$D"/*.oom)
python -I "$S/agregar_sws.py" "$TG/$SWS.sws" 04_CLASES "$PID" "01. UML/03 DIAGRAMAS/04_CLASES/04_CLASES.prj" >/dev/null
esperar
"$CSCRIPT" //nologo "$(w "$S/verificar_prj.vbs")" "$(w "$D/04_CLASES.prj")" "$(w "$TG/$SWS.sws")" 2>&1 | iconv -f CP850 -t UTF-8 | grep -vE "^ - "

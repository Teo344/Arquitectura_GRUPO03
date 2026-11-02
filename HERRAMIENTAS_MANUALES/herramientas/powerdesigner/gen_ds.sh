#!/usr/bin/env bash
# Genera diagramas de secuencia de un TG en PowerDesigner 16.1 a partir de specs/secuencia/<clave>/<DS_x>.txt (UTF-8):
#   <TG>/01. UML/03 DIAGRAMAS/03_SECUENCIA/<DS_x>.oom   (sin PNG; la imagen de revisión queda en tmp/revision)
# uso: bash gen_ds.sh <carpeta_TG> <clave_specs> [DS_x ...]   (sin nombres, genera todos los specs de la clave)
set -u
S="$(cd "$(dirname "$0")" && pwd)"
RAIZ="$(cd "$S/../../.." && pwd)"
TPL="$RAIZ/TI.1.1. PAO202651_SOAP_JAVA_SINBDD_NRC36884_GR03/01. UML/03 DIAGRAMAS/03_SECUENCIA/DS_02_INGRESO_SISTEMA.oom"
TMP="$S/tmp"; REV="$TMP/revision"; mkdir -p "$REV"
TG="$(cd "$1" && pwd)"; CLAVE="$2"; shift 2
D="$TG/01. UML/03 DIAGRAMAS/03_SECUENCIA"; mkdir -p "$D"

esperar(){ for k in $(seq 1 90); do tasklist | grep -qi pdshell || { sleep 4; return 0; }; sleep 2; done; echo "PowerDesigner sigue abierto"; }
w(){ cygpath -w "$1"; }
CSCRIPT=C:/Windows/SysWOW64/cscript.exe   # PowerDesigner 16 es de 32 bits

nombres=("$@")
[ ${#nombres[@]} -eq 0 ] && for f in "$S/specs/secuencia/$CLAVE"/DS_*.txt; do nombres+=("$(basename "$f" .txt)"); done
for a in "${nombres[@]}"; do
  iconv -f UTF-8 -t UTF-16LE "$S/specs/secuencia/$CLAVE/$a.txt" | { printf '\xff\xfe'; cat; } > "$TMP/spec_ds.txt"
  for intento in 1 2 3; do
    esperar; cp "$TPL" "$TMP/base_ds.oom"; rm -f "${D:?}/${a:?}.oom"
    "$CSCRIPT" //nologo "$(w "$S/ds_detalle.vbs")" "$(w "$TMP/base_ds.oom")" "$(w "$D/$a.oom")" "$(w "$TMP/spec_ds.txt")" "$a" >/dev/null 2>&1
    [ -f "$D/$a.oom" ] && [ "$(stat -c %s "$D/$a.oom")" -gt 20000 ] && break
  done
  python -I "$S/finalizar_ds.py" "$D/$a.oom" "$TMP/base_ds.oom" >/dev/null || echo "fallo al finalizar $a"
  rm -f "${REV:?}/${a:?}.png"
  for intento in 1 2 3; do esperar; "$CSCRIPT" //nologo "$(w "$S/exportar_diagrama.vbs")" "$(w "$D/$a.oom")" "$(w "$REV/$a.png")" >/dev/null 2>&1; [ -f "$REV/$a.png" ] && break; done
  echo "  $a oom=$(stat -c %s "$D/$a.oom") revision=$([ -f "$REV/$a.png" ] && echo ok || echo FALTA)"
done

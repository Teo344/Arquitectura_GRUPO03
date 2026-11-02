#!/usr/bin/env bash
# Genera diagramas de actividad de un TG en PowerDesigner 16.1 a partir de specs/actividad/<clave>/<DA_x>.txt (UTF-8):
#   <TG>/01. UML/03 DIAGRAMAS/02_ACTIVIDAD/<DA_x>.oom y .png
# uso: bash gen_da.sh <carpeta_TG> <clave_specs> [DA_x ...]   (sin nombres, genera todos los specs de la clave)
set -u
S="$(cd "$(dirname "$0")" && pwd)"
RAIZ="$(cd "$S/../../.." && pwd)"
TPL="$RAIZ/TI.1.1. PAO202651_SOAP_JAVA_SINBDD_NRC36884_GR03/01. UML/03 DIAGRAMAS/02_ACTIVIDAD/Diagrama_Actividad_Conversor.oom"
TMP="$S/tmp"; mkdir -p "$TMP"
TG="$(cd "$1" && pwd)"; CLAVE="$2"; shift 2
D="$TG/01. UML/03 DIAGRAMAS/02_ACTIVIDAD"; mkdir -p "$D"

esperar(){ for k in $(seq 1 90); do tasklist | grep -qi pdshell || { sleep 4; return 0; }; sleep 2; done; echo "PowerDesigner sigue abierto"; }
w(){ cygpath -w "$1"; }
CSCRIPT=C:/Windows/SysWOW64/cscript.exe   # PowerDesigner 16 es de 32 bits

nombres=("$@")
[ ${#nombres[@]} -eq 0 ] && for f in "$S/specs/actividad/$CLAVE"/DA_*.txt; do nombres+=("$(basename "$f" .txt)"); done
for a in "${nombres[@]}"; do
  spec="$S/specs/actividad/$CLAVE/$a.txt"
  # nombre del diagrama: primera línea "# DA <título>" del spec
  titulo="$(head -1 "$spec" | sed -e 's/^# *//')"
  iconv -f UTF-8 -t UTF-16LE "$spec" | { printf '\xff\xfe'; cat; } > "$TMP/spec_da.txt"
  for intento in 1 2 3; do
    esperar; cp "$TPL" "$TMP/base_da.oom"; rm -f "${D:?}/${a:?}.oom"
    "$CSCRIPT" //nologo "$(w "$S/da_detalle.vbs")" "$(w "$TMP/base_da.oom")" "$(w "$D/$a.oom")" "$(w "$TMP/spec_da.txt")" "$a" "$titulo" >/dev/null 2>&1
    [ -f "$D/$a.oom" ] && [ "$(stat -c %s "$D/$a.oom")" -gt 20000 ] && break
  done
  python -I "$S/finalizar_da.py" "$D/$a.oom" "$TMP/base_da.oom" >/dev/null || echo "fallo al finalizar $a"
  rm -f "${D:?}/${a:?}.png"
  for intento in 1 2 3; do esperar; "$CSCRIPT" //nologo "$(w "$S/exportar_diagrama.vbs")" "$(w "$D/$a.oom")" "$(w "$D/$a.png")" >/dev/null 2>&1; [ -f "$D/$a.png" ] && break; done
  echo "  $a oom=$(stat -c %s "$D/$a.oom") png=$([ -f "$D/$a.png" ] && echo ok || echo FALTA)"
done

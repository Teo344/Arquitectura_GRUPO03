#!/usr/bin/env bash
# Modelos de datos de una BD (conceptual .cdm, lógico .ldm y físico .pdm) a partir de su script de creación: los tres
# en <proyecto>/02. MER (las imágenes de revisión quedan en tmp/revision), MER.prj con todos los modelos MER_* de esa
# carpeta (un proyecto puede tener varias BD) y su entrada en el .sws.
# uso: bash gen_mer_tg.sh <carpeta_proyecto> <espacio_sws_sin_ext> <clave_spec> <dbms> <sistema> <sufijo_archivo> \
#        <script_sql_relativo> <perfil>
#   ej.: bash gen_mer_tg.sh "<TG.1.1...>" SOAP_BDDLOCAL_JAVA TG11 "MySQL 5.0" "EurekaBank SOAP Java" \
#          EurekaBank_SOAP_Java "03. BDD/init/01_crea_bd.sql" eureka
set -u
S="$(cd "$(dirname "$0")" && pwd)"
RAIZ="$(cd "$S/../../.." && pwd)"
TPLD="$RAIZ/TI.1.1. PAO202651_SOAP_JAVA_SINBDD_NRC36884_GR03/01. UML/03 DIAGRAMAS/01_CASOS_USO"
TMP="$S/tmp"; REV="$TMP/revision"; mkdir -p "$REV"
TG="$(cd "$1" && pwd)"; SWS="$2"; CLAVE="$3"; DBMS="$4"; SISTEMA="$5"; SUF="$6"; SQL="$7"; PERFIL="$8"
D="$TG/02. MER"; mkdir -p "$D"
CDM="MER_Conceptual_$SUF"; LDM="MER_Logico_$SUF"; PDM="MER_Fisico_$SUF"
esperar(){ for k in $(seq 1 90); do tasklist | grep -qi pdshell || { sleep 4; return 0; }; sleep 2; done; echo "PowerDesigner sigue abierto"; }
w(){ cygpath -w "$1"; }
CSCRIPT=C:/Windows/SysWOW64/cscript.exe   # PowerDesigner 16 es de 32 bits

SPEC=$(python -I "$S/generar_specs_mer.py" "$TG/$SQL" "$CLAVE" "$PERFIL") || exit 1
iconv -f UTF-8 -t UTF-16LE "$SPEC" | { printf '\xff\xfe'; cat; } > "$TMP/spec_mer.txt"
for intento in 1 2 3; do
  esperar; rm -f "${D:?}/${CDM:?}".cd? "${D:?}/${LDM:?}".ld? "${D:?}/${PDM:?}".pd?
  "$CSCRIPT" //nologo "$(w "$S/mer_detalle.vbs")" "$(w "$TMP/spec_mer.txt")" "$DBMS" \
    "$(w "$D/$CDM.cdm")" "$(w "$D/$LDM.ldm")" "$(w "$D/$PDM.pdm")" "$SISTEMA"
  ok=1; for f in "$D/$CDM.cdm" "$D/$LDM.ldm" "$D/$PDM.pdm"; do [ -f "$f" ] && [ "$(stat -c %s "$f")" -gt 20000 ] || ok=0; done
  [ $ok = 1 ] && break
done
rm -f "${D:?}/${CDM:?}.cdb" "${D:?}/${LDM:?}.ldb" "${D:?}/${PDM:?}.pdb"   # respaldos que deja PowerDesigner al guardar dos veces
esperar
python -I "$S/finalizar_mer.py" "$D/$CDM.cdm" "$D/$LDM.ldm" "$D/$PDM.pdm"

# las imágenes se exportan desde copias: el archivo recién generado puede seguir bloqueado por PowerDesigner
for m in "$CDM.cdm" "$LDM.ldm" "$PDM.pdm"; do
  cp "$D/$m" "$TMP/revision_$m"; rm -f "${REV:?}/${m%.*}.png"
  for intento in 1 2 3; do esperar; "$CSCRIPT" //nologo "$(w "$S/exportar_diagrama.vbs")" "$(w "$TMP/revision_$m")" "$(w "$REV/${m%.*}.png")" >/dev/null 2>&1; [ -f "$REV/${m%.*}.png" ] && break; done
  rm -f "$TMP/revision_$m"
done
PID=$(python -I "$S/crear_prj.py" "$TPLD/01_CASOS_USO.prj" "$D/MER.prj" "MER" "$D"/MER_*.cdm "$D"/MER_*.ldm "$D"/MER_*.pdm)
python -I "$S/agregar_sws.py" "$TG/$SWS.sws" MER "$PID" "02. MER/MER.prj" >/dev/null
esperar
"$CSCRIPT" //nologo "$(w "$S/verificar_prj.vbs")" "$(w "$D/MER.prj")" "$(w "$TG/$SWS.sws")" 2>&1 | iconv -f CP850 -t UTF-8 | grep -vE "^ - "
# PowerDesigner guarda el espacio de trabajo al cerrarse, a veces después de la primera limpieza
for vez in 1 2; do esperar; python -I "$S/limpiar_sws.py" "$RAIZ"/TG.*/*.sws "$RAIZ"/EX.*/*.sws; done

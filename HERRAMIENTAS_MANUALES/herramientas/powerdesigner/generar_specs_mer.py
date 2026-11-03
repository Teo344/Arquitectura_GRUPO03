import sys,re,os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))   # python -I no agrega la carpeta del script
from perfiles_mer import PERFILES
"""Lee el script de creación de una BD (01_crea_bd.sql, MySQL o SQL Server) y genera specs/mer/<clave>.txt para
mer_detalle.vbs: tablas, columnas, claves, índices, checks y referencias. Los tres modelos usan los nombres del script;
la descripción legible de cada tabla y columna (perfiles_mer.py) va como comentario.
uso: generar_specs_mer.py <01_crea_bd.sql> <clave> <perfil>

Formato (separado por |):
  TABLA|codigo|nombre|comentario|columna|fila
  COL|tabla|codigo|nombre|tipo|obligatoria(1/0)|default|identidad(1/0)|comentario|restriccion_default (SQL Server)
  PK|tabla|restriccion|col1,col2          UQ|tabla|restriccion|col1,col2          IDX|tabla|indice|col1,col2
  LOV|tabla|columna|restriccion|valor1;valor2          CHK|tabla|columna|restriccion|expresion
  TCHK|tabla|restriccion|expresion        (CHECK de varias columnas)
  REF|codigo|hija|padre|cols_hija|cols_padre|relacion|cardinalidad"""

PALABRAS = {'AND', 'OR', 'NOT', 'NULL', 'IS', 'IN', 'BETWEEN', 'LIKE'}

def partir(cuerpo):
    """Divide el cuerpo de un CREATE TABLE por las comas de primer nivel."""
    partes, nivel, act = [], 0, ''
    for ch in cuerpo:
        if ch == '(':
            nivel += 1
        elif ch == ')':
            nivel -= 1
        if ch == ',' and nivel == 0:
            partes.append(act.strip()); act = ''
        else:
            act += ch
    if act.strip():
        partes.append(act.strip())
    return partes

def cols(s):
    return ','.join(c.strip().strip('[]') for c in s.split(','))

def columnas_de(expr):
    """Identificadores de una expresión CHECK, sin las cadenas, los números ni las palabras reservadas."""
    sin_cadenas = re.sub(r"N?'[^']*'", '', expr)
    return {i.strip('[]') for i in re.findall(r'\[?[A-Za-z_]\w*\]?', sin_cadenas) if i.strip('[]').upper() not in PALABRAS}

sql_path, clave, perfil = sys.argv[1:4]
TABLAS, COLUMNAS, RELACIONES = (PERFILES[perfil][k] for k in ('TABLAS', 'COLUMNAS', 'RELACIONES'))
sql = open(sql_path, encoding='utf-8-sig').read()
sql = re.sub(r'--[^\n]*', '', sql)
lineas, refs, pend = [], [], []
for m in re.finditer(r'CREATE\s+TABLE\s+(?:\w+\.)?\[?(\w+)\]?\s*\((.*?)\)\s*(?:ENGINE[^;]*)?;', sql, re.S | re.I):
    tabla, cuerpo = m.group(1), m.group(2)
    nombre, coment, x, y = TABLAS[tabla]
    lineas.append(f'TABLA|{tabla}|{tabla}|{nombre}: {coment}|{x}|{y}')
    columnas = []
    for p in partir(cuerpo):
        p = ' '.join(p.split())
        i = re.match(r'(?:INDEX|KEY)\s+(\w+)\s*\((.*)\)$', p, re.I)
        if i:
            pend.append(f'IDX|{tabla}|{i.group(1)}|{cols(i.group(2))}')
            continue
        c = re.match(r'CONSTRAINT\s+(\w+)\s+(PRIMARY\s+KEY|UNIQUE|CHECK|FOREIGN\s+KEY)\s*(.*)', p, re.I)
        if c:
            nom, tipo, resto = c.group(1), c.group(2).upper(), c.group(3)
            if tipo.startswith('PRIMARY') or tipo == 'UNIQUE':
                k = re.match(r'(?:CLUSTERED\s+|NONCLUSTERED\s+)?\((.*?)\)', resto, re.I).group(1)
                pend.append(f'{"PK" if tipo.startswith("PRIMARY") else "UQ"}|{tabla}|{nom}|{cols(k)}')
            elif tipo == 'CHECK':
                expr = resto.strip()[1:-1].strip()
                lov = re.match(r'\[?(\w+)\]?\s+IN\s*\(([^()]*)\)$', expr, re.I)
                usadas = columnas_de(expr) & set(columnas)
                if lov:
                    vals = ';'.join(v.strip().strip("'").replace("N'", '') for v in lov.group(2).split(','))
                    pend.append(f'LOV|{tabla}|{lov.group(1)}|{nom}|{vals}')
                elif len(usadas) == 1:
                    pend.append(f'CHK|{tabla}|{usadas.pop()}|{nom}|{expr}')
                else:
                    pend.append(f'TCHK|{tabla}|{nom}|{expr}')
            else:
                f = re.match(r'\((.*?)\)\s*REFERENCES\s+(?:\w+\.)?\[?(\w+)\]?\s*\((.*?)\)', resto, re.I)
                rel, card = RELACIONES[nom]
                refs.append(f'REF|{nom}|{tabla}|{f.group(2)}|{cols(f.group(1))}|{cols(f.group(3))}|{rel}|{card}')
            continue
        c = re.match(r'\[?(\w+)\]?\s+(\w+(?:\s*\([\d,\s]+\))?)\s*(.*)', p)
        col, tipo, resto = c.group(1), re.sub(r'\s+', '', c.group(2)).upper(), c.group(3)
        columnas.append(col)
        obligatoria = '1' if re.search(r'\bNOT\s+NULL\b', resto, re.I) else '0'
        d = re.search(r"DEFAULT\s+\(?N?'?([^')\s]+)'?\)?", resto, re.I)
        ident = '1' if re.search(r'AUTO_INCREMENT|IDENTITY', resto, re.I) else '0'
        df = re.search(r'CONSTRAINT\s+(\w+)\s+DEFAULT', resto, re.I)
        desc = COLUMNAS.get(f'{tabla}.{col}', COLUMNAS.get(col))
        assert desc, f'falta la descripción de {tabla}.{col} en el perfil {perfil}'
        lineas.append(f'COL|{tabla}|{col}|{col}|{tipo}|{obligatoria}|{d.group(1) if d else ""}|{ident}|{desc}'
                      + (f'|{df.group(1)}' if df else ''))
    lineas += pend; pend = []
lineas += refs
assert len([l for l in lineas if l.startswith('TABLA')]) == len(TABLAS), 'faltan tablas en el script'
os.makedirs(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'specs', 'mer'), exist_ok=True)
salida = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'specs', 'mer', clave + '.txt')
open(salida, 'w', encoding='utf-8', newline='\r\n').write('\n'.join(lineas) + '\n')
print(salida)

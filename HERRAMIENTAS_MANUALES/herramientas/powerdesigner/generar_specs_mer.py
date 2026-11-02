import sys,re,os
"""Lee el script de creación de la BD (03. BDD/init/01_crea_bd.sql, MySQL o SQL Server) y genera
specs/mer/<clave>.txt para mer_detalle.vbs: tablas, columnas, claves, checks y referencias. Los tres modelos usan los
nombres del script; la descripción legible de cada tabla y columna va como comentario.
uso: generar_specs_mer.py <01_crea_bd.sql> <clave>

Formato (separado por |):
  TABLA|codigo|nombre|comentario|columna|fila
  COL|tabla|codigo|nombre|tipo|obligatoria(1/0)|default|identidad(1/0)|comentario|restriccion_default (SQL Server)
  PK|tabla|restriccion|col1,col2          UQ|tabla|restriccion|col1,col2
  LOV|tabla|columna|restriccion|valor1;valor2          CHK|tabla|columna|restriccion|expresion
  REF|codigo|hija|padre|cols_hija|cols_padre|relacion|cardinalidad"""

# nombre legible (solo para el comentario de la tabla), comentario y posición en el diagrama (columna, fila)
TABLAS = {
    'usuario':        ('Usuario', 'Credenciales de acceso del empleado (clave SHA-256).', 0, 0),
    'empleado':       ('Empleado', 'Empleado (cajero) que opera en ventanilla.', 1, 0),
    'asignado':       ('Asignado', 'Asignación vigente o histórica del empleado a una sucursal.', 2, 0),
    'sucursal':       ('Sucursal', 'Sucursal del banco.', 3, 0),
    'logsession':     ('Sesión', 'Registro de ingreso (RF-01) y salida (RF-02) de cada sesión.', 0, 1),
    'movimiento':     ('Movimiento', 'Movimiento de una cuenta: depósito, retiro o transferencia.', 1, 1),
    'cuenta':         ('Cuenta', 'Cuenta bancaria del cliente.', 2, 1),
    'moneda':         ('Moneda', 'Catálogo de monedas.', 3, 1),
    'tipomovimiento': ('Tipo de movimiento', 'Catálogo de tipos de movimiento (INGRESO o SALIDA).', 1, 2),
    'cliente':        ('Cliente', 'Titular de las cuentas.', 2, 2),
}
# descripción de cada columna (comentario en los modelos)
COLUMNAS = {
    'chr_monecodigo': 'Código de moneda', 'vch_monedescripcion': 'Descripción de moneda',
    'chr_tipocodigo': 'Código de tipo', 'vch_tipodescripcion': 'Descripción de tipo', 'vch_tipoaccion': 'Acción',
    'vch_tipoestado': 'Estado de tipo',
    'chr_sucucodigo': 'Código de sucursal', 'vch_sucunombre': 'Nombre de sucursal', 'vch_sucuciudad': 'Ciudad de sucursal',
    'vch_sucudireccion': 'Dirección de sucursal', 'int_sucucontcuenta': 'Contador de cuentas',
    'chr_emplcodigo': 'Código de empleado', 'vch_emplpaterno': 'Apellido paterno de empleado',
    'vch_emplmaterno': 'Apellido materno de empleado', 'vch_emplnombre': 'Nombre de empleado',
    'vch_emplciudad': 'Ciudad de empleado', 'vch_empldireccion': 'Dirección de empleado',
    'vch_emplusuario': 'Usuario', 'vch_emplclave': 'Clave', 'vch_emplestado': 'Estado de usuario',
    'chr_asigcodigo': 'Código de asignación', 'dtt_asigfechaalta': 'Fecha de alta', 'dtt_asigfechabaja': 'Fecha de baja',
    'int_logid': 'Id de sesión', 'dtt_logingreso': 'Fecha de ingreso', 'dtt_logsalida': 'Fecha de salida',
    'vch_logip': 'Dirección IP', 'vch_loghostname': 'Nombre del equipo',
    'chr_cliecodigo': 'Código de cliente', 'vch_cliepaterno': 'Apellido paterno de cliente',
    'vch_cliematerno': 'Apellido materno de cliente', 'vch_clienombre': 'Nombre de cliente', 'chr_cliedni': 'DNI',
    'vch_clieciudad': 'Ciudad de cliente', 'vch_cliedireccion': 'Dirección de cliente', 'vch_clietelefono': 'Teléfono',
    'vch_clieemail': 'Correo electrónico',
    'chr_cuencodigo': 'Número de cuenta', 'chr_emplcreacuenta': 'Empleado que crea la cuenta', 'dec_cuensaldo': 'Saldo',
    'dtt_cuenfechacreacion': 'Fecha de creación', 'vch_cuenestado': 'Estado de cuenta',
    'int_cuencontmov': 'Contador de movimientos', 'chr_cuenclave': 'Clave de cuenta',
    'int_movinumero': 'Número de movimiento', 'dtt_movifecha': 'Fecha de movimiento', 'dec_moviimporte': 'Importe',
    'chr_cuenreferencia': 'Cuenta de referencia',
}
# relación del modelo conceptual/lógico para cada FK: nombre (verbo leído del padre a la hija) y cardinalidad
RELACIONES = {
    'fk_usuario_empleado':      ('Tiene', '0..1'),
    'fk_asignado_empleado':     ('Es asignado', '0..*'),
    'fk_asignado_sucursal':     ('Recibe', '0..*'),
    'fk_logsession_empleado':   ('Inicia', '0..*'),
    'fk_cuenta_moneda':         ('Denomina', '0..*'),
    'fk_cuenta_sucursal':       ('Administra', '0..*'),
    'fk_cuenta_empleado':       ('Crea', '0..*'),
    'fk_cuenta_cliente':        ('Posee', '0..*'),
    'fk_movimiento_cuenta':     ('Registra', '0..*'),
    'fk_movimiento_empleado':   ('Realiza', '0..*'),
    'fk_movimiento_tipo':       ('Clasifica', '0..*'),
    'fk_movimiento_referencia': ('Es referencia de', '0..*'),
}
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

sql_path, clave = sys.argv[1:3]
sql = open(sql_path, encoding='utf-8-sig').read()
sql = re.sub(r'--[^\n]*', '', sql)
lineas, refs, pend = [], [], []
for m in re.finditer(r'CREATE\s+TABLE\s+(?:\w+\.)?\[?(\w+)\]?\s*\((.*?)\)\s*(?:ENGINE[^;]*)?;', sql, re.S | re.I):
    tabla, cuerpo = m.group(1).lower(), m.group(2)
    nombre, coment, x, y = TABLAS[tabla]
    lineas.append(f'TABLA|{tabla}|{tabla}|{nombre}: {coment}|{x}|{y}')
    for p in partir(cuerpo):
        p = ' '.join(p.split())
        c = re.match(r'CONSTRAINT\s+(\w+)\s+(PRIMARY\s+KEY|UNIQUE|CHECK|FOREIGN\s+KEY)\s*(.*)', p, re.I)
        if c:
            nom, tipo, resto = c.group(1), c.group(2).upper(), c.group(3)
            if tipo.startswith('PRIMARY') or tipo == 'UNIQUE':
                k = re.match(r'(?:CLUSTERED\s+|NONCLUSTERED\s+)?\((.*?)\)', resto, re.I).group(1)
                pend.append(f'{"PK" if tipo.startswith("PRIMARY") else "UQ"}|{tabla}|{nom}|{cols(k)}')
            elif tipo == 'CHECK':
                expr = resto.strip()[1:-1].strip()
                lov = re.match(r'\[?(\w+)\]?\s+IN\s*\((.*)\)$', expr, re.I)
                if lov:
                    vals = ';'.join(v.strip().strip("'").replace("N'", '') for v in lov.group(2).split(','))
                    pend.append(f'LOV|{tabla}|{lov.group(1)}|{nom}|{vals}')
                else:
                    col = re.match(r'\[?(\w+)\]?', expr).group(1)
                    pend.append(f'CHK|{tabla}|{col}|{nom}|{expr}')
            else:
                f = re.match(r'\((.*?)\)\s*REFERENCES\s+(?:\w+\.)?\[?(\w+)\]?\s*\((.*?)\)', resto, re.I)
                rel, card = RELACIONES[nom]
                refs.append(f'REF|{nom}|{tabla}|{f.group(2).lower()}|{cols(f.group(1))}|{cols(f.group(3))}|{rel}|{card}')
            continue
        c = re.match(r'\[?(\w+)\]?\s+(\w+(?:\s*\([\d,\s]+\))?)\s*(.*)', p)
        col, tipo, resto = c.group(1), re.sub(r'\s+', '', c.group(2)).upper(), c.group(3)
        obligatoria = '1' if re.search(r'\bNOT\s+NULL\b', resto, re.I) else '0'
        d = re.search(r"DEFAULT\s+\(?N?'?([^')\s]+)'?\)?", resto, re.I)
        ident = '1' if re.search(r'AUTO_INCREMENT|IDENTITY', resto, re.I) else '0'
        df = re.search(r'CONSTRAINT\s+(\w+)\s+DEFAULT', resto, re.I)
        lineas.append(f'COL|{tabla}|{col}|{col}|{tipo}|{obligatoria}|{d.group(1) if d else ""}|{ident}|{COLUMNAS[col]}'
                      + (f'|{df.group(1)}' if df else ''))
    lineas += pend; pend = []
lineas += refs
assert len([l for l in lineas if l.startswith('TABLA')]) == len(TABLAS), 'faltan tablas en el script'
os.makedirs(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'specs', 'mer'), exist_ok=True)
salida = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'specs', 'mer', clave + '.txt')
open(salida, 'w', encoding='utf-8', newline='\r\n').write('\n'.join(lineas) + '\n')
print(salida)

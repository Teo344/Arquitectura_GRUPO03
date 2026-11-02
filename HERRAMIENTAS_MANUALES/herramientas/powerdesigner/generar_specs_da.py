import sys,os,re
"""Genera specs/actividad/<clave>/DA_*.txt (diagramas de actividad) a partir de una cuadrícula de filas y columnas.
Columnas: CLI (cliente), F (columna de errores del servidor), M (flujo principal del servidor), BD.
Los flujos que salen de una decisión van primero en horizontal; los demás, primero en vertical.
uso: generar_specs_da.py <clave> <servidor> <bd> [perfil]   p. ej. TG11 "Payara 7 / JAX-WS SOAP" MySQL
perfil: java (por defecto), dotnet, que traduce los textos a sus equivalentes de WCF y ADO.NET (TG.1.2), o rest, que
los traduce a Jakarta REST (TG.1.3): rutas HTTP bajo /api, token Bearer, filtro @Protegido y errores JSON 400/401/500."""
clave,servidor,bd=sys.argv[1:4]; perfil=sys.argv[4] if len(sys.argv)>4 else 'java'
# equivalencias .NET: métodos en PascalCase, inspector de mensajes WCF, SqlTransaction y bloqueo de SQL Server
DOTNET=[(re.compile(r'\b([A-Z]\w+)\.([a-z])'),lambda m:m.group(1)+'.'+m.group(2).upper()),
        (re.compile(r'ManejadorAutenticacion lee el token de la cabecera SOAP de \w+'),
         'ManejadorAutenticacion (IDispatchMessageInspector) lee el token de la cabecera SOAP'),
        ('Iniciar la transacción y bloquear','Iniciar la transacción (SqlTransaction) y bloquear'),
        ('FOR UPDATE','WITH (UPDLOCK, ROWLOCK)'),
        ('(commit)','(Commit)'),('(rollback)','(Rollback)'),
        ('Generar SOAP Fault con ErrorServicio','WCF genera el SOAP Fault con ErrorServicio'),
        ('Revertir la transacción (Rollback) y generar SOAP Fault con ErrorServicio',
         'Rollback de la transacción; WCF genera el SOAP Fault con ErrorServicio')]
# equivalencias REST: cada operación es una ruta HTTP; los errores salen de MapeadorExcepciones como JSON
RUTAS={'consultarCuenta(cuenta)':'GET /api/cuentas/{cuenta}',
       'consultarMovimientos(cuenta)':'GET /api/cuentas/{cuenta}/movimientos',
       'depositar(cuenta, importe)':'POST /api/transacciones/depositos',
       'retirar(cuenta, importe)':'POST /api/transacciones/retiros',
       'transferir(origen, destino, importe)':'POST /api/transacciones/transferencias'}
REST=[(re.compile(r'Enviar SOAP Request (\w+\([^)]*\)) con el token en la cabecera'),
       lambda m:f'Enviar {RUTAS[m.group(1)]} con Authorization: Bearer'),
      ('Enviar SOAP Request iniciarSesion(usuario, clave, equipo)','Enviar POST /api/autenticacion/login con JSON {usuario, clave, equipo}'),
      ('Enviar SOAP Request cerrarSesion con el token en la cabecera','Enviar POST /api/autenticacion/cerrar-sesion con Authorization: Bearer'),
      ('Recibir la petición en AutenticacionWS y obtener la IP del cliente','AutenticacionRecurso.login recibe las credenciales y la IP del cliente'),
      ('Recibir la petición en AutenticacionWS (ServicioAutenticacion)','AutenticacionRecurso.cerrarSesion lee el token Bearer'),
      (re.compile(r'ManejadorAutenticacion lee el token de la cabecera SOAP de \w+'),
       'ManejadorAutenticacion (filtro @Protegido) lee el token Bearer'),
      ('Devolver SOAP Response','Devolver HTTP 200 con JSON'),
      ('Generar SOAP Fault con ErrorServicio (código, mensaje)','MapeadorExcepciones responde JSON ErrorServicio (código, mensaje)'),
      ('Revertir la transacción (rollback) y generar SOAP Fault con ErrorServicio',
       'Rollback de la transacción; MapeadorExcepciones responde JSON ErrorServicio'),
      ('Enviar SOAP Request','Enviar petición HTTP')]
def http(codigo): return '401' if codigo.startswith('AUTH') else '500' if codigo.startswith('SYS') else '400'
X={'CLI':-21000,'F':-4500,'M':10000,'BD':30000}
CALLE={'CLI':'CLI','F':'SRV','M':'SRV','BD':'BD'}
TAM={('ACT','CLI'):(13000,3600),('ACT','M'):(15000,3600),('ACT','F'):(12000,3600),('ACT','BD'):(13000,3600),('DEC','M'):(18000,4400)}
y=lambda fila:12000-5300*fila
FALLA='Generar SOAP Fault con ErrorServicio (código, mensaje)'
FALLA_TX='Revertir la transacción (rollback) y generar SOAP Fault con ErrorServicio'

def spec(titulo,nodos,flujos):
    ult=max(n[3] for n in nodos)
    out=['# '+titulo,f'AREA|{y(0)+3000}|{y(ult)-2500}',
         'CALLE|CLI|Cliente (Consola / Escritorio / Web / Móvil)|-30000|-12000',
         f'CALLE|SRV|Servidor ({servidor})|-12000|22000',f'CALLE|BD|Base de datos ({bd})|22000|38000']
    pos={}
    for t,c,col,fila,*txt in nodos:
        pos[c]=(t,X[col],y(fila))
        if t in('INI','FIN'): out.append(f'{t}|{c}|{CALLE[col]}|{X[col]}|{y(fila)}')
        else:
            w,h=TAM[(t,col)]; out.append(f'{t}|{c}|{CALLE[col]}|{X[col]}|{y(fila)}|{w}|{h}|{txt[0]}')
    for o,d,*e in flujos:
        et=e[0] if e else ''
        to,xo,yo=pos[o]; _,xd,yd=pos[d]
        p=''
        if xo!=xd and yo!=yd: p=f'{xd},{yo}' if to=='DEC' else f'{xo},{yd}'
        out.append(f'FLU|{o}|{d}|{et}|{p}')
    texto='\n'.join(out)+'\n'
    for viejo,nuevo in {'dotnet':DOTNET,'rest':REST}.get(perfil,[]):
        texto=viejo.sub(nuevo,texto) if hasattr(viejo,'sub') else texto.replace(viejo,nuevo)
    if perfil=='rest':   # código HTTP de cada rama de error: [No: AUTH-003 · 401]
        texto=re.sub(r'(?m)^(FLU\|[^|]*\|[^|]*\|No: ([A-Z]+)-\d+[^|]*)',lambda m:f'{m.group(1)} · {http(m.group(2))}',texto)
    return texto

def protegido(op,ingreso,ws):
    "inicio común de las operaciones protegidas por ManejadorAutenticacion"
    return [('INI','INI','CLI',0),('ACT','C1','CLI',1,ingreso),
            ('ACT','C2','CLI',2,f'Enviar SOAP Request {op} con el token en la cabecera'),
            ('ACT','S1','M',2,f'ManejadorAutenticacion lee el token de la cabecera SOAP de {ws}'),
            ('DEC','D1','M',3,'¿Token válido y vigente? (GestorToken)')], \
           [('INI','C1'),('C1','C2'),('C2','S1'),('S1','D1'),('D1','F','No: AUTH-003')]

D={}
# ---------- ECUD1 Iniciar sesión
D['DA_Iniciar_Sesion']=('DA1: Iniciar sesión',[
 ('INI','INI','CLI',0),('ACT','C1','CLI',1,'Ingresar usuario y contraseña'),
 ('ACT','C2','CLI',2,'Enviar SOAP Request iniciarSesion(usuario, clave, equipo)'),
 ('ACT','S1','M',2,'Recibir la petición en AutenticacionWS y obtener la IP del cliente'),
 ('DEC','D1','M',3,'¿Usuario y clave completos?'),
 ('ACT','S2','M',4,'Calcular el hash SHA-256 de la clave (UtilHash) y buscar el usuario (UsuarioDao)'),
 ('ACT','B1','BD',4,'Consultar usuario, empleado y sucursal asignada'),
 ('DEC','D2','M',5,'¿Base de datos disponible?'),('DEC','D3','M',6,'¿Credenciales correctas?'),
 ('DEC','D4','M',7,'¿Usuario ACTIVO con sucursal vigente?'),
 ('ACT','S3','M',8,'Registrar el ingreso (LogSesionDao.registrarIngreso)'),
 ('ACT','B2','BD',8,'Insertar el ingreso en logsession (fecha, IP, equipo)'),
 ('ACT','S4','M',9,'Generar el token con GestorToken (vence tras 30 min sin uso)'),
 ('ACT','S5','M',10,'Devolver SOAP Response OK-000 con token, empleado y sucursal'),
 ('ACT','C3','CLI',10,'Guardar el token y mostrar el menú de operaciones'),('FIN','FIN1','CLI',11),
 ('ACT','F','F',8,FALLA),('ACT','C4','CLI',8,'Mostrar el mensaje de error'),('FIN','FIN2','CLI',9)],
 [('INI','C1'),('C1','C2'),('C2','S1'),('S1','D1'),('D1','S2','Sí'),('D1','F','No: DATA-001'),('S2','B1'),
  ('B1','D2'),('D2','D3','Sí'),('D2','F','No: SYS-001'),('D3','D4','Sí'),('D3','F','No: AUTH-001'),
  ('D4','S3','Sí'),('D4','F','No: AUTH-002'),('S3','B2'),('B2','S4'),('S4','S5'),('S5','C3'),('C3','FIN1'),
  ('F','C4'),('C4','FIN2')])
# ---------- ECUD2 Cerrar sesión
D['DA_Cerrar_Sesion']=('DA2: Cerrar sesión',[
 ('INI','INI','CLI',0),('ACT','C1','CLI',1,'Seleccionar cerrar sesión'),
 ('ACT','C2','CLI',2,'Enviar SOAP Request cerrarSesion con el token en la cabecera'),
 ('ACT','S1','M',2,'Recibir la petición en AutenticacionWS (ServicioAutenticacion)'),
 ('DEC','D1','M',3,'¿Token activo? (GestorToken.invalidar)'),
 ('ACT','S2','M',4,'Eliminar el token de las sesiones activas'),
 ('ACT','S3','M',5,'Registrar la salida (LogSesionDao.registrarSalida)'),
 ('ACT','B1','BD',5,'Actualizar logsession con la fecha de salida'),
 ('DEC','D2','M',6,'¿Base de datos disponible?'),
 ('ACT','S4','M',8,'Devolver SOAP Response OK-000 "Sesión cerrada"'),
 ('ACT','C3','CLI',9,'Borrar el token y mostrar el inicio de sesión'),('FIN','FIN1','CLI',10),
 ('ACT','F','F',7,FALLA),('ACT','C4','CLI',7,'Borrar el token local y volver al inicio de sesión'),('FIN','FIN2','CLI',8)],
 [('INI','C1'),('C1','C2'),('C2','S1'),('S1','D1'),('D1','S2','Sí'),('D1','F','No: AUTH-003'),('S2','S3'),
  ('S3','B1'),('B1','D2'),('D2','S4','Sí'),('D2','F','No: SYS-001'),('S4','C3'),('C3','FIN1'),('F','C4'),('C4','FIN2')])
# ---------- ECUD3 Consultar cuenta
n,f=protegido('consultarCuenta(cuenta)','Ingresar el número de cuenta','CuentaWS')
D['DA_Consultar_Cuenta']=('DA3: Consultar cuenta',n+[
 ('DEC','D2','M',4,'¿Cuenta de 8 dígitos? (ServicioValidacion)'),
 ('ACT','S2','M',5,'Buscar la cuenta (ServicioCuenta → CuentaDao.buscar)'),
 ('ACT','B1','BD',5,'Consultar cuenta con cliente, moneda y sucursal'),
 ('DEC','D3','M',6,'¿Base de datos disponible?'),('DEC','D4','M',7,'¿La cuenta existe?'),
 ('ACT','S3','M',8,'Convertir la cuenta en CuentaDto (RespuestaMapeador)'),
 ('ACT','S4','M',9,'Devolver SOAP Response OK-000 con los datos de la cuenta'),
 ('ACT','C3','CLI',10,'Mostrar cliente, saldo, moneda y estado de la cuenta'),('FIN','FIN1','CLI',11),
 ('ACT','F','F',8,FALLA),('ACT','C4','CLI',8,'Mostrar el mensaje de error'),('FIN','FIN2','CLI',9)],
 f+[('D1','D2','Sí'),('D2','S2','Sí'),('D2','F','No: DATA-001'),('S2','B1'),('B1','D3'),('D3','D4','Sí'),
  ('D3','F','No: SYS-001'),('D4','S3','Sí'),('D4','F','No: CTA-001'),('S3','S4'),('S4','C3'),('C3','FIN1'),
  ('F','C4'),('C4','FIN2')])
# ---------- ECUD4 Consultar movimientos
n,f=protegido('consultarMovimientos(cuenta)','Ingresar el número de cuenta','CuentaWS')
D['DA_Consultar_Movimientos']=('DA4: Consultar movimientos',n+[
 ('DEC','D2','M',4,'¿Cuenta de 8 dígitos? (ServicioValidacion)'),
 ('ACT','S2','M',5,'Verificar que la cuenta exista (CuentaDao.buscar)'),
 ('ACT','B1','BD',5,'Consultar la cuenta'),
 ('DEC','D3','M',6,'¿Base de datos disponible?'),('DEC','D4','M',7,'¿La cuenta existe?'),
 ('ACT','S3','M',8,'Listar los movimientos (MovimientoDao.listar)'),
 ('ACT','B2','BD',8,'Consultar movimientos con su tipo, ordenados por número'),
 ('ACT','S4','M',9,'Devolver SOAP Response OK-000 con la lista de MovimientoDto'),
 ('ACT','C3','CLI',10,'Mostrar la tabla de movimientos'),('FIN','FIN1','CLI',11),
 ('ACT','F','F',8,FALLA),('ACT','C4','CLI',8,'Mostrar el mensaje de error'),('FIN','FIN2','CLI',9)],
 f+[('D1','D2','Sí'),('D2','S2','Sí'),('D2','F','No: DATA-001'),('S2','B1'),('B1','D3'),('D3','D4','Sí'),
  ('D3','F','No: SYS-001'),('D4','S3','Sí'),('D4','F','No: CTA-001'),('S3','B2'),('B2','S4'),('S4','C3'),
  ('C3','FIN1'),('F','C4'),('C4','FIN2')])
# ---------- ECUD5 / ECUD6 Depósito y retiro (misma estructura transaccional)
def deposito_retiro(op,titulo,ingreso,dec6,err6,tipo,comprobante):
    n,f=protegido(op,ingreso,'TransaccionWS')
    return (titulo,n+[
     ('DEC','D2','M',4,'¿Cuenta de 8 dígitos? (ServicioValidacion)'),
     ('DEC','D3','M',5,'¿Importe mayor que cero y con 2 decimales como máximo?'),
     ('ACT','S2','M',6,'Iniciar la transacción y bloquear la cuenta (CuentaDao.bloquear)'),
     ('ACT','B1','BD',6,'SELECT … FOR UPDATE de la cuenta'),
     ('DEC','D4','M',7,'¿Base de datos disponible?'),('DEC','D5','M',8,'¿La cuenta existe y está ACTIVA?'),
     ('DEC','D6','M',9,dec6),
     ('ACT','S3','M',10,f'Actualizar saldo y contador y registrar el movimiento {tipo}'),
     ('ACT','B2','BD',10,'UPDATE cuenta e INSERT en movimiento'),
     ('ACT','S4','M',11,'Confirmar la transacción (commit)'),
     ('ACT','S5','M',12,'Devolver SOAP Response OK-000 con el nuevo saldo y el número de movimiento'),
     ('ACT','C3','CLI',12,comprobante),('FIN','FIN1','CLI',13),
     ('ACT','F','F',10,FALLA_TX),('ACT','C4','CLI',10,'Mostrar el mensaje de error'),('FIN','FIN2','CLI',11)],
     f+[('D1','D2','Sí'),('D2','D3','Sí'),('D2','F','No: DATA-001'),('D3','S2','Sí'),('D3','F','No: MOV-001'),
      ('S2','B1'),('B1','D4'),('D4','D5','Sí'),('D4','F','No: SYS-001'),('D5','D6','Sí'),
      ('D5','F','No: CTA-001 / CTA-002'),('D6','S3','Sí'),('D6','F','No: '+err6),('S3','B2'),('B2','S4'),
      ('S4','S5'),('S5','C3'),('C3','FIN1'),('F','C4'),('C4','FIN2')])
D['DA_Registrar_Deposito']=deposito_retiro('depositar(cuenta, importe)','DA5: Registrar depósito',
 'Ingresar la cuenta y el importe del depósito','¿El nuevo saldo no supera el máximo?','MOV-001','DEPÓSITO',
 'Mostrar el comprobante con el nuevo saldo')
D['DA_Registrar_Retiro']=deposito_retiro('retirar(cuenta, importe)','DA6: Registrar retiro',
 'Ingresar la cuenta y el importe del retiro','¿Saldo suficiente para el retiro?','MOV-002','RETIRO',
 'Mostrar el comprobante con el nuevo saldo')
# ---------- ECUD7 Transferencia
n,f=protegido('transferir(origen, destino, importe)','Ingresar cuenta de origen, cuenta de destino e importe','TransaccionWS')
D['DA_Registrar_Transferencia']=('DA7: Registrar transferencia',n+[
 ('DEC','D2','M',4,'¿Ambas cuentas de 8 dígitos? (ServicioValidacion)'),
 ('DEC','D3','M',5,'¿La cuenta de origen es distinta de la de destino?'),
 ('DEC','D4','M',6,'¿Importe mayor que cero y con 2 decimales como máximo?'),
 ('ACT','S2','M',7,'Iniciar la transacción y bloquear las dos cuentas en orden de código'),
 ('ACT','B1','BD',7,'SELECT … FOR UPDATE de las dos cuentas'),
 ('DEC','D5','M',8,'¿Base de datos disponible?'),('DEC','D6','M',9,'¿Ambas cuentas existen y están ACTIVAS?'),
 ('DEC','D7','M',10,'¿Las dos cuentas tienen la misma moneda?'),
 ('DEC','D8','M',11,'¿Saldo suficiente en la cuenta de origen?'),
 ('ACT','S3','M',12,'Actualizar saldos y contadores y registrar TRANSFERENCIA SALIDA e INGRESO'),
 ('ACT','B2','BD',12,'UPDATE de las dos cuentas e INSERT de los dos movimientos'),
 ('ACT','S4','M',13,'Confirmar la transacción (commit)'),
 ('ACT','S5','M',14,'Devolver SOAP Response OK-000 con el saldo de origen y los números de movimiento'),
 ('ACT','C3','CLI',14,'Mostrar el comprobante de la transferencia'),('FIN','FIN1','CLI',15),
 ('ACT','F','F',12,FALLA_TX),('ACT','C4','CLI',12,'Mostrar el mensaje de error'),('FIN','FIN2','CLI',13)],
 f+[('D1','D2','Sí'),('D2','D3','Sí'),('D2','F','No: DATA-001'),('D3','D4','Sí'),('D3','F','No: MOV-003'),
  ('D4','S2','Sí'),('D4','F','No: MOV-001'),('S2','B1'),('B1','D5'),('D5','D6','Sí'),('D5','F','No: SYS-001'),
  ('D6','D7','Sí'),('D6','F','No: CTA-001 / CTA-002'),('D7','D8','Sí'),('D7','F','No: MOV-004'),
  ('D8','S3','Sí'),('D8','F','No: MOV-002'),('S3','B2'),('B2','S4'),('S4','S5'),('S5','C3'),('C3','FIN1'),
  ('F','C4'),('C4','FIN2')])

carpeta=os.path.join(os.path.dirname(os.path.abspath(__file__)),'specs','actividad',clave)
os.makedirs(carpeta,exist_ok=True)
for nombre,(titulo,nodos,flujos) in D.items():
    open(os.path.join(carpeta,nombre+'.txt'),'w',encoding='utf8').write(spec(titulo,nodos,flujos))
    print(nombre)

import sys,os,re
"""Genera specs/secuencia/<clave>/DS_*.txt (diagramas de secuencia) para ds_detalle.vbs.
Cada diagrama es una lista de participantes (de izquierda a derecha) y de mensajes en orden; las posiciones se calculan.
Mensajes: (origen, destino, texto) es una llamada; con 'R' al final es un retorno; origen = destino es reflexivo.
uso: generar_specs_ds.py <clave> [perfil]      p. ej. TG11 (textos de SOAP Java)
perfil: java (por defecto), dotnet, que traduce los mensajes del servidor a WCF y ADO.NET (TG.1.2), o rest, que los
traduce a Jakarta REST (TG.1.3): rutas HTTP bajo /api, token Bearer, filtro @Protegido y errores JSON 400/401/500."""
clave=sys.argv[1]; perfil=sys.argv[2] if len(sys.argv)>2 else 'java'
SEP=19000; X0=-31000; Y0=31000; PASO=3600; PASO_REF=4400

# equivalencias .NET (se aplican en este orden a todos los mensajes)
DOTNET=[('leerToken(cabecera SOAP)','AfterReceiveRequest: LeerToken(request.Headers)'),
        ('guarda la Sesion en el contexto del mensaje','guarda la Sesion en request.Properties'),
        ('obtiene la Sesion del contexto','SesionActual(): lee la Sesion de OperationContext'),
        ('setAutoCommit(false)','BeginTransaction(ReadCommitted)'),
        ('FOR UPDATE','WITH (UPDLOCK, ROWLOCK)'),
        ('DriverManager.getConnection(url)','SqlConnection.Open()'),
        ('SQLException','SqlException'),('registra el error en el log','Trace.TraceError(...)'),
        ('Optional<Empleado>','Empleado'),('Optional<Cuenta>','Cuenta'),('Optional vacío','null'),
        ('SOAPFaultException con ErrorServicio','FaultException<ErrorServicio>'),
        ('crearFault(ErrorServicio)','WCF convierte la ExcepcionServicio en SOAP Fault'),
        ('throw ExcepcionServicio','throw new ExcepcionServicio'),
        ('restar: verifica','Restar(): verifica'),
        ('TRANSFERENCIA_SALIDA','TransferenciaSalida'),('TRANSFERENCIA_INGRESO','TransferenciaIngreso'),
        ('DEPOSITO','Deposito'),('RETIRO','Retiro')]
# equivalencias REST: cada operación es una ruta HTTP con su cuerpo JSON; los errores salen como JSON ErrorServicio
RUTAS={'consultarCuenta(cuenta)':'GET /api/cuentas/{cuenta}',
       'consultarMovimientos(cuenta)':'GET /api/cuentas/{cuenta}/movimientos',
       'depositar(cuenta, importe)':'POST /api/transacciones/depositos {cuenta, importe}',
       'retirar(cuenta, importe)':'POST /api/transacciones/retiros {cuenta, importe}',
       'transferir(origen, destino, importe)':'POST /api/transacciones/transferencias {cuentaOrigen, cuentaDestino, importe}'}
DTO_RECURSO={'depositar(cuenta, importe)':'depositar(SolicitudTransaccionDto)',
             'retirar(cuenta, importe)':'retirar(SolicitudTransaccionDto)',
             'transferir(origen, destino, importe)':'transferir(SolicitudTransferenciaDto)'}
REST=[('SOAP Request iniciarSesion(usuario, clave, equipo)','POST /api/autenticacion/login {usuario, clave, equipo}'),
      ('SOAP Request cerrarSesion (token en la cabecera)','POST /api/autenticacion/cerrar-sesion (Authorization: Bearer)'),
      ('leerToken(cabecera SOAP)','filter(): leerToken(Authorization: Bearer)'),
      ('guarda la Sesion en el contexto del mensaje','setProperty("sesion", Sesion) en la petición'),
      ('obtiene la Sesion del contexto','sesion(): lee la Sesion de la petición'),
      ('crearFault(ErrorServicio)','abortWith(MapeadorExcepciones.respuesta(e))'),
      ('SOAPFaultException con ErrorServicio','respuesta HTTP de error con ErrorServicio'),
      ('SOAP Response RespuestaDto','HTTP 200 JSON RespuestaDto')]
def http(codigo): return '401' if codigo.startswith('AUTH') else '500' if codigo.startswith('SYS') else '400'
RENOMBRE={'dotnet':{'BD_MYSQL':'BD_SQLSERVER'},
          'rest':{'AUTENTICACION_WS':'AUTENTICACION_RECURSO','CUENTA_WS':'CUENTA_RECURSO',
                  'TRANSACCION_WS':'TRANSACCION_RECURSO','CLIENTE_WS_EUREKA':'CLIENTE_REST_EUREKA'}}
def es_cliente(p): return p in ('EMPLEADO','CLIENTE_WS_EUREKA') or p.endswith(('_VISTA','_CONTROLADOR'))
def traducir(o,d,t,f):
    """en .NET los métodos del servidor van en PascalCase; las operaciones SOAP (del contrato) y el cliente no cambian.
    En REST las peticiones son rutas HTTP y el filtro entrega al recurso el DTO de la solicitud."""
    if perfil=='dotnet':
        for viejo,nuevo in DOTNET: t=t.replace(viejo,nuevo)
        if f!='R' and not es_cliente(d) and not t.startswith('SOAP '):
            t=re.sub(r'\b([a-z])(\w*)(?=\()',lambda m:m.group(1).upper()+m.group(2),t)
    elif perfil=='rest':
        for viejo,nuevo in REST: t=t.replace(viejo,nuevo)
        t=re.sub(r'^SOAP Request (\w+\([^)]*\)) \(token en la cabecera\)',lambda m:RUTAS[m.group(1)]+' (Authorization: Bearer)',t)
        if o=='MANEJADOR_AUTENTICACION' and t in DTO_RECURSO: t=DTO_RECURSO[t]
        t=re.sub(r'^SOAP Fault ErrorServicio\(([A-Z]+)',lambda m:f'HTTP {http(m.group(1))} JSON ErrorServicio({m.group(1)}',t)
        if t.startswith('HTTP') and 'ErrorServicio' in t and o!='MANEJADOR_AUTENTICACION': t+=' vía MapeadorExcepciones'
    return t
def nombre_parte(p): return RENOMBRE.get(perfil,{}).get(p,p)

def spec(titulo,partes,mensajes):
    out=['# '+titulo]; x={}; ancho={}
    mensajes=[(nombre_parte(m[0]),nombre_parte(m[1]),traducir(m[0],m[1],m[2],m[3] if len(m)>3 else 'C'))+tuple(m[3:])
              for m in mensajes]
    partes=[nombre_parte(p) for p in partes]
    for i,p in enumerate(partes):
        x[p]=X0+i*SEP
        if p=='EMPLEADO': out.append(f'PART|{p}|Empleado|ACTOR|{x[p]}')
        else:
            ancho[p]=max(7000,560*len(p)+1500)
            out.append(f'PART|{p}|{p}|OBJ|{x[p]}|{ancho[p]}|{i-1}')
    y=Y0
    for m in mensajes:
        o,d,t=m[:3]; f=m[3] if len(m)>3 else 'C'
        assert o in x and d in x, (titulo,o,d)
        out.append(f'MSG|{o}|{d}|{t}|{f}|{y}')
        y-=PASO_REF if o==d else PASO
    p1=partes[1]; ult=partes[-1]
    out.append(f'MARCO|{x[p1]-ancho[p1]//2-3000}|40622|{x[ult]+26000}|{y-4000}')
    return '\n'.join(out)+'\n'

CWS='CLIENTE_WS_EUREKA'; MAN='MANEJADOR_AUTENTICACION'; GT='GESTOR_TOKEN'; BD='BD_MYSQL'
SV='SERVICIO_VALIDACION'; CD='CUENTA_DAO'; MD='MOVIMIENTO_DAO'

def peticion(ws,op):
    "el cliente envía la operación protegida y ManejadorAutenticacion valida el token antes de llegar al servicio web"
    return [(CWS,MAN,f'SOAP Request {op} (token en la cabecera)'),
            (MAN,MAN,'leerToken(cabecera SOAP)'),
            (MAN,GT,'validar(token)'),
            (GT,GT,'verifica que exista y no haya vencido; renueva el plazo'),
            (GT,MAN,'Sesion','R'),
            (MAN,MAN,'guarda la Sesion en el contexto del mensaje'),
            (MAN,ws,f'{op}')]

def respuesta(ws,mapeo,dto,vista,ctrl,final,guardar=None):
    "respuesta exitosa desde el servicio web hasta el empleado"
    r=[(ws,ws,f'RespuestaMapeador.{mapeo}'),
       (ws,CWS,f'SOAP Response RespuestaDto(OK-000, {dto})','R'),
       (CWS,ctrl,'RespuestaDto','R')]
    if guardar: r.append((ctrl,ctrl,guardar))
    return r+[(ctrl,vista,dto,'R'),(vista,'EMPLEADO',final,'R')]

def falla(origen,ws,codigo,mensaje,vista,ctrl,extra=None):
    "error: ExcepcionServicio convertida en SOAP Fault con ErrorServicio(codigo, mensaje)"
    r=[(origen,origen,f'throw ExcepcionServicio({codigo})')]
    if origen!=ws: r.append((origen,ws,f'ExcepcionServicio {codigo}','R'))
    r+=[(ws,CWS,f'SOAP Fault ErrorServicio({codigo}, "{mensaje}")','R'),
        (CWS,ctrl,'SOAPFaultException con ErrorServicio','R')]
    if extra: r.append((ctrl,ctrl,extra))
    return r+[(ctrl,vista,f'error {codigo}','R'),(vista,'EMPLEADO',f'muestra "{mensaje}"','R')]

D={}
# ---------- casos principales
LV,LC,AWS,SA,UD,LD='LOGIN_VISTA','LOGIN_CONTROLADOR','AUTENTICACION_WS','SERVICIO_AUTENTICACION','USUARIO_DAO','LOG_SESION_DAO'
inicio_login=[('EMPLEADO',LV,'ingresa usuario y contraseña'),
 (LV,LC,'iniciarSesion(usuario, clave)'),
 (LC,CWS,'iniciarSesion(usuario, clave, equipo)'),
 (CWS,AWS,'SOAP Request iniciarSesion(usuario, clave, equipo)'),
 (AWS,SA,'iniciarSesion(usuario, clave, ip, equipo)'),
 (SA,SA,'valida que usuario y clave no estén vacíos'),
 (SA,UD,'buscar(cn, usuario, sha256(clave))'),
 (UD,BD,'SELECT usuario JOIN empleado, asignado y sucursal'),
 (BD,UD,'fila del empleado','R'),
 (UD,SA,'Optional<Empleado>','R')]
D['DS_01_INICIAR_SESION']=('DS01: Iniciar sesión',
 ['EMPLEADO',LV,LC,CWS,AWS,SA,UD,LD,GT,BD],
 inicio_login+[(SA,SA,'verifica estado ACTIVO y sucursal asignada'),
 (SA,LD,'registrarIngreso(cn, empleado, ip, equipo)'),
 (LD,BD,'INSERT INTO logsession'),
 (BD,LD,'id generado','R'),(LD,SA,'logId','R'),
 (SA,GT,'generar(sesion)'),
 (GT,GT,'crea token UUID que vence tras 30 min sin uso'),
 (GT,SA,'TokenEmitido(token, expiraEn)','R'),
 (SA,AWS,'SesionIniciada(token, sesion)','R')]+
 respuesta(AWS,'deSesion(iniciada)','token, expiraEn, empleado, sucursal',LV,LC,'muestra el menú de operaciones','guarda el token de sesión'))

MV='MENU_VISTA'
D['DS_02_CERRAR_SESION']=('DS02: Cerrar sesión',
 ['EMPLEADO',MV,LC,CWS,AWS,SA,GT,LD,BD],
 [('EMPLEADO',MV,'selecciona cerrar sesión'),(MV,LC,'cerrarSesion()'),(LC,CWS,'cerrarSesion(token)'),
 (CWS,AWS,'SOAP Request cerrarSesion (token en la cabecera)'),(AWS,SA,'cerrarSesion(token)'),
 (SA,GT,'invalidar(token)'),(GT,GT,'elimina el token de las sesiones activas'),(GT,SA,'Sesion','R'),
 (SA,LD,'registrarSalida(cn, logId, ahora)'),(LD,BD,'UPDATE logsession (fecha de salida)'),
 (BD,LD,'fila actualizada','R'),(LD,SA,'salida registrada','R'),(SA,AWS,'sesión cerrada','R')]+
 respuesta(AWS,'deCierreSesion()','"Sesión cerrada"',MV,LC,'muestra el inicio de sesión','borra el token de sesión'))

CV,CC,CWSV,SC='CUENTA_VISTA','CUENTA_CONTROLADOR','CUENTA_WS','SERVICIO_CUENTA'
inicio_cuenta=lambda op,metodo:[('EMPLEADO',CV,'ingresa el número de cuenta'),(CV,CC,f'{metodo}(cuenta)'),
 (CC,CWS,f'{metodo}(token, cuenta)')]+peticion(CWSV,op)+[
 (CWSV,SC,f'{metodo}(cuenta)'),(SC,SV,'validarCuenta(cuenta)'),(SV,SV,'verifica 8 dígitos'),(SV,SC,'código','R'),
 (SC,CD,'buscar(cn, código)'),(CD,BD,'SELECT cuenta JOIN cliente, moneda y sucursal')]
D['DS_03_CONSULTAR_CUENTA']=('DS03: Consultar cuenta',
 ['EMPLEADO',CV,CC,CWS,MAN,GT,CWSV,SC,SV,CD,BD],
 inicio_cuenta('consultarCuenta(cuenta)','consultarCuenta')+[(BD,CD,'fila de la cuenta','R'),
 (CD,SC,'Optional<Cuenta>','R'),(SC,CWSV,'Cuenta','R')]+
 respuesta(CWSV,'deCuenta(cuenta)','CuentaDto',CV,CC,'muestra cliente, saldo, moneda y estado'))

D['DS_04_CONSULTAR_MOVIMIENTOS']=('DS04: Consultar movimientos',
 ['EMPLEADO',CV,CC,CWS,MAN,GT,CWSV,SC,SV,CD,MD,BD],
 inicio_cuenta('consultarMovimientos(cuenta)','consultarMovimientos')+[(BD,CD,'fila de la cuenta','R'),
 (CD,SC,'Optional<Cuenta>','R'),(SC,MD,'listar(cn, código)'),
 (MD,BD,'SELECT movimiento JOIN tipomovimiento ORDER BY número'),(BD,MD,'filas de movimientos','R'),
 (MD,SC,'List<Movimiento>','R'),(SC,CWSV,'List<Movimiento>','R')]+
 respuesta(CWSV,'deMovimientos(lista)','lista de MovimientoDto',CV,CC,'muestra la tabla de movimientos'))

TV,TC,TWS,ST='TRANSACCION_VISTA','TRANSACCION_CONTROLADOR','TRANSACCION_WS','SERVICIO_TRANSACCION'
P_TX=['EMPLEADO',TV,TC,CWS,MAN,GT,TWS,ST,SV,CD,MD,BD]
def inicio_tx(metodo,args,ingreso):
    return [('EMPLEADO',TV,ingreso),(TV,TC,f'{metodo}({args})'),(TC,CWS,f'{metodo}(token, {args})')]+\
           peticion(TWS,f'{metodo}({args})')+[(TWS,TWS,'obtiene la Sesion del contexto'),
           (TWS,ST,f'{metodo}(sesion, {args})')]
validar_una=[(ST,SV,'validarCuenta(cuenta)'),(SV,ST,'código','R'),(ST,SV,'validarImporte(importe)'),
 (SV,ST,'importe válido','R'),(ST,BD,'setAutoCommit(false)'),(ST,CD,'bloquear(cn, código)'),
 (CD,BD,'SELECT … FOR UPDATE'),(BD,CD,'fila bloqueada','R'),(CD,ST,'Cuenta','R'),(ST,ST,'exige cuenta ACTIVA')]
def registrar(tipo,ref=''):
    return [(ST,CD,'actualizar(cn, código, nuevoSaldo, contador + 1)'),(CD,BD,'UPDATE cuenta'),
            (ST,MD,f'insertar(cn, código, número, empleado, {tipo}, importe{ref})'),(MD,BD,'INSERT INTO movimiento')]
D['DS_05_REGISTRAR_DEPOSITO']=('DS05: Registrar depósito',P_TX,
 inicio_tx('depositar','cuenta, importe','ingresa la cuenta y el importe del depósito')+validar_una+
 [(ST,SV,'validarMaximo(saldo + importe)'),(SV,ST,'saldo válido','R')]+registrar('DEPOSITO')+
 [(ST,BD,'commit()'),(ST,TWS,'ResultadoTransaccion(nuevoSaldo, número)','R')]+
 respuesta(TWS,'deDeposito(resultado)','nuevo saldo y número de movimiento',TV,TC,'muestra el comprobante del depósito'))
D['DS_06_REGISTRAR_RETIRO']=('DS06: Registrar retiro',P_TX,
 inicio_tx('retirar','cuenta, importe','ingresa la cuenta y el importe del retiro')+validar_una+
 [(ST,ST,'restar: verifica saldo suficiente')]+registrar('RETIRO')+
 [(ST,BD,'commit()'),(ST,TWS,'ResultadoTransaccion(nuevoSaldo, número)','R')]+
 respuesta(TWS,'deRetiro(resultado)','nuevo saldo y número de movimiento',TV,TC,'muestra el comprobante del retiro'))
D['DS_07_REGISTRAR_TRANSFERENCIA']=('DS07: Registrar transferencia',P_TX,
 inicio_tx('transferir','origen, destino, importe','ingresa cuenta de origen, cuenta de destino e importe')+
 [(ST,SV,'validarCuenta(origen) y validarCuenta(destino)'),(SV,ST,'códigos','R'),
  (ST,ST,'verifica que origen y destino sean distintas'),(ST,SV,'validarImporte(importe)'),(SV,ST,'importe válido','R'),
  (ST,BD,'setAutoCommit(false)'),(ST,CD,'bloquear(cn, primera cuenta en orden de código)'),
  (CD,BD,'SELECT … FOR UPDATE'),(BD,CD,'fila bloqueada','R'),(CD,ST,'Cuenta','R'),
  (ST,CD,'bloquear(cn, segunda cuenta)'),(CD,BD,'SELECT … FOR UPDATE'),(BD,CD,'fila bloqueada','R'),(CD,ST,'Cuenta','R'),
  (ST,ST,'exige cuentas ACTIVAS y misma moneda'),(ST,ST,'restar: verifica saldo suficiente en origen'),
  (ST,SV,'validarMaximo(saldo destino + importe)'),(SV,ST,'saldo válido','R'),
  (ST,CD,'actualizar(cn, origen, saldoOrigen, contador + 1)'),(CD,BD,'UPDATE cuenta origen'),
  (ST,MD,'insertar(origen, TRANSFERENCIA_SALIDA, referencia destino)'),(MD,BD,'INSERT INTO movimiento'),
  (ST,CD,'actualizar(cn, destino, saldoDestino, contador + 1)'),(CD,BD,'UPDATE cuenta destino'),
  (ST,MD,'insertar(destino, TRANSFERENCIA_INGRESO, referencia origen)'),(MD,BD,'INSERT INTO movimiento'),
  (ST,BD,'commit()'),(ST,TWS,'ResultadoTransaccion(saldoOrigen, número, número destino)','R')]+
 respuesta(TWS,'deTransferencia(resultado)','saldo de origen y números de movimiento',TV,TC,'muestra el comprobante de la transferencia'))

# ---------- errores (casos extendidos ECUD11-ECUD16)
D['DS_08_ERROR_CREDENCIALES']=('DS08: Error de credenciales (AUTH-001)',
 ['EMPLEADO',LV,LC,CWS,AWS,SA,UD,BD],
 inicio_login[:-2]+[(BD,UD,'sin filas','R'),(UD,SA,'Optional vacío','R')]+
 falla(SA,AWS,'AUTH-001','Usuario o contraseña incorrectos',LV,LC))
D['DS_09_ERROR_SESION_INVALIDA']=('DS09: Error de sesión inválida o expirada (AUTH-003)',
 ['EMPLEADO',CV,CC,CWS,MAN,GT],
 [('EMPLEADO',CV,'ingresa el número de cuenta'),(CV,CC,'consultarCuenta(cuenta)'),(CC,CWS,'consultarCuenta(token, cuenta)'),
  (CWS,MAN,'SOAP Request consultarCuenta(cuenta) (token en la cabecera)'),(MAN,MAN,'leerToken(cabecera SOAP)'),
  (MAN,GT,'validar(token)'),(GT,GT,'token desconocido o vencido: throw ExcepcionServicio(AUTH-003)'),
  (GT,MAN,'ExcepcionServicio AUTH-003','R'),(MAN,MAN,'crearFault(ErrorServicio)'),
  (MAN,CWS,'SOAP Fault ErrorServicio(AUTH-003, "Sesión inválida o expirada; inicie sesión nuevamente")','R'),
  (CWS,CC,'SOAPFaultException con ErrorServicio','R'),(CC,CC,'borra el token de sesión'),(CC,CV,'error AUTH-003','R'),
  (CV,'EMPLEADO','muestra el mensaje y el inicio de sesión','R')])
D['DS_10_ERROR_CUENTA_INEXISTENTE']=('DS10: Error de cuenta inexistente (CTA-001)',
 ['EMPLEADO',CV,CC,CWS,MAN,GT,CWSV,SC,SV,CD,BD],
 inicio_cuenta('consultarCuenta(cuenta)','consultarCuenta')+[(BD,CD,'sin filas','R'),(CD,SC,'Optional vacío','R')]+
 falla(SC,CWSV,'CTA-001','La cuenta no existe',CV,CC))
D['DS_11_ERROR_FONDOS_INSUFICIENTES']=('DS11: Error de fondos insuficientes (MOV-002)',
 ['EMPLEADO',TV,TC,CWS,MAN,GT,TWS,ST,SV,CD,BD],
 inicio_tx('retirar','cuenta, importe','ingresa la cuenta y el importe del retiro')+validar_una+
 [(ST,ST,'saldo < importe: throw ExcepcionServicio(MOV-002)'),(ST,BD,'rollback()'),
  (ST,TWS,'ExcepcionServicio MOV-002','R'),
  (TWS,CWS,'SOAP Fault ErrorServicio(MOV-002, "Saldo insuficiente para realizar la operación")','R'),
  (CWS,TC,'SOAPFaultException con ErrorServicio','R'),(TC,TV,'error MOV-002','R'),
  (TV,'EMPLEADO','muestra "Saldo insuficiente para realizar la operación"','R')])
D['DS_12_ERROR_TRANSFERENCIA_INVALIDA']=('DS12: Error de transferencia inválida (MOV-003)',
 ['EMPLEADO',TV,TC,CWS,MAN,GT,TWS,ST,SV],
 inicio_tx('transferir','origen, destino, importe','ingresa la misma cuenta como origen y destino')+
 [(ST,SV,'validarCuenta(origen) y validarCuenta(destino)'),(SV,ST,'códigos','R')]+
 falla(ST,TWS,'MOV-003','La cuenta de destino debe ser diferente de la cuenta de origen',TV,TC))
CX='CONEXION_BD'
D['DS_13_ERROR_BASE_DATOS']=('DS13: Error de base de datos (SYS-001)',
 ['EMPLEADO',CV,CC,CWS,MAN,GT,CWSV,SC,SV,CX,BD],
 [('EMPLEADO',CV,'ingresa el número de cuenta'),(CV,CC,'consultarCuenta(cuenta)'),(CC,CWS,'consultarCuenta(token, cuenta)')]+
 peticion(CWSV,'consultarCuenta(cuenta)')+
 [(CWSV,SC,'consultarCuenta(cuenta)'),(SC,SV,'validarCuenta(cuenta)'),(SV,SC,'código','R'),
  (SC,CX,'obtenerConexion()'),(CX,BD,'DriverManager.getConnection(url)'),(BD,CX,'SQLException','R'),
  (CX,CX,'registra el error en el log'),(CX,SC,'ExcepcionServicio SYS-001','R'),(SC,CWSV,'ExcepcionServicio SYS-001','R'),
  (CWSV,CWS,'SOAP Fault ErrorServicio(SYS-001, "No se pudo completar la operación")','R'),
  (CWS,CC,'SOAPFaultException con ErrorServicio','R'),(CC,CV,'error SYS-001','R'),
  (CV,'EMPLEADO','muestra "No se pudo completar la operación; servicio o base de datos no disponible"','R')])

carpeta=os.path.join(os.path.dirname(os.path.abspath(__file__)),'specs','secuencia',clave)
os.makedirs(carpeta,exist_ok=True)
for nombre,(titulo,partes,mensajes) in D.items():
    open(os.path.join(carpeta,nombre+'.txt'),'w',encoding='utf8').write(spec(titulo,partes,mensajes))
    print(nombre)

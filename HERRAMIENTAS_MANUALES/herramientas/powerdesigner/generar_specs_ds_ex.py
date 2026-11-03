import os
"""Genera specs/secuencia/EX11/DS_*.txt (diagramas de secuencia del EX.1.1) para ds_detalle.vbs, a partir del ECUD de
EX.1.1 y del código de los dos servidores. Mismo formato que generar_specs_ds.py:
- DS01–DS12: casos de la Comercializadora (actor Empleado). El Core es una sola línea de vida, CORE_BANQUITO, a la que
  llama PASARELA_BANQUITO con X-Api-Key; su interior está en DS13–DS16.
- DS13–DS16: servicios del Core BanQuito (actor Comercializadora).
- DS17–DS21: errores de los flujos alternativos (token inválido, clave de API, Core sin respuesta, comprador que no es
  sujeto de crédito y falla de la base de datos).
Mensajes: (origen, destino, texto) es una llamada; con 'R' al final es un retorno; origen = destino es reflexivo.
uso: generar_specs_ds_ex.py"""
SEP=19000; X0=-31000; Y0=31000; PASO=3600; PASO_REF=4400
ACTORES={'EMPLEADO':'Empleado (vendedor)','COMERCIALIZADORA':'Comercializadora'}

def spec(titulo,partes,mensajes):
    out=['# '+titulo]; x={}; ancho={}
    for i,p in enumerate(partes):
        x[p]=X0+i*SEP
        if p in ACTORES: out.append(f'PART|{p}|{ACTORES[p]}|ACTOR|{x[p]}')
        else:
            ancho[p]=max(7000,560*len(p)+1500)
            out.append(f'PART|{p}|{p}|OBJ|{x[p]}|{ancho[p]}|{i-1}')
    yy=Y0
    for m in mensajes:
        o,d,t=m[:3]; f=m[3] if len(m)>3 else 'C'
        assert o in x and d in x, (titulo,o,d)
        out.append(f'MSG|{o}|{d}|{t}|{f}|{yy}')
        yy-=PASO_REF if o==d else PASO
    p1=partes[1]; ult=partes[-1]
    out.append(f'MARCO|{x[p1]-ancho[p1]//2-3000}|40622|{x[ult]+26000}|{yy-4000}')
    return '\n'.join(out)+'\n'

E='EMPLEADO'; CRC='CLIENTE_REST_COMERCIALIZADORA'; MAN='MANEJADOR_AUTENTICACION'; GT='GESTOR_TOKEN'
BDC='BD_COMERCIALIZADORA'; PB='PASARELA_BANQUITO'; CORE='CORE_BANQUITO'
COM='COMERCIALIZADORA'; FCA='FILTRO_CLAVE_API'; SCR='SERVICIO_CREDITO'; SV='SERVICIO_VALIDACION'
CLD='CLIENTE_DAO'; MVD='MOVIMIENTO_DAO'; CRD='CREDITO_DAO'; CUD='CUOTA_DAO'; BDB='BD_BANQUITO'

def peticion(recurso,http,metodo):
    "petición protegida: ManejadorAutenticacion (filtro @Protegido) valida el token Bearer antes del recurso (ECUD17)"
    return [(CRC,MAN,f'{http} (Authorization: Bearer)'),(MAN,MAN,'filter(): leerToken(Authorization)'),
            (MAN,GT,'validar(token)'),(GT,GT,'verifica que exista y no haya vencido; renueva el plazo'),
            (GT,MAN,'Sesion','R'),(MAN,MAN,'setProperty("sesion", Sesion)'),(MAN,recurso,metodo)]
def respuesta(recurso,mapeo,http,dto,vista,ctrl,final):
    return [(recurso,recurso,mapeo),(recurso,CRC,f'{http} {dto}','R'),(CRC,ctrl,dto,'R'),(ctrl,vista,dto,'R'),
            (vista,E,final,'R')]
def core(servicio,metodo,tipo_local,ruta,dto_core):
    "llamada de la Comercializadora al Core por la pasarela (HttpClient, X-Api-Key, 5 s / 10 s)"
    return [(servicio,PB,metodo),(PB,CORE,f'{ruta} (X-Api-Key)'),(CORE,PB,dto_core,'R'),(PB,servicio,tipo_local,'R')]
def falla(origen,recurso,codigo,http,mensaje,vista,ctrl,extra=None):
    r=[(origen,origen,f'throw ExcepcionServicio({codigo})')]
    if origen!=recurso: r.append((origen,recurso,f'ExcepcionServicio {codigo}','R'))
    r+=[(recurso,CRC,f'HTTP {http} problem+json ({codigo}) vía MapeadorExcepciones','R'),
        (CRC,ctrl,f'problema {codigo}','R')]
    if extra: r.append((ctrl,ctrl,extra))
    return r+[(ctrl,vista,f'error {codigo}','R'),(vista,E,f'muestra "{mensaje}"','R')]

D={}
# ---------------- Comercializadora
LV,LC,SR,SA,UD='LOGIN_VISTA','LOGIN_CONTROLADOR','SESION_RECURSO','SERVICIO_AUTENTICACION','USUARIO_DAO'
D['DS_01_INICIAR_SESION']=('DS01: Iniciar sesión',[E,LV,LC,CRC,SR,SA,UD,GT,BDC],[
 (E,LV,'ingresa usuario y clave'),(LV,LC,'iniciarSesion(usuario, clave)'),(LC,CRC,'iniciarSesion(usuario, clave)'),
 (CRC,SR,'POST /api/v1/sesiones {usuario, clave}'),(SR,SA,'iniciarSesion(usuario, clave)'),
 (SA,SA,'valida que usuario y clave no estén vacíos'),(SA,UD,'buscar(cn, usuario, sha256(clave))'),
 (UD,BDC,'SELECT USUARIO por usuario y clave'),(BDC,UD,'fila del usuario','R'),(UD,SA,'Optional<Usuario>','R'),
 (SA,SA,'verifica que el usuario esté ACTIVO'),(SA,GT,'generar(sesion)'),
 (GT,GT,'crea token UUID que vence tras 30 min sin uso'),(GT,SA,'TokenEmitido(token, expiraEn)','R'),
 (SA,SR,'SesionIniciada(token, sesion)','R')]+
 respuesta(SR,'Mapeador.deSesion(iniciada)','HTTP 201 JSON','SesionDto',LV,LC,'muestra el menú')[:1]+[
 (SR,CRC,'HTTP 201 JSON SesionDto(token, expiraEn, código, usuario, nombre)','R'),(CRC,LC,'SesionDto','R'),
 (LC,LC,'guarda el token de sesión'),(LC,LV,'sesión iniciada','R'),(LV,E,'muestra el menú','R')])
MV='MENU_VISTA'
D['DS_02_CERRAR_SESION']=('DS02: Cerrar sesión',[E,MV,LC,CRC,MAN,GT,SR,SA],[
 (E,MV,'selecciona cerrar sesión'),(MV,LC,'cerrarSesion()'),(LC,CRC,'cerrarSesion(token)')]+
 peticion(SR,'DELETE /api/v1/sesiones/actual','cerrar(Authorization)')+[
 (SR,SR,'leerToken(Authorization)'),(SR,SA,'cerrarSesion(token)'),(SA,GT,'invalidar(token)'),
 (GT,GT,'elimina el token de las sesiones activas'),(GT,SA,'Sesion','R'),(SA,SR,'sesión cerrada','R'),
 (SR,CRC,'HTTP 204 No Content','R'),(CRC,LC,'204','R'),(LC,LC,'borra el token de sesión'),
 (LC,MV,'sesión cerrada','R'),(MV,E,'muestra el inicio de sesión','R')])
TV,TC,TR,ST,TD='TELEFONO_VISTA','TELEFONO_CONTROLADOR','TELEFONO_RECURSO','SERVICIO_TELEFONO','TELEFONO_DAO'
P_TEL=[E,TV,TC,CRC,MAN,GT,TR,ST,SV,TD,BDC]
D['DS_03_CONSULTAR_CATALOGO']=('DS03: Consultar catálogo de teléfonos',[E,TV,TC,CRC,MAN,GT,TR,ST,TD,BDC],[
 (E,TV,'abre el catálogo'),(TV,TC,'listarTelefonos()'),(TC,CRC,'listarTelefonos(token)')]+
 peticion(TR,'GET /api/v1/telefonos','listar()')+[
 (TR,ST,'listar()'),(ST,TD,'listarActivos(cn)'),(TD,BDC,"SELECT TELEFONO WHERE ESTADO = 'ACTIVO' ORDER BY PRECIO"),
 (BDC,TD,'filas de teléfonos','R'),(TD,ST,'List<Telefono>','R'),(ST,TR,'List<Telefono>','R')]+
 respuesta(TR,'Mapeador.deTelefonos: precio en efectivo con −42 %','HTTP 200 JSON','lista de TelefonoDto',TV,TC,
           'muestra el catálogo con precio y precio en efectivo'))
def telefono_alta(titulo,ingreso,metodo_vc,http,metodo_r,servicio,sql,resp_bd,ok_dao,estado,final,con_texto=True):
    m=[(E,TV,ingreso),(TV,TC,metodo_vc),(TC,CRC,metodo_vc.replace('(','(token, ',1))]+peticion(TR,http,metodo_r)+[(TR,ST,servicio)]
    if con_texto: m+=[(ST,SV,'validarTexto(marca) y validarTexto(modelo)'),(SV,ST,'textos válidos','R'),
                      (ST,SV,'validarPrecio(precio)'),(SV,ST,'precio válido','R')]
    else: m+=[(ST,SV,'validarCodigo(codTelefono)'),(SV,ST,'código','R')]
    m+=[(ST,TD,sql[0]),(TD,BDC,sql[1]),(BDC,TD,resp_bd,'R'),(TD,ST,ok_dao,'R')]
    if estado!='HTTP 204 No Content':
        m+=[(ST,TD,'buscar(cn, codTelefono)'),(TD,BDC,'SELECT TELEFONO WHERE COD_TELEFONO'),(BDC,TD,'fila','R'),
            (TD,ST,'Telefono','R'),(ST,TR,'Telefono','R')]+respuesta(TR,'Mapeador.deTelefono(telefono)',estado,
            'TelefonoDto',TV,TC,final)
    else:
        m+=[(ST,TR,'baja registrada','R'),(TR,CRC,estado,'R'),(CRC,TC,'204','R'),(TC,TV,'teléfono eliminado','R'),
            (TV,E,final,'R')]
    return (titulo,P_TEL,m)
D['DS_04_REGISTRAR_TELEFONO']=telefono_alta('DS04: Registrar teléfono','ingresa marca, modelo y precio',
 'registrar(marca, modelo, precio)','POST /api/v1/telefonos {marca, modelo, precio}','crear(SolicitudTelefonoDto)',
 'crear(marca, modelo, precio)',('insertar(cn, marca, modelo, precio)',"INSERT INTO TELEFONO (ESTADO 'ACTIVO')"),
 'código generado','codTelefono','HTTP 201 Created + Location','muestra el teléfono registrado')
D['DS_05_EDITAR_TELEFONO']=telefono_alta('DS05: Editar teléfono','cambia marca, modelo y precio',
 'editar(codTelefono, marca, modelo, precio)','PUT /api/v1/telefonos/{codTelefono}',
 'actualizar(codTelefono, SolicitudTelefonoDto)','actualizar(codTelefono, marca, modelo, precio)',
 ('actualizar(cn, codTelefono, marca, modelo, precio)','UPDATE TELEFONO SET MARCA, MODELO, PRECIO'),
 'filas actualizadas','true (false: TEL-001)','HTTP 200 JSON','muestra el teléfono actualizado')
D['DS_06_ELIMINAR_TELEFONO']=telefono_alta('DS06: Eliminar teléfono','elige un teléfono y confirma la baja',
 'eliminar(codTelefono)','DELETE /api/v1/telefonos/{codTelefono}','eliminar(codTelefono)','eliminar(codTelefono)',
 ('inactivar(cn, codTelefono)',"UPDATE TELEFONO SET ESTADO = 'INACTIVO'"),'filas actualizadas',
 'true (false: TEL-001)','HTTP 204 No Content','quita el teléfono del catálogo',con_texto=False)
CV,VC,CR,SCL='COMPRADOR_VISTA','VENTA_CONTROLADOR','CLIENTE_RECURSO','SERVICIO_CLIENTE'
D['DS_07_VERIFICAR_COMPRADOR']=('DS07: Verificar al comprador',[E,CV,VC,CRC,MAN,GT,CR,SCL,SV,PB,CORE],[
 (E,CV,'ingresa la cédula del comprador'),(CV,VC,'verificar(cedula)'),(VC,CRC,'evaluar(token, cedula)')]+
 peticion(CR,'GET /api/v1/clientes/sujeto-credito?cedula=','evaluar(cedula)')+[
 (CR,SCL,'evaluar(cedula)'),(SCL,SV,'validarCedula(cedula)'),(SV,SCL,'cédula de 10 dígitos','R')]+
 core(SCL,'evaluar(cedula)','EvaluacionBanco','GET /api/v1/clientes/sujeto-credito?cedula=','HTTP 200 EvaluacionCreditoDto')+[
 (SCL,SCL,'si es cliente, pide su monto máximo')]+
 core(SCL,'montoMaximo(codCliente)','MontoMaximoBanco','GET /api/v1/clientes/{codCliente}/monto-maximo','HTTP 200 MontoMaximoDto')+[
 (SCL,CR,'EvaluacionCliente(sujetoCredito, motivos, montoMaximo)','R')]+
 respuesta(CR,'Mapeador.deEvaluacion(evaluacion)','HTTP 200 JSON','EvaluacionClienteDto',CV,VC,
           'muestra si es sujeto de crédito, sus motivos y su monto máximo'))
SIV,CRR='SIMULACION_VISTA','CREDITO_RECURSO'
D['DS_08_SIMULAR_TABLA']=('DS08: Simular tabla de amortización',[E,SIV,VC,CRC,MAN,GT,CRR,SCL,PB,CORE],[
 (E,SIV,'ingresa monto y número de cuotas'),(SIV,VC,'simular(monto, cuotas)'),(VC,CRC,'simular(token, monto, cuotas)')]+
 peticion(CRR,'GET /api/v1/creditos/simulacion?monto=&cuotas=','simular(monto, cuotas)')+[
 (CRR,SCL,'simular(monto, cuotas)')]+
 core(SCL,'simular(monto, cuotas)','List<CuotaBanco>','GET /api/v1/creditos/simulacion?monto=&cuotas=',
      'HTTP 200 cuotas calculadas sin guardar')+[(SCL,CRR,'List<CuotaBanco>','R')]+
 respuesta(CRR,'Mapeador.deCuotas(cuotas)','HTTP 200 JSON','lista de CuotaDto',SIV,VC,'muestra la tabla de amortización'))
VV,FR,SF,FD,DD='VENTA_VISTA','FACTURA_RECURSO','SERVICIO_FACTURACION','FACTURA_DAO','DETALLE_FACTURA_DAO'
P_VENTA=[E,VV,VC,CRC,MAN,GT,FR,SF,TD,PB,CORE,FD,DD,BDC]
def venta_inicio(forma,cuotas):
    return [(E,VV,f'ingresa cédula, teléfonos y cantidades ({forma})'),(VV,VC,f'vender(cedula, {forma.upper()}{cuotas}, detalles)'),
            (VC,CRC,'facturar(token, solicitud)')]+\
           peticion(FR,'POST /api/v1/facturas {cedula, formaPago, '+('numCuotas, ' if cuotas else '')+'detalles}',
                    'facturar(SolicitudFacturaDto)')+[
           (FR,FR,'sesion(): lee la Sesion de la petición'),
           (FR,SF,f'facturar(sesion, cedula, {forma.upper()}, {"numCuotas" if cuotas else "null"}, lineas)'),
           (SF,SF,'valida cédula, forma de pago'+(', cuotas' if cuotas else '')+' y líneas (ServicioValidacion)'),
           (SF,TD,'buscar(cn, codTelefono) por cada línea'),(TD,BDC,'SELECT TELEFONO WHERE COD_TELEFONO'),
           (BDC,TD,'fila del teléfono','R'),(TD,SF,'Telefono ACTIVO','R')]
def venta_guardar(credito):
    return [(SF,BDC,'setAutoCommit(false)'),(SF,FD,'insertar(cn, codUsuario, factura'+(' con codCredito' if credito else '')+')'),
            (FD,BDC,'INSERT INTO FACTURA'),(BDC,FD,'número generado','R'),(FD,SF,'numFactura','R'),
            (SF,DD,'insertar(cn, numFactura, detalles)'),(DD,BDC,'INSERT INTO DETALLE_FACTURA'),(SF,BDC,'commit()'),
            (SF,FR,'Factura','R')]
D['DS_09_VENDER_EFECTIVO']=('DS09: Vender en efectivo',P_VENTA,venta_inicio('efectivo','')+[
 (SF,SF,'subtotal, descuento de 42 % y total (CalculadoraFactura)')]+
 core(SF,'buscarCliente(cedula)','ClienteBanco','GET /api/v1/clientes?cedula=','HTTP 200 ClienteDto')+venta_guardar(False)+
 respuesta(FR,'Mapeador.deFactura(factura)','HTTP 201 Created JSON','FacturaDto',VV,VC,'muestra la factura'))
D['DS_10_VENDER_CREDITO']=('DS10: Vender con crédito directo',P_VENTA,venta_inicio('crédito',', numCuotas')+[
 (SF,SF,'subtotal y total sin descuento (CalculadoraFactura)')]+
 core(SF,'evaluar(cedula)','EvaluacionBanco','GET /api/v1/clientes/sujeto-credito?cedula=','HTTP 200 EvaluacionCreditoDto')+[
 (SF,SF,'exige que sea cliente y sujeto de crédito')]+
 core(SF,'montoMaximo(codCliente)','MontoMaximoBanco','GET /api/v1/clientes/{codCliente}/monto-maximo','HTTP 200 MontoMaximoDto')+[
 (SF,SF,'exige que el total no supere el monto máximo')]+
 core(SF,'otorgarCredito(codCliente, total, numCuotas)','CreditoBanco','POST /api/v1/creditos {codCliente, monto, numCuotas}',
      'HTTP 201 CreditoDto con su tabla')+venta_guardar(True)+
 respuesta(FR,'Mapeador.deFactura(factura)','HTTP 201 Created JSON','FacturaDto con el crédito',VV,VC,
           'muestra la factura y la tabla del crédito'))
FV,FCT='FACTURA_VISTA','FACTURA_CONTROLADOR'
D['DS_11_CONSULTAR_FACTURAS']=('DS11: Consultar facturas',[E,FV,FCT,CRC,MAN,GT,FR,SF,FD,DD,BDC],[
 (E,FV,'ingresa el número de factura'),(FV,FCT,'consultar(numFactura)'),(FCT,CRC,'buscarFactura(token, numFactura)')]+
 peticion(FR,'GET /api/v1/facturas/{numFactura}','buscar(numFactura)')+[
 (FR,SF,'buscar(numFactura)'),(SF,SF,'validarCodigo(numFactura)'),(SF,FD,'buscar(cn, numero)'),
 (FD,BDC,'SELECT FACTURA WHERE NUM_FACTURA'),(BDC,FD,'fila de la factura','R'),(FD,SF,'Optional<Factura>','R'),
 (SF,DD,'listar(cn, numero)'),(DD,BDC,'SELECT DETALLE_FACTURA'),(BDC,DD,'filas del detalle','R'),
 (DD,SF,'List<DetalleFactura>','R'),(SF,FR,'Factura con su detalle','R')]+
 respuesta(FR,'Mapeador.deFactura(factura)','HTTP 200 JSON','FacturaDto',FV,FCT,'muestra la factura con su detalle'))
KV,KC='CREDITO_VISTA','CREDITO_CONTROLADOR'
D['DS_12_CONSULTAR_CREDITOS']=('DS12: Consultar créditos de un comprador',[E,KV,KC,CRC,MAN,GT,CR,CRR,SCL,PB,CORE],[
 (E,KV,'ingresa la cédula del comprador'),(KV,KC,'consultarCreditos(cedula)'),(KC,CRC,'creditos(token, cedula)')]+
 peticion(CR,'GET /api/v1/clientes/creditos?cedula=','creditos(cedula)')+[(CR,SCL,'creditos(cedula)')]+
 core(SCL,'buscarCliente(cedula)','ClienteBanco','GET /api/v1/clientes?cedula=','HTTP 200 ClienteDto')+
 core(SCL,'creditos(codCliente)','List<CreditoBanco>','GET /api/v1/clientes/{codCliente}/creditos','HTTP 200 lista de CreditoDto')+[
 (SCL,CR,'List<CreditoBanco>','R'),(CR,CRC,'HTTP 200 JSON lista de CreditoDto','R'),(CRC,KC,'créditos','R'),
 (KC,KV,'créditos','R'),(KV,E,'muestra los créditos','R'),
 (E,KV,'elige un crédito'),(KV,KC,'verCuotas(codCredito)'),(KC,CRC,'cuotas(token, codCredito)'),
 (CRC,MAN,'GET /api/v1/creditos/{codCredito}/cuotas (Authorization: Bearer)'),(MAN,GT,'validar(token)'),
 (GT,MAN,'Sesion','R'),(MAN,CRR,'cuotas(codCredito)'),(CRR,SCL,'cuotas(codCredito)')]+
 core(SCL,'cuotas(codCredito)','List<CuotaBanco>','GET /api/v1/creditos/{codCredito}/cuotas','HTTP 200 lista de CuotaDto')+[
 (SCL,CRR,'List<CuotaBanco>','R')]+
 respuesta(CRR,'Mapeador.deCuotas(cuotas)','HTTP 200 JSON','lista de CuotaDto',KV,KC,'muestra la tabla de amortización'))
# ---------------- Core BanQuito (actor Comercializadora)
CLR,CRB='CLIENTE_RECURSO','CREDITO_RECURSO'
def clave(ruta,recurso,metodo):
    "toda petición al Core pasa por FiltroClaveApi (ECUD18)"
    return [(COM,FCA,f'{ruta} (X-Api-Key)'),(FCA,FCA,'compara X-Api-Key con api.clave (MessageDigest.isEqual)'),
            (FCA,recurso,metodo)]
D['DS_13_VERIFICAR_SUJETO_CREDITO']=('DS13: Verificar sujeto de crédito',[COM,FCA,CLR,SCR,SV,CLD,BDB],
 clave('GET /api/v1/clientes/sujeto-credito?cedula=',CLR,'evaluar(cedula)')+[
 (CLR,SCR,'evaluar(cedula)'),(SCR,SV,'validarCedula(cedula)'),(SV,SCR,'cédula de 10 dígitos','R'),
 (SCR,CLD,'buscarPorCedula(cn, cedula)'),(CLD,BDB,'SELECT CLIENTE WHERE CEDULA'),(BDB,CLD,'fila del cliente','R'),
 (CLD,SCR,'Optional<Cliente>','R'),(SCR,SCR,'si no es cliente: motivo NO_ES_CLIENTE (sin más reglas)'),
 (SCR,CLD,'reglas(cn, codCliente)'),(CLD,BDB,'SELECT depósito del último mes, hombre menor de 25, crédito activo'),
 (BDB,CLD,'fila de reglas','R'),(CLD,SCR,'ReglasCredito','R'),
 (SCR,SCR,'motivos(): SIN_DEPOSITO_ULTIMO_MES, HOMBRE_MENOR_25, CREDITO_ACTIVO'),
 (SCR,CLR,'EvaluacionCredito','R'),(CLR,CLR,'Mapeador.deEvaluacion(evaluacion)'),
 (CLR,COM,'HTTP 200 JSON EvaluacionCreditoDto(sujetoCredito, motivos)','R')])
D['DS_14_CALCULAR_MONTO_MAXIMO']=('DS14: Calcular monto máximo',[COM,FCA,CLR,SCR,SV,CLD,MVD,BDB],
 clave('GET /api/v1/clientes/{codCliente}/monto-maximo',CLR,'montoMaximo(codCliente)')+[
 (CLR,SCR,'montoMaximo(codCliente)'),(SCR,SV,'validarCodigo(codCliente)'),(SV,SCR,'código','R'),
 (SCR,CLD,'buscar(cn, codCliente)'),(CLD,BDB,'SELECT CLIENTE WHERE COD_CLIENTE'),(BDB,CLD,'fila del cliente','R'),
 (CLD,SCR,'Optional<Cliente>','R'),(SCR,MVD,'promedios(cn, codCliente, 3)'),
 (MVD,BDB,'SELECT AVG de depósitos y retiros de los últimos 3 meses'),(BDB,MVD,'promedios','R'),
 (MVD,SCR,'Promedios','R'),(SCR,SCR,'monto = (depósitos − retiros) × 30 % × 6, mínimo 0 (RN-05)'),
 (SCR,CLR,'MontoMaximo','R'),(CLR,CLR,'Mapeador.deMontoMaximo(monto)'),
 (CLR,COM,'HTTP 200 JSON MontoMaximoDto','R')])
CA='CALCULADORA_AMORTIZACION'
D['DS_15_OTORGAR_CREDITO']=('DS15: Otorgar crédito',[COM,FCA,CRB,SCR,SV,CA,CLD,MVD,CRD,CUD,BDB],
 clave('POST /api/v1/creditos {codCliente, monto, numCuotas}',CRB,'otorgar(SolicitudCreditoDto)')+[
 (CRB,SCR,'otorgar(codCliente, monto, numCuotas)'),(SCR,SV,'validarCodigo, validarMonto y validarCuotas'),
 (SV,SCR,'datos válidos','R'),(SCR,CA,'tabla(monto, numCuotas, 16.50 %)'),(CA,CA,'cuota fija (sistema francés)'),
 (CA,SCR,'List<Cuota>','R'),(SCR,BDB,'setAutoCommit(false)'),(SCR,CLD,'bloquear(cn, codCliente)'),
 (CLD,BDB,'SELECT CLIENTE … FOR UPDATE'),(BDB,CLD,'fila bloqueada','R'),(CLD,SCR,'Cliente','R'),
 (SCR,CLD,'reglas(cn, codCliente)'),(CLD,BDB,'SELECT reglas RN-02 a RN-04'),(BDB,CLD,'fila de reglas','R'),
 (CLD,SCR,'ReglasCredito','R'),(SCR,SCR,'sin motivos de rechazo (si hay: CRE-001)'),
 (SCR,MVD,'promedios(cn, codCliente, 3)'),(MVD,BDB,'SELECT AVG de 3 meses'),(BDB,MVD,'promedios','R'),
 (MVD,SCR,'Promedios','R'),(SCR,SCR,'monto ≤ monto máximo (si no: CRE-002)'),
 (SCR,CRD,'insertar(cn, codCliente, monto, cuotas, tasa, valorCuota)'),(CRD,BDB,'INSERT INTO CREDITO'),
 (BDB,CRD,'código generado','R'),(CRD,SCR,'codCredito','R'),(SCR,CUD,'insertar(cn, codCredito, tabla)'),
 (CUD,BDB,'INSERT INTO CUOTA (una por cuota)'),(SCR,BDB,'commit()'),(SCR,CRB,'Credito','R'),
 (CRB,CRB,'Mapeador.deCredito(credito, true)'),
 (CRB,COM,'HTTP 201 Created + Location, CreditoDto con su tabla','R')])
D['DS_16_CONSULTAR_TABLA_AMORTIZACION']=('DS16: Consultar tabla de amortización',[COM,FCA,CRB,SCR,SV,CRD,CUD,BDB],
 clave('GET /api/v1/creditos/{codCredito}/cuotas',CRB,'tabla(codCredito)')+[
 (CRB,SCR,'tabla(codCredito)'),(SCR,SV,'validarCodigo(codCredito)'),(SV,SCR,'código','R'),
 (SCR,CRD,'buscar(cn, codCredito)'),(CRD,BDB,'SELECT CREDITO WHERE COD_CREDITO'),(BDB,CRD,'fila del crédito','R'),
 (CRD,SCR,'Optional<Credito>','R'),(SCR,CUD,'listar(cn, codCredito)'),(CUD,BDB,'SELECT CUOTA ORDER BY NUM_CUOTA'),
 (BDB,CUD,'filas de cuotas','R'),(CUD,SCR,'List<Cuota>','R'),(SCR,CRB,'List<Cuota>','R'),
 (CRB,CRB,'Mapeador.deCuotas(cuotas)'),(CRB,COM,'HTTP 200 JSON lista de CuotaDto','R')])
# ---------------- errores
D['DS_17_ERROR_TOKEN_INVALIDO']=('DS17: Error de token inválido o vencido (AUTH-003)',[E,TV,TC,CRC,MAN,GT],[
 (E,TV,'abre el catálogo'),(TV,TC,'listarTelefonos()'),(TC,CRC,'listarTelefonos(token)'),
 (CRC,MAN,'GET /api/v1/telefonos (Authorization: Bearer)'),(MAN,MAN,'filter(): leerToken(Authorization)'),
 (MAN,GT,'validar(token)'),(GT,GT,'token desconocido o vencido: throw ExcepcionServicio(AUTH-003)'),
 (GT,MAN,'ExcepcionServicio AUTH-003','R'),(MAN,MAN,'abortWith(MapeadorExcepciones.respuesta(e))'),
 (MAN,CRC,'HTTP 401 problem+json (AUTH-003)','R'),(CRC,TC,'problema AUTH-003','R'),(TC,TC,'borra el token de sesión'),
 (TC,TV,'error AUTH-003','R'),(TV,E,'muestra el mensaje y el inicio de sesión','R')])
D['DS_18_ERROR_CLAVE_API']=('DS18: Error de clave de API rechazada (AUTH-004 → EXT-001)',[E,CV,VC,CRC,MAN,GT,CR,SCL,PB,FCA],[
 (E,CV,'ingresa la cédula del comprador'),(CV,VC,'verificar(cedula)'),(VC,CRC,'evaluar(token, cedula)')]+
 peticion(CR,'GET /api/v1/clientes/sujeto-credito?cedula=','evaluar(cedula)')+[
 (CR,SCL,'evaluar(cedula)'),(SCL,PB,'evaluar(cedula)'),
 (PB,FCA,'GET /api/v1/clientes/sujeto-credito?cedula= (X-Api-Key distinta)'),
 (FCA,FCA,'clave distinta de api.clave: abortWith(AUTH-004)'),(FCA,PB,'HTTP 401 problem+json (AUTH-004)','R'),
 (PB,PB,'AUTH-004 no es un código de negocio del Core: EXT-001 y log SEVERE'),
 (PB,SCL,'ExcepcionServicio EXT-001','R'),(SCL,CR,'ExcepcionServicio EXT-001','R'),
 (CR,CRC,'HTTP 502 problem+json (EXT-001) vía MapeadorExcepciones','R'),(CRC,VC,'problema EXT-001','R'),
 (VC,CV,'error EXT-001','R'),(CV,E,'muestra "El Core BanQuito no está disponible; intente más tarde"','R')])
D['DS_19_ERROR_CORE_NO_DISPONIBLE']=('DS19: Error de Core sin respuesta (EXT-001)',[E,SIV,VC,CRC,MAN,GT,CRR,SCL,PB,CORE],[
 (E,SIV,'ingresa monto y número de cuotas'),(SIV,VC,'simular(monto, cuotas)'),(VC,CRC,'simular(token, monto, cuotas)')]+
 peticion(CRR,'GET /api/v1/creditos/simulacion?monto=&cuotas=','simular(monto, cuotas)')+[
 (CRR,SCL,'simular(monto, cuotas)'),(SCL,PB,'simular(monto, cuotas)'),
 (PB,CORE,'GET /api/v1/creditos/simulacion (X-Api-Key)'),
 (PB,PB,'sin respuesta en 5 s (conexión) o 10 s (respuesta): IOException'),
 (PB,PB,'throw ExcepcionServicio(EXT-001)'),(PB,SCL,'ExcepcionServicio EXT-001','R'),
 (SCL,CRR,'ExcepcionServicio EXT-001','R'),
 (CRR,CRC,'HTTP 502 problem+json (EXT-001) vía MapeadorExcepciones','R'),(CRC,VC,'problema EXT-001','R'),
 (VC,SIV,'error EXT-001','R'),(SIV,E,'muestra "El Core BanQuito no está disponible; intente más tarde"','R')])
D['DS_20_ERROR_NO_SUJETO_CREDITO']=('DS20: Error de comprador que no es sujeto de crédito (CRE-001)',
 [E,VV,VC,CRC,MAN,GT,FR,SF,TD,PB,CORE,BDC],venta_inicio('crédito',', numCuotas')+[
 (SF,SF,'subtotal y total sin descuento (CalculadoraFactura)')]+
 core(SF,'evaluar(cedula)','EvaluacionBanco','GET /api/v1/clientes/sujeto-credito?cedula=',
      'HTTP 200 sujetoCredito = false con motivos')+
 falla(SF,FR,'CRE-001','409','El cliente no es sujeto de crédito: <motivos>',VV,VC))
D['DS_21_ERROR_BASE_DATOS']=('DS21: Error de base de datos (SYS-001)',[E,TV,TC,CRC,MAN,GT,TR,ST,'CONEXION_BD',BDC],[
 (E,TV,'abre el catálogo'),(TV,TC,'listarTelefonos()'),(TC,CRC,'listarTelefonos(token)')]+
 peticion(TR,'GET /api/v1/telefonos','listar()')+[
 (TR,ST,'listar()'),(ST,'CONEXION_BD','getInstancia().obtenerConexion()'),
 ('CONEXION_BD',BDC,'DriverManager.getConnection(url)'),(BDC,'CONEXION_BD','SQLException','R'),
 ('CONEXION_BD',ST,'SQLException','R'),(ST,ST,'registra el error (SEVERE)')]+
 falla(ST,TR,'SYS-001','503','No se pudo completar la operación; servicio o base de datos no disponible',TV,TC))

carpeta=os.path.join(os.path.dirname(os.path.abspath(__file__)),'specs','secuencia','EX11')
os.makedirs(carpeta,exist_ok=True)
for f in os.listdir(carpeta): os.remove(os.path.join(carpeta,f))
for nombre,(titulo,partes,mensajes) in D.items():
    open(os.path.join(carpeta,nombre+'.txt'),'w',encoding='utf8').write(spec(titulo,partes,mensajes))
print(len(D),'specs en',carpeta)

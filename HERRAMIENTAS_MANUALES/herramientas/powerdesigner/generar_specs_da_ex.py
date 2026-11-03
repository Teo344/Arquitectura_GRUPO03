import os
"""Genera specs/actividad/EX11/DA_*.txt (diagramas de actividad del EX.1.1: Comercializadora de celulares y Core
BanQuito) para da_detalle.vbs, a partir del ECUD de EX.1.1 y del código de los dos servidores.
Cada caso es una lista de pasos que se ubican solos en la cuadrícula:
  ('C', texto)                 acción del cliente (en el Core: de la Comercializadora, que es quien lo invoca)
  ('S', texto)                 acción del servidor
  ('D', texto, 'No: COD · http')  decisión; el "No" va al nodo de error del servidor
  ('D', texto, etiqueta, id)   decisión cuyo "No" salta al paso con ese id (no es un error)
  ('B', texto) / ('K', texto)  acción en la base de datos o en el Core BanQuito, junto al paso de servidor anterior
  ('R', texto)                 respuesta del servidor; el cliente la recibe en el paso 'C' siguiente
Un paso puede llevar id con la forma (tipo, texto, ..., {'id': 'X'}). Los carriles son Cliente y Servidor más los que
el caso use (BD, Core). Si el flujo vuelve al cliente después de alguna decisión, primero se cierra un bloque de error
(nodo de error, mensaje al cliente y fin) para que ninguna flecha de error cruce el regreso al cliente.
uso: generar_specs_da_ex.py"""
ANCHO={'CLI':18000,'SRV':34000,'BD':17000,'CORE':17000}
NOMBRE_CALLE={'CLI':'Cliente (Consola / Escritorio / Web / Móvil)','SRV':'Comercializadora (Payara 7 / Jakarta REST)',
              'BD':'BD comercializadora (MySQL)','CORE':'Core BanQuito (API REST)'}
NOMBRE_CORE={'CLI':'Comercializadora (cliente del servicio)','SRV':'Core BanQuito (Payara 7 / Jakarta REST)',
             'BD':'BD banquito (MySQL)'}
TAM={'C':(13000,3600),'S':(15000,3600),'R':(15000,3600),'F':(12000,3600),'B':(14000,3600),'K':(14000,3600),
     'D':(18000,4400)}
y=lambda fila:12000-5300*fila

def construir(titulo,pasos,del_core=False,error_cliente='Mostrar el mensaje de error'):
    usados=['CLI','SRV']+[c for c,t in (('BD','B'),('CORE','K')) if any(p[0]==t for p in pasos)]
    if del_core: usados=[c for c in usados if c!='CORE']
    x0=-30000; lim={}
    for c in usados: lim[c]=(x0,x0+ANCHO[c]); x0+=ANCHO[c]
    cx={'CLI':sum(lim['CLI'])//2,'F':lim['SRV'][0]+7500,'M':lim['SRV'][0]+22000}
    for c in ('BD','CORE'):
        if c in lim: cx[c]=sum(lim[c])//2
    desvio=cx['M']-11000     # saltos que no son error: por la izquierda, para no cruzar las flechas hacia la BD o el Core
    nodos=[]; flujos=[]; ids={}; fila=0; n=0
    def nodo(tipo,col,f,txt=''):
        nonlocal n
        n+=1; cod=f'N{n}'; nodos.append((tipo,cod,col,f,txt)); return cod
    prev=nodo('INI','CLI',0); prev_t='INI'; prev_col='CLI'
    pendientes=[]; bloques=0; saltos=[]; etiqueta_si=None
    def cerrar_bloque():
        nonlocal fila,bloques
        if not pendientes: return
        fila+=1; bloques+=1
        # PowerDesigner no admite dos actividades con el mismo nombre: desde el segundo bloque se indica la petición
        sufijo='' if bloques==1 else f' ({bloques}.ª petición)'
        f=nodo('F','F',fila,'MapeadorExcepciones responde problem+json con código y estado'+sufijo)
        ce=nodo('CE','CLI',fila,error_cliente+sufijo); fin=nodo('FIN','CLI',fila+1)
        for d,et in pendientes: flujos.append((d,f,et,'h'))
        flujos.append((f,ce,'',None)); flujos.append((ce,fin,'',None))
        pendientes.clear(); fila+=1
    for p in pasos:
        tipo,txt=p[0],p[1]; extra=p[-1] if isinstance(p[-1],dict) else {}
        if tipo=='C':
            cerrar_bloque()
            fila+=1 if prev_t!='INI' else 1
            cod=nodo('C','CLI',fila,txt)
        elif tipo in ('S','R'):
            if prev_t=='C': cod=nodo(tipo,'M',fila,txt)          # misma fila que la petición del cliente
            else: fila+=1; cod=nodo(tipo,'M',fila,txt)
        elif tipo=='D':
            fila+=1; cod=nodo('D','M',fila,txt)
            et=p[2]
            destino=p[3] if len(p)>3 and isinstance(p[3],str) else None
            if destino: saltos.append((cod,destino,et))
            else: pendientes.append((cod,et))
        elif tipo in ('B','K'):
            cod=nodo(tipo,'BD' if tipo=='B' else 'CORE',fila,txt)
        if 'id' in extra: ids[extra['id']]=cod
        flujos.append((prev,cod,'Sí' if prev_t=='D' else '',None))
        prev,prev_t=cod,tipo
    cerrar_bloque()
    fila+=1; fin=nodo('FIN','CLI',fila); flujos.append((prev,fin,'',None))
    for d,destino,et in saltos: flujos.append((d,ids[destino],et,'salto'))
    # ---------- spec
    calles=NOMBRE_CORE if del_core else NOMBRE_CALLE
    out=['# '+titulo,f'AREA|{y(0)+3000}|{y(fila)-2500}']+[f'CALLE|{c}|{calles[c]}|{lim[c][0]}|{lim[c][1]}' for c in usados]
    pos={}
    calle={'CLI':'CLI','F':'SRV','M':'SRV','BD':'BD','CORE':'CORE'}
    for tipo,cod,col,f,txt in nodos:
        pos[cod]=(tipo,cx[col],y(f))
        if tipo in ('INI','FIN'): out.append(f'{tipo}|{cod}|{calle[col]}|{cx[col]}|{y(f)}')
        else:
            w,h=TAM['C' if tipo=='CE' else tipo]
            out.append(f"{'DEC' if tipo=='D' else 'ACT'}|{cod}|{calle[col]}|{cx[col]}|{y(f)}|{w}|{h}|{txt}")
    for o,d,et,modo in flujos:
        to,xo,yo=pos[o]; _,xd,yd=pos[d]; pts=''
        if modo=='salto': pts=f'{desvio},{yo};{desvio},{yd}'
        elif to in ('B','K') and xo!=xd: pts=f'{xo},{yo-2650};{xd},{yo-2650}'   # regreso de la BD o el Core: por el hueco entre filas
        elif xo!=xd and yo!=yd: pts=f'{xd},{yo}' if to=='D' else f'{xo},{yd}'
        out.append(f'FLU|{o}|{d}|{et}|{pts}')
    return '\n'.join(out)+'\n'

TOKEN=[('S','ManejadorAutenticacion (filtro @Protegido) lee el token Bearer'),
       ('D','¿Token válido y vigente? (GestorToken, ECUD17)','No: AUTH-003 · 401')]
CLAVE=[('S','FiltroClaveApi compara la cabecera X-Api-Key con api.clave'),
       ('D','¿Clave de API válida? (ECUD18)','No: AUTH-004 · 401')]
BDD=('D','¿Base de datos disponible?','No: SYS-001 · 503')
CORE_OK=('D','¿El Core respondió a tiempo (5 s / 10 s) y sin error 5xx?','No: EXT-001 · 502')
ERR_CORE='PasarelaBanquito recibe el problem+json (código del Core o EXT-001)'
D={}
# ---------------- Comercializadora (actor Empleado)
D['DA_Iniciar_Sesion']=('DA1: Iniciar sesión',[
 ('C','Ingresar usuario y clave'),('C','Enviar POST /api/v1/sesiones con {usuario, clave}'),
 ('S','SesionRecurso.iniciar recibe las credenciales'),
 ('D','¿Usuario y clave completos?','No: DATA-001 · 400'),
 ('S','Calcular el SHA-256 de la clave (UtilHash) y buscar el usuario (UsuarioDao)'),
 ('B','SELECT USUARIO por usuario y clave'),BDD,
 ('D','¿Usuario y clave correctos?','No: AUTH-001 · 401'),('D','¿Usuario ACTIVO?','No: AUTH-002 · 401'),
 ('S','Generar el token UUID con GestorToken (vence tras 30 min sin uso)'),
 ('R','Responder 201 con token, expiraEn, código, usuario y nombre'),
 ('C','Guardar el token y mostrar el menú')])
D['DA_Cerrar_Sesion']=('DA2: Cerrar sesión',[
 ('C','Seleccionar cerrar sesión'),('C','Enviar DELETE /api/v1/sesiones/actual con Authorization: Bearer')]+TOKEN+[
 ('S','SesionRecurso.cerrar: GestorToken.invalidar(token)'),('R','Responder 204 No Content'),
 ('C','Borrar el token y mostrar el inicio de sesión')],)
D['DA_Consultar_Catalogo']=('DA3: Consultar catálogo de teléfonos',[
 ('C','Abrir el catálogo o elegir un teléfono'),
 ('C','Enviar GET /api/v1/telefonos (o /telefonos/{codTelefono}) con Bearer')]+TOKEN+[
 ('S','Listar los teléfonos ACTIVOS o buscar uno por código (TelefonoDao)'),
 ('B','SELECT TELEFONO (ACTIVO por precio, o por código aunque esté INACTIVO)'),BDD,
 ('D','¿El teléfono existe? (consulta de uno)','No: TEL-001 · 404'),
 ('S','Mapeador calcula el precio en efectivo (−42 %)'),
 ('R','Responder 200 con la lista o el teléfono'),
 ('C','Mostrar el catálogo con precio y precio en efectivo')])
D['DA_Registrar_Telefono']=('DA4: Registrar teléfono',[
 ('C','Ingresar marca, modelo y precio'),
 ('C','Enviar POST /api/v1/telefonos {marca, modelo, precio} con Bearer')]+TOKEN+[
 ('D','¿Marca y modelo no vacíos y dentro del largo?','No: DATA-001 · 400'),
 ('D','¿Precio mayor que cero y con 2 decimales como máximo?','No: TEL-003 · 400'),
 ('S','Insertar el teléfono ACTIVO (TelefonoDao.insertar)'),('B','INSERT INTO TELEFONO'),BDD,
 ('R','Responder 201 Created con el teléfono y su Location'),('C','Mostrar el teléfono registrado')])
D['DA_Editar_Telefono']=('DA5: Editar teléfono',[
 ('C','Elegir un teléfono y cambiar marca, modelo y precio'),
 ('C','Enviar PUT /api/v1/telefonos/{codTelefono} con Bearer')]+TOKEN+[
 ('D','¿Marca y modelo no vacíos y dentro del largo?','No: DATA-001 · 400'),
 ('D','¿Precio mayor que cero y con 2 decimales como máximo?','No: TEL-003 · 400'),
 ('S','Reemplazar marca, modelo y precio (TelefonoDao.actualizar)'),
 ('B','UPDATE TELEFONO SET MARCA, MODELO, PRECIO'),BDD,
 ('D','¿Se actualizó el teléfono (existe)?','No: TEL-001 · 404'),
 ('R','Responder 200 con el teléfono actualizado'),('C','Mostrar el teléfono actualizado')])
D['DA_Eliminar_Telefono']=('DA6: Eliminar teléfono',[
 ('C','Elegir un teléfono y confirmar la baja'),
 ('C','Enviar DELETE /api/v1/telefonos/{codTelefono} con Bearer')]+TOKEN+[
 ('S','Dar de baja lógica el teléfono (TelefonoDao.inactivar)'),
 ('B',"UPDATE TELEFONO SET ESTADO = 'INACTIVO'"),BDD,
 ('D','¿Se dio de baja el teléfono (existe)?','No: TEL-001 · 404'),
 ('R','Responder 204 No Content'),('C','Quitar el teléfono del catálogo mostrado')])
D['DA_Verificar_Comprador']=('DA7: Verificar al comprador',[
 ('C','Ingresar la cédula del comprador'),
 ('C','Enviar GET /api/v1/clientes/sujeto-credito?cedula= con Bearer')]+TOKEN+[
 ('D','¿Cédula de 10 dígitos?','No: DATA-001 · 400'),
 ('S','PasarelaBanquito pide la evaluación al Core (X-Api-Key)'),
 ('K','Verificar sujeto de crédito (ECUD13)'),CORE_OK,
 ('D','¿La persona es cliente del banco?','No: sin monto máximo','RESP'),
 ('S','PasarelaBanquito pide el monto máximo al Core'),('K','Calcular monto máximo (ECUD14)'),
 ('R','Responder 200 con sujetoCredito, motivos y monto máximo',{'id':'RESP'}),
 ('C','Mostrar si es sujeto de crédito, sus motivos y su monto máximo')])
D['DA_Simular_Tabla']=('DA8: Simular tabla de amortización',[
 ('C','Ingresar monto y número de cuotas'),
 ('C','Enviar GET /api/v1/creditos/simulacion?monto=&cuotas= con Bearer')]+TOKEN+[
 ('S','PasarelaBanquito reenvía la simulación al Core (X-Api-Key)'),
 ('K','Calcular la tabla francesa con 16.50 % anual sin guardarla'),CORE_OK,
 ('D','¿El Core aceptó cuotas (3..18) y monto?','No: CRE-003 / CRE-005 · 400'),
 ('R','Responder 200 con las cuotas'),('C','Mostrar la tabla de amortización')])
VENTA_INICIO=lambda forma,extra:[
 ('C',f'Ingresar cédula, teléfonos y cantidades ({forma})'),
 ('C','Enviar POST /api/v1/facturas {cedula, formaPago, '+extra+'detalles} con Bearer')]+TOKEN+[
 ('D','¿Cédula, forma de pago y líneas válidas?','No: DATA-001 · 400')]   # líneas sin repetidos y cantidad 1..100
DETALLE=[('S','Armar el detalle con los precios del catálogo (TelefonoDao)'),('B','SELECT TELEFONO de cada línea'),
         ('D','¿Todos los teléfonos existen y están ACTIVOS?','No: TEL-001 · 404 / TEL-002 · 409')]
GUARDAR=[('S','Guardar la factura y su detalle en una transacción (commit)'),
         ('B','INSERT FACTURA y DETALLE_FACTURA'),BDD]
D['DA_Vender_Efectivo']=('DA9: Vender en efectivo',VENTA_INICIO('efectivo','')+DETALLE+[
 ('S','Calcular subtotal, descuento de 42 % y total (CalculadoraFactura)'),
 ('S','PasarelaBanquito busca al comprador en el Core'),('K','GET /clientes?cedula= (X-Api-Key)'),CORE_OK,
 ('D','¿El comprador es cliente del banco?','No: CLI-001 · 404')]+GUARDAR+[
 ('R','Responder 201 Created con la factura'),('C','Mostrar la factura')])
D['DA_Vender_Credito']=('DA10: Vender con crédito directo',VENTA_INICIO('crédito directo','numCuotas, ')+[
 ('D','¿Cuotas entre 3 y 18?','No: FAC-002 · 400')]+DETALLE+[
 ('S','Calcular subtotal y total (sin descuento)'),
 ('S','PasarelaBanquito pide la evaluación al Core'),('K','Verificar sujeto de crédito (ECUD13)'),CORE_OK,
 ('D','¿Es cliente del banco?','No: CLI-001 · 404'),('D','¿Es sujeto de crédito?','No: CRE-001 · 409'),
 ('S','PasarelaBanquito pide el monto máximo'),('K','Calcular monto máximo (ECUD14)'),
 ('D','¿El total no supera el monto máximo?','No: CRE-002 · 409'),
 ('S','PasarelaBanquito pide otorgar el crédito por el total'),('K','Otorgar crédito (ECUD15)'),
 ('S','Guardar la factura con el código del crédito (commit); si falla, el crédito queda en el log SEVERE'),('B','INSERT FACTURA y DETALLE_FACTURA'),
 ('D','¿Se guardó la factura?','No: SYS-001 · 503'),
 ('R','Responder 201 Created con la factura y el crédito'),('C','Mostrar la factura y la tabla del crédito')])
D['DA_Consultar_Facturas']=('DA11: Consultar facturas',[
 ('C','Ingresar el número de factura o la cédula del comprador'),
 ('C','Enviar GET /api/v1/facturas/{numFactura} o /facturas?cedula= con Bearer')]+TOKEN+[
 ('D','¿Número o cédula con formato válido?','No: DATA-001 · 400'),
 ('S','Buscar la factura con su detalle o listar las del comprador (FacturaDao)'),
 ('B','SELECT FACTURA y DETALLE_FACTURA'),BDD,
 ('D','¿La factura existe? (consulta por número)','No: FAC-001 · 404'),
 ('R','Responder 200 con la factura o la lista'),('C','Mostrar la factura o la lista de facturas')])
D['DA_Consultar_Creditos']=('DA12: Consultar créditos de un comprador',[
 ('C','Ingresar la cédula del comprador'),
 ('C','Enviar GET /api/v1/clientes/creditos?cedula= con Bearer')]+TOKEN+[
 ('D','¿Cédula de 10 dígitos?','No: DATA-001 · 400'),
 ('S','PasarelaBanquito busca al cliente en el Core'),('K','GET /clientes?cedula= (X-Api-Key)'),CORE_OK,
 ('D','¿Es cliente del banco?','No: CLI-001 · 404'),
 ('S','PasarelaBanquito pide sus créditos'),('K','GET /clientes/{codCliente}/creditos'),
 ('R','Responder 200 con los créditos (lista vacía si no tiene)'),
 ('C','Mostrar los créditos y elegir uno'),
 ('C','Enviar GET /api/v1/creditos/{codCredito}/cuotas con Bearer'),
 ('S','PasarelaBanquito pide la tabla de amortización'),('K','Consultar tabla de amortización (ECUD16)'),
 ('D','¿El crédito existe?','No: CRE-004 · 404'),
 ('R','Responder 200 con las cuotas'),('C','Mostrar la tabla de amortización')])
# ---------------- Core BanQuito (actor Comercializadora)
D['DA_Verificar_Sujeto_Credito']=('DA13: Verificar sujeto de crédito',[
 ('C','Enviar GET /api/v1/clientes/sujeto-credito?cedula= con X-Api-Key')]+CLAVE+[
 ('D','¿Cédula de 10 dígitos?','No: DATA-001 · 400'),
 ('S','Buscar al cliente por cédula (ClienteDao.buscarPorCedula)'),('B','SELECT CLIENTE WHERE CEDULA'),BDD,
 ('D','¿Es cliente del banco?','No: NO_ES_CLIENTE','RESP'),
 ('S','Evaluar las reglas RN-02 a RN-04 (ClienteDao.reglas)'),
 ('B','Depósito en el último mes, hombre menor de 25, crédito activo'),
 ('S','Agregar los motivos de rechazo que se cumplan (pueden ser varios)'),
 ('R','Responder 200 con sujetoCredito y motivos (un "no" no es error)',{'id':'RESP'}),
 ('C','Recibir la evaluación')],True)
D['DA_Calcular_Monto_Maximo']=('DA14: Calcular monto máximo',[
 ('C','Enviar GET /api/v1/clientes/{codCliente}/monto-maximo con X-Api-Key')]+CLAVE+[
 ('D','¿Código de cliente mayor que cero?','No: DATA-001 · 400'),
 ('S','Buscar al cliente (ClienteDao.buscar)'),('B','SELECT CLIENTE WHERE COD_CLIENTE'),BDD,
 ('D','¿El cliente existe?','No: CLI-001 · 404'),
 ('S','Promediar depósitos y retiros de los últimos 3 meses (MovimientoDao)'),
 ('B','AVG de MOVIMIENTO por tipo (DEP / RET)'),
 ('S','Monto máximo = (prom. depósitos − prom. retiros) × 30 % × 6, mínimo 0 (RN-05)'),
 ('R','Responder 200 con promedios, porcentaje, plazo y monto máximo'),('C','Recibir el monto máximo')],True)
D['DA_Otorgar_Credito']=('DA15: Otorgar crédito',[
 ('C','Enviar POST /api/v1/creditos {codCliente, monto, numCuotas} con X-Api-Key')]+CLAVE+[
 ('D','¿Código de cliente válido?','No: DATA-001 · 400'),
 ('D','¿Monto mayor que cero y con 2 decimales?','No: CRE-005 · 400'),
 ('D','¿Cuotas entre 3 y 18?','No: CRE-003 · 400'),
 ('S','Calcular la tabla francesa con 16.50 % anual (CalculadoraAmortizacion)'),
 ('D','¿Todas las cuotas tienen capital positivo?','No: CRE-005 · 400'),
 ('S','Iniciar la transacción y bloquear al cliente (ClienteDao.bloquear)'),
 ('B','SELECT CLIENTE … FOR UPDATE'),BDD,
 ('D','¿El cliente existe?','No: CLI-001 · 404'),
 ('S','Reevaluar las reglas RN-02 a RN-04 (ClienteDao.reglas)'),('B','Consultar las reglas del cliente'),
 ('D','¿Es sujeto de crédito?','No: CRE-001 · 409'),
 ('S','Calcular el monto máximo (MovimientoDao.promedios)'),('B','AVG de MOVIMIENTO de 3 meses'),
 ('D','¿El monto no supera el monto máximo?','No: CRE-002 · 409'),
 ('S','Insertar el crédito y sus cuotas (CreditoDao, CuotaDao)'),('B','INSERT CREDITO y CUOTA'),
 ('S','Confirmar la transacción (commit)'),
 ('R','Responder 201 Created con el crédito y su tabla'),('C','Recibir el crédito otorgado')],True)
D['DA_Consultar_Tabla_Amortizacion']=('DA16: Consultar tabla de amortización',[
 ('C','Enviar GET /api/v1/creditos/{codCredito}/cuotas con X-Api-Key')]+CLAVE+[
 ('D','¿Código de crédito mayor que cero?','No: DATA-001 · 400'),
 ('S','Buscar el crédito (CreditoDao.buscar)'),('B','SELECT CREDITO WHERE COD_CREDITO'),BDD,
 ('D','¿El crédito existe?','No: CRE-004 · 404'),
 ('S','Listar sus cuotas (CuotaDao.listar)'),('B','SELECT CUOTA ORDER BY NUM_CUOTA'),
 ('R','Responder 200 con todas las cuotas'),('C','Recibir la tabla de amortización')],True)

carpeta=os.path.join(os.path.dirname(os.path.abspath(__file__)),'specs','actividad','EX11')
os.makedirs(carpeta,exist_ok=True)
for f in os.listdir(carpeta): os.remove(os.path.join(carpeta,f))
for nombre,(titulo,pasos,*core) in D.items():
    del_core=bool(core and core[0])
    texto=construir(titulo,pasos,del_core,ERR_CORE if del_core else
                    ('Borrar el token local y volver al inicio de sesión' if nombre=='DA_Cerrar_Sesion' else
                     'Mostrar el mensaje de error'))
    open(os.path.join(carpeta,nombre+'.txt'),'w',encoding='utf8').write(texto)
print(len(D),'specs en',carpeta)

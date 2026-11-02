BanQuito + Comercializadora de Celulares - Grupo 03 - Servidores REST (Java)
============================================================================

Dos aplicaciones Web separadas, cada una con su base de datos (ver 03. BDD):

  Clientes (consola / escritorio / web / móvil)
     |  Authorization: Bearer <token>
     v
  WS_COMERCIALIZADORA_JAVA_REST  --- X-Api-Key --->  WS_BANQUITO_JAVA_REST
     BD comercializadora                              BD banquito

  WS_BANQUITO_JAVA_REST          Core del banco: los 4 servicios del módulo de crédito.
                                 Solo lo llama la comercializadora (clave de API).
  WS_COMERCIALIZADORA_JAVA_REST  Catálogo, ventas y facturas. Los clientes solo hablan
                                 con este servidor; él consulta al Core por HTTP.

Herramientas: Maven, Jakarta EE 11 (JAX-RS, JSON-B, CDI), Payara 7, MySQL local,
mysql-connector-j 9.7.0 y MicroProfile Config (las mismas de TG.1.3).

Arquitectura MVC por capas (paquete ec.edu.monster, igual en los dos)
  controladores.recursos  Recursos JAX-RS: solo traducen HTTP <-> servicio
  vista                   DTO de entrada y salida, Mapeador (modelo -> DTO), ProblemaDto
  servicios               Reglas de negocio, cálculos y transacciones
  datos                   ConexionBD (singleton) y DAO; PasarelaBanquito es el
                          acceso remoto al Core desde la comercializadora
  modelo                  Records y enums del dominio, CodigoError
  seguridad               Filtros de autenticación y CORS
  pruebas                 Pruebas con main(), sin framework

Convenciones REST
  - Base /api/v1 (versión en la ruta). Recursos en plural, sin verbos.
  - GET consulta, POST crea (201 + cabecera Location), PUT reemplaza,
    DELETE elimina (204). PUT y DELETE son idempotentes.
  - Respuestas exitosas: el recurso directamente, en JSON (camelCase, dinero con
    2 decimales, fechas ISO-8601, los campos nulos no se envían).
  - Errores: application/problem+json (RFC 9457)
      {"type":"about:blank","title":"Conflict","status":409,
       "detail":"El cliente no es sujeto de crédito: Tiene un crédito activo",
       "instance":"/facturas","codigo":"CRE-001"}
    400 datos inválidos, 401 sin token o sin clave, 404 no existe,
    409 una regla de negocio lo impide, 502 el Core no responde,
    503 la base de datos no responde.

------------------------------------------------------------------------------
1. Configuración (una sola vez)
------------------------------------------------------------------------------
En cada proyecto, copiar src/main/resources/db.properties.example como
db.properties (no se sube al repositorio) y revisar:
  - db.usuario / db.clave: los de 03. BDD/<bd>/00_usuario_local.sql.
  - La clave de API entre los dos servidores es MONSTER9 (proyecto académico):
      Core:              api.clave=MONSTER9
      Comercializadora:  core.claveApi=MONSTER9
    Las dos deben ser iguales. Para probar el Core a mano se envía en la
    cabecera X-Api-Key: MONSTER9. Si se cambia, volver a compilar y desplegar
    los dos WAR (la clave va empaquetada dentro).
Valores de negocio (META-INF/microprofile-config.properties):
  Core:              credito.tasaAnual=16.50, credito.porcentajeMonto=30, credito.plazoMeses=6
  Comercializadora:  factura.descuentoEfectivo=42, sesion.minutos=30, cors.origenes

------------------------------------------------------------------------------
2. Compilar y desplegar (PowerShell, desde esta carpeta)
------------------------------------------------------------------------------
  $mvn = "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd"
  $asadmin = "C:\payara7\payara-7.2026.9\payara7\bin\asadmin.bat"
  net start MySQL267                      (consola como administrador)
  & $asadmin start-domain domain1
  cd WS_BANQUITO_JAVA_REST;         & $mvn package; & $asadmin deploy --force=true target\WS_BANQUITO_JAVA_REST-1.0-SNAPSHOT.war; cd ..
  cd WS_COMERCIALIZADORA_JAVA_REST; & $mvn package; & $asadmin deploy --force=true target\WS_COMERCIALIZADORA_JAVA_REST-1.0-SNAPSHOT.war; cd ..

  Core:             http://localhost:8080/WS_BANQUITO_JAVA_REST/api/v1/
  Comercializadora: http://localhost:8080/WS_COMERCIALIZADORA_JAVA_REST/api/v1/
  (el context root está fijo en WEB-INF/glassfish-web.xml)

------------------------------------------------------------------------------
3. Endpoints del Core (todos exigen X-Api-Key)
------------------------------------------------------------------------------
  GET  clientes?cedula={cedula}                   Buscar cliente               200 / 404 CLI-001
  GET  clientes/sujeto-credito?cedula={cedula}    1. ¿Es sujeto de crédito?    200 (un "no" también es 200,
                                                  con motivos: NO_ES_CLIENTE, SIN_DEPOSITO_ULTIMO_MES,
                                                  HOMBRE_MENOR_25, CREDITO_ACTIVO)
  GET  clientes/{codCliente}/monto-maximo         2. Monto máximo              200 / 404
  POST creditos {codCliente, monto, numCuotas}    3. Otorgar crédito con su    201 + Location / 400 CRE-003,
                                                     tabla (una transacción)    CRE-005 / 404 / 409 CRE-001, CRE-002
  GET  creditos/{codCredito}/cuotas               4. Tabla de amortización     200 / 404 CRE-004
  GET  creditos/{codCredito}                      Crédito con su tabla         200 / 404
  GET  clientes/{codCliente}/creditos             Créditos de un cliente       200
  GET  creditos/simulacion?monto=&cuotas=         Tabla sin guardar            200 / 400

  Monto máximo = ((promedio depósitos - promedio retiros) * 30 %) * 6, con los
  movimientos de los últimos 3 meses de todas las cuentas del cliente; nunca < 0.
  Tabla: tasa mensual = 16.50 % / 12; cuota fija = monto*i / (1 - (1+i)^-n),
  redondeo HALF_UP (como Excel). La última cuota abona todo el saldo como
  capital y el interés absorbe el centavo de redondeo: el saldo cierra en 0.00.
  POST creditos vuelve a comprobar las reglas y el monto máximo con el cliente
  bloqueado (SELECT ... FOR UPDATE): un reintento no crea un segundo crédito.

------------------------------------------------------------------------------
4. Endpoints de la Comercializadora (todos exigen token, salvo POST sesiones)
------------------------------------------------------------------------------
  POST   sesiones {usuario, clave}                Iniciar sesión (MONSTER / MONSTER9)   201 / 401
  DELETE sesiones/actual                          Cerrar sesión                         204
  GET    telefonos                                Catálogo a la venta (con precioEfectivo)
  GET    telefonos/{codTelefono}                  Un teléfono (también INACTIVO)        200 / 404
  POST   telefonos {marca, modelo, precio}        Crear                                 201 + Location
  PUT    telefonos/{codTelefono}                  Editar marca, modelo y precio         200 / 404
  DELETE telefonos/{codTelefono}                  Baja lógica (INACTIVO)                204 / 404
  GET    clientes/sujeto-credito?cedula=          Servicios 1 y 2 del Core juntos (incluye montoMaximo)
  GET    clientes/creditos?cedula=                Créditos del comprador en el banco
  GET    creditos/{codCredito}/cuotas             Tabla de amortización (servicio 4)
  GET    creditos/simulacion?monto=&cuotas=       Tabla antes de vender
  POST   facturas                                 Vender                                201 + Location
         {"cedula":"1712345678","formaPago":"CREDITO","numCuotas":6,
          "detalles":[{"codTelefono":5,"cantidad":1}]}
  GET    facturas/{numFactura}                    Factura con detalle                   200 / 404
  GET    facturas?cedula=                         Facturas (sin cédula: las últimas 100)

  Venta: los precios salen del catálogo (nunca del JSON) y el empleado sale del
  token. EFECTIVO: 42 % de descuento; el comprador debe ser cliente del banco.
  CREDITO: sin descuento; servicio 1 (sujeto de crédito), servicio 2 (total <=
  monto máximo) y servicio 3 (crédito por el total) y luego se guarda la factura.
  Limitación conocida: son dos BD distintas, sin transacción distribuida. Si la
  factura no se puede guardar después de otorgar el crédito, el código del
  crédito queda en el log del servidor (SEVERE) para conciliarlo a mano.

------------------------------------------------------------------------------
5. Pruebas (clases con main() en el paquete pruebas)
------------------------------------------------------------------------------
En NetBeans: clic derecho sobre la clase > Run File. Por consola, desde la
carpeta de cada proyecto y después de mvn package:
  java -cp "target\classes;target\WS_BANQUITO_JAVA_REST-1.0-SNAPSHOT\WEB-INF\lib\*" ec.edu.monster.pruebas.PruebaConexion
  (en la comercializadora, con su nombre de carpeta)
Cada prueba imprime [OK] / [FALLA] por caso y termina en PRUEBA APROBADA.

Unitarias (sin servidor ni BD):
  Core:              PruebaCalculoAmortizacion
  Comercializadora:  PruebaCalculoFactura, PruebaHash

De integración (Payara con los dos WAR y las BD recién cargadas). Las que
otorgan créditos solo se pueden correr una vez por carga, porque un cliente con
crédito activo deja de ser sujeto de crédito. Orden recomendado:
  1. Recargar las dos BD (01 + 02 de cada una, ver 03. BDD/README.txt).
  2. Core: PruebaConexion, PruebaClaveApi, PruebaSujetoCredito, PruebaMontoMaximo,
     PruebaTablaAmortizacion, PruebaOtorgarCredito (cliente 6),
     PruebaConcurrencia (cliente 2).
  3. Comercializadora: PruebaConexion, PruebaInicioSesion, PruebaValidacionToken,
     PruebaTelefonos, PruebaFacturacion (cliente 1).
  PruebaClaveApi, PruebaMontoMaximo, PruebaTablaAmortizacion, PruebaTelefonos,
  PruebaInicioSesion y PruebaValidacionToken se pueden repetir sin recargar.

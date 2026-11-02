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

Herramientas: las mismas de TG.1.3 (detalle en la sección 1).

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
1. Tecnologías
------------------------------------------------------------------------------
  Lenguaje                 Java (JDK 25; el código se compila para Java 21)
  Plataforma               Jakarta EE 11 (Web Profile)
    Servicios REST         JAX-RS (implementación Jersey, incluida en Payara)
    JSON                   JSON-B (implementación Yasson, incluida en Payara)
    Inyección              CDI (@ApplicationScoped, @Inject)
    Filtros web            Servlet (FiltroCors)
  Configuración            MicroProfile Config 3.1 (microprofile-config.properties)
  Servidor de aplicaciones Payara Server 7 (7.2026.9), puerto 8080
  Base de datos            MySQL 26.7 local (servicio MySQL267), InnoDB, utf8mb4
  Acceso a datos           JDBC con mysql-connector-j 9.7.0 (empaquetado en el WAR)
  Llamadas al Core         java.net.http.HttpClient (JDK), timeouts de 5 s / 10 s
  Seguridad                Comercializadora: usuario + clave en SHA-256 (MessageDigest)
                           y token UUID en memoria, 30 min de inactividad.
                           Core: clave de API en la cabecera X-Api-Key.
  Formatos                 JSON; errores en application/problem+json (RFC 9457)
  Construcción             Apache Maven 3.9.16 (incluido en NetBeans); empaquetado WAR
  IDE                      Apache NetBeans
  Pruebas                  Clases con main() sin framework (HttpURLConnection para HTTP)
  Patrones                 MVC por capas, Singleton (ConexionBD), DAO, DTO + Mapeador,
                           Gateway (PasarelaBanquito)

------------------------------------------------------------------------------
2. Actores y casos de uso
------------------------------------------------------------------------------
Actores
  Empleado         Vendedor de la comercializadora. Usa cualquiera de los 4 clientes
                   (consola, escritorio, web, móvil); debe iniciar sesión.
  Comercializadora Sistema que consume los servicios del Core (actor del Core).
  Core BanQuito    Sistema externo para la comercializadora: dueño de clientes,
                   cuentas, movimientos y créditos.

Casos de uso de la Comercializadora (actor: Empleado)
  CU-01 Iniciar sesión                    POST /sesiones
        Pre:  el usuario existe y está ACTIVO.
        Flujo: ingresa usuario y clave -> el sistema compara el SHA-256 de la clave
              -> entrega un token que vence tras 30 min sin uso.
        Alt.: clave o usuario incorrectos (AUTH-001); usuario ANULADO (AUTH-002);
              datos vacíos (DATA-001).
  CU-02 Cerrar sesión                     DELETE /sesiones/actual
        Flujo: el sistema invalida el token; usarlo de nuevo da AUTH-003.
  CU-03 Consultar catálogo de teléfonos   GET /telefonos, GET /telefonos/{cod}
        Flujo: lista los teléfonos ACTIVOS con su precio y su precio en efectivo
              (42 % de descuento).
        Alt.: teléfono inexistente (TEL-001).
  CU-04 Registrar teléfono                POST /telefonos
        Flujo: ingresa marca, modelo y precio -> queda ACTIVO en el catálogo.
        Alt.: precio <= 0 o con más de 2 decimales (TEL-003); marca o modelo
              vacíos o muy largos (DATA-001).
  CU-05 Editar teléfono                   PUT /telefonos/{cod}
        Flujo: reemplaza marca, modelo y precio. Las facturas ya emitidas
              conservan el precio con que se vendió.
        Alt.: teléfono inexistente (TEL-001); datos inválidos (TEL-003, DATA-001).
  CU-06 Eliminar teléfono                 DELETE /telefonos/{cod}
        Flujo: baja lógica: el teléfono pasa a INACTIVO, sale del catálogo y ya
              no se puede vender; las facturas antiguas no cambian.
        Alt.: teléfono inexistente (TEL-001).
  CU-07 Verificar al comprador            GET /clientes/sujeto-credito?cedula=
        Flujo: ingresa la cédula -> el sistema consulta al Core (CU-13 y CU-14)
              -> muestra si es sujeto de crédito, los motivos si no lo es y su
              monto máximo.
        Alt.: cédula inválida (DATA-001); Core no disponible (EXT-001).
  CU-08 Simular tabla de amortización     GET /creditos/simulacion?monto=&cuotas=
        Flujo: ingresa monto y número de cuotas -> muestra la tabla sin guardar.
        Alt.: cuotas fuera de 3..18 (CRE-003); monto inválido (CRE-005).
  CU-09 Vender en efectivo                POST /facturas (formaPago EFECTIVO)
        Flujo: ingresa la cédula y los teléfonos con su cantidad -> el sistema
              toma los precios del catálogo, verifica en el Core que el comprador
              sea cliente, aplica 42 % de descuento y registra la factura con el
              empleado de la sesión.
        Alt.: no es cliente del banco (CLI-001); teléfono inexistente (TEL-001)
              o dado de baja (TEL-002); teléfono repetido, cantidad fuera de
              1..100 o venta sin teléfonos (DATA-001); Core no disponible (EXT-001).
  CU-10 Vender con crédito directo        POST /facturas (formaPago CREDITO)
        Flujo: ingresa la cédula, los teléfonos y el número de cuotas -> el
              sistema calcula el total (sin descuento) -> consulta al Core si es
              sujeto de crédito (CU-13) y su monto máximo (CU-14) -> si el total
              no supera el monto máximo, el Core otorga el crédito (CU-15) ->
              registra la factura con el código del crédito.
        Alt.: no es cliente (CLI-001); no es sujeto de crédito, con sus motivos
              (CRE-001); total mayor que el monto máximo (CRE-002); cuotas
              fuera de 3..18 (FAC-002); los mismos de CU-09.
  CU-11 Consultar facturas                GET /facturas/{num}, GET /facturas?cedula=
        Flujo: una factura con su detalle, o la lista (de un cliente o las
              últimas 100).
        Alt.: factura inexistente (FAC-001).
  CU-12 Consultar créditos de un comprador
                                          GET /clientes/creditos?cedula=,
                                          GET /creditos/{cod}/cuotas
        Flujo: lista los créditos del comprador en el banco y muestra la tabla
              de amortización del que elija (CU-16).
        Alt.: no es cliente (CLI-001); crédito inexistente (CRE-004).
  Todos (salvo CU-01): sin token o con token vencido -> AUTH-003.

Casos de uso del Core (actor: Comercializadora; exigen X-Api-Key, si no AUTH-004)
  CU-13 Verificar sujeto de crédito (servicio 1)
                                          GET /clientes/sujeto-credito?cedula=
        Flujo: aplica RN-01 a RN-04 y responde sí/no con los motivos.
  CU-14 Calcular monto máximo (servicio 2) GET /clientes/{cod}/monto-maximo
        Flujo: aplica RN-05.  Alt.: cliente inexistente (CLI-001).
  CU-15 Otorgar crédito (servicio 3)      POST /creditos
        Flujo: en una sola transacción, con el cliente bloqueado: vuelve a
              verificar RN-01 a RN-05, registra el crédito ACTIVO y su tabla
              de amortización (RN-06 a RN-08).
        Alt.: no es sujeto (CRE-001); supera el monto máximo (CRE-002);
              cuotas fuera de 3..18 (CRE-003); monto inválido (CRE-005).
  CU-16 Consultar tabla de amortización (servicio 4)
                                          GET /creditos/{cod}/cuotas
        Alt.: crédito inexistente (CRE-004).

------------------------------------------------------------------------------
3. Reglas de negocio
------------------------------------------------------------------------------
  RN-01 El comprador debe ser cliente del banco (en efectivo y a crédito).
  RN-02 Sujeto de crédito: tiene al menos un depósito en el último mes.
  RN-03 Sujeto de crédito: si es hombre, tiene 25 años o más.
  RN-04 Sujeto de crédito: no tiene un crédito ACTIVO.
  RN-05 Monto máximo = ((promedio de depósitos - promedio de retiros) * 30 %) * 6,
        con los movimientos de los últimos 3 meses de todas sus cuentas; nunca < 0.
        El total de una venta a crédito no puede superarlo.
  RN-06 Crédito de cuota fija, pago mensual, plazo de 3 a 18 meses, tasa anual
        de 16.5 % (mensual = 16.5 % / 12).
  RN-07 Cuota = monto * i / (1 - (1 + i)^-n), redondeada a 2 decimales (HALF_UP).
        Por cuota: interés = saldo * i; capital = cuota - interés.
  RN-08 La última cuota abona todo el saldo como capital y el interés absorbe el
        centavo del redondeo: la cuota sigue fija y el saldo cierra en 0.00.
        Ejemplo del enunciado: 450 a 6 cuotas -> cuota 78.65.
  RN-09 Pago en efectivo: 42 % de descuento sobre el subtotal. A crédito: sin
        descuento.
  RN-10 Los precios se toman del catálogo y el empleado de la factura sale de la
        sesión, nunca de lo que envía el cliente.
  RN-11 Eliminar un teléfono es una baja lógica (INACTIVO).

------------------------------------------------------------------------------
4. Requisitos no funcionales (resumen)
------------------------------------------------------------------------------
  RNF-01 Arquitectura MVC por capas; dos aplicaciones Web con BD separadas.
  RNF-02 Servicios REST con JSON, versionados en /api/v1.
  RNF-03 Seguridad: login con token (30 min de inactividad) en la
         comercializadora; claves guardadas en SHA-256; clave de API entre
         servidores; CORS solo para los orígenes configurados.
  RNF-04 Integridad: otorgar un crédito y guardar una factura son transacciones
         (todo o nada); dos ventas simultáneas al mismo cliente no generan dos
         créditos (bloqueo de fila).
  RNF-05 Errores uniformes en application/problem+json con código de error y
         estado HTTP correcto (400, 401, 404, 409, 502, 503).
  RNF-06 Conexión a la BD con el patrón Singleton; credenciales fuera del
         código (db.properties, no se sube al repositorio).
  RNF-07 Disponibilidad: si el Core no responde en 5 s (conexión) o 10 s
         (respuesta), la comercializadora responde EXT-001 sin quedarse colgada.
  RNF-08 Pruebas automáticas ejecutables sin framework (clases con main()).

------------------------------------------------------------------------------
5. Configuración (una sola vez)
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
6. Compilar y desplegar (PowerShell, desde esta carpeta)
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
7. Endpoints del Core (todos exigen X-Api-Key)
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
8. Endpoints de la Comercializadora (todos exigen token, salvo POST sesiones)
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
9. Pruebas (clases con main() en el paquete pruebas)
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

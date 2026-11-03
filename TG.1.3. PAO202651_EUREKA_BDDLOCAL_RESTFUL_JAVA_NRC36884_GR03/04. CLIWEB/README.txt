EurekaBank - Grupo 03 - Cliente web REST (Java)
================================================

WS_EUREKA_JAVA_REST_CLIWEB es el cliente web de la API REST WS_EUREKA_JAVA_REST
(04. SERVIDOR). Tiene el mismo diseño y comportamiento que el cliente web SOAP de
TG.1.1 (y que el cliente de escritorio): mismas pantallas (login, menú, consultas y
transacciones con confirmación y comprobante), mismas validaciones y mensajes, y la
misma identidad "Azul" (colores, tipografía Plus Jakarta Sans, iconos y medidas).

Lo único que cambia es la comunicación con el servidor:
  - ClienteRESTEureka (el mismo del cliente de consola REST de TG.1.3) llama a la API
    con java.net.http.HttpClient y JSON (Gson), y envía el token en la cabecera
    Authorization: Bearer.
  - Los errores llegan como JSON {estado, codigo, mensaje} y se muestran con su código
    (por ejemplo, MOV-002); AUTH-003 (sesión vencida) devuelve al login.

Tecnologías: Servlets (controladores) + JSP/JSTL con tag files (vistas), Jakarta EE 11,
Payara 7, Maven. El WAR incluye Gson 2.11.0 (Payara no la trae).

Arquitectura MVC (paquete ec.edu.monster)
  modelo        Los mismos modelos de los clientes de consola y web
  servicio      ClienteRESTEureka (el "equipo" del login es la IP del navegador)
  controlador   LoginControlador, MenuControlador, CuentaControlador,
                TransaccionControlador (Servlets) + FiltroSesion, FiltroCsrf, EscuchaSesion
  vista         WEB-INF/vista/*.jsp (pantallas), WEB-INF/tags/*.tag (componentes),
                css/eureka.css (tema), Formato y Fichas (funciones EL en WEB-INF/eureka.tld)
  pruebas       PruebaClienteWeb (main, sin framework)

Seguridad web
  - Sesión del navegador de 30 min (igual que el token); al terminar, también se
    cierra la sesión en la API REST.
  - Token CSRF en todos los formularios POST; cookie http-only y SameSite=Lax.
  - Las páginas con datos no se guardan en la caché del navegador.

Requisitos
  - MySQL iniciado (net start MySQL267) con la BD de 03. BDD cargada.
  - Payara con WS_EUREKA_JAVA_REST desplegado (API en /WS_EUREKA_JAVA_REST/api).

Compilar y desplegar (PowerShell, desde WS_EUREKA_JAVA_REST_CLIWEB)
  $mvn = "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd"
  $asadmin = "C:\payara7\payara-7.2026.9\payara7\bin\asadmin.bat"
  & $asadmin start-domain domain1
  & $mvn package
  & $asadmin deploy --force=true target\WS_EUREKA_JAVA_REST_CLIWEB-1.0-SNAPSHOT.war

  Abrir: http://localhost:8080/WS_EUREKA_JAVA_REST_CLIWEB/
  Usuario de prueba: MONSTER / MONSTER9

Servidor en otro equipo
  Cambiar servidor.url en src/main/resources/servidor.properties (incluye /api) o
  iniciar Payara con -Dservidor.url=http://IP:8080/WS_EUREKA_JAVA_REST/api (tiene prioridad).

Rutas
  GET  /login           Iniciar sesión          POST /login
  GET  /menu            Menú principal          POST /salir (cerrar sesión)
  GET  /consultas?modo=cuenta|movimientos&cuenta=00100001
  GET  /transacciones?tipo=DEPOSITO|RETIRO|TRANSFERENCIA
  POST /transacciones/preparar   valida y pide confirmación
  POST /transacciones/registrar  registra lo confirmado y redirige al comprobante
                                 (recargar la página no repite la operación)

Prueba
  Con todo desplegado, en NetBeans: clic derecho en pruebas/PruebaClienteWeb > Run File,
  o por consola desde WS_EUREKA_JAVA_REST_CLIWEB:
    java -cp target\classes ec.edu.monster.pruebas.PruebaClienteWeb
  Debe terminar en PRUEBA APROBADA. Cada ejecución deposita 1.00 en la cuenta 00100001.

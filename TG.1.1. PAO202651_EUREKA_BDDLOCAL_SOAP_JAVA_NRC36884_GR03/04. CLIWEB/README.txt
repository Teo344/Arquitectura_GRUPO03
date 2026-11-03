EurekaBank - Grupo 03 - Cliente web SOAP (Java)
================================================

WS_EUREKA_JAVA_SOAP_CLIWEB es el cliente web del servidor SOAP WS_EUREKA_JAVA_SOAP
(04. SERVIDOR). Es el mismo cliente de escritorio (04. CLIESC) llevado al navegador:
  - Mismo cliente SOAP: ContratoWSEureka + ClienteWSEureka (token en la cabecera
    SOAP, errores leídos del detalle ErrorServicio de los SOAP Fault).
  - Mismo modelo, mismas validaciones y mensajes, mismas pantallas (login, menú,
    consultas y transacciones con confirmación y comprobante) y mismo diseño
    "Azul" (colores, tipografía Plus Jakarta Sans, iconos y medidas de Tema.java).

Tecnologías: Servlets (controladores) + JSP/JSTL con tag files (vistas), Jakarta EE 11,
JAX-WS (Metro, incluido en Payara), Payara 7, Maven. Sin dependencias en el WAR.

Arquitectura MVC (paquete ec.edu.monster)
  modelo        Copiado del cliente de escritorio sin cambios
  servicio      ContratoWSEureka y ClienteWSEureka del escritorio (el "equipo" del
                login es la IP del navegador)
  controlador   LoginControlador, MenuControlador, CuentaControlador,
                TransaccionControlador (Servlets) + FiltroSesion, FiltroCsrf, EscuchaSesion
  vista         WEB-INF/vista/*.jsp (pantallas), WEB-INF/tags/*.tag (los componentes
                de Componentes.java), css/eureka.css (Tema.java), Formato y Fichas
  pruebas       PruebaClienteWeb (main, sin framework)

Seguridad web
  - Sesión del navegador de 30 min (igual que el token); al terminar, también se
    invalida el token en el servidor SOAP.
  - Token CSRF en todos los formularios POST; cookie http-only y SameSite=Lax.
  - Las páginas con datos no se guardan en la caché del navegador.

Requisitos
  - MySQL iniciado (net start MySQL267) con la BD de 03. BDD cargada.
  - Payara con WS_EUREKA_JAVA_SOAP desplegado en /WS_EUREKA_JAVA_SOAP.

Compilar y desplegar (PowerShell, desde WS_EUREKA_JAVA_SOAP_CLIWEB)
  $mvn = "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd"
  $asadmin = "C:\payara7\payara-7.2026.9\payara7\bin\asadmin.bat"
  & $asadmin start-domain domain1
  & $mvn package
  & $asadmin deploy --force=true target\WS_EUREKA_JAVA_SOAP_CLIWEB-1.0-SNAPSHOT.war

  Abrir: http://localhost:8080/WS_EUREKA_JAVA_SOAP_CLIWEB/
  Usuario de prueba: MONSTER / MONSTER9

Servidor en otro equipo
  Cambiar servidor.url en src/main/resources/servidor.properties o iniciar Payara con
  -Dservidor.url=http://IP:8080/WS_EUREKA_JAVA_SOAP (tiene prioridad).

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
  o por consola desde WS_EUREKA_JAVA_SOAP_CLIWEB:
    java -cp target\classes ec.edu.monster.pruebas.PruebaClienteWeb
  Debe terminar en PRUEBA APROBADA. Cada ejecución deposita 1.00 en la cuenta 00100001.

MonsterCell - Grupo 03 - Cliente web de la comercializadora (REST Java)
=======================================================================

WS_COMERCIALIZADORA_JAVA_REST_CLIWEB es el cliente web que usan los empleados de la tienda.
Consume la API REST WS_COMERCIALIZADORA_JAVA_REST (/api/v1), que a su vez consulta al Core
BanQuito para el crédito directo. El navegador nunca habla con el banco.

  Navegador ── WS_COMERCIALIZADORA_JAVA_REST_CLIWEB ── Bearer ──▶ WS_COMERCIALIZADORA_JAVA_REST
                                                                       │ X-Api-Key
                                                                       ▼
                                                               WS_BANQUITO_JAVA_REST

Diseño: la misma identidad visual "Azul" de los clientes de EurekaBank (colores, tipografía
Plus Jakarta Sans, iconos, radios y espaciado) y la foto del grupo en el acceso. Los componentes
de la tienda (tarjeta de producto, carrito con contador, tablas, factura, evaluación del banco)
están hechos solo con esos mismos tokens (css/monstercell.css, sección "Tienda").

Navegación
  Después del login todas las pantallas llevan una barra lateral con la marca, las secciones
  (Ventas: Nueva venta y Facturas; Catálogo: Teléfonos; Crédito BanQuito: Consultar cliente y
  Simulador de cuotas), las unidades de la venta en curso, el empleado, la vigencia de la sesión y
  Cerrar sesión. En pantallas de menos de 1024 px se abre como panel con el botón de menú.

Pantallas
  /login        Acceso de empleados (MONSTER / MONSTER9)
  /venta        Pantalla de inicio (/menu también lleva aquí). Punto de venta: catálogo a la
                izquierda (búsqueda en vivo) y la venta actual a la derecha en 4 pasos: 1) comprador verificado en BanQuito, 2) teléfonos con
                cantidades, 3) efectivo (−42 %) o crédito BanQuito con número de cuotas, cuota
                estimada y tabla, 4) totales y Facturar (con confirmación)
  /facturas     Facturas emitidas, filtro por cédula
  /facturas/N   Factura lista para imprimir (Imprimir oculta la barra y los botones)
  /telefonos    Catálogo: agregar, editar y dar de baja (baja lógica)
  /clientes     Crédito BanQuito: sujeto de crédito, monto máximo y créditos de un cliente
  /creditos/N   Tabla de amortización de un crédito
  /simulador    Tabla de cuotas sin otorgar el crédito

Arquitectura MVC (paquete ec.edu.monster), igual a los clientes web de EurekaBank
  modelo        Sesión, teléfono, carrito (vive en la sesión), evaluación, crédito, cuota, factura
  servicio      ClienteRESTMonsterCell: única clase que conoce HTTP/JSON (Gson + HttpClient);
                token en Authorization: Bearer; errores application/problem+json con su código
  controlador   Login, Menu, Venta, Factura, Telefono y Credito (Servlets) + filtros de sesión y
                CSRF, y escucha que cierra el token al terminar la sesión
  vista         WEB-INF/vista/*.jsp, WEB-INF/tags/*.tag, css/monstercell.css, js/monstercell.js

Reglas que se cuidan en la pantalla (el servidor las vuelve a validar)
  - Solo los clientes de BanQuito pueden comprar; a crédito, además, deben ser sujetos de crédito
    y el total no puede superar su monto máximo. Facturar se deshabilita y se explica el motivo.
  - Los precios nunca salen del navegador: la API los toma del catálogo al facturar.
  - Recargar después de facturar no repite la venta (Post/Redirect/Get).

Requisitos
  - MySQL iniciado con las BD banquito y comercializadora cargadas (03. BDD/README.txt).
  - Payara con WS_BANQUITO_JAVA_REST y WS_COMERCIALIZADORA_JAVA_REST desplegados (04. SERVIDOR).

Compilar y desplegar (PowerShell, desde WS_COMERCIALIZADORA_JAVA_REST_CLIWEB)
  $mvn = "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd"
  $asadmin = "C:\payara7\payara-7.2026.9\payara7\bin\asadmin.bat"
  & $asadmin start-domain domain1
  & $mvn package
  & $asadmin deploy --force=true target\WS_COMERCIALIZADORA_JAVA_REST_CLIWEB-1.0-SNAPSHOT.war

  Abrir: http://localhost:8080/WS_COMERCIALIZADORA_JAVA_REST_CLIWEB/
  Usuario: MONSTER / MONSTER9

Servidor en otro equipo
  Cambiar servidor.url en src/main/resources/servidor.properties (incluye /api/v1) o iniciar
  Payara con -Dservidor.url=http://IP:8080/WS_COMERCIALIZADORA_JAVA_REST/api/v1

Prueba
  Con todo desplegado y las BD recién cargadas (la venta a crédito al cliente 1 se aprueba una
  sola vez por carga), en NetBeans: clic derecho en pruebas/PruebaClienteWeb > Run File, o:
    java -cp target\classes ec.edu.monster.pruebas.PruebaClienteWeb
  Recorre login, CSRF, catálogo, venta en efectivo (392.66), créditos rechazados (no sujeto y
  monto máximo), crédito aprobado con su tabla, facturas, simulador y salir. Debe terminar en
  PRUEBA APROBADA.

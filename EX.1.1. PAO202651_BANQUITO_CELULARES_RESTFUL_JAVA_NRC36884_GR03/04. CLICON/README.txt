MonsterCell - Grupo 03 - Cliente de consola de la comercializadora (REST Java)
==============================================================================

WS_COMERCIALIZADORA_JAVA_REST_CLICON es el cliente de consola que usan los empleados de la
tienda. Consume la API REST WS_COMERCIALIZADORA_JAVA_REST (/api/v1), que a su vez consulta al
Core BanQuito para el crédito directo. La consola no se conecta a MySQL ni al banco.

  Consola ── Bearer ──▶ WS_COMERCIALIZADORA_JAVA_REST ── X-Api-Key ──▶ WS_BANQUITO_JAVA_REST
                             BD comercializadora                          BD banquito

Navegación
  Después del login, un menú numérico con las secciones de los clientes web y de escritorio:
  Ventas (Nueva venta y Facturas), Catálogo (Teléfonos), Crédito BanQuito (Consultar cliente y
  Simulador de cuotas) y Sesión (Cerrar sesión y Salir). La venta en curso se conserva al volver
  al menú principal y sus unidades se muestran junto a Nueva venta. Si la sesión vence (AUTH-003),
  la consola vuelve al inicio de sesión.

Pantallas (las mismas de los clientes web y de escritorio)
  Login              Acceso de empleados (MONSTER / MONSTER9); la clave no se muestra en la terminal
  Nueva venta        La venta actual en 4 pasos: 1) comprador verificado en BanQuito, 2) teléfonos
                     con cantidades, 3) efectivo (-42 %) o crédito BanQuito con número de cuotas y
                     tabla estimada, 4) totales y Facturar (con confirmación). A crédito, la factura
                     se muestra con su tabla de amortización
  Facturas           Facturas emitidas con filtro por cédula; cada factura con su detalle y, si es a
                     crédito, su tabla de amortización
  Teléfonos          Catálogo: ver, agregar, editar y dar de baja (baja lógica, con confirmación)
  Consultar cliente  Sujeto de crédito, motivos, monto máximo, créditos y tabla de cada crédito
  Simulador          Tabla de cuotas sin otorgar el crédito

Arquitectura MVC (paquete ec.edu.monster), la misma de las consolas de EurekaBank
  modelo        Los mismos de los clientes web y de escritorio: sesión, teléfono, carrito,
                evaluación, crédito, cuota, factura y ErrorServicioModelo; además LoginModelo
  servicio      ClienteRESTMonsterCell: la misma clase de los otros clientes, única que conoce
                HTTP/JSON (Gson + HttpClient); token en Authorization: Bearer; errores
                application/problem+json con su código
  controlador   Login, Venta, Factura, Telefono, Credito y Simulador. Validan y coordinan: devuelven
                datos o lanzan una excepción, nunca imprimen ni leen el teclado. Validacion: los
                mismos límites y mensajes de los otros clientes
  vista         LoginVista, MenuVista y una vista por sección. Solo capturan datos e imprimen
                resultados; MensajesVista da el formato a los mensajes, ComponentesTienda imprime
                catálogo, carrito, evaluación, factura y tabla de amortización, y Formato y
                ResumenCredito son los de los otros clientes
  util          EntradaUtil: lectura del teclado; solo convierte tipos

Reglas que se cuidan en la pantalla (el servidor las vuelve a validar)
  - Solo los clientes de BanQuito pueden comprar; a crédito, además, deben ser sujetos de crédito
    y el total no puede superar su monto máximo. La venta muestra lo pendiente antes de facturar.
  - Los precios nunca salen del cliente: la API los toma del catálogo al facturar.

Requisitos
  - JDK 21 o superior.
  - MySQL iniciado con las BD banquito y comercializadora cargadas (03. BDD/README.txt).
  - Payara con WS_BANQUITO_JAVA_REST y WS_COMERCIALIZADORA_JAVA_REST desplegados, cada uno con su
    src/main/resources/db.properties (copia de db.properties.example; ver 04. SERVIDOR/README.txt).

Compilar y ejecutar (PowerShell, desde WS_COMERCIALIZADORA_JAVA_REST_CLICON)
  $mvn = "C:\dev\apache-maven-3.9.11\bin\mvn.cmd"
  & $mvn package
  & $mvn exec:java
  En NetBeans: abrir el proyecto y Run (clase principal WS_COMERCIALIZADORA_JAVA_REST_CLICON).
  En la ventana Output de NetBeans la clave se ve al escribirla; en una terminal no.
  Usuario: MONSTER / MONSTER9

Servidor en otro equipo
  Cambiar servidor.url en src/main/resources/servidor.properties (incluye /api/v1) o ejecutar
  con -Dservidor.url=http://IP:8080/WS_COMERCIALIZADORA_JAVA_REST/api/v1

Prueba
  Con todo desplegado y las BD recién cargadas (la venta a crédito al cliente 1 se aprueba una
  sola vez por carga), en NetBeans: clic derecho en pruebas/PruebaClienteConsola > Run File.
  Recorre con las capas de servicio y modelo de la consola: login (AUTH-001), catálogo, venta en
  efectivo (392.66), créditos rechazados (CRE-001 y CRE-002), crédito aprobado con su tabla,
  facturas, simulador (78.65) y salir. Debe terminar en PRUEBA APROBADA.

MonsterCell - Grupo 03 - Cliente de escritorio de la comercializadora (REST Java Swing)
======================================================================================

WS_COMERCIALIZADORA_JAVA_REST_CLIESC es el cliente de escritorio que usan los empleados de la
tienda. Consume la API REST WS_COMERCIALIZADORA_JAVA_REST (/api/v1), que a su vez consulta al
Core BanQuito para el crédito directo. El escritorio no se conecta a MySQL ni al banco.

  Escritorio (Swing) ── Bearer ──▶ WS_COMERCIALIZADORA_JAVA_REST ── X-Api-Key ──▶ WS_BANQUITO_JAVA_REST
                                         BD comercializadora                          BD banquito

Diseño: la misma identidad visual "Azul" del cliente web y de los escritorios de EurekaBank
(colores, Plus Jakarta Sans, iconos, radios y espaciado) y la foto del grupo en el acceso.

Navegación
  Después del login, una sola ventana con barra lateral: la marca, las secciones (Ventas: Nueva
  venta y Facturas; Catálogo: Teléfonos; Crédito BanQuito: Consultar cliente y Simulador de
  cuotas), las unidades de la venta en curso, el empleado, la vigencia de la sesión y Cerrar
  sesión. La venta en curso se conserva al cambiar de sección. Cerrar la ventana con la sesión
  abierta también invalida el token.

Pantallas (las mismas del cliente web)
  Login            Acceso de empleados (MONSTER / MONSTER9)
  Nueva venta      Catálogo con búsqueda en vivo a la izquierda y la venta actual a la derecha en
                   4 pasos: 1) comprador verificado en BanQuito, 2) teléfonos con cantidades,
                   3) efectivo (−42 %) o crédito BanQuito con número de cuotas, cuota estimada y
                   tabla, 4) totales y Facturar (con confirmación)
  Facturas         Facturas emitidas con filtro por cédula; cada factura con su detalle, Imprimir
                   y, si es a crédito, su tabla de amortización
  Teléfonos        Catálogo: agregar, editar y dar de baja (baja lógica, con confirmación)
  Consultar cliente  Sujeto de crédito, motivos, monto máximo, créditos y tabla de cada crédito
  Simulador        Tabla de cuotas sin otorgar el crédito

Arquitectura MVC (paquete ec.edu.monster), la misma del cliente web
  modelo        Los mismos del cliente web: sesión, teléfono, carrito, evaluación, crédito,
                cuota, factura y ErrorServicioModelo (código de error de la API)
  servicio      ClienteRESTMonsterCell: la misma clase del cliente web, única que conoce
                HTTP/JSON (Gson + HttpClient); token en Authorization: Bearer; errores
                application/problem+json con su código
  controlador   Login, Principal (barra lateral y salir), Venta, Factura, Telefono, Credito y
                Simulador. ControladorBase llama a la API en un hilo aparte (la ventana no se
                congela) y vuelve al hilo de Swing; con AUTH-003 regresa al login.
                Validacion: los mismos límites y mensajes del cliente web
  vista         Ventana, LoginVista, PrincipalVista y una vista por sección; Tema, Iconos,
                Componentes (los de EurekaBank) y ComponentesTienda (catálogo, carrito, tablas,
                evaluación del banco)

Reglas que se cuidan en la pantalla (el servidor las vuelve a validar)
  - Solo los clientes de BanQuito pueden comprar; a crédito, además, deben ser sujetos de crédito
    y el total no puede superar su monto máximo. Facturar se deshabilita y se explica el motivo.
  - Los precios nunca salen del cliente: la API los toma del catálogo al facturar.

Requisitos
  - JDK 21 o superior.
  - MySQL iniciado con las BD banquito y comercializadora cargadas (03. BDD/README.txt).
  - Payara con WS_BANQUITO_JAVA_REST y WS_COMERCIALIZADORA_JAVA_REST desplegados, cada uno con su
    src/main/resources/db.properties (copia de db.properties.example; ver 04. SERVIDOR/README.txt).

Compilar y ejecutar (PowerShell, desde WS_COMERCIALIZADORA_JAVA_REST_CLIESC)
  $mvn = "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd"
  & $mvn package
  & $mvn exec:java
  En NetBeans: abrir el proyecto y Run (clase principal WS_COMERCIALIZADORA_JAVA_REST_CLIESC).
  Usuario: MONSTER / MONSTER9

Servidor en otro equipo
  Cambiar servidor.url en src/main/resources/servidor.properties (incluye /api/v1) o ejecutar
  con -Dservidor.url=http://IP:8080/WS_COMERCIALIZADORA_JAVA_REST/api/v1

Prueba
  Con todo desplegado y las BD recién cargadas (la venta a crédito al cliente 1 se aprueba una
  sola vez por carga), en NetBeans: clic derecho en pruebas/PruebaClienteEscritorio > Run File.
  Recorre con las capas de servicio y modelo del escritorio: login (AUTH-001), catálogo, venta en
  efectivo (392.66), créditos rechazados (CRE-001 y CRE-002), crédito aprobado con su tabla,
  facturas, simulador (78.65) y salir. Debe terminar en PRUEBA APROBADA.

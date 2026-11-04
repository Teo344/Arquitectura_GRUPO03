MonsterCell - Grupo 03 - Cliente móvil de la comercializadora (REST Android)
============================================================================

WS_COMERCIALIZADORA_JAVA_REST_CLIMOV es el cliente Android que usan los empleados de la tienda.
Consume la API REST WS_COMERCIALIZADORA_JAVA_REST (/api/v1), que a su vez consulta al Core BanQuito
para el crédito directo. El móvil no se conecta a MySQL ni al banco.

  Móvil (Android) ── Bearer ──▶ WS_COMERCIALIZADORA_JAVA_REST ── X-Api-Key ──▶ WS_BANQUITO_JAVA_REST
                                     BD comercializadora                         BD banquito

Diseño: el mismo del cliente web, con la identidad visual "Azul" (colores, Plus Jakarta Sans, iconos,
radios y espaciado) y la foto del grupo en el acceso. Es responsive con los cortes de 600 y 840 dp: en
pantalla amplia la venta se muestra a dos columnas, como en la web.

Navegación
  Después del login, un menú con las secciones de la barra lateral web: Ventas (Nueva venta y
  Facturas), Catálogo (Teléfonos) y Crédito BanQuito (Consultar cliente y Simulador de cuotas), con la
  vigencia de la sesión y Salir. La venta en curso se conserva al volver al menú y sus unidades se
  muestran en Nueva venta. Si la sesión vence (AUTH-003), la app vuelve al inicio de sesión.

Pantallas (las mismas de los clientes web, escritorio y consola)
  Login              Acceso de empleados (MONSTER / MONSTER9)
  Nueva venta        Catálogo con búsqueda en vivo y la venta actual en 4 pasos: 1) comprador
                     verificado en BanQuito, 2) teléfonos con cantidades, 3) efectivo (−42 %) o crédito
                     BanQuito con número de cuotas, cuota estimada y tabla, 4) totales y Facturar (con
                     confirmación). En pantallas angostas una barra inferior lleva a la venta
  Facturas           Facturas emitidas con filtro por cédula; cada factura con su detalle y, si es a
                     crédito, su tabla de amortización
  Teléfonos          Catálogo: agregar, editar y dar de baja (baja lógica, con confirmación)
  Consultar cliente  Sujeto de crédito, motivos, monto máximo, créditos y tabla de cada crédito
  Simulador          Tabla de cuotas sin otorgar el crédito

Arquitectura MVC (paquete ec.edu.monster), la misma de los móviles de EurekaBank
  El desarrollo es en Java; solo las pantallas Compose de la vista están en Kotlin.
  modelo        Los mismos de los otros clientes: sesión, teléfono, carrito, evaluación, crédito,
                cuota, factura y ErrorServicioModelo
  servicio      ClienteRest: única clase que conoce HTTP/JSON (HttpURLConnection + org.json); token en
                Authorization: Bearer; errores application/problem+json con su código.
                AutenticacionRestService, TelefonoRestService, CreditoRestService y FacturaRestService
                conocen las rutas de la API
  controlador   Login, Menu, Venta, Factura, Telefono, Credito y Simulador. ControladorBase llama a la
                API en un hilo aparte y vuelve al hilo de la interfaz; con AUTH-003 regresa al login.
                Validacion: los mismos límites y mensajes de los otros clientes
  vista         Una Activity por sección, Tema, Componentes y ComponentesTienda (catálogo, carrito,
                evaluación, tabla de amortización y factura) y ResumenCredito. Solo capturan datos y
                muestran lo que entrega el controlador

Reglas que se cuidan en la pantalla (el servidor las vuelve a validar)
  - Solo los clientes de BanQuito pueden comprar; a crédito, además, deben ser sujetos de crédito y el
    total no puede superar su monto máximo. Facturar se deshabilita y se explica el motivo.
  - Al cambiar la cédula hay que volver a verificarla antes de facturar.
  - Los precios nunca salen del cliente: la API los toma del catálogo al facturar.

Requisitos
  - Android Studio con el SDK 37 y un emulador o celular con Android 7.0 (API 24) o superior.
  - MySQL iniciado con las BD banquito y comercializadora cargadas (03. BDD/README.txt).
  - Payara con WS_BANQUITO_JAVA_REST y WS_COMERCIALIZADORA_JAVA_REST desplegados, cada uno con su
    src/main/resources/db.properties (copia de db.properties.example; ver 04. SERVIDOR/README.txt).

Compilar y ejecutar
  En Android Studio: File > Open, elegir la carpeta 04. CLIMOV, esperar la sincronización de Gradle,
  elegir el emulador (por ejemplo Pixel_6) y Run 'app'.
  Usuario: MONSTER / MONSTER9
  En Android 17 la app pide permiso de red local al ingresar: hay que aceptarlo.

Servidor en otro equipo
  El emulador llega al equipo con 10.0.2.2. Para un celular físico cambiar servidor.url en
  gradle.properties por la IP del equipo (incluye /api/v1), por ejemplo
  servidor.url=http://192.168.1.10:8080/WS_COMERCIALIZADORA_JAVA_REST/api/v1
  La compilación debug acepta HTTP hacia cualquier IP; la release solo hacia las direcciones de
  app/src/main/res/xml/network_security_config.xml.

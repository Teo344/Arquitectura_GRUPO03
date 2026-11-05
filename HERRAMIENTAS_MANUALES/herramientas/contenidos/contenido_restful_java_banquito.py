# Contenido del manual de EX.1.1: BanQuito y Comercializadora de Celulares, RESTful Java - GR03
# Formato de referencia: manual de TG.1.3 (pasos numerados sin título). Las imágenes son marcadores.
from pathlib import Path

from docxlib import Doc, t

# Raíz del repositorio: contenidos/ -> herramientas/ -> HERRAMIENTAS_MANUALES/ -> repo
REPO = Path(__file__).resolve().parents[3]
RAIZ = REPO / 'EX.1.1. PAO202651_BANQUITO_CELULARES_RESTFUL_JAVA_NRC36884_GR03'
BDD = RAIZ / '03. BDD'
JAVA = 'src/main/java/ec/edu/monster/'
CORE = RAIZ / '04. SERVIDOR/WS_BANQUITO_JAVA_REST'
COM = RAIZ / '04. SERVIDOR/WS_COMERCIALIZADORA_JAVA_REST'
CON = RAIZ / '04. CLICON/WS_COMERCIALIZADORA_JAVA_REST_CLICON'
ESC = RAIZ / '04. CLIESC/WS_COMERCIALIZADORA_JAVA_REST_CLIESC'
WEB = RAIZ / '04. CLIWEB/WS_COMERCIALIZADORA_JAVA_REST_CLIWEB'
MOV = RAIZ / '04. CLIMOV'
MOVA = MOV / 'app/src/main'
MOVJ = MOVA / 'java/ec/edu/monster'

# Textos de la portada de plantilla_gr03.docx
TEMA = 'SISTEMA BANQUITO Y COMERCIALIZADORA DE CELULARES CON RESTFUL Y BDD LOCAL EN '
PORTADA = [
    ('SISTEMA CONVERSIÓN UNIDADES MONSTER CON SOAP SIN BDD EN ', TEMA),
    ('SISTEMA CONVERSIÓN DE UNIDADES CON SOAP SIN BDD EN ', TEMA),
    ('DOTNET', 'JAVA'),
    ('24/', '04/'),
    ('10', '11'),
]


def leer(ruta):
    with open(ruta, encoding='utf-8-sig') as f:
        return f.read()


class DocEx(Doc):
    """Igual que Doc, pero con los pasos sin título: 'Paso N', como en el manual de TG.1.3."""

    def paso_(self, titulo=None):
        self.paso += 1
        self.add('<w:p><w:pPr><w:pStyle w:val="Ttulo3"/><w:numPr><w:ilvl w:val="2"/><w:numId w:val="5"/></w:numPr>'
                 '<w:rPr><w:rFonts w:eastAsia="Calibri"/></w:rPr></w:pPr>'
                 f'<w:r><w:rPr><w:rFonts w:eastAsia="Calibri"/></w:rPr>{t(f"Paso {self.paso}")}</w:r></w:p>')

    def archivo(self, titulo, ruta):
        self.codigo(titulo, leer(ruta))


def construir():
    d = DocEx()
    introduccion(d)
    objetivos(d)
    marco_teorico(d)
    d.h1_('DESARROLLO DEL APLICATIVO')
    analisis_diseno(d)
    base_datos(d)
    servidor_core(d)
    servidor_comercializadora(d)
    cliente_consola(d)
    cliente_escritorio(d)
    cliente_movil(d)
    cliente_web(d)
    conclusiones(d)
    recomendaciones(d)
    referencias(d)
    return d


# ---------------------------------------------------------------------- INTRODUCCIÓN
def introduccion(d):
    d.h1_('INTRODUCCIÓN')
    d.p('El Banco BanQuito ha formado una alianza estratégica con una compañía comercializadora de teléfonos celulares '
        'para ofrecer una línea de crédito directo a los clientes del banco que deseen adquirir un teléfono de acuerdo '
        'con su capacidad de pago. Para resolver este caso de estudio, propuesto en el examen práctico, se construyeron '
        'dos aplicaciones web independientes, cada una con su propia base de datos MySQL local: el **Core BanQuito** con '
        'su módulo de crédito y la **Comercializadora de Teléfonos Celulares**, cuya tienda se presenta a los empleados '
        'con el nombre MonsterCell.')
    d.p('El Core BanQuito (WS_BANQUITO_JAVA_REST) solo expone servicios web: verifica si una persona es sujeto de crédito '
        'por su cédula, calcula el monto máximo de crédito por código de cliente, otorga el crédito registrando su tabla '
        'de amortización en una sola transacción y permite consultar esa tabla. La Comercializadora '
        '(WS_COMERCIALIZADORA_JAVA_REST) gestiona las sesiones de los empleados, el catálogo de teléfonos y la facturación '
        'con dos formas de pago: efectivo, con 42 % de descuento, y crédito directo, que el Core aprueba automáticamente '
        'si el comprador cumple las reglas del banco y el total no supera su monto máximo.')
    d.p('Ambas aplicaciones son servicios RESTful desarrollados en Java con JAX-RS y JSON-B y se despliegan en Payara Server. '
        'Sus recursos se publican bajo la ruta versionada /api/v1 e intercambian JSON; los errores se devuelven en el '
        'formato application/problem+json (RFC 9457). La seguridad tiene dos niveles: los empleados se autentican ante la '
        'Comercializadora con un token que viaja en la cabecera `Authorization: Bearer` y la Comercializadora se autentica '
        'ante el Core con una clave de API en la cabecera `X-Api-Key`.')
    d.p('Cuatro clientes consumen únicamente la Comercializadora: una aplicación de consola en Java, una de escritorio en '
        'Java Swing, una web con Servlets y JSP, y una móvil en Android con Java (modelo, controlador y servicio) y Kotlin '
        'con Jetpack Compose (vista). Ningún cliente accede a MySQL ni al Core: las reglas de crédito residen solo en el '
        'Core y las de facturación solo en la Comercializadora, por lo que todos los clientes obtienen el mismo resultado.')


# ---------------------------------------------------------------------- OBJETIVOS
def objetivos(d):
    d.h1_('OBJETIVOS')
    d.h2('Objetivo General')
    d.p('Documentar la configuración, la implementación y el consumo de los servicios RESTful en Java del Core BanQuito y de '
        'la Comercializadora de Teléfonos Celulares, con bases de datos MySQL locales independientes, para que el equipo '
        'pueda desplegarlos, utilizarlos desde los clientes de consola, escritorio, móvil y web, y defender cada ítem de la '
        'rúbrica del examen práctico.')
    d.h2('Objetivos Específicos')
    d.vineta('Describir el diseño de la solución y las dos bases de datos: banquito, con el modelo del Core más las tablas '
             'del módulo de crédito, y comercializadora, con el modelo de facturación.')
    d.vineta('Explicar la implementación de los cuatro servicios del módulo de crédito (sujeto de crédito, monto máximo, '
             'otorgamiento y tabla de amortización) y de los recursos de sesión, catálogo, crédito y facturación de la '
             'Comercializadora.')
    d.vineta('Explicar la integración entre las dos aplicaciones mediante la pasarela PasarelaBanquito, la clave de API y el '
             'manejo de las fallas del Core.')
    d.vineta('Proporcionar una guía para desarrollar y ejecutar los cuatro clientes con el patrón MVC, incluidas la venta en '
             'efectivo, la venta con crédito directo y la consulta de la tabla de amortización.')


# ---------------------------------------------------------------------- MARCO TEÓRICO
def marco_teorico(d):
    d.h1_('MARCO TEÓRICO')
    d.h2('Servicios web RESTful y JSON')
    d.p('Un servicio web RESTful sigue el estilo de arquitectura REST (Representational State Transfer): la información se '
        'organiza en recursos, cada uno identificado por una URL, y los clientes operan sobre ellos con los métodos estándar '
        'de HTTP: GET consulta, POST crea, PUT reemplaza y DELETE elimina. Cada petición es independiente (sin estado), por '
        'lo que debe incluir toda la información necesaria, como el token de autenticación en una cabecera.')
    d.p('Los mensajes se intercambian en formato JSON (JavaScript Object Notation), un formato de texto ligero basado en pares '
        'nombre-valor. Los códigos de estado HTTP indican el resultado: 200 o 201 si la operación es exitosa, 204 si no hay '
        'contenido que devolver, 400 si los datos son inválidos, 401 si falta una credencial válida, 404 si el recurso no '
        'existe, 409 si una regla de negocio impide la operación, 502 si no responde un servicio externo y 503 si falla la '
        'base de datos. Los errores usan Problem Details (RFC 9457), un formato estándar con los campos type, title, status, '
        'detail e instance, al que este proyecto agrega el campo codigo con el código de error propio.')
    d.h2('Arquitectura SOA')
    d.p('La Arquitectura Orientada a Servicios (SOA) es un enfoque de diseño en el que las aplicaciones reutilizan sus '
        'funciones a través de interfaces que se comunican en la red. Cada servicio funciona de manera autónoma y ejecuta '
        'una tarea puntual del negocio, como verificar a un sujeto de crédito u otorgar un crédito.')
    d.p('Este proyecto aplica ese enfoque entre dos organizaciones: la Comercializadora no conoce la base de datos del banco ni '
        'cómo se calcula un crédito; solo consume sus servicios. Así cada aplicación puede cambiar por dentro sin afectar a la '
        'otra, mientras se respete el contrato: rutas, métodos, JSON y códigos de error.')
    d.h2('Patrón de Arquitectura Modelo-Vista-Controlador (MVC)')
    d.p('El patrón MVC divide una aplicación en tres componentes principales:')
    d.p('**Modelo:** representa los datos del negocio. En los clientes agrupa las clases que guardan la sesión del empleado, '
        'los teléfonos, el carrito de la venta, la evaluación del comprador, los créditos, las cuotas y las facturas.')
    d.p('**Vista:** presenta la información al usuario. Corresponde a los menús de la consola, las ventanas Swing, las pantallas '
        'de Android y las páginas JSP. En los servidores, el paquete vista contiene los DTO que viajan en el JSON.')
    d.p('**Controlador:** es el intermediario que atiende las solicitudes, coordina al modelo y actualiza la vista. En los '
        'servidores corresponde a los recursos JAX-RS, que solo traducen entre HTTP y los servicios de negocio.')
    d.p('Este patrón mejora la separación de responsabilidades, facilitando el mantenimiento y la escalabilidad. Los servidores '
        'lo complementan con las capas de servicios (reglas de negocio y transacciones), datos (DAO y pasarela) y seguridad.')
    d.h2('Patrones de diseño utilizados')
    d.p('**Singleton:** garantiza una sola instancia de una clase. ConexionBD lee una sola vez db.properties y entrega una '
        'conexión nueva a cada operación. **DAO (Data Access Object):** aísla las sentencias SQL del resto de la aplicación. '
        '**DTO y Mapeador:** separan los objetos del JSON del modelo de dominio. **Gateway (Pasarela):** encapsula el acceso a '
        'un sistema externo; PasarelaBanquito es el único componente de la Comercializadora que conoce las rutas del Core.')
    d.h2('Crédito de cuota fija y tabla de amortización')
    d.p('En un crédito de cuota fija todas las cuotas tienen el mismo valor. La cuota incluye el interés del periodo, que se '
        'calcula sobre el saldo pendiente, y el capital, que es la diferencia entre la cuota y el interés. A medida que el '
        'saldo baja, el interés disminuye y el capital aumenta. La tabla de amortización muestra este detalle cuota por cuota, '
        'con las columnas # Cuota, Valor Cuota, Interés Pagado, Capital Pagado y Saldo.')
    d.p('Con el monto M, el número de cuotas n y la tasa del periodo i, la cuota es **Cuota = M × i / (1 − (1 + i)^−n)**. En este '
        'sistema el pago es mensual, por lo que i = 16.5 % / 12 = 1.375 %. El redondeo es HALF_UP, igual que la función '
        'REDONDEAR de Excel, y el resultado se puede comprobar con la función PAGO.')
    d.h2('Servidor Payara y JAX-RS')
    d.p('Payara Server es una plataforma de grado empresarial para ejecutar aplicaciones Jakarta EE. Derivado de GlassFish, ofrece '
        'un entorno robusto para desplegar servicios web. En este proyecto aloja los dos servicios y el cliente web, cada uno '
        'como un archivo WAR con su propia ruta de contexto, fijada en WEB-INF/glassfish-web.xml.')
    d.p('JAX-RS permite exponer clases Java como recursos REST mediante anotaciones como @Path, @GET, @POST, @PUT, @DELETE y '
        '@Produces, y JSON-B convierte los objetos Java en JSON y viceversa. CDI inyecta las dependencias con @Inject y '
        'MicroProfile Config lee los valores de negocio del archivo microprofile-config.properties.')
    d.h2('Base de datos MySQL y JDBC')
    d.p('MySQL es un sistema gestor de bases de datos relacionales de código abierto. El proyecto utiliza MySQL Community Server '
        '26.7 instalado localmente con el motor InnoDB, que admite transacciones y claves foráneas: otorgar un crédito con su '
        'tabla y guardar una factura con su detalle son operaciones que se confirman o se revierten por completo.')
    d.p('JDBC (Java Database Connectivity) es la API estándar de Java para conectarse a una base de datos y ejecutar SQL. Los '
        'servidores usan el driver mysql-connector-j, sentencias preparadas (PreparedStatement) que evitan la inyección SQL y '
        'la cláusula SELECT … FOR UPDATE, con la que el Core bloquea al cliente mientras le otorga un crédito.')
    d.h2('Android y Jetpack Compose')
    d.p('Android es el sistema operativo móvil sobre el que se ejecuta el cliente móvil. La aplicación se desarrolla en Android '
        'Studio y se compila con Gradle: el modelo, el controlador y el servicio están escritos en Java y la vista en Kotlin '
        'con Jetpack Compose, el kit declarativo de Android, en el que cada pantalla se describe con funciones @Composable que '
        'se vuelven a dibujar cuando cambia su estado. Las peticiones usan HttpURLConnection y el JSON se arma y se lee con '
        'org.json, incluida en Android.')


# ---------------------------------------------------------------------- ANÁLISIS Y DISEÑO
def analisis_diseno(d):
    d.h2('Análisis y Diseño')
    d.p('Esta sección resume el diseño detallado en la ERS y en el ECUD, y relaciona cada ítem de la rúbrica del examen con el '
        'lugar donde se demuestra. Los diagramas se elaboraron en PowerDesigner y están en las carpetas 01. UML y 02. MER.')
    d.paso_()
    d.p('La tabla siguiente relaciona los ítems de la rúbrica con los requisitos del sistema y los entregables del proyecto. Los '
        'puntajes son los del enunciado y suman 20 puntos.')
    d.datos('Trazabilidad con la rúbrica de evaluación',
            ['Ítem de la rúbrica', 'Puntaje', 'Requisitos', 'Entregable'],
            [['Diagrama de casos de uso del Módulo de Crédito', '2.0', 'RF-13 a RF-16', '01. UML/03 DIAGRAMAS/01_CASOS_USO'],
             ['Descripción del caso de uso que verifica si el cliente es sujeto de crédito', '1.0', 'RF-13', '01. UML/02 ECUD'],
             ['Base de datos del Core: 5 clientes, 5 cuentas, 50 movimientos, crédito y amortización', '2.0', 'RNF-006', '03. BDD/banquito'],
             ['Diagrama entidad-relación de la Comercializadora', '1.0', 'RNF-006', '02. MER'],
             ['Servicio web: sujeto de crédito por cédula', '1.5', 'RF-13', 'WS_BANQUITO_JAVA_REST'],
             ['Servicio web: monto máximo por código de cliente', '1.5', 'RF-14', 'WS_BANQUITO_JAVA_REST'],
             ['Servicio web: otorgar crédito y crear la tabla de amortización', '2.0', 'RF-15', 'WS_BANQUITO_JAVA_REST'],
             ['Mantenimiento del catálogo de teléfonos', '1.0', 'RF-03 a RF-06', 'WS_COMERCIALIZADORA_JAVA_REST'],
             ['Facturación: venta con pago en efectivo, en los cuatro clientes', '2.0', 'RF-07, RF-09, RF-11', 'Servidor y 4 clientes'],
             ['Facturación: venta con crédito directo, en los cuatro clientes', '3.0', 'RF-07, RF-10, RF-11', 'Servidor y 4 clientes'],
             ['Servicio REST que retorna la tabla de amortización, en los cuatro clientes', '1.0', 'RF-16', 'Servidor y 4 clientes'],
             ['Consulta de la tabla de amortización desde la Comercializadora', '1.0', 'RF-08, RF-12', 'Servidor y 4 clientes'],
             ['Diagrama de arquitectura de la solución completa', '1.0', 'RNF-001', '01. UML/03 DIAGRAMAS/05_ARQUITECTURA']],
            [2550, 950, 1350, 3654])
    d.paso_()
    d.p('La arquitectura se compone de cuatro clientes, dos aplicaciones y dos bases de datos. Los clientes solo se comunican con '
        'la Comercializadora mediante HTTP y token; esta consulta al Core por HTTP con la clave de API, y cada aplicación accede '
        'únicamente a su base de datos mediante JDBC.')
    d.figura_('Diagrama de arquitectura de la solución completa',
              'Arquitectura_BanQuito_Comercializadora_REST_Java.eam (01. UML/03 DIAGRAMAS/05_ARQUITECTURA)')
    d.paso_()
    d.p('El sistema tiene un actor humano, el Empleado, que usa la Comercializadora desde cualquiera de los cuatro clientes, y dos '
        'actores de tipo sistema: la Comercializadora es el único actor del Core, y el Core es un sistema externo para la '
        'Comercializadora. RF-01 a RF-12 pertenecen a la Comercializadora y RF-13 a RF-16 al Core.')
    d.figura_('Diagrama de casos de uso del Módulo de Crédito (Core BanQuito)',
              'CU_General_Core_BanQuito.oom (01. UML/03 DIAGRAMAS/01_CASOS_USO)')
    d.figura_('Diagrama de casos de uso de la Comercializadora',
              'CU_General_Comercializadora.oom (01. UML/03 DIAGRAMAS/01_CASOS_USO)')
    d.datos('Requisitos funcionales y rutas REST',
            ['Requisito', 'Función', 'Método y ruta'],
            [['RF-01', 'Iniciar sesión', 'POST sesiones'],
             ['RF-02', 'Cerrar sesión', 'DELETE sesiones/actual'],
             ['RF-03', 'Consultar catálogo', 'GET telefonos, GET telefonos/{codTelefono}'],
             ['RF-04', 'Registrar teléfono', 'POST telefonos'],
             ['RF-05', 'Editar teléfono', 'PUT telefonos/{codTelefono}'],
             ['RF-06', 'Eliminar teléfono (baja lógica)', 'DELETE telefonos/{codTelefono}'],
             ['RF-07', 'Verificar al comprador', 'GET clientes/sujeto-credito?cedula='],
             ['RF-08', 'Simular tabla de amortización', 'GET creditos/simulacion?monto=&cuotas='],
             ['RF-09', 'Vender en efectivo', 'POST facturas (formaPago EFECTIVO)'],
             ['RF-10', 'Vender con crédito directo', 'POST facturas (formaPago CREDITO)'],
             ['RF-11', 'Consultar facturas', 'GET facturas/{numFactura}, GET facturas?cedula='],
             ['RF-12', 'Consultar créditos de un comprador', 'GET clientes/creditos?cedula=, GET creditos/{codCredito}/cuotas'],
             ['RF-13', 'Core, servicio 1: sujeto de crédito', 'GET clientes/sujeto-credito?cedula='],
             ['RF-14', 'Core, servicio 2: monto máximo', 'GET clientes/{codCliente}/monto-maximo'],
             ['RF-15', 'Core, servicio 3: otorgar crédito', 'POST creditos'],
             ['RF-16', 'Core, servicio 4: tabla de amortización', 'GET creditos/{codCredito}/cuotas']],
            [1250, 3100, 4154])
    d.paso_()
    d.p('El caso de uso que verifica si el cliente es sujeto de crédito, calificado en la rúbrica, se describe a continuación de '
        'acuerdo con el ECUD (ECUD13). El Core responde 200 también cuando la respuesta es negativa: un "no" no es un error, sino '
        'una evaluación con todos sus motivos.')
    d.datos('Descripción del caso de uso Verificar sujeto de crédito (ECUD13)',
            ['Campo', 'Descripción'],
            [['Descripción', 'Determinar, con la cédula de una persona, si es sujeto de crédito de BanQuito aplicando las reglas RN-01 a RN-04 (RF-13, CU-13, servicio 1).'],
             ['Actor', 'Comercializadora.'],
             ['Precondiciones', 'La Comercializadora envía la petición con la clave de API en la cabecera X-Api-Key. La base de datos banquito contiene los clientes, sus cuentas y sus movimientos.'],
             ['Flujo básico', '1. La Comercializadora solicita la evaluación de una cédula. 2. El sistema recibe GET /clientes/sujeto-credito?cedula={cedula}. 3. Valida la clave de API. 4. Valida que la cédula tenga exactamente 10 dígitos. 5. RN-01: busca al cliente por su cédula. 6. RN-02: verifica que tenga al menos un depósito en el último mes. 7. RN-03: si su género es M, verifica que tenga 25 años o más. 8. RN-04: verifica que no tenga un crédito ACTIVO. 9. Reúne todos los motivos de rechazo; si no hay ninguno, es sujeto de crédito. 10. La Comercializadora recibe la evaluación. 11. El sistema responde 200 con cédula, codCliente, nombre, sujetoCredito y la lista de motivos.'],
             ['Flujos alternativos', 'La cédula no es de un cliente: 200 con el motivo NO_ES_CLIENTE. Sin depósitos en el último mes: SIN_DEPOSITO_ULTIMO_MES. Hombre menor de 25 años: HOMBRE_MENOR_25. Crédito activo: CREDITO_ACTIVO. Puede tener varios motivos a la vez. Cédula vacía o distinta de 10 dígitos: DATA-001 (400). Clave de API ausente o inválida: AUTH-004 (401). Falla de la base de datos: SYS-001 (503).'],
             ['Postcondiciones', 'Se responde si la persona es sujeto de crédito y, si no lo es, todos los motivos. La consulta no modifica datos.']],
            [1800, 6704])
    d.paso_()
    d.p('La venta con crédito directo es el flujo que integra a las dos aplicaciones. Sus reglas de negocio se validan en el servidor '
        'correspondiente; los clientes pueden validar los mismos datos para orientar al empleado, pero eso no reemplaza la '
        'validación del servidor.')
    d.figura_('Diagrama de secuencia Vender con crédito directo',
              'DS_10_VENDER_CREDITO.oom (01. UML/03 DIAGRAMAS/03_SECUENCIA)')
    d.datos('Reglas de negocio',
            ['Código', 'Regla', 'Sistema'],
            [['RN-01', 'El comprador debe ser cliente del banco, tanto en efectivo como a crédito.', 'Core y Comerc.'],
             ['RN-02', 'Sujeto de crédito: tiene al menos un depósito en el último mes.', 'Core'],
             ['RN-03', 'Sujeto de crédito: si es de género masculino, tiene 25 años o más.', 'Core'],
             ['RN-04', 'Sujeto de crédito: no tiene un crédito ACTIVO.', 'Core'],
             ['RN-05', 'Monto máximo = ((promedio de depósitos − promedio de retiros) × 30 %) × 6, con los movimientos de los últimos 3 meses de todas sus cuentas; nunca es menor que 0. El total de una venta a crédito no puede superarlo.', 'Core'],
             ['RN-06', 'Crédito de cuota fija con pago mensual, plazo de 3 a 18 meses y tasa anual de 16.5 %.', 'Core'],
             ['RN-07', 'Cuota = monto × i / (1 − (1 + i)^−n), redondeada a 2 decimales (HALF_UP). Interés = saldo × i; capital = cuota − interés.', 'Core'],
             ['RN-08', 'La última cuota abona todo el saldo como capital y su interés es la diferencia con la cuota fija: la cuota se mantiene y el saldo cierra en 0.00.', 'Core'],
             ['RN-09', 'Pago en efectivo: 42 % de descuento sobre el subtotal. Crédito directo: sin descuento.', 'Comerc.'],
             ['RN-10', 'Los precios se toman del catálogo y el empleado sale de la sesión, nunca de lo que envía el cliente.', 'Comerc.'],
             ['RN-11', 'Eliminar un teléfono es una baja lógica (INACTIVO); un teléfono INACTIVO no se puede vender.', 'Comerc.']],
            [1000, 6204, 1300])
    d.p('El enunciado indica en su texto "35 % de la diferencia", pero su fórmula usa 30 %. Se aplica la fórmula (30 %), y el '
        'porcentaje es configurable con la propiedad credito.porcentajeMonto por si el tribunal requiere otro valor.')
    d.paso_()
    d.p('Los errores se identifican con un código propio y su estado HTTP. Los clientes muestran el mensaje junto con el código, y '
        'AUTH-003 los regresa al inicio de sesión.')
    d.datos('Códigos de error y estados HTTP',
            ['Código', 'HTTP', 'Situación', 'Sistema'],
            [['AUTH-001', '401', 'Usuario o contraseña incorrectos.', 'Comerc.'],
             ['AUTH-002', '401', 'El usuario no se encuentra activo.', 'Comerc.'],
             ['AUTH-003', '401', 'Sesión inválida o expirada.', 'Comerc.'],
             ['AUTH-004', '401', 'Clave de API ausente o inválida.', 'Core'],
             ['DATA-001', '400', 'Datos incompletos o con formato inválido.', 'Ambos'],
             ['TEL-001', '404', 'El teléfono no existe.', 'Comerc.'],
             ['TEL-002', '409', 'El teléfono no está a la venta.', 'Comerc.'],
             ['TEL-003', '400', 'Precio no mayor que cero o con más de dos decimales.', 'Comerc.'],
             ['FAC-001', '404', 'La factura no existe.', 'Comerc.'],
             ['FAC-002', '400', 'Cuotas fuera de 3 a 18 en una venta a crédito.', 'Comerc.'],
             ['CLI-001', '404', 'No existe el cliente; el comprador debe ser cliente de BanQuito.', 'Ambos'],
             ['CRE-001', '409', 'El cliente no es sujeto de crédito.', 'Ambos'],
             ['CRE-002', '409', 'El monto o el total supera el monto máximo de crédito.', 'Ambos'],
             ['CRE-003', '400', 'El número de cuotas debe estar entre 3 y 18.', 'Ambos'],
             ['CRE-004', '404', 'El crédito no existe.', 'Ambos'],
             ['CRE-005', '400', 'Monto no mayor que cero o con más de dos decimales.', 'Ambos'],
             ['EXT-001', '502', 'El Core BanQuito no está disponible.', 'Comerc.'],
             ['SYS-001', '503', 'Servicio o base de datos no disponible.', 'Ambos']],
            [1100, 700, 5504, 1200])
    d.paso_()
    d.p('Las clases de cada servidor se modelaron en PowerDesigner a partir del código, organizadas por las capas del patrón MVC. Los diagramas están en la carpeta 01. UML/03 DIAGRAMAS/04_CLASES.')


# ---------------------------------------------------------------------- BASE DE DATOS
def base_datos(d):
    d.h2('Base de Datos')
    d.paso_()
    d.p('Las bases de datos se instalan en MySQL Community Server 26.7 en el equipo local (carpeta C:\\Program Files\\MySQL\\MySQL '
        'Server 26.7, servicio MySQL267, puerto 3306). Antes de crearlas se inicia el servicio desde una consola abierta como '
        'administrador con el comando net start MySQL267.')
    d.paso_()
    d.p('Los scripts están en la carpeta 03. BDD, con una subcarpeta por aplicación. El script banquito/01_crea_bd.sql crea la base '
        'de datos banquito con utf8mb4 y sus tablas InnoDB: CLIENTE, CUENTA y MOVIMIENTO, del modelo Core entregado en el enunciado, '
        'con sus mismos nombres y tipos, y CREDITO y CUOTA, del módulo de crédito, en el mismo esquema. Las restricciones CHECK '
        'garantizan, entre otras cosas, que las cuotas estén entre 3 y 18 y que los movimientos sean DEP o RET. Si la base de datos '
        'ya existe, el script la borra y la vuelve a crear.')
    d.figura_('Modelo físico de la base de datos banquito', 'MER_Fisico_BanQuito_REST_Java.pdm (02. MER)')
    d.archivo('banquito/01_crea_bd.sql', BDD / 'banquito/01_crea_bd.sql')
    d.paso_()
    d.p('El script banquito/02_carga_datos.sql carga 6 clientes, 7 cuentas y 52 movimientos; la rúbrica pide al menos 5, 5 y 50. Todas '
        'las fechas son relativas a la fecha actual (CURDATE()), de modo que las reglas de "último mes" y "últimos 3 meses" dan el '
        'mismo resultado el día de la defensa. Cada cliente cubre un caso de las reglas de crédito, como se resume en la tabla '
        'siguiente, y el cliente 5 ya tiene el crédito de ejemplo del enunciado.')
    d.datos('Clientes de prueba del Core',
            ['Código', 'Cédula', 'Nombre', 'Caso', 'Sujeto', 'Monto máximo'],
            [['1', '1712345678', 'Ana Lucía Torres', 'Cumple todo', 'Sí', '2250.00'],
             ['2', '1723456789', 'Carlos Andrés Mena', 'Cumple todo, promedios bajos', 'Sí', '450.00'],
             ['3', '1734567890', 'Diego Fernando Ríos', 'Hombre de 22 años (RN-03)', 'No', '—'],
             ['4', '1745678901', 'María José Paredes', 'Sin depósitos en el último mes (RN-02)', 'No', '—'],
             ['5', '1756789012', 'Luis Alberto Cevallos', 'Tiene un crédito ACTIVO (RN-04)', 'No', '—'],
             ['6', '1767890123', 'Sofía Valentina Andrade', 'Cumple todo, con 2 cuentas', 'Sí', '945.00'],
             ['—', '9999999999', 'No es cliente', 'No existe en el banco (RN-01)', 'No', '—']],
            [950, 1400, 1900, 2100, 900, 1254])
    d.paso_()
    d.p('El script comercializadora/01_crea_bd.sql crea la base de datos comercializadora, con USUARIO (empleados), TELEFONO (catálogo '
        'sin inventario), FACTURA y DETALLE_FACTURA. La factura guarda la cédula, el código y el nombre del cliente del banco tal como '
        'los devuelve el Core y, en las ventas a crédito, el código del crédito y el número de cuotas. No hay claves foráneas hacia el '
        'banco porque son bases de datos distintas. El diagrama entidad-relación de esta base de datos es un ítem de la rúbrica.')
    d.figura_('Diagrama entidad-relación de la Comercializadora', 'MER_Conceptual_Comercializadora_REST_Java.cdm (02. MER)')
    d.archivo('comercializadora/01_crea_bd.sql', BDD / 'comercializadora/01_crea_bd.sql')
    d.paso_()
    d.p('El script comercializadora/02_carga_datos.sql carga 2 usuarios y 8 teléfonos. El usuario principal es MONSTER con la clave '
        'MONSTER9, y JPEREZ está ANULADO para probar el error AUTH-002. Las claves se guardan como hash SHA-256.')
    d.paso_()
    d.p('Desde la carpeta 03. BDD se ejecutan los scripts como root, en este orden: 01_crea_bd.sql, 02_carga_datos.sql y '
        '00_usuario_local.sql de cada base de datos. En PowerShell se usa el comando source, porque no admite la redirección "<". El '
        'script 00 crea el usuario de cada aplicación (banquito_app y comercializadora_app) solo con permisos SELECT, INSERT y UPDATE; '
        'su clave se coloca después en el archivo db.properties de cada servidor.')
    d.codigo('Instalación de las bases de datos desde PowerShell',
             '$mysql = "C:\\Program Files\\MySQL\\MySQL Server 26.7\\bin\\mysql.exe"\n'
             '& $mysql -u root -p --default-character-set=utf8mb4 -e "source banquito/01_crea_bd.sql"\n'
             '& $mysql -u root -p --default-character-set=utf8mb4 -e "source banquito/02_carga_datos.sql"\n'
             '& $mysql -u root -p --default-character-set=utf8mb4 -e "source banquito/00_usuario_local.sql"\n'
             '& $mysql -u root -p --default-character-set=utf8mb4 -e "source comercializadora/01_crea_bd.sql"\n'
             '& $mysql -u root -p --default-character-set=utf8mb4 -e "source comercializadora/02_carga_datos.sql"\n'
             '& $mysql -u root -p --default-character-set=utf8mb4 -e "source comercializadora/00_usuario_local.sql"')
    d.paso_()
    d.p('Para comprobar la carga se ejecuta 03_consultas.sql de cada base de datos, que no modifica datos. En banquito deben aparecer 6 '
        'clientes, 7 cuentas, 52 movimientos, 1 crédito y 6 cuotas; los clientes 1, 2 y 6 como sujetos de crédito; los montos máximos '
        '2250.00, 450.00 y 945.00, y ok_saldo = 1 en todas las cuentas. En comercializadora deben aparecer 2 usuarios y 8 teléfonos.')
    d.figura_('Verificación de la base de datos banquito', 'resultado de banquito/03_consultas.sql')
    d.p('La tabla de amortización del cliente 5 es la del ejemplo del enunciado: 450.00 a 6 cuotas con una cuota fija de 78.65. Sirve para '
        'comprobar el cálculo del servidor.')
    d.datos('Tabla de amortización de ejemplo (crédito 1 del cliente 5)',
            ['# Cuota', 'Valor Cuota', 'Interés Pagado', 'Capital Pagado', 'Saldo'],
            [['1', '78.65', '6.19', '72.46', '377.54'],
             ['2', '78.65', '5.19', '73.46', '304.08'],
             ['3', '78.65', '4.18', '74.47', '229.61'],
             ['4', '78.65', '3.16', '75.49', '154.12'],
             ['5', '78.65', '2.12', '76.53', '77.59'],
             ['6', '78.65', '1.06', '77.59', '0.00']],
            [1304, 1800, 1800, 1800, 1800])


# ---------------------------------------------------------------------- SERVIDOR CORE
def servidor_core(d):
    j = CORE / JAVA
    d.h2('Servidor Core BanQuito')
    d.p('El Core es la primera de las dos aplicaciones web. Solo expone servicios: no tiene pantallas para los empleados y solo la '
        'Comercializadora lo invoca, con la clave de API.')
    d.paso_()
    d.p('En primer lugar, en NetBeans, en la sección de Servicios se crea un servidor, específicamente Payara.')
    d.paso_()
    d.p('Se selecciona la versión 7.2026.9, se aceptan los términos y condiciones y se selecciona un directorio donde instalar el servidor.')
    d.paso_()
    d.p('Para terminar la configuración se indica el dominio domain1 y el host; el proyecto usa el puerto HTTP 8080 y el de '
        'administración 4848.')
    d.paso_()
    d.p('Una vez creado el servidor, se crea un proyecto Java with Maven de tipo Web Application.')
    d.paso_()
    d.p('Se asigna el nombre del proyecto WS_BANQUITO_JAVA_REST y se configura el grupo ec.edu.monster.')
    d.paso_()
    d.p('Se selecciona el servidor Payara creado y Jakarta EE 11 como plataforma.')
    d.p('En el pom.xml las dependencias jakartaee-api 11.0.0 y microprofile-config-api 3.1 se declaran con alcance provided, porque '
        'Payara ya las incluye; JAX-RS, JSON-B y CDI forman parte de Jakarta EE. El driver mysql-connector-j 9.7.0 se empaqueta en el WAR '
        'porque Payara no lo trae. El proyecto se compila con Java 21. El archivo glassfish-web.xml fija la ruta de contexto.')
    d.paso_()
    d.p('Aplicando el patrón MVC junto a otras capas, se crean los paquetes dentro de ec.edu.monster: controladores.recursos (recursos '
        'JAX-RS, que solo traducen entre HTTP y el servicio), vista (DTO de entrada y salida, Mapeador y ProblemaDto), servicios (reglas '
        'de negocio, cálculos y transacciones), datos (ConexionBD y DAO), modelo (records y enums del dominio y CodigoError), seguridad '
        '(FiltroClaveApi) y pruebas.')
    d.paso_()
    d.p('Dentro de controladores.recursos se crea ConfiguracionJaxRs, que extiende Application y, con @ApplicationPath, activa JAX-RS: '
        'todos los recursos quedan publicados bajo /api/v1, con la versión en la ruta.')
    d.paso_()
    d.p('Se crean dos recursos. ClienteRecurso expone el **servicio 1** (GET clientes/sujeto-credito?cedula=) y el **servicio 2** (GET '
        'clientes/{codCliente}/monto-maximo), además de la búsqueda por cédula y los créditos de un cliente. La cédula va como '
        'parámetro de consulta porque es un criterio de búsqueda, no la clave del recurso.')
    d.archivo('ClienteRecurso.java (Core)', j / 'controladores/recursos/ClienteRecurso.java')
    d.p('CreditoRecurso expone el **servicio 3** (POST creditos), que responde 201 con la cabecera Location y el crédito con su tabla, y el '
        '**servicio 4** (GET creditos/{codCredito}/cuotas), además de la consulta del crédito y la simulación sin guardar.')
    d.archivo('CreditoRecurso.java (Core)', j / 'controladores/recursos/CreditoRecurso.java')
    d.paso_()
    d.p('Los resultados viajan como DTO. EvaluacionCreditoDto es la respuesta del servicio 1, con los motivos de rechazo, y '
        'SolicitudCreditoDto es el cuerpo del servicio 3. Mapeador convierte los objetos del modelo en estos DTO y ProblemaDto representa '
        'los errores en formato RFC 9457.')
    d.paso_()
    d.p('En el paquete seguridad se crea FiltroClaveApi, un ContainerRequestFilter que se aplica a todos los recursos: exige la cabecera '
        'X-Api-Key con la clave de api.clave y, si falta o no coincide, corta la petición con AUTH-004 (401). La comparación usa '
        'MessageDigest.isEqual, de tiempo constante, para que el tiempo de respuesta no revele cuántos caracteres son correctos. Sin clave '
        'configurada se rechazan todas las peticiones.')
    d.archivo('FiltroClaveApi.java', j / 'seguridad/FiltroClaveApi.java')
    d.paso_()
    d.p('Los errores se centralizan en el enum CodigoError, que une cada código con su estado HTTP y su mensaje. Los servicios lanzan '
        'ExcepcionServicio y MapeadorExcepciones, un ExceptionMapper de JAX-RS, la convierte en una respuesta application/problem+json.')
    d.paso_()
    d.p('Se completa el acceso a la base de datos. ConexionBD es un Singleton que lee db.properties una sola vez y entrega una conexión nueva '
        'en cada llamada, porque cada transacción necesita la suya. El archivo db.properties no se sube al repositorio: se copia de '
        'db.properties.example y se revisan su usuario, su clave y la clave de API. Los valores de negocio están en '
        'microprofile-config.properties, por lo que cambiar la tasa o el porcentaje no requiere modificar el código.')
    d.archivo('db.properties.example (Core)', CORE / 'src/main/resources/db.properties.example')
    d.archivo('microprofile-config.properties (Core)', CORE / 'src/main/resources/META-INF/microprofile-config.properties')
    d.archivo('ConexionBD.java (Core)', j / 'datos/ConexionBD.java')
    d.paso_()
    d.p('Las clases DAO ejecutan el SQL con PreparedStatement. ClienteDao busca al cliente, evalúa en una sola consulta las reglas RN-02 a RN-04 '
        'y bloquea su fila con SELECT … FOR UPDATE. MovimientoDao calcula los promedios de depósitos y retiros de los últimos 3 meses de todas '
        'las cuentas del cliente. CreditoDao y CuotaDao registran y consultan el crédito y su tabla.')
    d.archivo('ClienteDao.java', j / 'datos/ClienteDao.java')
    d.archivo('MovimientoDao.java', j / 'datos/MovimientoDao.java')
    d.paso_()
    d.p('CalculadoraAmortizacion aplica las reglas RN-06 a RN-08. Es una clase pura, sin base de datos ni CDI, para poder probarla sin servidor. '
        'La última fila abona todo el saldo como capital y el interés absorbe el centavo que dejan los redondeos, de modo que la cuota sigue '
        'fija y el saldo cierra en 0.00.')
    d.archivo('CalculadoraAmortizacion.java', j / 'servicios/CalculadoraAmortizacion.java')
    d.paso_()
    d.p('ServicioCredito contiene los cuatro servicios del módulo. El método evaluar reúne todos los motivos de rechazo, y una cédula que no es de '
        'un cliente no es un error, sino una evaluación con el motivo NO_ES_CLIENTE. El método otorgar no confía en lo que el llamador haya '
        'consultado antes: dentro de una transacción READ COMMITTED bloquea al cliente, vuelve a comprobar las reglas y el monto máximo y '
        'registra el crédito y sus cuotas. Así, un reintento o dos ventas simultáneas no crean dos créditos activos. ServicioValidacion '
        'comprueba el formato de los datos de entrada.')
    d.archivo('ServicioCredito.java', j / 'servicios/ServicioCredito.java')
    d.paso_()
    d.p('Con MySQL iniciado se compila y se despliega el proyecto. En NetBeans se usa Run; por consola, desde la carpeta 04. SERVIDOR, se ejecutan '
        'los comandos siguientes. La página http://localhost:8080/WS_BANQUITO_JAVA_REST/ lista los servicios publicados bajo /api/v1. Como todos '
        'exigen la clave, para probarlos a mano se envía la cabecera X-Api-Key: MONSTER9.')
    d.codigo('Compilación y despliegue del Core desde PowerShell',
             '$mvn = "C:\\Program Files\\Apache NetBeans\\java\\maven\\bin\\mvn.cmd"\n'
             '$asadmin = "C:\\payara7\\payara-7.2026.9\\payara7\\bin\\asadmin.bat"\n'
             '& $asadmin start-domain domain1\n'
             'cd WS_BANQUITO_JAVA_REST\n'
             '& $mvn package\n'
             '& $asadmin deploy --force=true target\\WS_BANQUITO_JAVA_REST-1.0-SNAPSHOT.war\n'
             'cd ..')
    d.figura_('Servicio 1: sujeto de crédito', 'respuesta de GET clientes/sujeto-credito?cedula=1712345678 con X-Api-Key')
    d.figura_('Servicio 2: monto máximo', 'respuesta de GET clientes/1/monto-maximo con X-Api-Key')
    d.figura_('Servicio 3: otorgar crédito', 'respuesta 201 de POST creditos con codCliente, monto y numCuotas')
    d.figura_('Servicio 4: tabla de amortización', 'respuesta de GET creditos/1/cuotas con X-Api-Key')
    d.paso_()
    d.p('El paquete pruebas contiene clases con main(), sin framework, que imprimen [OK] o [FALLA] por caso y terminan en PRUEBA APROBADA. '
        'PruebaCalculoAmortizacion es unitaria y no requiere servidor. Las demás son de integración y requieren el WAR desplegado y las bases de '
        'datos recién cargadas, porque las que otorgan créditos solo pueden correr una vez por carga. El orden recomendado es PruebaConexion, '
        'PruebaClaveApi, PruebaSujetoCredito, PruebaMontoMaximo, PruebaTablaAmortizacion, PruebaOtorgarCredito y PruebaConcurrencia.')


# ---------------------------------------------------------------------- SERVIDOR COMERCIALIZADORA
def servidor_comercializadora(d):
    j = COM / JAVA
    d.h2('Servidor Comercializadora')
    d.p('La Comercializadora es la segunda aplicación web y la única que usan los cuatro clientes. Tiene su propia base de datos y consulta al '
        'Core por HTTP cuando necesita datos del banco.')
    d.paso_()
    d.p('En NetBeans se crea otro proyecto Java with Maven de tipo Web Application, con el nombre WS_COMERCIALIZADORA_JAVA_REST, el grupo '
        'ec.edu.monster, el mismo servidor Payara y Jakarta EE 11. Su pom.xml declara las mismas dependencias que el Core, y su archivo '
        'glassfish-web.xml fija la ruta /WS_COMERCIALIZADORA_JAVA_REST.')
    d.paso_()
    d.p('Se crean los mismos paquetes que en el Core. El paquete seguridad contiene ahora el token de sesión y CORS, y el paquete datos agrega '
        'PasarelaBanquito, que es el acceso remoto al Core.')
    d.paso_()
    d.p('Se codifican los recursos, todos bajo /api/v1. SesionRecurso expone POST sesiones, que es público, y DELETE sesiones/actual. El empleado '
        'recibe un token con su fecha de expiración y lo envía en las demás peticiones.')
    d.p('TelefonoRecurso implementa el mantenimiento del catálogo, calificado en la rúbrica: consultar, registrar, editar y dar de baja. La baja es '
        'lógica: el teléfono pasa a INACTIVO y las facturas antiguas se conservan.')
    d.archivo('TelefonoRecurso.java', j / 'controladores/recursos/TelefonoRecurso.java')
    d.p('ClienteRecurso y CreditoRecurso permiten verificar al comprador por su cédula, consultar sus créditos, simular una tabla y consultar la '
        'tabla de amortización de un crédito otorgado. FacturaRecurso vende y consulta facturas; el empleado que factura sale de la sesión del '
        'token, nunca del cuerpo de la petición.')
    d.archivo('CreditoRecurso.java (Comercializadora)', j / 'controladores/recursos/CreditoRecurso.java')
    d.archivo('FacturaRecurso.java', j / 'controladores/recursos/FacturaRecurso.java')
    d.paso_()
    d.p('El cuerpo de una venta es SolicitudFacturaDto: solo lleva la cédula, la forma de pago, el número de cuotas y, por cada teléfono, su código y '
        'cantidad; no lleva precios, porque se toman del catálogo. FacturaDto es la respuesta, con su detalle.')
    d.archivo('SolicitudFacturaDto.java', j / 'vista/SolicitudFacturaDto.java')
    d.paso_()
    d.p('Una clase importante es ManejadorAutenticacion, un filtro que actúa como middleware de los recursos marcados con la anotación @Protegido: lee '
        'el token de la cabecera Authorization, lo valida y deja la sesión del empleado en la petición; si no es válido, corta la petición con '
        'AUTH-003 (401). La anotación usa @NameBinding, de modo que solo POST sesiones queda público.')
    d.archivo('ManejadorAutenticacion.java', j / 'seguridad/ManejadorAutenticacion.java')
    d.p('GestorToken emite tokens UUID y administra las sesiones en memoria: cada validación correcta renueva el plazo de 30 minutos de inactividad y, '
        'al reiniciar el servidor, las sesiones se pierden. UtilHash calcula el SHA-256 de las claves. FiltroCors permite las llamadas del navegador '
        'solo desde los orígenes configurados en cors.origenes; no reemplaza la autenticación.')
    d.archivo('GestorToken.java', j / 'seguridad/GestorToken.java')
    d.paso_()
    d.p('Los valores de negocio están en microprofile-config.properties y la configuración local en db.properties, que tampoco se sube al repositorio. '
        'Además de la conexión, este archivo contiene core.url y core.claveApi; la clave debe ser igual a api.clave del Core. CodigoError agrega los '
        'códigos de sesión, teléfonos, facturas y EXT-001, y permite reconocer los códigos que devuelve el Core.')
    d.archivo('db.properties.example (Comercializadora)', COM / 'src/main/resources/db.properties.example')
    d.archivo('microprofile-config.properties (Comercializadora)', COM / 'src/main/resources/META-INF/microprofile-config.properties')
    d.paso_()
    d.p('PasarelaBanquito es la pieza que integra las dos aplicaciones (patrón Gateway): es el "DAO" remoto de la Comercializadora, que nunca ve la base de '
        'datos banquito. Usa java.net.http.HttpClient con tiempos máximos de 5 s para conectar y 10 s para responder, y envía la clave en X-Api-Key. '
        'Traduce las respuestas así: un estado 2xx se convierte en el objeto leído con JSON-B; un error de negocio del Core (CLI, CRE o DATA) se '
        'convierte en la misma excepción con su detalle; y la falta de respuesta, un tiempo agotado, un 5xx o una clave rechazada se convierten en '
        'EXT-001 (502), con la causa en el log.')
    d.archivo('PasarelaBanquito.java', j / 'datos/PasarelaBanquito.java')
    d.paso_()
    d.p('Los DAO de esta aplicación son UsuarioDao, TelefonoDao, FacturaDao y DetalleFacturaDao. TelefonoDao y FacturaDao concentran el catálogo y la '
        'facturación; la baja lógica es un UPDATE, porque el usuario de la base de datos no tiene permiso DELETE.')
    d.paso_()
    d.p('ServicioAutenticacion compara el hash de la clave y verifica que el usuario esté ACTIVO. ServicioTelefono valida el catálogo. ServicioCliente '
        'consulta los servicios 1 y 2 del Core y entrega la evaluación con su monto máximo. CalculadoraFactura es una clase pura que calcula los '
        'subtotales y el 42 % de descuento.')
    d.archivo('CalculadoraFactura.java', j / 'servicios/CalculadoraFactura.java')
    d.paso_()
    d.p('ServicioFacturacion implementa la venta en cuatro pasos: valida la solicitud; arma las líneas con los precios del catálogo, solo de teléfonos '
        'ACTIVOS, y calcula subtotal, descuento y total; consulta al Core; y guarda el encabezado y el detalle en una transacción. En **efectivo** solo '
        'verifica que el comprador sea cliente. A **crédito** consulta el servicio 1 y, si es sujeto de crédito, el servicio 2; si el total no supera el '
        'monto máximo, pide el servicio 3 por el total.')
    d.p('Por ejemplo, dos Galaxy A15 de 199.00 y un Redmi Note 13 de 279.00 suman un subtotal de 677.00: en efectivo, el descuento es 284.34 y el total '
        '392.66; a crédito, el total es 677.00 y el comprador necesita un monto máximo igual o mayor. Como son dos bases de datos sin transacción '
        'distribuida, si la factura no se puede guardar después de otorgar el crédito, su código queda en el log (SEVERE) para conciliarlo a mano.')
    d.archivo('ServicioFacturacion.java', j / 'servicios/ServicioFacturacion.java')
    d.paso_()
    d.p('Se compila y se despliega el proyecto en el mismo Payara, con el Core ya desplegado. La página http://localhost:8080/WS_COMERCIALIZADORA_JAVA_REST/ '
        'lista los recursos. El inicio de sesión se prueba con un POST a /api/v1/sesiones y el cuerpo {"usuario": "MONSTER", "clave": "MONSTER9"}; las demás '
        'rutas requieren el token en la cabecera Authorization.')
    d.codigo('Compilación y despliegue de la Comercializadora desde PowerShell',
             'cd WS_COMERCIALIZADORA_JAVA_REST\n'
             '& $mvn package\n'
             '& $asadmin deploy --force=true target\\WS_COMERCIALIZADORA_JAVA_REST-1.0-SNAPSHOT.war\n'
             'cd ..')
    d.figura_('Aplicaciones desplegadas en Payara', 'las dos aplicaciones en la consola de administración, puerto 4848')
    d.paso_()
    d.p('Las pruebas de esta aplicación son PruebaCalculoFactura y PruebaHash, unitarias, y PruebaConexion, PruebaInicioSesion, PruebaValidacionToken, '
        'PruebaTelefonos y PruebaFacturacion, de integración. PruebaFacturacion usa al cliente 1, por lo que se ejecuta una vez por cada carga de datos.')


# ---------------------------------------------------------------------- CLIENTE CONSOLA
def cliente_consola(d):
    j = CON / JAVA
    d.h2('Cliente Consola')
    d.paso_()
    d.p('Para el cliente consola se usó NetBeans. Se crea un nuevo proyecto con la categoría Java with Maven y el tipo de proyecto Java Application.')
    d.paso_()
    d.p('Se asigna el nombre WS_COMERCIALIZADORA_JAVA_REST_CLICON, se selecciona como ubicación la carpeta 04. CLICON y se configura el Group Id ec.edu.monster.')
    d.paso_()
    d.p('Se generan los paquetes del patrón MVC: ec.edu.monster.modelo, ec.edu.monster.servicio, ec.edu.monster.controlador, ec.edu.monster.vista, '
        'ec.edu.monster.util (lectura del teclado) y ec.edu.monster.pruebas.')
    d.paso_()
    d.p('En el pom.xml se agrega la dependencia gson 2.11.0, que se usa para armar y leer el JSON. Las peticiones HTTP se envían con '
        'java.net.http.HttpClient, incluido en Java. El proyecto se compila con Java 21 y la clase principal se indica en exec.mainClass. La URL de la '
        'API se configura en servidor.properties o, con prioridad, con la propiedad -Dservidor.url.')
    d.archivo('servidor.properties', CON / 'src/main/resources/servidor.properties')
    d.paso_()
    d.p('El paquete servicio contiene ClienteRESTMonsterCell, la única clase que conoce HTTP, JSON y las rutas de la API: agrega el token en '
        'Authorization: Bearer y convierte las respuestas application/problem+json en ErrorServicioModelo con su código. La misma clase se usa en los '
        'clientes de escritorio y web.')
    d.archivo('ClienteRESTMonsterCell.java', j / 'servicio/ClienteRESTMonsterCell.java')
    d.paso_()
    d.p('El paquete modelo contiene la sesión, el teléfono, la evaluación, el crédito, la cuota y la factura. CarritoModelo representa la venta en curso, con '
        'sus líneas, el comprador verificado, la forma de pago y el número de cuotas. Los totales que muestra son orientativos: la API vuelve a calcularlos al facturar.')
    d.paso_()
    d.p('Los controladores validan y coordinan: devuelven datos o lanzan una excepción, y nunca imprimen ni leen el teclado. Validacion aplica los mismos límites y '
        'mensajes de los demás clientes. VentaControlador lleva la venta en cuatro pasos y solo factura si el comprador está verificado y, a crédito, si es sujeto de '
        'crédito y el total no supera su monto máximo.')
    d.archivo('VentaControlador.java (consola)', j / 'controlador/VentaControlador.java')
    d.paso_()
    d.p('Las vistas solo capturan datos e imprimen resultados. MenuVista organiza las secciones de los demás clientes en un menú numérico; VentaVista conduce la '
        'venta; ComponentesTienda imprime el catálogo, el carrito, la evaluación, la factura y la tabla de amortización. La clase principal abre LoginVista.')
    d.paso_()
    d.p('Con MySQL y los dos servidores en ejecución, se ejecuta el proyecto con Run en NetBeans o con mvn exec:java. Las credenciales son MONSTER y MONSTER9; en una '
        'terminal la clave no se muestra al escribirla. Si la sesión vence, la consola vuelve al inicio de sesión. La secuencia de uso es:')
    d.vineta('Iniciar sesión y elegir 1. Nueva venta en el menú.')
    d.vineta('Ingresar la cédula del comprador para verificarlo en BanQuito, por ejemplo 1712345678.')
    d.vineta('Agregar los teléfonos con sus cantidades.')
    d.vineta('Elegir efectivo (−42 %) o crédito BanQuito con el número de cuotas y revisar la tabla estimada.')
    d.vineta('Revisar los totales y confirmar Facturar. A crédito, la factura se muestra con su tabla de amortización.')
    d.figura_('Venta en efectivo en la consola', 'factura con 42 % de descuento')
    d.figura_('Venta con crédito directo en la consola', 'factura con código de crédito y tabla de amortización')
    d.figura_('Tabla de amortización en la consola', 'tabla de un crédito o simulador de cuotas')


# ---------------------------------------------------------------------- CLIENTE ESCRITORIO
def cliente_escritorio(d):
    j = ESC / JAVA
    d.h2('Cliente de Escritorio')
    d.p('El cliente de escritorio se desarrolló en Java Swing y consume la API de la Comercializadora. Después del inicio de sesión, una sola ventana con barra lateral '
        'da acceso a Nueva venta, Facturas, Teléfonos, Consultar cliente y Simulador de cuotas, con la identidad visual "Azul" del grupo.')
    d.paso_()
    d.p('En NetBeans se selecciona File > New Project > Java with Maven > Java Application con los siguientes valores:')
    d.vineta('Project Name: WS_COMERCIALIZADORA_JAVA_REST_CLIESC')
    d.vineta('Group Id: ec.edu.monster')
    d.vineta('Artifact Id: WS_COMERCIALIZADORA_JAVA_REST_CLIESC')
    d.vineta('Package: ec.edu.monster')
    d.paso_()
    d.p('El pom.xml agrega gson 2.11.0 e indica la clase principal. La URL de la API se configura en src/main/resources/servidor.properties, que junto con las carpetas '
        'fuentes (Plus Jakarta Sans) e imagenes (foto del acceso) forma los recursos del proyecto.')
    d.paso_()
    d.p('Se crean los paquetes ec.edu.monster.modelo, servicio, controlador, vista y pruebas. Los paquetes modelo y servicio contienen las mismas clases del cliente '
        'web, incluida ClienteRESTMonsterCell, descrita en el cliente consola.')
    d.paso_()
    d.p('Los controladores extienden ControladorBase, que ejecuta las llamadas REST en un hilo aparte para que la ventana no se congele y devuelve el resultado al hilo de '
        'Swing con SwingUtilities.invokeLater. Si la API responde AUTH-003, la vista regresa al inicio de sesión. VentaControlador coordina la venta y PrincipalControlador '
        'abre las secciones y cierra la sesión; cerrar la ventana con la sesión abierta también invalida el token.')
    d.archivo('ControladorBase.java (escritorio)', j / 'controlador/ControladorBase.java')
    d.archivo('VentaControlador.java (escritorio)', j / 'controlador/VentaControlador.java')
    d.paso_()
    d.p('En el paquete vista, Ventana aloja las pantallas: LoginVista, PrincipalVista y una vista por sección. Tema, Iconos y Componentes dan la identidad visual y '
        'ComponentesTienda reúne el catálogo, el carrito, las tablas y la evaluación del banco. Se muestra la vista de venta; las demás '
        'siguen la misma estructura.')
    d.archivo('VentaVista.java (escritorio)', j / 'vista/VentaVista.java')
    d.paso_()
    d.p('Primero se inician MySQL y Payara con los dos servidores. Después se ejecuta el proyecto desde NetBeans o con mvn exec:java y se ingresa con MONSTER y MONSTER9. '
        'En Nueva venta el catálogo tiene búsqueda en vivo a la izquierda y la venta actual a la derecha, en cuatro pasos. Facturar se deshabilita y se explica el motivo si '
        'falta algún dato o si el comprador no cumple las condiciones.')
    d.figura_('Venta en efectivo en el escritorio', 'factura con 42 % de descuento')
    d.figura_('Venta con crédito directo en el escritorio', 'factura con crédito y tabla de amortización')
    d.figura_('Consulta de cliente y tabla de amortización en el escritorio', 'sección Consultar cliente con los créditos y la tabla')


# ---------------------------------------------------------------------- CLIENTE MÓVIL
def cliente_movil(d):
    d.h2('Cliente Móvil')
    d.p('El cliente móvil es una aplicación Android nativa desarrollada en Android Studio. Sigue el mismo patrón MVC que los demás clientes: el modelo, el controlador y el '
        'servicio están en Java y la vista en Kotlin con Jetpack Compose. Tiene las mismas pantallas de los otros clientes y se adapta con los cortes de 600 y 840 dp: en '
        'pantalla amplia la venta se muestra a dos columnas.')
    d.paso_()
    d.p('En Android Studio se selecciona File > New > New Project y la plantilla Empty Activity, que utiliza Jetpack Compose.')
    d.paso_()
    d.p('Se configura el proyecto con los siguientes valores:')
    d.vineta('Nombre: WS_COMERCIALIZADORA_JAVA_REST_CLIMOV')
    d.vineta('Paquete (namespace): ec.edu.monster')
    d.vineta('Identificador de la aplicación (applicationId): ec.edu.monster.cliente_movil_rest_monstercell')
    d.vineta('SDK mínimo: API 24; compileSdk y targetSdk: 37')
    d.vineta('Lenguaje de configuración de compilación: Kotlin DSL (build.gradle.kts)')
    d.paso_()
    d.p('En app/build.gradle.kts se habilitan Compose y BuildConfig, y se crea el campo URL_SERVIDOR con la dirección de la API, incluida la ruta /api/v1. Su valor se toma de '
        'servidor.url en gradle.properties; 10.0.2.2 es la dirección con la que el emulador llega al localhost del computador. Para un celular físico se coloca la IP del equipo.')
    d.archivo('app/build.gradle.kts', MOV / 'app/build.gradle.kts')
    d.paso_()
    d.p('En AndroidManifest.xml se declaran los permisos INTERNET y ACCESS_LOCAL_NETWORK (Android 17 bloquea la red local sin él), la configuración de seguridad de red y una '
        'actividad por sección. Como la API se publica por HTTP, network_security_config.xml permite tráfico sin cifrar solo hacia 10.0.2.2 y localhost. La compilación debug '
        'usa otra configuración que lo permite hacia cualquier dirección, para probar con un celular físico.')
    d.archivo('res/xml/network_security_config.xml', MOVA / 'res/xml/network_security_config.xml')
    d.paso_()
    d.p('Dentro de ec.edu.monster se crean los paquetes del patrón MVC: modelo, servicio, controlador y vista.')
    d.paso_()
    d.p('El paquete servicio contiene el acceso a la API. ClienteRest es la única clase que conoce HTTP y JSON: usa HttpURLConnection y org.json, agrega la cabecera '
        'Authorization: Bearer y convierte los errores application/problem+json en ErrorServicioModelo. AutenticacionRestService, TelefonoRestService, CreditoRestService y '
        'FacturaRestService conocen las rutas. Al facturar solo se envían el teléfono y la cantidad.')
    d.archivo('ClienteRest.java', MOVJ / 'servicio/ClienteRest.java')
    d.archivo('FacturaRestService.java', MOVJ / 'servicio/FacturaRestService.java')
    d.paso_()
    d.p('Los controladores extienden ControladorBase, que ejecuta las llamadas en un hilo secundario, porque Android no permite operaciones de red en el hilo de la interfaz, y '
        'devuelve el resultado a la pantalla. Si la API responde AUTH-003, se regresa al inicio de sesión. Al cambiar la cédula hay que volver a verificarla antes de facturar.')
    d.archivo('VentaControlador.java (móvil)', MOVJ / 'controlador/VentaControlador.java')
    d.paso_()
    d.p('Las pantallas se construyen con Jetpack Compose. Tema.kt define los colores y la tipografía Plus Jakarta Sans; Componentes.kt y ComponentesTienda.kt reúnen los elementos '
        'reutilizables. Cada actividad dibuja una pantalla y delega sus acciones a su controlador. Se muestra la pantalla de venta; las demás siguen la misma estructura.')
    d.archivo('VentaActivity.kt', MOVJ / 'vista/VentaActivity.kt')
    d.paso_()
    d.p('Se inician MySQL y Payara y se ejecuta la aplicación en un emulador desde Android Studio con Run \'app\'. Se ingresa con MONSTER y MONSTER9; en Android 17 la aplicación solicita '
        'el permiso de red local, que se debe aceptar. En pantallas angostas una barra inferior lleva a la venta.')
    d.figura_('Venta en efectivo en el cliente móvil', 'factura con 42 % de descuento')
    d.figura_('Venta con crédito directo en el cliente móvil', 'factura con crédito y tabla de amortización')
    d.figura_('Consulta de cliente y tabla de amortización en el cliente móvil', 'ClientesActivity')


# ---------------------------------------------------------------------- CLIENTE WEB
def cliente_web(d):
    j = WEB / JAVA
    d.h2('Cliente Web')
    d.p('El cliente web es una aplicación Jakarta EE que se despliega en Payara, distinta de los dos servidores. Sigue el patrón MVC: los servlets del paquete controlador atienden las '
        'peticiones, las páginas JSP de WEB-INF/vista muestran la información y ClienteRESTMonsterCell consume la API. El navegador nunca habla con el banco.')
    d.paso_()
    d.p('En NetBeans se selecciona File > New Project > Java with Maven > Web Application.')
    d.paso_()
    d.p('Se asigna el nombre WS_COMERCIALIZADORA_JAVA_REST_CLIWEB con el Group Id ec.edu.monster y se selecciona el servidor Payara con Jakarta EE 11.')
    d.paso_()
    d.p('El pom.xml declara jakartaee-api 11.0.0 con alcance provided y gson 2.11.0, que se empaqueta en el WAR porque Payara no la trae. En web.xml se fija la codificación UTF-8 y una '
        'sesión de 30 minutos, igual que el token del servidor; la cookie es http-only y SameSite=Lax. La URL de la API se configura en servidor.properties.')
    d.paso_()
    d.p('Se crean los paquetes ec.edu.monster.controlador, modelo, servicio, vista y pruebas, la carpeta WEB-INF/vista para las páginas JSP, WEB-INF/tags para los componentes reutilizables '
        'y las carpetas css, js, fuentes e img. Los paquetes modelo y servicio son los mismos de los clientes de consola y escritorio; cada sesión del navegador guarda su propio '
        'ClienteRESTMonsterCell, y el carrito también vive en la sesión.')
    d.paso_()
    d.p('Cada controlador es un servlet que extiende ControladorBase, que reúne la navegación a las vistas JSP, los avisos entre redirecciones y la lectura de los errores de la API. '
        'FiltroSesion redirige al inicio de sesión cuando se intenta abrir una ruta privada sin haber ingresado. FiltroCsrf exige un token aleatorio en todos los formularios POST y '
        'EscuchaSesion invalida el token de la API cuando termina la sesión web.')
    d.paso_()
    d.p('VentaControlador atiende /venta. Al facturar redirige con Post/Redirect/Get, de modo que recargar la página no repite la venta. FacturaControlador muestra las facturas y '
        'CreditoControlador atiende /clientes, /creditos y /simulador, donde se consulta la tabla de amortización.')
    d.archivo('VentaControlador.java (web)', j / 'controlador/VentaControlador.java')
    d.archivo('CreditoControlador.java (web)', j / 'controlador/CreditoControlador.java')
    d.paso_()
    d.p('Las páginas JSP están en WEB-INF/vista, por lo que solo se muestran a través de los controladores. Usan JSTL, los componentes de WEB-INF/tags y la hoja de estilos '
        'css/monstercell.css con la identidad visual "Azul". Se muestra venta.jsp; las demás siguen la misma estructura.')
    d.archivo('venta.jsp', WEB / 'src/main/webapp/WEB-INF/vista/venta.jsp')
    d.paso_()
    d.p('Con MySQL y los dos servidores en ejecución, se compila y se despliega el WAR en Payara. La aplicación se abre en http://localhost:8080/WS_COMERCIALIZADORA_JAVA_REST_CLIWEB/, '
        'que muestra el inicio de sesión; se ingresa con MONSTER y MONSTER9. La pantalla de inicio es el punto de venta. PruebaClienteWeb recorre todo el flujo y debe terminar en PRUEBA APROBADA.')
    d.codigo('Compilación y despliegue del cliente web desde PowerShell',
             '& $mvn package\n'
             '& $asadmin deploy --force=true target\\WS_COMERCIALIZADORA_JAVA_REST_CLIWEB-1.0-SNAPSHOT.war')
    d.figura_('Venta en efectivo en el cliente web', 'factura con 42 % de descuento')
    d.figura_('Venta con crédito directo en el cliente web', 'factura con el crédito otorgado')
    d.figura_('Mantenimiento de teléfonos en el cliente web', 'pantalla /telefonos')
    d.figura_('Tabla de amortización en el cliente web', 'pantalla /creditos/N')


# ---------------------------------------------------------------------- CIERRE
def conclusiones(d):
    d.h1_('CONCLUSIONES')
    d.p('La solución cumple el caso de estudio con dos aplicaciones web independientes, cada una con su base de datos. La Comercializadora factura ventas en efectivo y a crédito sin conocer '
        'la base de datos del banco, lo que refleja la alianza entre las dos organizaciones y demuestra la interoperabilidad de los servicios RESTful.')
    d.p('Centralizar las reglas en los servidores garantiza que los cuatro clientes obtengan los mismos resultados. El Core decide automáticamente si otorga el crédito y vuelve a verificar las '
        'reglas dentro de la transacción, con el cliente bloqueado, por lo que dos ventas simultáneas no generan dos créditos activos.')
    d.p('El cálculo de la tabla de amortización con BigDecimal y redondeo HALF_UP reproduce el ejemplo del enunciado: con 450.00 a 6 cuotas la cuota es 78.65 y el saldo cierra en 0.00.')
    d.p('Los clientes consumen las mismas rutas con técnicas distintas: la consola, el escritorio y la web usan HttpClient de Java con Gson, mientras que la aplicación Android usa '
        'HttpURLConnection y org.json. La separación MVC permitió reutilizar el modelo y el servicio entre los clientes Java.')
    d.p('La pasarela PasarelaBanquito y los errores uniformes en application/problem+json permiten que una falla del Core se informe como EXT-001 sin bloquear a la Comercializadora y que cada '
        'cliente muestre un mensaje claro con su código.')


def recomendaciones(d):
    d.h1_('RECOMENDACIONES')
    d.vineta('Volver a ejecutar los scripts 01_crea_bd.sql y 02_carga_datos.sql de las dos bases de datos antes de la defensa: un cliente con crédito activo deja de ser sujeto de crédito.')
    d.vineta('Desplegar y verificar primero el Core, y comprobar que api.clave del Core y core.claveApi de la Comercializadora sean iguales; si se cambian, se deben volver a compilar y desplegar los dos WAR.')
    d.vineta('Verificar la dirección del servidor en cada cliente antes de ejecutarlo: servidor.url en servidor.properties (o -Dservidor.url) en la consola, el escritorio y la web, y en gradle.properties en el cliente móvil.')
    d.vineta('Si el tribunal exige el 35 % que indica el texto del enunciado, cambiar credito.porcentajeMonto en el Core y volver a desplegarlo.')
    d.vineta('Mantener sincronizadas las rutas y el formato JSON de los cuatro clientes cuando cambien los recursos de la Comercializadora.')
    d.vineta('Cambiar las claves de banquito_app y comercializadora_app en los scripts 00 y en db.properties, y no subir ese archivo al repositorio.')
    d.vineta('En una versión futura, publicar los servicios mediante HTTPS y reemplazar la conciliación manual entre crédito y factura por un mecanismo de compensación entre las dos aplicaciones.')


def referencias(d):
    d.h1_('REFERENCIAS BIBLIOGRÁFICAS')
    for ref in (
        'Bray, T. (Ed.). (2017). The JavaScript Object Notation (JSON) Data Interchange Format (RFC 8259). IETF. https://www.rfc-editor.org/rfc/rfc8259',
        'Eclipse Foundation. (s. f.). Jakarta JSON Binding. https://jakarta.ee/specifications/jsonb/',
        'Eclipse Foundation. (s. f.). Jakarta RESTful Web Services. https://jakarta.ee/specifications/restful-ws/',
        'Fielding, R. T. (2000). Architectural Styles and the Design of Network-based Software Architectures [Tesis doctoral, University of California, Irvine]. https://ics.uci.edu/~fielding/pubs/dissertation/top.htm',
        'Google. (s. f.). Gson. GitHub. https://github.com/google/gson',
        'Google. (s. f.). Jetpack Compose. Android Developers. https://developer.android.com/develop/ui/compose',
        'Google. (s. f.). Network security configuration. Android Developers. https://developer.android.com/privacy-and-security/security-config',
        'Nottingham, M., Wilde, E. y Dalal, S. (2023). Problem Details for HTTP APIs (RFC 9457). IETF. https://www.rfc-editor.org/rfc/rfc9457',
        'Oracle. (s. f.). JDBC Basics. The Java Tutorials. https://docs.oracle.com/javase/tutorial/jdbc/basics/index.html',
        'Oracle Corporation. (s. f.). MySQL Documentation. https://dev.mysql.com/doc/',
        'Payara Services. (s. f.). Payara Server Documentation. https://docs.payara.fish/',
        'World Wide Web Consortium (W3C). (2004). Web Services Architecture. https://www.w3.org/TR/ws-arch/',
    ):
        d.vineta(ref, num=7, jc='left')

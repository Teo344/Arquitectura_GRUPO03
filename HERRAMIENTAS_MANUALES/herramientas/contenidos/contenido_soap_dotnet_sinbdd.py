# Contenido del manual SOAP .NET sin BDD - GR03
import os
from pathlib import Path

from docxlib import Doc

# Raíz del repositorio: contenidos/ -> herramientas/ -> HERRAMIENTAS_MANUALES/ -> repo
REPO = Path(__file__).resolve().parents[3]
RAIZ = str(REPO / 'TI.1.2. PAO202651_SOAP_DOTNET_SINBDD_NRC36884_GR03')
SRV = RAIZ + '/04. SERVIDOR/WS_CON_UNI_DOTNET_SOAP/'
CON = RAIZ + '/04. CLICON/ConUni_ClienteConsola_GR3/ConUni_ClienteConsola_GR3/'
CONE = CON + 'ec/edu/monster/'
SHA = RAIZ + '/04. CLIESC/SistemaConversorUI.Shared/SistemaConversorUI.Shared/'
ESC = RAIZ + '/04. CLIESC/SistemaConversorUI.Desktop/'
WEB = RAIZ + '/04. CLIWEB/SistemaConversorUI.Web/'


def leer(ruta):
    with open(ruta, encoding='utf-8-sig') as f:
        return f.read()


def varios(*rutas):
    return '\n\n'.join(leer(r).strip('\n') for r in rutas)


def construir():
    d = Doc()

    # ------------------------------------------------------------------ INTRODUCCIÓN
    d.h1_('INTRODUCCIÓN')
    d.vacio()
    d.p('El proyecto **Conversión de Unidades Monster** centraliza la autenticación, la consulta del catálogo de unidades '
        'y los cálculos de conversión en un servicio web SOAP desarrollado sobre la plataforma .NET mediante Windows '
        'Communication Foundation (WCF). El backend permite convertir medidas de longitud, masa y temperatura, y consultar '
        'un catálogo de seis categorías (longitud, peso, volumen, tiempo, temperatura y área). Volumen, tiempo y área se '
        'exponen únicamente como parte del catálogo; su conversión queda planificada para una segunda fase, según el '
        'alcance definido en la Especificación de Requerimientos de Software (ERS).')
    d.p('El servidor se organiza en capas con responsabilidades separadas: los **Controladores** exponen los contratos SOAP '
        'y no contienen lógica; los **Servicios** validan, orquestan y calculan; el **Modelo** define las categorías, '
        'unidades y códigos de error; la capa **Vista** contiene los contratos de datos (DTO) que viajan en el XML; y la '
        'capa de **Seguridad** intercepta cada mensaje para validar el token. Esta separación permite que el núcleo de '
        'cálculo sea reutilizable con independencia del medio de exposición.')
    d.p('Los servicios se publican como dos endpoints SOAP 1.1 (`AutenticacionWS.svc` y `ConversionUnidadesWS.svc`) '
        'descritos por su contrato WSDL y alojados en Internet Information Services (IIS). Cuatro clientes consumen el '
        'mismo backend: una aplicación de consola en .NET Framework, una aplicación de escritorio .NET MAUI Blazor Hybrid, '
        'una aplicación web Blazor y una aplicación móvil Android basada en .NET MAUI. Ningún cliente implementa lógica de '
        'conversión, de modo que los resultados son idénticos en todos los canales.')
    d.p('La solución no utiliza una base de datos: las credenciales del usuario único, las sesiones activas y el catálogo '
        'se mantienen en memoria o en el archivo de configuración `Web.config`. Las operaciones de catálogo y conversión '
        'están protegidas con un token temporal que viaja en la cabecera del mensaje SOAP, y todos los errores se devuelven '
        'como SOAP Faults estructurados con un código y un mensaje.')

    # ------------------------------------------------------------------ OBJETIVOS
    d.h1_('OBJETIVOS')
    d.h2('Objetivo General')
    d.p('Documentar la configuración, la implementación, el despliegue y el consumo del servicio web SOAP en .NET (WCF) del '
        'sistema Conversión de Unidades Monster, basado en una arquitectura orientada a servicios sin base de datos, para que '
        'el equipo pueda desplegarlo en IIS y utilizar sus funciones desde los clientes de consola, escritorio, web y móvil.')
    d.h2('Objetivos Específicos')
    d.vineta('Describir el diseño por capas del servidor WCF, sus contratos de servicio y de datos, y la generación automática del contrato WSDL.')
    d.vineta('Explicar el mecanismo de seguridad: inicio de sesión, emisión y validación del token en la cabecera SOAP, caducidad de la sesión y cambio de clave.')
    d.vineta('Detallar la configuración del archivo Web.config y la publicación del servicio en Internet Information Services (IIS).')
    d.vineta('Proporcionar guías para el desarrollo de los clientes de consola, escritorio, web y móvil, detallando cómo consumen el servicio y cómo manejan los errores del servidor.')
    d.vacio()

    # ------------------------------------------------------------------ MARCO TEÓRICO
    d.h1_('MARCO TEÓRICO')
    d.h2('Servicios Web SOAP y WSDL')
    d.p('SOAP (Simple Object Access Protocol) es un protocolo de mensajería basado en XML que permite que sistemas '
        'heterogéneos intercambien información a través de la red. Cada mensaje viaja dentro de un sobre (Envelope) que '
        'contiene una cabecera opcional (Header), utilizada para datos de control como los tokens de seguridad, y un cuerpo '
        '(Body) con la operación y sus parámetros. Cuando ocurre un error, el servidor responde con un elemento Fault que '
        'describe la causa.')
    d.p('El WSDL (Web Services Description Language) es el documento XML que actúa como contrato del servicio: define las '
        'operaciones disponibles, los tipos de datos de entrada y salida, el protocolo de transporte y la dirección del '
        'endpoint. En este proyecto WCF genera el WSDL de forma automática al acceder a la dirección del servicio con el '
        'sufijo `?wsdl`.')
    d.h2('Arquitectura Orientada a Servicios (SOA)')
    d.p('La Arquitectura Orientada a Servicios es un enfoque de diseño en el que la funcionalidad del negocio se expone como '
        'servicios autónomos, reutilizables y accesibles a través de interfaces bien definidas. Los consumidores solo '
        'conocen el contrato del servicio, no su implementación, lo que permite que clientes construidos con tecnologías '
        'distintas compartan la misma lógica.')
    d.p('En el sistema Conversión de Unidades Monster, SOA se refleja en la centralización de toda la lógica de cálculo y '
        'seguridad en el servidor. Los cuatro clientes consumen los mismos contratos, y la incorporación de nuevas '
        'magnitudes (por ejemplo volumen, tiempo o área) solo requiere agregar una estrategia de conversión en el servidor, '
        'sin alterar los contratos existentes.')
    d.h2('Windows Communication Foundation (WCF)')
    d.p('WCF es el framework de Microsoft para construir aplicaciones distribuidas orientadas a servicios sobre .NET '
        'Framework. Se basa en tres elementos: la dirección (dónde está el servicio), el enlace o binding (cómo se comunica) '
        'y el contrato (qué operaciones ofrece). Los contratos se declaran con los atributos `[ServiceContract]` y '
        '`[OperationContract]` sobre interfaces, y los datos que viajan en los mensajes se describen con `[DataContract]` y '
        '`[DataMember]`.')
    d.p('El proyecto utiliza `basicHttpBinding` (SOAP 1.1 sobre HTTP) para máxima interoperabilidad. WCF ofrece además '
        'puntos de extensión como `IDispatchMessageInspector`, que permite inspeccionar cada mensaje antes de que llegue a '
        'la operación, e `IServiceBehavior`, que registra ese inspector en los endpoints de un servicio. Estos mecanismos se '
        'usan para validar el token en la cabecera SOAP. Los errores de negocio se envían como `FaultException<T>`, cuyo '
        'detalle queda declarado en el WSDL mediante `[FaultContract]`.')
    d.h2('Patrón de Arquitectura Modelo-Vista-Controlador (MVC)')
    d.p('El patrón MVC divide una aplicación en tres componentes con responsabilidades diferenciadas, y en este proyecto se '
        'aplica tanto en el servidor como en los clientes:')
    d.vineta('**Modelo:** representa los datos y las reglas del dominio. En el servidor corresponde a las clases `Categoria`, `Unidad` y `CodigoError`; en los clientes, a los objetos de sesión, solicitudes y respuestas.')
    d.vineta('**Vista:** presenta la información. En el servidor, la capa Vista contiene los DTO que se serializan en el XML de respuesta; en los clientes, son las pantallas de consola o los componentes Razor.')
    d.vineta('**Controlador:** recibe las solicitudes y coordina el modelo y la vista. En el servidor son las clases WCF `AutenticacionWS` y `ConversionUnidadesWS`, que delegan en los servicios; en el cliente de consola son `LoginControlador`, `CatalogoControlador` y `ConversionControlador`.')
    d.p('Esta separación mejora el mantenimiento y permite modificar la presentación de un cliente sin afectar la lógica de conversión, que permanece exclusivamente en el servidor.')
    d.h2('Internet Information Services (IIS)')
    d.p('IIS es el servidor web de Microsoft para Windows. Aloja aplicaciones ASP.NET y servicios WCF mediante grupos de '
        'aplicaciones (Application Pools) que ejecutan el código con una versión determinada del CLR. Para hospedar archivos '
        '`.svc` es necesario habilitar la característica de activación HTTP de WCF en Windows. IIS se encarga del ciclo de '
        'vida del servicio, de los enlaces de red (puerto y host) y de los permisos de acceso a la carpeta física.')
    d.h2('.NET MAUI Blazor Hybrid y Blazor Web')
    d.p('.NET MAUI (Multi-platform App UI) permite crear aplicaciones nativas para Windows, Android, iOS y macOS desde un '
        'mismo código base. La variante Blazor Hybrid incrusta componentes Razor dentro de un control `BlazorWebView`, de '
        'modo que la interfaz se escribe con HTML, CSS y C#. Blazor Web, por su parte, ejecuta esos mismos componentes en un '
        'servidor ASP.NET Core y los muestra en el navegador.')
    d.p('Gracias a esta compatibilidad, los clientes de escritorio, web y móvil comparten una Biblioteca de clases de Razor '
        '(`SistemaConversorUI.Shared`) que contiene las vistas, los modelos y los adaptadores SOAP. Cada cliente solo '
        'configura la dirección del servidor y registra los adaptadores, lo que garantiza una interfaz y un comportamiento '
        'uniformes, como exige la ERS.')
    d.h2('Requisitos del sistema')
    d.p('El desarrollo se sustenta en la ERS del proyecto (norma IEEE 830). La Tabla 1 relaciona los requisitos funcionales '
        'con las operaciones SOAP que los implementan, y la Tabla 2 resume los códigos de error definidos para las '
        'respuestas de falla.')
    d.datos('Requisitos funcionales y operaciones SOAP que los implementan',
            ['Código', 'Requisito', 'Servicio / operación'],
            [['RQF-001', 'Autenticar cliente y generar token temporal', 'AutenticacionWS / login, cambiarClave'],
             ['RQF-002', 'Consultar catálogo de categorías', 'ConversionUnidadesWS / consultarCatalogo'],
             ['RQF-003', 'Consultar unidades por categoría', 'ConversionUnidadesWS / consultarUnidades'],
             ['RQF-004', 'Convertir longitud', 'ConversionUnidadesWS / convertirLongitud'],
             ['RQF-005', 'Convertir masa', 'ConversionUnidadesWS / convertirMasa'],
             ['RQF-006', 'Convertir temperatura', 'ConversionUnidadesWS / convertirTemperatura'],
             ['RQF-007', 'Validar token', 'ManejadorAutenticacion (cabecera SOAP)'],
             ['RQF-008', 'Validar datos', 'ServicioValidacion'],
             ['RQF-009', 'Generar respuesta estructurada', 'RespuestaDto / RespuestaMapeador'],
             ['RQF-010', 'Gestionar errores', 'ExcepcionServicio (SOAP Fault)']],
            [1300, 3404, 3800])
    d.datos('Códigos de error de las respuestas de falla',
            ['Código', 'Mensaje', 'Situación que lo produce'],
            [['AUTH-001', 'Token inválido o ausente', 'La cabecera SOAP no contiene un token o el token no existe.'],
             ['AUTH-002', 'Sesión expirada', 'El token superó su tiempo de vida (30 minutos por defecto).'],
             ['AUTH-003', 'Credenciales incorrectas', 'Usuario o clave no coinciden en login o cambio de clave.'],
             ['UNIT-001', 'Unidad no soportada', 'La unidad no existe o pertenece a otra categoría.'],
             ['DATA-001', 'Datos inconsistentes', 'Valor nulo, no finito o negativo (excepto temperatura), categoría inexistente o clave nueva vacía.']],
            [1300, 2400, 4804])

    # ------------------------------------------------------------------ DESARROLLO
    d.h1_('DESARROLLO DEL APLICATIVO')
    d.p('El desarrollo se organiza en cinco partes: el servidor WCF, el cliente de consola, la biblioteca compartida de '
        'componentes Razor y los clientes de escritorio, móvil y web que la reutilizan. En todos los casos se trabajó con '
        'Visual Studio 2022 o superior sobre Windows 11.')

    # ---------------- SERVIDOR
    d.h2('Servidor')
    d.paso_('Creación del proyecto')
    d.p('En Visual Studio se selecciona **Crear un proyecto** y se busca la plantilla **Aplicación de servicios WCF** '
        '(WCF Service Application) para C#. Esta plantilla genera un sitio web que aloja servicios `.svc` sobre .NET Framework.')
    d.figura_('Selección de la plantilla Aplicación de servicios WCF.', 'Visual Studio > Crear un proyecto > plantilla "Aplicación de servicios WCF"')
    d.paso_('Nombre del proyecto y versión del framework')
    d.p('Se asigna el nombre **WS_CON_UNI_DOTNET_SOAP**, se elige la carpeta `04. SERVIDOR` como ubicación y se selecciona '
        '**.NET Framework 4.8** como versión del framework.')
    d.figura_('Configuración del nombre del proyecto WCF y versión .NET Framework 4.8.', 'ventana "Configure su nuevo proyecto" con nombre WS_CON_UNI_DOTNET_SOAP y .NET Framework 4.8')
    d.paso_('Estructura de carpetas por capas')
    d.p('Se eliminan los archivos de ejemplo `Service1.svc` e `IService1.cs` y se crean las carpetas que separan las responsabilidades del servidor:')
    d.vineta('**Controladores:** contratos de servicio (interfaces) y sus implementaciones WCF. No contienen lógica.')
    d.vineta('**Modelo:** categorías, unidades con su factor a la unidad base y códigos de error.')
    d.vineta('**Seguridad:** gestor de tokens, inspector de mensajes y atributo que protege un servicio.')
    d.vineta('**Servicios:** validación, catálogo, conversión, autenticación y excepción de negocio. La subcarpeta **Conversores** contiene las estrategias de cálculo.')
    d.vineta('**Vista:** contratos de datos (DTO) que se serializan en el XML y el mapeador de respuestas.')
    d.figura_('Estructura de carpetas del servidor en el Explorador de soluciones.', 'Explorador de soluciones con las carpetas Controladores, Modelo, Seguridad, Servicios/Conversores y Vista')

    d.paso_('Namespace del contrato')
    d.p('Todos los contratos comparten un mismo namespace XML, definido una sola vez en la clase `Contrato`. Así el WSDL y los '
        'mensajes mantienen nombres de elementos estables que los clientes pueden referenciar.')
    d.codigo('Clase Contrato (Vista/Contrato.cs)', leer(SRV + 'Vista/Contrato.cs'))

    d.paso_('Modelo del dominio')
    d.p('La clase `Categoria` define las seis categorías del catálogo e indica cuáles son convertibles. La clase `Unidad` '
        'registra cada unidad con su código, nombre, símbolo, categoría y factor de conversión a la unidad base (metro para '
        'longitud, kilogramo para masa). En temperatura el factor es nulo porque se usan fórmulas. `CodigoError` centraliza '
        'los códigos de la Tabla 2.')
    d.codigo('Clase Categoria (Modelo/Categoria.cs)', leer(SRV + 'Modelo/Categoria.cs'))
    d.codigo('Clase Unidad (Modelo/Unidad.cs)', leer(SRV + 'Modelo/Unidad.cs'))
    d.codigo('Clase CodigoError (Modelo/CodigoError.cs)', leer(SRV + 'Modelo/CodigoError.cs'))

    d.paso_('Contratos de datos (DTO)')
    d.p('Los DTO de la carpeta Vista se marcan con `[DataContract]` y `[DataMember]`; el orden y el nombre de cada miembro '
        'determinan el XML generado. `RespuestaDto` es la respuesta común de todas las operaciones: los campos que no aplican '
        'quedan nulos y no se serializan gracias a `EmitDefaultValue = false`. El token no forma parte de la solicitud de '
        'conversión porque viaja en la cabecera SOAP.')
    d.codigo('Clase SolicitudConversionDto (Vista/SolicitudConversionDto.cs)', leer(SRV + 'Vista/SolicitudConversionDto.cs'))
    d.codigo('Clase RespuestaDto (Vista/RespuestaDto.cs)', leer(SRV + 'Vista/RespuestaDto.cs'))
    d.codigo('Clases CategoriaDto y UnidadDto (Vista)', varios(SRV + 'Vista/CategoriaDto.cs', SRV + 'Vista/UnidadDto.cs'))
    d.p('El `RespuestaMapeador` transforma los objetos del modelo en `RespuestaDto`, fijando el estado `OK` y el código `200` en las respuestas exitosas.')
    d.codigo('Clase RespuestaMapeador (Vista/RespuestaMapeador.cs)', leer(SRV + 'Vista/RespuestaMapeador.cs'))

    d.paso_('Definición de los contratos de servicio')
    d.p('Se definen dos contratos. `IAutenticacionWS` es público (no requiere token) y expone `login` y `cambiarClave`. '
        '`IConversionUnidadesWS` es protegido y expone el catálogo y las tres conversiones. Cada operación declara con '
        '`[FaultContract]` que puede devolver un `ErrorServicio`, y `[return: MessageParameter(Name = "return")]` fija el '
        'nombre del elemento de retorno en el XML.')
    d.codigo('Interfaz IAutenticacionWS (Controladores/IAutenticacionWS.cs)', leer(SRV + 'Controladores/IAutenticacionWS.cs'))
    d.codigo('Interfaz IConversionUnidadesWS (Controladores/IConversionUnidadesWS.cs)', leer(SRV + 'Controladores/IConversionUnidadesWS.cs'))

    d.paso_('Manejo estructurado de errores')
    d.p('`ExcepcionServicio` hereda de `FaultException<ErrorServicio>`. Cuando cualquier capa la lanza, WCF la convierte en '
        'un SOAP Fault cuyo detalle contiene el código y el mensaje del error, de modo que el cliente siempre recibe una '
        'respuesta estructurada (RQF-010).')
    d.codigo('Clases ErrorServicio y ExcepcionServicio (Servicios/ExcepcionServicio.cs)', leer(SRV + 'Servicios/ExcepcionServicio.cs'))

    d.paso_('Estrategias de conversión')
    d.p('El cálculo se implementa con el patrón Strategy. La interfaz `IConversor` indica qué categorías soporta cada '
        'estrategia. `ConversorLineal` resuelve longitud y masa multiplicando por el factor de origen y dividiendo por el de '
        'destino; `ConversorTemperatura` convierte primero a grados Celsius y luego a la unidad destino mediante fórmulas.')
    d.codigo('Interfaz IConversor (Servicios/Conversores/IConversor.cs)', leer(SRV + 'Servicios/Conversores/IConversor.cs'))
    d.codigo('Clase ConversorLineal (Servicios/Conversores/ConversorLineal.cs)', leer(SRV + 'Servicios/Conversores/ConversorLineal.cs'))
    d.codigo('Clase ConversorTemperatura (Servicios/Conversores/ConversorTemperatura.cs)', leer(SRV + 'Servicios/Conversores/ConversorTemperatura.cs'))

    d.paso_('Servicios de negocio')
    d.p('`ServicioValidacion` comprueba la categoría, las unidades y el valor (RQF-008). `ServicioCatalogo` devuelve las '
        'categorías y las unidades de una categoría. `ServicioConversion` valida la solicitud, elige la estrategia adecuada '
        'y redondea el resultado a cuatro decimales. `ServicioAutenticacion` valida las credenciales, emite el token y '
        'gestiona el cambio de clave, que cierra todas las sesiones activas.')
    d.codigo('Clase ServicioValidacion (Servicios/ServicioValidacion.cs)', leer(SRV + 'Servicios/ServicioValidacion.cs'))
    d.codigo('Clase ServicioCatalogo (Servicios/ServicioCatalogo.cs)', leer(SRV + 'Servicios/ServicioCatalogo.cs'))
    d.codigo('Clase ServicioConversion (Servicios/ServicioConversion.cs)', leer(SRV + 'Servicios/ServicioConversion.cs'))
    d.codigo('Clase ServicioAutenticacion (Servicios/ServicioAutenticacion.cs)', leer(SRV + 'Servicios/ServicioAutenticacion.cs'))

    d.paso_('Seguridad mediante token en la cabecera SOAP')
    d.p('`GestorToken` es una instancia única que guarda en memoria las sesiones activas. Al iniciar sesión emite un token '
        'opaco (GUID) con su fecha de expiración; al validar, distingue un token inexistente (AUTH-001) de uno caducado '
        '(AUTH-002). El usuario, la clave inicial y la duración del token se leen de `Web.config`.')
    d.codigo('Clase GestorToken (Seguridad/GestorToken.cs)', leer(SRV + 'Seguridad/GestorToken.cs'))
    d.p('`ManejadorAutenticacion` implementa `IDispatchMessageInspector`: antes de ejecutar cualquier operación lee el '
        'elemento `token` de la cabecera SOAP y lo valida. Si la validación falla, la excepción se devuelve como SOAP Fault '
        'y la operación nunca se ejecuta.')
    d.codigo('Clase ManejadorAutenticacion (Seguridad/ManejadorAutenticacion.cs)', leer(SRV + 'Seguridad/ManejadorAutenticacion.cs'))
    d.p('Para aplicar el inspector se crea el atributo `[Protegido]`, que implementa `IServiceBehavior` y registra el '
        'inspector en todos los endpoints del servicio excepto el de metadatos, de modo que el WSDL siga siendo público.')
    d.codigo('Atributo ProtegidoAttribute (Seguridad/ProtegidoAttribute.cs)', leer(SRV + 'Seguridad/ProtegidoAttribute.cs'))

    d.paso_('Implementación de los servicios WCF')
    d.p('Las clases de la carpeta Controladores implementan los contratos y únicamente delegan en los servicios de negocio '
        'y en el mapeador. `ConversionUnidadesWS` se decora con `[Protegido]`, mientras que `AutenticacionWS` queda pública '
        'para permitir el inicio de sesión.')
    d.codigo('Clase AutenticacionWS (Controladores/AutenticacionWS.cs)', leer(SRV + 'Controladores/AutenticacionWS.cs'))
    d.codigo('Clase ConversionUnidadesWS (Controladores/ConversionUnidadesWS.cs)', leer(SRV + 'Controladores/ConversionUnidadesWS.cs'))
    d.p('Finalmente se crean en la raíz del proyecto los archivos `.svc`, que indican a IIS qué clase atiende cada endpoint:')
    d.codigo('Archivos AutenticacionWS.svc y ConversionUnidadesWS.svc', varios(SRV + 'AutenticacionWS.svc', SRV + 'ConversionUnidadesWS.svc'))

    d.paso_('Configuración del archivo Web.config')
    d.p('En `appSettings` se definen la duración del token en minutos y las credenciales iniciales del usuario único '
        '(MONSTER / MONSTER9). En `system.serviceModel` se habilita la publicación del WSDL, se ocultan los detalles '
        'internos de las excepciones y se asigna `basicHttpBinding` (SOAP 1.1) al esquema HTTP.')
    d.codigo('Archivo Web.config del servidor', leer(SRV + 'Web.config'))

    d.paso_('Ejecución local y verificación del WSDL')
    d.p('En las propiedades del proyecto, pestaña **Web**, se configura IIS Express con la dirección `http://localhost:8095/`. '
        'Al ejecutar el proyecto (F5) se abre el listado del directorio con los dos archivos `.svc`. Al agregar `?wsdl` a la '
        'dirección de cada servicio se obtiene su contrato:')
    d.vineta('`http://localhost:8095/AutenticacionWS.svc?wsdl`')
    d.vineta('`http://localhost:8095/ConversionUnidadesWS.svc?wsdl`')
    d.figura_('Servicio AutenticacionWS en ejecución.', 'navegador mostrando la página del servicio http://localhost:8095/AutenticacionWS.svc')
    d.figura_('Contrato WSDL del servicio ConversionUnidadesWS.', 'navegador mostrando http://localhost:8095/ConversionUnidadesWS.svc?wsdl')

    d.paso_('Prueba de los mensajes SOAP')
    d.p('Con una herramienta como SoapUI o Postman se pueden enviar mensajes al servicio. El inicio de sesión no requiere '
        'cabecera y devuelve el token:')
    d.codigo('Petición y respuesta SOAP de la operación login', '''POST http://localhost:8095/AutenticacionWS.svc
Content-Type: text/xml; charset=utf-8
SOAPAction: "http://wservicios.controladores.monster.edu.ec/AutenticacionWS/login"

<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:ws="http://wservicios.controladores.monster.edu.ec/">
  <soap:Body>
    <ws:login>
      <ws:usuario>MONSTER</ws:usuario>
      <ws:clave>MONSTER9</ws:clave>
    </ws:login>
  </soap:Body>
</soap:Envelope>

<!-- Respuesta -->
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/">
  <s:Body>
    <loginResponse xmlns="http://wservicios.controladores.monster.edu.ec/">
      <return>
        <estado>OK</estado>
        <codigo>200</codigo>
        <mensaje>Autenticación exitosa</mensaje>
        <token>3f2b8c1e-6a47-4d0e-9b55-1c2d3e4f5a6b</token>
        <expiraEn>2026-10-24T15:30:00.0000000Z</expiraEn>
      </return>
    </loginResponse>
  </s:Body>
</s:Envelope>''')
    d.p('Las operaciones protegidas deben incluir el token en la cabecera. Por ejemplo, convertir 1 kilómetro a metros:')
    d.codigo('Petición y respuesta SOAP de la operación convertirLongitud', '''POST http://localhost:8095/ConversionUnidadesWS.svc
SOAPAction: "http://wservicios.controladores.monster.edu.ec/ConversionUnidadesWS/convertirLongitud"

<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:ws="http://wservicios.controladores.monster.edu.ec/">
  <soap:Header>
    <ws:token>3f2b8c1e-6a47-4d0e-9b55-1c2d3e4f5a6b</ws:token>
  </soap:Header>
  <soap:Body>
    <ws:convertirLongitud>
      <ws:solicitud>
        <ws:valor>1</ws:valor>
        <ws:unidadOrigen>KM</ws:unidadOrigen>
        <ws:unidadDestino>M</ws:unidadDestino>
      </ws:solicitud>
    </ws:convertirLongitud>
  </soap:Body>
</soap:Envelope>

<!-- Respuesta -->
<convertirLongitudResponse xmlns="http://wservicios.controladores.monster.edu.ec/">
  <return>
    <estado>OK</estado>
    <codigo>200</codigo>
    <mensaje>Conversión realizada</mensaje>
    <resultado>1000</resultado>
    <unidadOrigen>KM</unidadOrigen>
    <unidadDestino>M</unidadDestino>
  </return>
</convertirLongitudResponse>''')
    d.p('Si el token falta, no existe o ha caducado, el servidor responde con un SOAP Fault que contiene el código del error:')
    d.codigo('Respuesta SOAP Fault por token inválido', '''<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/">
  <s:Body>
    <s:Fault>
      <faultcode>s:Server</faultcode>
      <faultstring xml:lang="es-EC">Token inválido o ausente</faultstring>
      <detail>
        <ErrorServicio xmlns="http://wservicios.controladores.monster.edu.ec/">
          <codigo>AUTH-001</codigo>
          <mensaje>Token inválido o ausente</mensaje>
        </ErrorServicio>
      </detail>
    </s:Fault>
  </s:Body>
</s:Envelope>''')
    d.figura_('Prueba de la operación convertirLongitud con el token en la cabecera.', 'SoapUI o Postman con la petición convertirLongitud y su respuesta')

    d.paso_('Publicación del servicio en una carpeta local')
    d.p('Para desplegar en IIS se hace clic derecho sobre el proyecto, se selecciona **Publicar** y se crea un perfil de '
        'tipo **Carpeta**, indicando un directorio propio, por ejemplo `C:\\inetpub\\WS_CON_UNI_DOTNET_SOAP`. Se elige la '
        'configuración **Release** y se pulsa **Publicar**.')
    d.figura_('Perfil de publicación en carpeta local.', 'asistente Publicar > Carpeta con la ruta de destino')
    d.p('Al finalizar se verifica que la carpeta contenga los archivos `.svc`, el `Web.config` y la carpeta `bin` con el ensamblado `WS_CON_UNI_DOTNET_SOAP.dll`.')
    d.figura_('Archivos generados por la publicación.', 'Explorador de archivos con AutenticacionWS.svc, ConversionUnidadesWS.svc, Web.config y bin')

    d.paso_('Habilitación de IIS y de la activación HTTP de WCF')
    d.p('En **Panel de control > Programas > Activar o desactivar las características de Windows** se habilitan:')
    d.vineta('**Internet Information Services**, incluida la **Consola de administración de IIS** y, dentro de Características de desarrollo de aplicaciones, **ASP.NET 4.8**.')
    d.vineta('**Servicios avanzados de .NET Framework 4.8 > Servicios WCF > Activación HTTP**, necesaria para que IIS atienda los archivos `.svc`.')
    d.figura_('Características de Windows para IIS y activación HTTP de WCF.', 'ventana "Características de Windows" con IIS y Servicios WCF > Activación HTTP marcados')

    d.paso_('Creación del sitio web en IIS')
    d.p('En el Administrador de IIS se hace clic derecho sobre **Sitios > Agregar sitio web**. Se asigna el nombre '
        '`WS_CON_UNI_DOTNET_SOAP`, la ruta física de la carpeta publicada y el puerto **8095**, que es el que esperan los '
        'clientes. El grupo de aplicaciones debe usar **.NET CLR v4.0** con canalización integrada.')
    d.p('Si IIS Express sigue en ejecución con el mismo puerto, debe detenerse antes de iniciar el sitio para evitar el conflicto.')
    d.figura_('Configuración del nuevo sitio web en IIS.', 'ventana "Agregar sitio web" con nombre, ruta física y puerto 8095')
    d.figura_('Grupo de aplicaciones con .NET CLR v4.0.', 'Grupos de aplicaciones > configuración básica del grupo del sitio')

    d.paso_('Permisos del grupo IIS_IUSRS')
    d.p('Sobre la carpeta publicada se abre **Propiedades > Seguridad > Editar > Agregar**, se escribe `IIS_IUSRS` y se le '
        'otorgan permisos de lectura y ejecución. Sin este permiso IIS no puede leer los archivos y responde con un error de acceso.')
    d.figura_('Permisos de lectura y ejecución para IIS_IUSRS.', 'Propiedades de la carpeta > Seguridad con IIS_IUSRS y permisos de lectura y ejecución')

    d.paso_('Verificación del servicio publicado')
    d.p('Se inicia el sitio y se navega a `http://localhost:8095/`. Como `Web.config` habilita la exploración de '
        'directorios, se listan los archivos `.svc`. Al abrir cada servicio con `?wsdl` se comprueba que el contrato se '
        'publica correctamente desde IIS. A partir de este punto los clientes pueden conectarse.')
    d.figura_('Servicio publicado en IIS.', 'navegador mostrando http://localhost:8095/ con el listado de los archivos .svc')

    # ---------------- CLIENTE CONSOLA
    d.h2('Cliente Consola')
    d.p('El cliente de consola es una aplicación .NET Framework 4.8 organizada en MVC. Consume el servidor con '
        '`ChannelFactory` de WCF a partir de interfaces que replican el contrato, sin generar código desde el WSDL.')
    d.paso_('Creación del proyecto')
    d.p('En Visual Studio se selecciona la plantilla **Aplicación de consola (.NET Framework)** para C#.')
    d.figura_('Selección de la plantilla Aplicación de consola (.NET Framework).', 'Visual Studio > Crear un proyecto > "Aplicación de consola (.NET Framework)"')
    d.paso_('Nombre del proyecto y framework')
    d.p('Se asigna el nombre **ConUni_ClienteConsola_GR3**, la ubicación `04. CLICON` y el framework **.NET Framework 4.8**.')
    d.figura_('Configuración del proyecto de consola.', 'ventana "Configure su nuevo proyecto" con nombre ConUni_ClienteConsola_GR3')
    d.paso_('Referencias del proyecto')
    d.p('Desde **Referencias > Agregar referencia > Ensamblados** se agregan `System.ServiceModel`, '
        '`System.Runtime.Serialization` y `System.Configuration`, necesarias para WCF, la serialización de los contratos de '
        'datos y la lectura de `App.config`.')
    d.figura_('Referencias de WCF agregadas al proyecto.', 'Administrador de referencias con System.ServiceModel, System.Runtime.Serialization y System.Configuration marcados')
    d.paso_('Estructura de paquetes')
    d.p('Se crea la estructura `ec/edu/monster` con las carpetas **controlador**, **modelo**, **servicio**, **util** y **vista**.')
    d.figura_('Estructura de carpetas del cliente de consola.', 'Explorador de soluciones con ec/edu/monster y sus subcarpetas')
    d.paso_('Configuración de la dirección del servidor')
    d.p('La dirección base del servidor se define en `App.config`, de modo que se puede apuntar a IIS Express o a IIS sin recompilar.')
    d.codigo('Archivo App.config del cliente de consola', leer(CON + 'App.config'))
    d.paso_('Capa de servicio: contratos y DTO')
    d.p('Las interfaces del cliente replican los contratos del servidor con los mismos nombres de operación y el mismo '
        'namespace, condición indispensable para que los mensajes coincidan.')
    d.codigo('Clase ContratoWS e interfaz IAutenticacionWS (servicio)', varios(CONE + 'servicio/ContratoWS.cs', CONE + 'servicio/IAutenticacionWS.cs'))
    d.codigo('Interfaz IConversionUnidadesWS (servicio)', leer(CONE + 'servicio/IConversionUnidadesWS.cs'))
    d.codigo('Clases RespuestaDto, SolicitudConversionDto y ErrorServicio (servicio)',
             varios(CONE + 'servicio/RespuestaDto.cs', CONE + 'servicio/SolicitudConversionDto.cs', CONE + 'servicio/ErrorServicio.cs'))
    d.paso_('Inserción del token en la cabecera SOAP')
    d.p('`ManejadorTokenSOAP` implementa `IClientMessageInspector` y agrega el elemento `token` a la cabecera de cada '
        'mensaje enviado al servicio protegido. El token se consulta en cada envío porque la fábrica se reutiliza entre sesiones.')
    d.codigo('Clase ManejadorTokenSOAP (servicio)', leer(CONE + 'servicio/ManejadorTokenSOAP.cs'))
    d.paso_('Cliente SOAP')
    d.p('`ClienteSOAPConversor` crea una fábrica de canales por servicio, guarda el token tras el inicio de sesión y '
        'traduce los SOAP Faults, los tiempos de espera y los errores de comunicación a excepciones `ErrorSOAPModelo` con '
        'mensajes comprensibles. Si el servidor responde AUTH-001 o AUTH-002, la sesión local se cierra.')
    d.codigo('Clase ClienteSOAPConversor (servicio)', leer(CONE + 'servicio/ClienteSOAPConversor.cs'))
    d.paso_('Modelos')
    d.p('Los modelos representan la sesión, las solicitudes de conversión y los errores devueltos por el servidor.')
    d.codigo('Clases SesionModelo, LoginModelo y TipoConversion (modelo)',
             varios(CONE + 'modelo/SesionModelo.cs', CONE + 'modelo/LoginModelo.cs', CONE + 'modelo/TipoConversion.cs'))
    d.codigo('Clases ConversionModelo y ErrorSOAPModelo (modelo)', varios(CONE + 'modelo/ConversionModelo.cs', CONE + 'modelo/ErrorSOAPModelo.cs'))
    d.paso_('Controladores')
    d.p('Los controladores validan la entrada del usuario y llaman al cliente SOAP. `LoginControlador` crea y comparte el '
        '`ClienteSOAPConversor` con los demás controladores para que todos usen el mismo token.')
    d.codigo('Clase LoginControlador (controlador)', leer(CONE + 'controlador/LoginControlador.cs'))
    d.codigo('Clase CatalogoControlador (controlador)', leer(CONE + 'controlador/CatalogoControlador.cs'))
    d.codigo('Clase ConversionControlador (controlador)', leer(CONE + 'controlador/ConversionControlador.cs'))
    d.paso_('Vistas y utilidades')
    d.p('Las vistas muestran los menús numéricos y los resultados. `EntradaUtil` centraliza la lectura validada de datos por teclado.')
    d.codigo('Clase LoginVista (vista)', leer(CONE + 'vista/LoginVista.cs'))
    d.codigo('Clase ConversionVista (vista)', leer(CONE + 'vista/ConversionVista.cs'))
    d.codigo('Clase CatalogoVista (vista)', leer(CONE + 'vista/CatalogoVista.cs'))
    d.codigo('Clase EntradaUtil (util)', leer(CONE + 'util/EntradaUtil.cs'))
    d.codigo('Clase Program (punto de entrada)', leer(CON + 'Program.cs'))
    d.paso_('Ejecución del cliente de consola')
    d.p('Con el servidor en ejecución se inicia el cliente. Se ingresan las credenciales MONSTER / MONSTER9; tras el inicio '
        'de sesión se muestra el menú para consultar el catálogo o realizar conversiones de longitud, masa y temperatura.')
    d.figura_('Inicio de sesión en el cliente de consola.', 'consola con el banner, el servidor configurado y el inicio de sesión exitoso')
    d.figura_('Consulta del catálogo desde la consola.', 'consola mostrando las categorías y las unidades de una categoría')
    d.figura_('Conversión de unidades desde la consola.', 'consola con una conversión de longitud, masa o temperatura y su resultado')

    # ---------------- PROYECTO COMPARTIDO
    d.h2('Proyecto compartido de interfaz (Blazor)')
    d.p('Los clientes de escritorio, móvil y web comparten la Biblioteca de clases de Razor `SistemaConversorUI.Shared`. '
        'Allí se ubican las vistas, los modelos y los adaptadores que construyen los sobres SOAP con `HttpClient`, de modo '
        'que cada cliente solo aporta su punto de entrada y su configuración.')
    d.paso_('Creación de la biblioteca de clases de Razor')
    d.p('En Visual Studio se selecciona la plantilla **Biblioteca de clases de Razor**, se asigna el nombre '
        '**SistemaConversorUI.Shared** dentro de la carpeta `04. CLIESC` y se elige **.NET 10.0**.')
    d.figura_('Creación de la Biblioteca de clases de Razor.', 'Visual Studio > plantilla "Biblioteca de clases de Razor" con nombre SistemaConversorUI.Shared')
    d.paso_('Estructura del proyecto compartido')
    d.p('Se crean las carpetas **Adaptadores**, **Interfaces**, **Modelos** y **Vistas**, y se agregan los espacios de nombres comunes en `_Imports.razor`.')
    d.figura_('Estructura del proyecto SistemaConversorUI.Shared.', 'Explorador de soluciones con Adaptadores, Interfaces, Modelos y Vistas')
    d.paso_('Modelos del cliente')
    d.p('`OpcionesCliente` permite que cada aplicación indique la dirección del servidor y el nombre con el que se presenta. '
        'Los demás modelos representan la sesión, las categorías, las unidades y la solicitud de conversión.')
    d.codigo('Modelos del proyecto compartido (Modelos)', varios(*(SHA + 'Modelos/' + n for n in
             ('OpcionesCliente.cs', 'SesionCliente.cs', 'CategoriaCliente.cs', 'UnidadCliente.cs', 'SolicitudConversionCliente.cs'))))
    d.paso_('Interfaces de los servicios')
    d.p('Las vistas dependen únicamente de estas interfaces, lo que permite sustituir la implementación sin modificar la interfaz gráfica.')
    d.codigo('Interfaces IAutenticacionSoap e IConversionSoap (Interfaces)', varios(SHA + 'Interfaces/IAutenticacionSoap.cs', SHA + 'Interfaces/IConversionSoap.cs'))
    d.paso_('Adaptadores SOAP')
    d.p('Los adaptadores construyen manualmente el sobre SOAP con `XDocument`, envían la petición con `HttpClient` y '
        'el encabezado `SOAPAction`, y leen la respuesta por nombre de elemento. Este enfoque funciona igual en Windows, '
        'Android y el servidor web, sin depender de clases generadas. El adaptador de conversión agrega el token en la '
        'cabecera SOAP y convierte los SOAP Faults en excepciones con el mensaje del servidor.')
    d.codigo('Clase AdaptadorAutenticacionSoap (Adaptadores)', leer(SHA + 'Adaptadores/AdaptadorAutenticacionSoap.cs'))
    d.codigo('Clase AdaptadorConversionSoap (Adaptadores)', leer(SHA + 'Adaptadores/AdaptadorConversionSoap.cs'))
    d.paso_('Vistas compartidas')
    d.p('`Login.razor` es el componente raíz: muestra el formulario de acceso y, una vez autenticado, alterna entre el '
        'menú, el catálogo y el conversor. Cada vista tiene su hoja de estilos aislada (`.razor.css`), lo que mantiene la '
        'misma línea gráfica en todos los clientes.')
    d.codigo('Componente Login.razor (Vistas)', leer(SHA + 'Vistas/Login.razor'))
    d.codigo('Componente Menu.razor (Vistas)', leer(SHA + 'Vistas/Menu.razor'))
    d.codigo('Componente Catalogo.razor (Vistas)', leer(SHA + 'Vistas/Catalogo.razor'))
    d.codigo('Componente Conversion.razor (Vistas)', leer(SHA + 'Vistas/Conversion.razor'))

    # ---------------- CLIENTE ESCRITORIO
    d.h2('Cliente Escritorio')
    d.paso_('Creación del proyecto MAUI Blazor Hybrid')
    d.p('Se agrega a la solución un nuevo proyecto con la plantilla **Aplicación .NET MAUI Blazor Hybrid**, con el nombre '
        '**SistemaConversorUI.Desktop** y **.NET 10.0**. En el archivo de proyecto se deja como destino únicamente Windows (`net10.0-windows10.0.19041.0`).')
    d.figura_('Creación del proyecto .NET MAUI Blazor Hybrid para escritorio.', 'Visual Studio > plantilla "Aplicación .NET MAUI Blazor Hybrid" con nombre SistemaConversorUI.Desktop')
    d.paso_('Referencia al proyecto compartido y paquetes')
    d.p('Desde **Dependencias > Agregar referencia de proyecto** se selecciona `SistemaConversorUI.Shared`. Además se agregan los paquetes NuGet `System.ServiceModel.*`.')
    d.figura_('Referencia al proyecto SistemaConversorUI.Shared.', 'Administrador de referencias con SistemaConversorUI.Shared marcado')
    d.codigo('Referencias del proyecto de escritorio (SistemaConversorUI.Desktop.csproj)', '''<ItemGroup>
  <ProjectReference Include="..\\SistemaConversorUI.Shared\\SistemaConversorUI.Shared\\SistemaConversorUI.Shared.csproj" />
</ItemGroup>
<ItemGroup>
  <PackageReference Include="System.ServiceModel.Duplex" Version="4.10.*" />
  <PackageReference Include="System.ServiceModel.Http" Version="4.10.*" />
  <PackageReference Include="System.ServiceModel.NetTcp" Version="4.10.*" />
  <PackageReference Include="System.ServiceModel.Security" Version="4.10.*" />
  <PackageReference Include="System.ServiceModel.Federation" Version="4.10.*" />
</ItemGroup>''')
    d.paso_('Registro de servicios en MauiProgram')
    d.p('En `MauiProgram.cs` se registran las opciones del cliente con la dirección del servidor, el `HttpClient` y los adaptadores SOAP mediante inyección de dependencias.')
    d.codigo('Clase MauiProgram del cliente de escritorio', leer(ESC + 'MauiProgram.cs'))
    d.paso_('Componente raíz')
    d.p('En `MainPage.xaml` se indica que el `BlazorWebView` cargue como componente raíz la vista `Login` del proyecto compartido.')
    d.codigo('Archivo MainPage.xaml del cliente de escritorio', leer(ESC + 'MainPage.xaml'))
    d.paso_('Ejecución del cliente de escritorio')
    d.p('Se selecciona **Windows Machine** como destino y se ejecuta el proyecto con el servidor en funcionamiento.')
    d.figura_('Inicio de sesión en el cliente de escritorio.', 'ventana de escritorio con la pantalla de inicio de sesión')
    d.figura_('Menú principal del cliente de escritorio.', 'ventana de escritorio con el menú "¿Qué deseas hacer?"')
    d.figura_('Catálogo de unidades en el cliente de escritorio.', 'ventana de escritorio con el catálogo de categorías y unidades')
    d.figura_('Conversión de unidades en el cliente de escritorio.', 'ventana de escritorio con una conversión y su historial')

    # ---------------- CLIENTE MÓVIL
    d.h2('Cliente Móvil')
    d.p('El cliente móvil reutiliza el mismo proyecto compartido sobre .NET MAUI Blazor Hybrid con destino Android. Solo '
        'cambian la dirección del servidor y la configuración propia de la plataforma.')
    d.paso_('Creación del proyecto móvil')
    d.p('Se agrega un proyecto **Aplicación .NET MAUI Blazor Hybrid** con el nombre **SistemaConversorUI.Movil** en la '
        'carpeta `04. CLIMOV`, con **.NET 10.0**. En el archivo de proyecto se deja como destino `net10.0-android` y se '
        'agrega la referencia a `SistemaConversorUI.Shared`, igual que en el cliente de escritorio.')
    d.figura_('Creación del proyecto .NET MAUI Blazor Hybrid para Android.', 'Visual Studio > plantilla "Aplicación .NET MAUI Blazor Hybrid" con nombre SistemaConversorUI.Movil')
    d.paso_('Registro de servicios en MauiProgram')
    d.p('El emulador de Android accede al equipo anfitrión mediante la dirección `10.0.2.2`, por lo que la URL del servidor '
        'cambia respecto al escritorio. En un dispositivo físico se usa la IP del equipo en la red local.')
    d.codigo('Clase MauiProgram del cliente móvil', '''using Microsoft.Extensions.Logging;
using SistemaConversorUI.Shared.Adaptadores;
using SistemaConversorUI.Shared.Interfaces;
using SistemaConversorUI.Shared.Modelos;

namespace SistemaConversorUI.Movil
{
    public static class MauiProgram
    {
        public static MauiApp CreateMauiApp()
        {
            var builder = MauiApp.CreateBuilder();
            builder.Services.AddSingleton(new OpcionesCliente
            {
                // 10.0.2.2 es el equipo anfitrión visto desde el emulador de Android
                UrlServidor = "http://10.0.2.2:8095/",
                NombreCliente = "Cliente móvil"
            });
            builder.Services.AddSingleton<HttpClient>();
            builder.Services.AddSingleton<IAutenticacionSoap, AdaptadorAutenticacionSoap>();
            builder.Services.AddSingleton<IConversionSoap, AdaptadorConversionSoap>();
            builder
                .UseMauiApp<App>()
                .ConfigureFonts(fonts =>
                {
                    fonts.AddFont("OpenSans-Regular.ttf", "OpenSansRegular");
                });

            builder.Services.AddMauiBlazorWebView();

#if DEBUG
            builder.Services.AddBlazorWebViewDeveloperTools();
            builder.Logging.AddDebug();
#endif

            return builder.Build();
        }
    }
}''')
    d.paso_('Permitir tráfico HTTP en Android')
    d.p('Android bloquea por defecto las conexiones HTTP sin cifrar. Como el servidor se publica en HTTP, se habilita '
        '`usesCleartextTraffic` en `Platforms/Android/AndroidManifest.xml`. Además, el sitio de IIS debe escuchar en todas '
        'las direcciones (`*:8095`) y el firewall de Windows debe permitir el puerto 8095.')
    d.codigo('Archivo AndroidManifest.xml del cliente móvil', '''<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application android:allowBackup="true"
                 android:icon="@mipmap/appicon"
                 android:supportsRtl="true"
                 android:usesCleartextTraffic="true"></application>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
</manifest>''')
    d.paso_('Ejecución en el emulador de Android')
    d.p('Se selecciona un emulador de Android (por ejemplo, Pixel con API 34) y se ejecuta el proyecto. La aplicación muestra las mismas vistas del proyecto compartido adaptadas a la pantalla del dispositivo.')
    d.figura_('Inicio de sesión en el cliente móvil.', 'emulador de Android con la pantalla de inicio de sesión')
    d.figura_('Menú principal en el cliente móvil.', 'emulador de Android con el menú principal')
    d.figura_('Conversión de unidades en el cliente móvil.', 'emulador de Android con una conversión realizada')

    # ---------------- CLIENTE WEB
    d.h2('Cliente Web')
    d.p('El cliente web es una aplicación Blazor con renderizado interactivo en el servidor. Las vistas compartidas se '
        'ejecutan en el servidor web, por lo que las llamadas SOAP salen desde ASP.NET Core y no desde el navegador, lo que '
        'evita problemas de CORS.')
    d.paso_('Creación del proyecto web')
    d.p('Se agrega un proyecto con la plantilla **Aplicación web de Blazor**, con el nombre **SistemaConversorUI.Web** en la '
        'carpeta `04. CLIWEB`, **.NET 10.0** y modo de representación interactiva **Server**.')
    d.figura_('Creación del proyecto Aplicación web de Blazor.', 'Visual Studio > plantilla "Aplicación web de Blazor" con nombre SistemaConversorUI.Web y modo Server')
    d.paso_('Referencia al proyecto compartido')
    d.p('Se agrega la referencia de proyecto a `SistemaConversorUI.Shared`, ubicado en la carpeta del cliente de escritorio.')
    d.figura_('Referencia al proyecto compartido desde el cliente web.', 'Administrador de referencias del proyecto web con SistemaConversorUI.Shared marcado')
    d.paso_('Configuración de la dirección del servidor')
    d.p('La sección `Cliente` de `appsettings.json` contiene la URL del servidor SOAP y el nombre del cliente.')
    d.codigo('Archivo appsettings.json del cliente web', leer(WEB + 'appsettings.json'))
    d.paso_('Registro de servicios en Program')
    d.p('En `Program.cs` se leen las opciones, se registran los adaptadores con `AddHttpClient` y se habilita el modo interactivo de servidor.')
    d.codigo('Archivo Program.cs del cliente web', leer(WEB + 'Program.cs'))
    d.paso_('Página de inicio')
    d.p('La página raíz `Inicio.razor` carga el mismo componente `Login` que usa el cliente de escritorio.')
    d.codigo('Página Inicio.razor del cliente web', leer(WEB + 'Components/Pages/Inicio.razor'))
    d.paso_('Ejecución del cliente web')
    d.p('Se ejecuta el proyecto, que queda disponible en `http://localhost:8097`, y se accede desde el navegador.')
    d.figura_('Inicio de sesión en el cliente web.', 'navegador en http://localhost:8097 con la pantalla de inicio de sesión')
    d.figura_('Catálogo de unidades en el cliente web.', 'navegador con el catálogo de categorías y unidades')
    d.figura_('Conversión de unidades en el cliente web.', 'navegador con una conversión realizada')

    # ------------------------------------------------------------------ CONCLUSIONES
    d.h1_('CONCLUSIONES')
    d.p('Se implementó un servicio web SOAP con Windows Communication Foundation que centraliza la autenticación, la '
        'consulta del catálogo y las conversiones de longitud, masa y temperatura, cumpliendo los requisitos funcionales '
        'RQF-001 a RQF-010 de la ERS sin necesidad de una base de datos.')
    d.p('La separación del servidor en controladores, servicios, modelo, vista y seguridad, junto con el patrón Strategy en '
        'los conversores, mantuvo los contratos SOAP libres de lógica y deja preparado el sistema para incorporar volumen, '
        'tiempo y área agregando únicamente nuevas estrategias.')
    d.p('El uso de un inspector de mensajes de WCF para validar el token en la cabecera SOAP, junto con los SOAP Faults '
        'estructurados, garantizó que todo cliente reciba siempre una respuesta con un código de error identificable.')
    d.p('La biblioteca compartida de componentes Razor permitió que los clientes de escritorio, móvil y web presenten la '
        'misma interfaz y el mismo comportamiento, mientras que el cliente de consola demostró la interoperabilidad del '
        'contrato desde .NET Framework mediante ChannelFactory.')

    # ------------------------------------------------------------------ RECOMENDACIONES
    d.h1_('RECOMENDACIONES')
    d.vineta('Publicar el servicio con HTTPS (enlace con certificado en IIS y modo de seguridad Transport) antes de un uso fuera del entorno local, ya que las credenciales y el token viajan en texto plano con basicHttpBinding.')
    d.vineta('Mantener sincronizados el contrato WSDL y los cuatro clientes: cualquier cambio en nombres de operaciones, parámetros o namespace exige actualizar las interfaces del cliente de consola y los adaptadores del proyecto compartido.')
    d.vineta('Externalizar la dirección del servidor de los clientes de escritorio y móvil a un archivo de configuración, como ya se hace en los clientes de consola y web, para evitar recompilar al cambiar de entorno.')
    d.vineta('Implementar en una segunda fase la conversión de volumen, tiempo y área, y considerar la persistencia de credenciales y sesiones, ya que actualmente se pierden al reiniciar el sitio en IIS.')
    d.vacio()

    # ------------------------------------------------------------------ REFERENCIAS
    d.h1_('REFERENCIAS BIBLIOGRÁFICAS')
    for ref in [
        'IEEE Computer Society. (1998). IEEE Std 830-1998: Recommended Practice for Software Requirements Specifications. IEEE.',
        'Microsoft. (s. f.). Windows Communication Foundation. Microsoft Learn. https://learn.microsoft.com/dotnet/framework/wcf/',
        'Microsoft. (s. f.). Message inspectors. Microsoft Learn. https://learn.microsoft.com/dotnet/framework/wcf/samples/message-inspectors',
        'Microsoft. (s. f.). Specifying and handling faults in contracts and services. Microsoft Learn. https://learn.microsoft.com/dotnet/framework/wcf/specifying-and-handling-faults-in-contracts-and-services',
        'Microsoft. (s. f.). Internet Information Services (IIS). Microsoft Learn. https://learn.microsoft.com/iis/',
        'Microsoft. (s. f.). .NET Multi-platform App UI documentation. Microsoft Learn. https://learn.microsoft.com/dotnet/maui/',
        'Microsoft. (s. f.). ASP.NET Core Blazor. Microsoft Learn. https://learn.microsoft.com/aspnet/core/blazor/',
        'World Wide Web Consortium (W3C). (2000). Simple Object Access Protocol (SOAP) 1.1. https://www.w3.org/TR/2000/NOTE-SOAP-20000508/',
        'Christensen, E., Curbera, F., Meredith, G., & Weerawarana, S. (2001). Web Services Description Language (WSDL) 1.1. W3C. https://www.w3.org/TR/2001/NOTE-wsdl-20010315',
        'World Wide Web Consortium (W3C). (2004). Web Services Architecture. https://www.w3.org/TR/ws-arch/',
    ]:
        d.vineta(ref, num=7, jc='left')
    return d

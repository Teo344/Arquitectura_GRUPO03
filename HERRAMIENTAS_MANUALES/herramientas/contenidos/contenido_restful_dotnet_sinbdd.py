# Contenido del manual RESTful .NET sin BDD - GR03
from pathlib import Path

from docxlib import Doc

# Raíz del repositorio: contenidos/ -> herramientas/ -> HERRAMIENTAS_MANUALES/ -> repo
REPO = Path(__file__).resolve().parents[3]
RAIZ = str(REPO / 'TI.1.4. PAO202651_RESTFUL_DOTNET_SINBDD_NRC36884_GR03')
SRV = RAIZ + '/04. SERVIDOR/WS_CON_UNI_DOTNET_REST/'
CON = RAIZ + '/04. CLICON/ConUni_ClienteConsola_REST_GR3/'
CONE = CON + 'ec/edu/monster/'
SHA = RAIZ + '/04. CLIESC/SistemaConversorREST.UI.Shared/'
ESC = RAIZ + '/04. CLIESC/SistemaConversorREST.UI.Desktop/'
WEB = RAIZ + '/04. CLIWEB/SistemaConversorREST.UI.Web/'

# La plantilla es el manual SOAP .NET del grupo
PORTADA = [('CON SOAP', 'CON RESTFUL')]


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
        'y los cálculos de conversión en un servicio web RESTful desarrollado en C# con ASP.NET Core Web API sobre .NET 8. '
        'El backend permite convertir medidas de longitud, masa y temperatura, y consultar un catálogo de seis categorías '
        '(longitud, peso, volumen, tiempo, temperatura y área). Volumen, tiempo y área se exponen únicamente como parte '
        'del catálogo; su conversión queda planificada para una segunda fase, según el alcance definido en la '
        'Especificación de Requerimientos de Software (ERS).')
    d.p('El servidor se organiza en capas con responsabilidades separadas: los **Controladores** exponen los recursos HTTP '
        'y no contienen lógica; los **Servicios** validan, orquestan y calculan; el **Modelo** define las categorías, '
        'unidades y códigos de error; la capa **Vista** contiene los objetos de transferencia (DTO) que se serializan en '
        'JSON; y la capa de **Seguridad** valida el token antes de que la petición llegue al controlador.')
    d.p('Los recursos se publican bajo la ruta base `/api` mediante los verbos GET y POST, intercambiando mensajes JSON. '
        'Las operaciones de catálogo y conversión exigen un token temporal en la cabecera `Authorization: Bearer`, y '
        'cualquier error se devuelve como un objeto JSON con estado, código y mensaje, acompañado del código HTTP '
        'correspondiente (400, 401, 404, 405 o 500).')
    d.p('Cuatro clientes consumen el mismo backend: una aplicación de consola en .NET 8, una aplicación de escritorio y una '
        'aplicación móvil Android construidas con un único proyecto .NET MAUI Blazor Hybrid, y una aplicación web Blazor. '
        'La solución no utiliza una base de datos: las credenciales del usuario único, las sesiones activas y el catálogo '
        'se mantienen en memoria o en el archivo `appsettings.json`.')

    # ------------------------------------------------------------------ OBJETIVOS
    d.h1_('OBJETIVOS')
    d.h2('Objetivo General')
    d.p('Documentar la configuración, la implementación, el despliegue y el consumo del servicio web RESTful en ASP.NET '
        'Core del sistema Conversión de Unidades Monster, basado en una arquitectura orientada a servicios sin base de '
        'datos, para que el equipo pueda desplegarlo en IIS y utilizar sus funciones desde los clientes de consola, '
        'escritorio, web y móvil.')
    d.h2('Objetivos Específicos')
    d.vineta('Describir el diseño por capas de la Web API, sus recursos, verbos HTTP y el formato JSON de las solicitudes y respuestas.')
    d.vineta('Explicar el mecanismo de seguridad: inicio de sesión, emisión del token Bearer, validación mediante un filtro de autorización, caducidad de la sesión y cambio de clave.')
    d.vineta('Detallar la configuración de la aplicación (Program.cs y appsettings.json) y su publicación en Internet Information Services (IIS) con el ASP.NET Core Hosting Bundle.')
    d.vineta('Proporcionar guías para el desarrollo de los clientes de consola, escritorio, móvil y web, detallando cómo consumen la API y cómo manejan los errores del servidor.')
    d.vacio()

    # ------------------------------------------------------------------ MARCO TEÓRICO
    d.h1_('MARCO TEÓRICO')
    d.h2('Servicios Web RESTful')
    d.p('REST (Representational State Transfer) es un estilo arquitectónico para sistemas distribuidos en el que la '
        'información se modela como recursos identificados por URI y manipulados mediante los verbos estándar de HTTP. '
        'Las peticiones son independientes entre sí (sin estado en el servidor), y el resultado de cada operación se '
        'comunica con los códigos de estado HTTP: 200 para éxito, 400 para datos inválidos, 401 para falta de '
        'autorización, 404 para recursos inexistentes y 500 para errores internos.')
    d.p('En este proyecto se usa GET para las consultas del catálogo, que no modifican el estado, y POST para el inicio de '
        'sesión, el cambio de clave y las conversiones, que reciben un cuerpo con datos.')
    d.h2('Formato JSON')
    d.p('JSON (JavaScript Object Notation) es un formato de texto ligero para representar objetos y listas. Es el formato de '
        'intercambio de todas las peticiones y respuestas del servicio. El servidor serializa las propiedades en '
        'camelCase, omite los campos nulos y exige que los valores numéricos se envíen como números, no como texto.')
    d.h2('Arquitectura Orientada a Servicios (SOA)')
    d.p('La Arquitectura Orientada a Servicios expone la funcionalidad del negocio como servicios autónomos y reutilizables, '
        'accesibles mediante interfaces bien definidas. En el sistema Conversión de Unidades Monster toda la lógica de '
        'cálculo y seguridad reside en el servidor, y los cuatro clientes consumen la misma API. Incorporar nuevas '
        'magnitudes solo requiere agregar una estrategia de conversión en el servidor, sin alterar los clientes.')
    d.h2('ASP.NET Core Web API')
    d.p('ASP.NET Core es el framework web multiplataforma de Microsoft. Una Web API se compone de controladores que heredan '
        'de `ControllerBase`, marcados con `[ApiController]`; sus rutas se declaran con `[Route]` y los verbos con '
        '`[HttpGet]` y `[HttpPost]`. El contenedor de inyección de dependencias crea y entrega los servicios a los '
        'controladores, y el servidor web integrado Kestrel atiende las peticiones.')
    d.p('El pipeline de ASP.NET Core admite filtros que se ejecutan alrededor de las acciones. Este proyecto usa un '
        '`IAuthorizationFilter` para validar el token antes de cada acción protegida y un `IExceptionFilter` para '
        'convertir las excepciones de negocio en respuestas JSON con el código HTTP adecuado. CORS (Cross-Origin Resource '
        'Sharing) controla qué páginas web de otros orígenes pueden invocar la API desde el navegador.')
    d.h2('Patrón de Arquitectura Modelo-Vista-Controlador (MVC)')
    d.p('El patrón MVC divide una aplicación en tres componentes con responsabilidades diferenciadas, y en este proyecto se '
        'aplica tanto en el servidor como en los clientes:')
    d.vineta('**Modelo:** representa los datos y las reglas del dominio. En el servidor corresponde a las clases `Categoria`, `Unidad` y `CodigoError`; en los clientes, a los objetos de sesión, solicitudes y respuestas.')
    d.vineta('**Vista:** presenta la información. En el servidor, la capa Vista contiene los DTO que se serializan en JSON; en los clientes, son las pantallas de consola o los componentes Razor.')
    d.vineta('**Controlador:** recibe las solicitudes y coordina el modelo y la vista. En el servidor son `AutenticacionControlador` y `ConversionUnidadesControlador`, que delegan en los servicios; en el cliente de consola son `LoginControlador`, `CatalogoControlador` y `ConversionControlador`.')
    d.h2('Internet Information Services (IIS) y ASP.NET Core Hosting Bundle')
    d.p('IIS es el servidor web de Microsoft para Windows. Para alojar aplicaciones ASP.NET Core requiere el ASP.NET Core '
        'Hosting Bundle, que instala el runtime de .NET y el módulo ASP.NET Core Module (ANCM). Este módulo recibe las '
        'peticiones en IIS y las entrega a la aplicación, que se ejecuta dentro del proceso de trabajo. El grupo de '
        'aplicaciones se configura "Sin código administrado", porque la aplicación no usa el CLR clásico de .NET Framework.')
    d.h2('.NET MAUI Blazor Hybrid y Blazor Web')
    d.p('.NET MAUI permite crear aplicaciones nativas para Windows, Android, iOS y macOS desde un mismo proyecto. La '
        'variante Blazor Hybrid incrusta componentes Razor dentro de un `BlazorWebView`. Blazor Web ejecuta esos mismos '
        'componentes en un servidor ASP.NET Core y los muestra en el navegador.')
    d.p('Los clientes de escritorio, móvil y web comparten la Biblioteca de clases de Razor `SistemaConversorREST.UI.Shared`, '
        'que contiene las vistas, los modelos y los adaptadores HTTP. El cliente de escritorio y el móvil son, además, el '
        'mismo proyecto MAUI compilado para Windows o para Android, lo que garantiza una interfaz uniforme como exige la ERS.')
    d.h2('Requisitos del sistema')
    d.p('El desarrollo se sustenta en la ERS del proyecto (norma IEEE 830). La Tabla 1 relaciona los requisitos funcionales '
        'con los recursos REST que los implementan, y la Tabla 2 resume los códigos de error y el estado HTTP con el que '
        'se devuelven.')
    d.datos('Requisitos funcionales y recursos REST que los implementan',
            ['Código', 'Requisito', 'Verbo y ruta'],
            [['RQF-001', 'Autenticar cliente y generar token temporal', 'POST /api/autenticacion/login y POST /api/autenticacion/cambiar-clave'],
             ['RQF-002', 'Consultar catálogo de categorías', 'GET /api/catalogo/categorias'],
             ['RQF-003', 'Consultar unidades por categoría', 'GET /api/catalogo/categorias/{categoria}/unidades'],
             ['RQF-004', 'Convertir longitud', 'POST /api/conversiones/longitud'],
             ['RQF-005', 'Convertir masa', 'POST /api/conversiones/masa'],
             ['RQF-006', 'Convertir temperatura', 'POST /api/conversiones/temperatura'],
             ['RQF-007', 'Validar token', 'ManejadorAutenticacion (cabecera Authorization)'],
             ['RQF-008', 'Validar datos', 'ServicioValidacion'],
             ['RQF-009', 'Generar respuesta estructurada', 'RespuestaDto / RespuestaMapeador'],
             ['RQF-010', 'Gestionar errores', 'ManejadorErrores (ErrorServicio en JSON)']],
            [1300, 3000, 4204])
    d.datos('Códigos de error y estado HTTP de las respuestas de falla',
            ['Código', 'HTTP', 'Mensaje', 'Situación que lo produce'],
            [['AUTH-001', '401', 'Token inválido o ausente', 'Falta la cabecera Authorization Bearer o el token no existe.'],
             ['AUTH-002', '401', 'Sesión expirada', 'El token superó su tiempo de vida (30 minutos por defecto).'],
             ['AUTH-003', '401', 'Credenciales incorrectas', 'Usuario o clave no coinciden en login o cambio de clave.'],
             ['UNIT-001', '400', 'Unidad no soportada', 'La unidad no existe o pertenece a otra categoría.'],
             ['DATA-001', '400', 'Datos inconsistentes', 'JSON mal formado, valor no numérico, nulo, no finito o negativo (excepto temperatura), categoría inexistente o clave nueva vacía.'],
             ['HTTP-xxx', '404/405/500', 'Frase del estado HTTP', 'Ruta inexistente, método no permitido o error interno.']],
            [1150, 1100, 2100, 4154])

    # ------------------------------------------------------------------ DESARROLLO
    d.h1_('DESARROLLO DEL APLICATIVO')
    d.p('El desarrollo se organiza en el servidor Web API, el cliente de consola, la biblioteca compartida de componentes '
        'Razor y los clientes de escritorio, móvil y web que la reutilizan. Se trabajó con Visual Studio 2022 o superior '
        'sobre Windows 11.')

    # ---------------- SERVIDOR
    d.h2('Servidor')
    d.paso_('Creación del proyecto')
    d.p('En Visual Studio se selecciona **Crear un proyecto** y se busca la plantilla **ASP.NET Core Web API** para C#.')
    d.figura_('Selección de la plantilla ASP.NET Core Web API.', 'Visual Studio > Crear un proyecto > plantilla "ASP.NET Core Web API"')
    d.paso_('Nombre del proyecto')
    d.p('Se asigna el nombre **WS_CON_UNI_DOTNET_REST** y se elige la carpeta `04. SERVIDOR` como ubicación.')
    d.figura_('Configuración del nombre del proyecto Web API.', 'ventana "Configure su nuevo proyecto" con nombre WS_CON_UNI_DOTNET_REST')
    d.paso_('Información adicional del proyecto')
    d.p('Se selecciona **.NET 8.0** como framework, sin autenticación, con la opción **Usar controladores** marcada y sin '
        'compatibilidad con OpenAPI ni HTTPS, ya que el servicio se publica por HTTP en la red local.')
    d.figura_('Información adicional: .NET 8.0 y uso de controladores.', 'ventana "Información adicional" con .NET 8.0 y "Usar controladores" marcado')
    d.p('En el archivo de proyecto se agrega `RollForward` para que el servicio también se ejecute si solo está instalada una versión mayor de .NET:')
    d.codigo('Archivo de proyecto WS_CON_UNI_DOTNET_REST.csproj', leer(SRV + 'WS_CON_UNI_DOTNET_REST.csproj'))
    d.paso_('Estructura de carpetas por capas')
    d.p('Se eliminan el controlador y la clase de ejemplo `WeatherForecast` y se crean las carpetas del servidor:')
    d.vineta('**Controladores:** controladores de la API y filtro de manejo de errores. No contienen lógica de negocio.')
    d.vineta('**Modelo:** categorías, unidades con su factor a la unidad base y códigos de error.')
    d.vineta('**Seguridad:** gestor de tokens, filtro de autorización y atributo que protege un controlador.')
    d.vineta('**Servicios:** validación, catálogo, conversión, autenticación y excepción de negocio. La subcarpeta **Conversores** contiene las estrategias de cálculo.')
    d.vineta('**Vista:** objetos de transferencia (DTO) que se serializan en JSON y el mapeador de respuestas.')
    d.figura_('Estructura de carpetas del servidor en el Explorador de soluciones.', 'Explorador de soluciones con Controladores, Modelo, Seguridad, Servicios/Conversores y Vista')

    d.paso_('Modelo del dominio')
    d.p('La clase `Categoria` define las seis categorías del catálogo e indica cuáles son convertibles. La clase `Unidad` '
        'registra cada unidad con su código, nombre, símbolo, categoría y factor de conversión a la unidad base (metro para '
        'longitud, kilogramo para masa); en temperatura el factor es nulo porque se usan fórmulas. `CodigoError` '
        'centraliza los códigos de la Tabla 2.')
    d.codigo('Clase Categoria (Modelo/Categoria.cs)', leer(SRV + 'Modelo/Categoria.cs'))
    d.codigo('Clase Unidad (Modelo/Unidad.cs)', leer(SRV + 'Modelo/Unidad.cs'))
    d.codigo('Clase CodigoError (Modelo/CodigoError.cs)', leer(SRV + 'Modelo/CodigoError.cs'))

    d.paso_('Objetos de transferencia (DTO)')
    d.p('Los DTO de la carpeta Vista definen el JSON que entra y sale de la API. `CredencialesDto` y `CambioClaveDto` son los '
        'cuerpos de autenticación; `SolicitudConversionDto` es el cuerpo de las conversiones. `RespuestaDto` es la '
        'respuesta común: los campos que no aplican a la operación quedan nulos y no se incluyen en el JSON.')
    d.codigo('Clases CredencialesDto, CambioClaveDto y SolicitudConversionDto (Vista)',
             varios(SRV + 'Vista/CredencialesDto.cs', SRV + 'Vista/CambioClaveDto.cs', SRV + 'Vista/SolicitudConversionDto.cs'))
    d.codigo('Clase RespuestaDto (Vista/RespuestaDto.cs)', leer(SRV + 'Vista/RespuestaDto.cs'))
    d.codigo('Clases CategoriaDto y UnidadDto (Vista)', varios(SRV + 'Vista/CategoriaDto.cs', SRV + 'Vista/UnidadDto.cs'))
    d.p('El `RespuestaMapeador` transforma los objetos del modelo en `RespuestaDto`, fijando el estado `OK` y el código `200` en las respuestas exitosas.')
    d.codigo('Clase RespuestaMapeador (Vista/RespuestaMapeador.cs)', leer(SRV + 'Vista/RespuestaMapeador.cs'))

    d.paso_('Excepción de negocio y cuerpo de error')
    d.p('`ExcepcionServicio` transporta un `CodigoError`. `ErrorServicio` es el cuerpo JSON de toda respuesta de error, con '
        'estado `ERROR`, código y mensaje (RQF-010).')
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
        'y redondea el resultado a cuatro decimales. `ServicioAutenticacion` recibe el `GestorToken` por inyección de '
        'dependencias, valida las credenciales, emite el token y gestiona el cambio de clave, que cierra todas las sesiones.')
    d.codigo('Clase ServicioValidacion (Servicios/ServicioValidacion.cs)', leer(SRV + 'Servicios/ServicioValidacion.cs'))
    d.codigo('Clase ServicioCatalogo (Servicios/ServicioCatalogo.cs)', leer(SRV + 'Servicios/ServicioCatalogo.cs'))
    d.codigo('Clase ServicioConversion (Servicios/ServicioConversion.cs)', leer(SRV + 'Servicios/ServicioConversion.cs'))
    d.codigo('Clase ServicioAutenticacion (Servicios/ServicioAutenticacion.cs)', leer(SRV + 'Servicios/ServicioAutenticacion.cs'))

    d.paso_('Seguridad mediante token Bearer')
    d.p('`GestorToken` guarda en memoria las sesiones activas. Lee de `appsettings.json` la duración del token y las '
        'credenciales iniciales; al iniciar sesión emite un token opaco (GUID) con su expiración, y al validar distingue un '
        'token inexistente (AUTH-001) de uno caducado (AUTH-002).')
    d.codigo('Clase GestorToken (Seguridad/GestorToken.cs)', leer(SRV + 'Seguridad/GestorToken.cs'))
    d.p('`ManejadorAutenticacion` implementa `IAuthorizationFilter`: antes de cada acción lee la cabecera '
        '`Authorization: Bearer <token>` y la valida. Si falla, corta la petición con un 401 y el mismo cuerpo '
        '`ErrorServicio`. El atributo `[Protegido]` aplica este filtro a un controlador completo.')
    d.codigo('Clase ManejadorAutenticacion (Seguridad/ManejadorAutenticacion.cs)', leer(SRV + 'Seguridad/ManejadorAutenticacion.cs'))
    d.codigo('Atributo ProtegidoAttribute (Seguridad/ProtegidoAttribute.cs)', leer(SRV + 'Seguridad/ProtegidoAttribute.cs'))

    d.paso_('Manejo centralizado de errores')
    d.p('`ManejadorErrores` implementa `IExceptionFilter` y convierte cualquier excepción en una respuesta JSON: los errores '
        'AUTH salen con 401, los de datos con 400 y los no controlados con 500. También construye la respuesta de rutas '
        'inexistentes o métodos no permitidos (HTTP-404, HTTP-405), de modo que el cliente siempre recibe el mismo formato.')
    d.codigo('Clase ManejadorErrores (Controladores/ManejadorErrores.cs)', leer(SRV + 'Controladores/ManejadorErrores.cs'))

    d.paso_('Implementación de los controladores')
    d.p('`AutenticacionControlador` es público y expone el inicio de sesión y el cambio de clave bajo `api/autenticacion`. '
        '`ConversionUnidadesControlador` está decorado con `[Protegido]` y expone el catálogo y las tres conversiones. '
        'Ambos reciben los servicios por constructor y solo delegan en ellos.')
    d.codigo('Clase AutenticacionControlador (Controladores/AutenticacionControlador.cs)', leer(SRV + 'Controladores/AutenticacionControlador.cs'))
    d.codigo('Clase ConversionUnidadesControlador (Controladores/ConversionUnidadesControlador.cs)', leer(SRV + 'Controladores/ConversionUnidadesControlador.cs'))

    d.paso_('Configuración de la aplicación')
    d.p('En `Program.cs` se registran los controladores con el filtro de errores, se configura el JSON (sin nulos y con '
        'números estrictos), se responde DATA-001 ante JSON inválido, se registran los servicios como instancias únicas y '
        'se habilita CORS para los orígenes configurados.')
    d.codigo('Archivo Program.cs del servidor', leer(SRV + 'Program.cs'))
    d.p('En `appsettings.json` se definen la duración del token, las credenciales iniciales del usuario único (MONSTER / '
        'MONSTER9) y los orígenes permitidos por CORS.')
    d.codigo('Archivo appsettings.json del servidor', leer(SRV + 'appsettings.json'))

    d.paso_('Ejecución local con Kestrel')
    d.p('El archivo `launchSettings.json` define dos perfiles. El perfil **http** atiende en `http://localhost:8096` y abre '
        'el navegador en el catálogo; el perfil **red** escucha en todas las interfaces (`0.0.0.0:8096`) para que el '
        'emulador de Android u otros equipos puedan conectarse.')
    d.codigo('Archivo launchSettings.json del servidor', leer(SRV + 'Properties/launchSettings.json'))
    d.p('Al ejecutar con el perfil http, el navegador abre `http://localhost:8096/api/catalogo/categorias`. Como la petición '
        'no lleva token, la API responde 401 con el error AUTH-001, lo que confirma que el servicio y la seguridad están activos.')
    d.figura_('Servicio en ejecución con Kestrel y respuesta AUTH-001 sin token.', 'navegador en http://localhost:8096/api/catalogo/categorias mostrando el JSON de error AUTH-001')

    d.paso_('Prueba de los recursos REST')
    d.p('Con Postman o un archivo `.http` de Visual Studio se prueban los recursos. El inicio de sesión devuelve el token:')
    d.codigo('Petición y respuesta de POST /api/autenticacion/login', '''POST http://localhost:8096/api/autenticacion/login
Content-Type: application/json

{
  "usuario": "MONSTER",
  "clave": "MONSTER9"
}

HTTP/1.1 200 OK
{
  "estado": "OK",
  "codigo": "200",
  "mensaje": "Autenticación exitosa",
  "token": "3f2b8c1e-6a47-4d0e-9b55-1c2d3e4f5a6b",
  "expiraEn": "2026-10-24T15:30:00.0000000Z"
}''')
    d.p('Las rutas protegidas deben enviar el token en la cabecera `Authorization`. Por ejemplo, convertir 1 kilómetro a metros:')
    d.codigo('Petición y respuesta de POST /api/conversiones/longitud', '''POST http://localhost:8096/api/conversiones/longitud
Authorization: Bearer 3f2b8c1e-6a47-4d0e-9b55-1c2d3e4f5a6b
Content-Type: application/json

{
  "valor": 1,
  "unidadOrigen": "KM",
  "unidadDestino": "M"
}

HTTP/1.1 200 OK
{
  "estado": "OK",
  "codigo": "200",
  "mensaje": "Conversión realizada",
  "resultado": 1000,
  "unidadOrigen": "KM",
  "unidadDestino": "M"
}''')
    d.p('Si el token falta o ha caducado, o los datos son inválidos, la API responde con el estado HTTP correspondiente y el cuerpo de error:')
    d.codigo('Respuestas de error 401 y 400', '''GET http://localhost:8096/api/catalogo/categorias

HTTP/1.1 401 Unauthorized
{
  "estado": "ERROR",
  "codigo": "AUTH-001",
  "mensaje": "Token inválido o ausente"
}

POST http://localhost:8096/api/conversiones/masa   (unidadOrigen: "KM")

HTTP/1.1 400 Bad Request
{
  "estado": "ERROR",
  "codigo": "UNIT-001",
  "mensaje": "Unidad no soportada"
}''')
    d.figura_('Prueba de POST /api/conversiones/longitud con el token Bearer.', 'Postman con la petición de conversión, la cabecera Authorization y la respuesta 200')

    d.paso_('Publicación del servicio en una carpeta local')
    d.p('Se hace clic derecho sobre el proyecto, se selecciona **Publicar** y se crea un perfil de tipo **Carpeta**, por '
        'ejemplo `C:\\inetpub\\WS_CON_UNI_DOTNET_REST`, con configuración **Release** y modo de implementación '
        '**Dependiente del marco**. Al publicar, Visual Studio genera el ensamblado, los archivos `appsettings.json` y un '
        '`web.config` que configura el módulo ASP.NET Core para IIS.')
    d.figura_('Perfil de publicación en carpeta local.', 'asistente Publicar > Carpeta con la ruta de destino')
    d.figura_('Archivos generados por la publicación.', 'Explorador de archivos con WS_CON_UNI_DOTNET_REST.dll, appsettings.json y web.config')

    d.paso_('Instalación de IIS y del ASP.NET Core Hosting Bundle')
    d.p('En **Activar o desactivar las características de Windows** se habilita **Internet Information Services** con su '
        'consola de administración. Luego se descarga desde el sitio de .NET e instala el **ASP.NET Core Hosting Bundle** '
        'correspondiente a la versión del runtime (.NET 8 o superior). Tras la instalación se reinicia IIS con el comando '
        '`iisreset` en una consola de administrador.')
    d.figura_('Instalación del ASP.NET Core Hosting Bundle.', 'instalador del ASP.NET Core Hosting Bundle finalizado correctamente')

    d.paso_('Creación del sitio web en IIS')
    d.p('En el Administrador de IIS se selecciona **Sitios > Agregar sitio web**. Se asigna el nombre '
        '`WS_CON_UNI_DOTNET_REST`, la ruta física de la carpeta publicada y el puerto **8096**, dejando la dirección IP en '
        '"Todas las no asignadas" para que el emulador y otros equipos puedan acceder. El grupo de aplicaciones del sitio '
        'se configura con **Sin código administrado**.')
    d.p('Si el proyecto sigue ejecutándose con Kestrel en el mismo puerto, debe detenerse antes de iniciar el sitio.')
    d.figura_('Configuración del nuevo sitio web en IIS.', 'ventana "Agregar sitio web" con nombre, ruta física y puerto 8096')
    d.figura_('Grupo de aplicaciones sin código administrado.', 'Grupos de aplicaciones > configuración básica con "Sin código administrado"')

    d.paso_('Permisos del grupo IIS_IUSRS')
    d.p('Sobre la carpeta publicada se abre **Propiedades > Seguridad > Editar > Agregar**, se escribe `IIS_IUSRS` y se le '
        'otorgan permisos de lectura y ejecución. Sin este permiso IIS no puede leer los archivos de la aplicación.')
    d.figura_('Permisos de lectura y ejecución para IIS_IUSRS.', 'Propiedades de la carpeta > Seguridad con IIS_IUSRS y permisos de lectura y ejecución')

    d.paso_('Verificación del servicio publicado')
    d.p('Se inicia el sitio y se navega a `http://localhost:8096/api/catalogo/categorias`; la respuesta 401 con AUTH-001 '
        'confirma que IIS ejecuta la API. Para descartar problemas de red se puede iniciar sesión desde Postman contra la '
        'misma dirección. A partir de este punto los clientes pueden conectarse.')
    d.figura_('Servicio publicado en IIS.', 'navegador mostrando la respuesta JSON del servicio publicado en IIS en el puerto 8096')

    # ---------------- CLIENTE CONSOLA
    d.h2('Cliente Consola')
    d.p('El cliente de consola es una aplicación .NET 8 organizada en MVC. Consume la API con un único `HttpClient` y '
        '`System.Text.Json`, y todas sus operaciones son asíncronas.')
    d.paso_('Creación del proyecto')
    d.p('En Visual Studio se selecciona la plantilla **Aplicación de consola** para C#.')
    d.figura_('Selección de la plantilla Aplicación de consola.', 'Visual Studio > Crear un proyecto > "Aplicación de consola"')
    d.paso_('Nombre del proyecto y framework')
    d.p('Se asigna el nombre **ConUni_ClienteConsola_REST_GR3**, la ubicación `04. CLICON` y el framework **.NET 8.0**. En el '
        'archivo de proyecto se indica que `appsettings.json` se copie a la carpeta de salida.')
    d.figura_('Configuración del proyecto de consola.', 'ventanas de nombre del proyecto y de información adicional con .NET 8.0')
    d.codigo('Archivo de proyecto ConUni_ClienteConsola_REST_GR3.csproj', leer(CON + 'ConUni_ClienteConsola_REST_GR3.csproj'))
    d.paso_('Estructura de paquetes')
    d.p('Se crea la estructura `ec/edu/monster` con las carpetas **controlador**, **modelo**, **servicio**, **util** y **vista**.')
    d.figura_('Estructura de carpetas del cliente de consola.', 'Explorador de soluciones con ec/edu/monster y sus subcarpetas')
    d.paso_('Configuración de la dirección del servidor')
    d.p('La URL base de la API se lee de la variable de entorno `REST_BASE_URL` o, si no existe, de `appsettings.json`.')
    d.codigo('Archivo appsettings.json del cliente de consola', leer(CON + 'appsettings.json'))
    d.paso_('Capa de servicio: DTO')
    d.p('Los DTO del cliente reflejan el JSON del servidor.')
    d.codigo('Clases CredencialesDto, SolicitudConversionDto y RespuestaDto (servicio)',
             varios(CONE + 'servicio/CredencialesDto.cs', CONE + 'servicio/SolicitudConversionDto.cs', CONE + 'servicio/RespuestaDto.cs'))
    d.codigo('Clases CategoriaDto y UnidadDto (servicio)', varios(CONE + 'servicio/CategoriaDto.cs', CONE + 'servicio/UnidadDto.cs'))
    d.paso_('Cliente REST')
    d.p('`ClienteRESTConversor` es la única capa que conoce HTTP, JSON y las rutas del servidor. Guarda la sesión tras el '
        'inicio de sesión, agrega la cabecera `Authorization: Bearer` en las rutas protegidas y traduce los errores JSON, '
        'los tiempos de espera y las fallas de conexión a excepciones `ErrorRESTModelo` con mensajes comprensibles.')
    d.codigo('Clase ClienteRESTConversor (servicio)', leer(CONE + 'servicio/ClienteRESTConversor.cs'))
    d.paso_('Modelos')
    d.p('Los modelos representan la sesión, las solicitudes de conversión y los errores devueltos por la API.')
    d.codigo('Clases SesionModelo, LoginModelo y TipoConversion (modelo)',
             varios(CONE + 'modelo/SesionModelo.cs', CONE + 'modelo/LoginModelo.cs', CONE + 'modelo/TipoConversion.cs'))
    d.codigo('Clases ConversionModelo, CategoriaModelo, UnidadModelo y ErrorRESTModelo (modelo)',
             varios(CONE + 'modelo/ConversionModelo.cs', CONE + 'modelo/CategoriaModelo.cs', CONE + 'modelo/UnidadModelo.cs', CONE + 'modelo/ErrorRESTModelo.cs'))
    d.paso_('Controladores')
    d.p('Los controladores validan la entrada del usuario y llaman al cliente REST. `LoginControlador` comparte su '
        '`ClienteRESTConversor` con los demás controladores para que todos usen el mismo token.')
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
    d.figura_('Inicio de sesión en el cliente de consola.', 'consola con el banner, la URL del servidor y el inicio de sesión exitoso')
    d.figura_('Consulta del catálogo desde la consola.', 'consola mostrando las categorías y las unidades de una categoría')
    d.figura_('Conversión de unidades desde la consola.', 'consola con una conversión y su resultado')

    # ---------------- PROYECTO COMPARTIDO
    d.h2('Proyecto compartido de interfaz (Blazor)')
    d.p('Los clientes de escritorio, móvil y web comparten la Biblioteca de clases de Razor `SistemaConversorREST.UI.Shared`, '
        'que contiene las vistas, los modelos y los adaptadores que consumen la API con `HttpClient`.')
    d.paso_('Creación de la biblioteca de clases de Razor')
    d.p('En Visual Studio se selecciona la plantilla **Biblioteca de clases de Razor**, con el nombre '
        '**SistemaConversorREST.UI.Shared** dentro de la carpeta `04. CLIESC` y **.NET 10.0**.')
    d.figura_('Creación de la Biblioteca de clases de Razor.', 'Visual Studio > plantilla "Biblioteca de clases de Razor" con nombre SistemaConversorREST.UI.Shared')
    d.paso_('Estructura del proyecto compartido')
    d.p('Se crean las carpetas **Adaptadores**, **Interfaces**, **Modelos** y **Vistas**, y se agregan los espacios de nombres comunes en `_Imports.razor`.')
    d.figura_('Estructura del proyecto SistemaConversorREST.UI.Shared.', 'Explorador de soluciones con Adaptadores, Interfaces, Modelos y Vistas')
    d.paso_('Modelos del cliente')
    d.p('`OpcionesCliente` indica la dirección del servidor y el nombre con el que se presenta cada aplicación. '
        '`RespuestaApi` refleja la respuesta JSON del servidor, y los demás modelos representan la sesión, las '
        'categorías, las unidades y la solicitud de conversión.')
    d.codigo('Modelos del proyecto compartido (Modelos)', varios(*(SHA + 'Modelos/' + n for n in
             ('OpcionesCliente.cs', 'RespuestaApi.cs', 'SesionCliente.cs', 'CategoriaCliente.cs', 'UnidadCliente.cs', 'SolicitudConversionCliente.cs'))))
    d.paso_('Interfaces de los servicios')
    d.p('Las vistas dependen únicamente de estas interfaces, lo que permite sustituir la implementación sin modificar la interfaz gráfica.')
    d.codigo('Interfaces IAutenticacionRest e IConversionRest (Interfaces)', varios(SHA + 'Interfaces/IAutenticacionRest.cs', SHA + 'Interfaces/IConversionRest.cs'))
    d.paso_('Adaptadores REST')
    d.p('Los adaptadores reciben un `HttpClient` configurado con la dirección del servidor, envían el JSON con '
        '`JsonContent`, agregan el token Bearer en las rutas protegidas y convierten las respuestas de error en '
        'excepciones con el mensaje del servidor.')
    d.codigo('Clase AdaptadorAutenticacionRest (Adaptadores)', leer(SHA + 'Adaptadores/AdaptadorAutenticacionRest.cs'))
    d.codigo('Clase AdaptadorConversionRest (Adaptadores)', leer(SHA + 'Adaptadores/AdaptadorConversionRest.cs'))
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
    d.p('Se agrega a la solución un proyecto con la plantilla **Aplicación .NET MAUI Blazor Hybrid**, con el nombre '
        '**SistemaConversorREST.UI.Desktop** y **.NET 10.0**. El mismo proyecto se compila para Windows (escritorio) y '
        'para Android (móvil), según los destinos declarados en el archivo de proyecto.')
    d.figura_('Creación del proyecto .NET MAUI Blazor Hybrid.', 'Visual Studio > plantilla "Aplicación .NET MAUI Blazor Hybrid" con nombre SistemaConversorREST.UI.Desktop')
    d.paso_('Referencia al proyecto compartido')
    d.p('Desde **Dependencias > Agregar referencia de proyecto** se selecciona `SistemaConversorREST.UI.Shared`.')
    d.figura_('Referencia al proyecto SistemaConversorREST.UI.Shared.', 'Administrador de referencias con SistemaConversorREST.UI.Shared marcado')
    d.codigo('Destinos y referencia del proyecto (SistemaConversorREST.UI.Desktop.csproj)', '''<PropertyGroup>
    <TargetFrameworks>net10.0-android</TargetFrameworks>
    <TargetFrameworks Condition="$([MSBuild]::IsOSPlatform('windows'))">$(TargetFrameworks);net10.0-windows10.0.19041.0</TargetFrameworks>
    <UseMaui>true</UseMaui>
</PropertyGroup>
<ItemGroup>
    <ProjectReference Include="..\\SistemaConversorREST.UI.Shared\\SistemaConversorREST.UI.Shared.csproj" />
</ItemGroup>''')
    d.paso_('Registro de servicios en MauiProgram')
    d.p('En `MauiProgram.cs` se registran las opciones del cliente, un `HttpClient` con la dirección base de la API y los '
        'adaptadores. La directiva `#if ANDROID` selecciona la dirección `10.0.2.2`, con la que el emulador de Android '
        'llega al equipo anfitrión; en Windows se usa `localhost`.')
    d.codigo('Clase MauiProgram del cliente de escritorio y móvil', leer(ESC + 'MauiProgram.cs'))
    d.paso_('Componente raíz')
    d.p('En `MainPage.xaml` se indica que el `BlazorWebView` cargue como componente raíz la vista `Login` del proyecto compartido.')
    d.codigo('Archivo MainPage.xaml', leer(ESC + 'MainPage.xaml'))
    d.paso_('Ejecución del cliente de escritorio')
    d.p('Se selecciona **Windows Machine** como destino y se ejecuta el proyecto con el servidor en funcionamiento.')
    d.figura_('Inicio de sesión en el cliente de escritorio.', 'ventana de escritorio con la pantalla de inicio de sesión')
    d.figura_('Menú principal del cliente de escritorio.', 'ventana de escritorio con el menú principal')
    d.figura_('Catálogo de unidades en el cliente de escritorio.', 'ventana de escritorio con el catálogo de categorías y unidades')
    d.figura_('Conversión de unidades en el cliente de escritorio.', 'ventana de escritorio con una conversión y su historial')

    # ---------------- CLIENTE MÓVIL
    d.h2('Cliente Móvil')
    d.p('El cliente móvil es el mismo proyecto `SistemaConversorREST.UI.Desktop` compilado para Android. Comparte las vistas '
        'y los adaptadores; solo cambian la dirección del servidor, resuelta en `MauiProgram`, y la configuración propia de la plataforma.')
    d.paso_('Permitir tráfico HTTP en Android')
    d.p('Android bloquea por defecto las conexiones HTTP sin cifrar. Como la API se publica en HTTP, el manifiesto de la '
        'plataforma habilita `usesCleartextTraffic` y declara el permiso de Internet.')
    d.codigo('Archivo Platforms/Android/AndroidManifest.xml', leer(ESC + 'Platforms/Android/AndroidManifest.xml'))
    d.paso_('Acceso del emulador al servidor')
    d.p('El emulador accede al equipo anfitrión con `http://10.0.2.2:8096/`. Para ello el servidor debe escuchar en todas '
        'las interfaces: en IIS, con el enlace en "Todas las no asignadas"; con Kestrel, usando el perfil **red**. En un '
        'dispositivo físico se reemplaza la dirección por la IP del equipo en la red local y se permite el puerto 8096 en '
        'el firewall de Windows.')
    d.paso_('Ejecución en el emulador de Android')
    d.p('En la barra de Visual Studio se elige el destino **Android Emulators** y un dispositivo (por ejemplo, Pixel con '
        'API 34), y se ejecuta el proyecto.')
    d.figura_('Selección del emulador de Android como destino.', 'barra de Visual Studio con Android Emulators y el dispositivo seleccionado')
    d.figura_('Inicio de sesión en el cliente móvil.', 'emulador de Android con la pantalla de inicio de sesión')
    d.figura_('Menú principal en el cliente móvil.', 'emulador de Android con el menú principal')
    d.figura_('Conversión de unidades en el cliente móvil.', 'emulador de Android con una conversión realizada')

    # ---------------- CLIENTE WEB
    d.h2('Cliente Web')
    d.p('El cliente web es una aplicación Blazor con renderizado interactivo en el servidor. Las vistas compartidas se '
        'ejecutan en el servidor web, por lo que las llamadas a la API salen desde ASP.NET Core y no desde el navegador.')
    d.paso_('Creación del proyecto web')
    d.p('Se agrega un proyecto con la plantilla **Aplicación web de Blazor**, con el nombre **SistemaConversorREST.UI.Web** '
        'en la carpeta `04. CLIWEB`, **.NET 10.0** y modo de representación interactiva **Server**.')
    d.figura_('Creación del proyecto Aplicación web de Blazor.', 'Visual Studio > plantilla "Aplicación web de Blazor" con nombre SistemaConversorREST.UI.Web y modo Server')
    d.paso_('Referencia al proyecto compartido')
    d.p('Se agrega la referencia de proyecto a `SistemaConversorREST.UI.Shared`, ubicado en la carpeta del cliente de escritorio.')
    d.figura_('Referencia al proyecto compartido desde el cliente web.', 'Administrador de referencias del proyecto web con SistemaConversorREST.UI.Shared marcado')
    d.paso_('Configuración de la dirección del servidor')
    d.p('La sección `Cliente` de `appsettings.json` contiene la URL de la API y el nombre del cliente.')
    d.codigo('Archivo appsettings.json del cliente web', leer(WEB + 'appsettings.json'))
    d.paso_('Registro de servicios en Program')
    d.p('En `Program.cs` se leen las opciones, se registran los adaptadores con `AddHttpClient` configurando la dirección '
        'base y el tiempo de espera, y se habilita el modo interactivo de servidor.')
    d.codigo('Archivo Program.cs del cliente web', leer(WEB + 'Program.cs'))
    d.paso_('Página de inicio')
    d.p('La página raíz `Inicio.razor` carga el mismo componente `Login` que usan los clientes de escritorio y móvil.')
    d.codigo('Página Inicio.razor del cliente web', leer(WEB + 'Components/Pages/Inicio.razor'))
    d.paso_('Ejecución del cliente web')
    d.p('Se ejecuta el proyecto, que queda disponible en `http://localhost:8098`, y se accede desde el navegador.')
    d.figura_('Inicio de sesión en el cliente web.', 'navegador en http://localhost:8098 con la pantalla de inicio de sesión')
    d.figura_('Catálogo de unidades en el cliente web.', 'navegador con el catálogo de categorías y unidades')
    d.figura_('Conversión de unidades en el cliente web.', 'navegador con una conversión realizada')

    # ------------------------------------------------------------------ CONCLUSIONES
    d.h1_('CONCLUSIONES')
    d.p('Se implementó un servicio web RESTful con ASP.NET Core Web API que centraliza la autenticación, la consulta del '
        'catálogo y las conversiones de longitud, masa y temperatura, cumpliendo los requisitos funcionales RQF-001 a '
        'RQF-010 de la ERS sin necesidad de una base de datos.')
    d.p('El uso de filtros de ASP.NET Core para la autorización y el manejo de errores mantuvo los controladores libres de '
        'lógica transversal y garantizó que todo cliente reciba una respuesta JSON uniforme, con un código de error '
        'identificable y el estado HTTP adecuado.')
    d.p('El diseño por capas y el patrón Strategy permitieron reutilizar el mismo núcleo de dominio y cálculo de la versión '
        'SOAP del proyecto, cambiando únicamente la capa de exposición, lo que demuestra la independencia entre la lógica de '
        'negocio y el protocolo de comunicación.')
    d.p('La biblioteca compartida de componentes Razor y un único proyecto MAUI para Windows y Android permitieron que los '
        'clientes de escritorio, móvil y web presenten la misma interfaz y el mismo comportamiento con un mínimo de código '
        'específico por plataforma.')

    # ------------------------------------------------------------------ RECOMENDACIONES
    d.h1_('RECOMENDACIONES')
    d.vineta('Publicar la API con HTTPS (certificado en el enlace de IIS) antes de un uso fuera del entorno local, ya que las credenciales y el token Bearer viajan en texto plano por HTTP.')
    d.vineta('Mantener sincronizados las rutas y los DTO de los clientes con la API: cualquier cambio en rutas, nombres de propiedades o códigos de error exige actualizar el cliente de consola y los adaptadores del proyecto compartido.')
    d.vineta('Actualizar la lista de orígenes de CORS en appsettings.json si se agregan clientes que llamen a la API directamente desde el navegador.')
    d.vineta('Implementar en una segunda fase la conversión de volumen, tiempo y área, y considerar la persistencia de credenciales y sesiones, ya que actualmente se pierden al reiniciar la aplicación en IIS.')
    d.vacio()

    # ------------------------------------------------------------------ REFERENCIAS
    d.h1_('REFERENCIAS BIBLIOGRÁFICAS')
    for ref in [
        'Fielding, R. T. (2000). Architectural Styles and the Design of Network-based Software Architectures [Tesis doctoral, University of California, Irvine]. https://ics.uci.edu/~fielding/pubs/dissertation/top.htm',
        'IEEE Computer Society. (1998). IEEE Std 830-1998: Recommended Practice for Software Requirements Specifications. IEEE.',
        'Microsoft. (s. f.). Create web APIs with ASP.NET Core. Microsoft Learn. https://learn.microsoft.com/aspnet/core/web-api/',
        'Microsoft. (s. f.). Filters in ASP.NET Core. Microsoft Learn. https://learn.microsoft.com/aspnet/core/mvc/controllers/filters',
        'Microsoft. (s. f.). Enable Cross-Origin Requests (CORS) in ASP.NET Core. Microsoft Learn. https://learn.microsoft.com/aspnet/core/security/cors',
        'Microsoft. (s. f.). Host ASP.NET Core on Windows with IIS. Microsoft Learn. https://learn.microsoft.com/aspnet/core/host-and-deploy/iis/',
        'Microsoft. (s. f.). .NET Multi-platform App UI documentation. Microsoft Learn. https://learn.microsoft.com/dotnet/maui/',
        'Microsoft. (s. f.). ASP.NET Core Blazor. Microsoft Learn. https://learn.microsoft.com/aspnet/core/blazor/',
        'Bray, T. (Ed.). (2017). The JavaScript Object Notation (JSON) Data Interchange Format (RFC 8259). IETF. https://www.rfc-editor.org/rfc/rfc8259',
        'Fielding, R., Nottingham, M., & Reschke, J. (Eds.). (2022). HTTP Semantics (RFC 9110). IETF. https://www.rfc-editor.org/rfc/rfc9110',
    ]:
        d.vineta(ref, num=7, jc='left')
    return d

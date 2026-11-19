# Taller 2.2: consumo de una API pública con .NET, SQL Server y MVC

Versión .NET del Taller 2.1 (Java). Es una aplicación ASP.NET Core MVC (.NET 8) que consume la API
REST Countries, guarda los países en SQL Server con Entity Framework Core y aplica el patrón MVC.
También incluye la actividad avanzada: importar universidades desde otra API y guardarlas en la
entidad `Universidad`.

| Carpeta | Contenido |
|---------|-----------|
| `docs/casos_de_uso.md` | Actores, diagrama y especificación de CU-01 a CU-10 |
| `docs/arquitectura.md` | Capas MVC, secuencias, principios SOLID y diferencias con el enunciado |
| `bdd/` | Script del esquema `ApiPaisesDB` (el mismo SQL de la migración) y consultas de los reportes |
| `ApiPaisesMVC_SQLServer/` | Proyecto web (.NET 8): vistas Razor, controladores, servicio y modelo (EF Core) |
| `ApiPaisesMVC_SQLServer.Tests/` | Pruebas xUnit que no necesitan SQL Server ni red |

## Puesta en marcha

1. Instalar Visual Studio 2022 (o el SDK de .NET 8 o superior), SQL Server 2022 o superior (Express
   o Developer) con autenticación de Windows, y SSMS.
2. Crear la base de datos. Hay dos caminos y son compatibles entre sí:
   - Abrir `ApiPaisesMVC_SQLServer.sln`, dejar `ApiPaisesMVC_SQLServer` como proyecto de inicio y,
     en *Tools → NuGet Package Manager → Package Manager Console*, ejecutar `Update-Database`. Esto
     crea `ApiPaisesDB` con las tablas `Paises` y `Universidades` a partir de la migración
     `InitialCreate`.
   - Ejecutar `bdd/01_esquema_api_paises.sql` en SSMS. Es el mismo SQL de la migración y también la
     deja registrada.
3. Revisar `ApiPaisesMVC_SQLServer/appsettings.json`:
   - `ConnectionStrings:SqlServerConnection` debe apuntar a tu instancia. El enunciado usa
     `Server=localhost`, que solo sirve para la instancia por defecto; con una instancia con nombre
     se escribe, por ejemplo, `Server=.\\SQLEXPRESS01` (en JSON la barra va doble). Para usar
     autenticación de SQL Server, reemplazar `Trusted_Connection` por `User Id=...;Password=...`.
   - `RestCountries:Clave` lleva la misma clave del grupo que el Taller 2.1. La API v3.1 del
     enunciado fue retirada (hoy responde *"This API version has been deprecated"*) y la v5 pide una
     cuenta gratuita en <https://restcountries.com/sign-up>, con 1000 solicitudes al mes. Importar
     todos los países gasta unas 3. La clave demo `rc_live_demo` también funciona, pero devuelve un
     solo país de muestra.
4. Ejecutar con F5 en Visual Studio (perfil `http`), o en consola desde `ApiPaisesMVC_SQLServer` con
   `dotnet run --launch-profile http`, y abrir <http://localhost:8100>. La pantalla de inicio indica
   si SQL Server responde y si la clave de la API es propia o la demo.
5. Correr las pruebas con `dotnet test` desde esta carpeta (o desde el Test Explorer de Visual
   Studio). Usan dobles en memoria, como las del Taller 2.1, y comprueban que la migración coincide
   con el modelo.

## Pantallas y actividades

| Ruta | Pantalla | Actividad del enunciado |
|------|----------|-------------------------|
| `/` | Inicio: estado de SQL Server y de la API | — |
| `/paises` | Países: importar, CRUD, búsquedas, filtros por región y tabla paginada | Base, 2, 3 y 4 |
| `/busqueda` | Buscar en la API por nombre y guardar | 1 |
| `/reportes` | Países por región, los 5 más poblados y promedio por región | 5 |
| `/universidades` | Importar universidades por país, buscarlas y filtrarlas | Avanzada |

## Preguntas de reflexión

Respuestas a las preguntas del enunciado, aplicadas a lo que se construyó en este proyecto.

### ¿Qué principios SOLID se aplican en este proyecto?

Se aplican los cinco, y cada uno se puede señalar en el código:

- **Responsabilidad única (SRP).** Cada clase cambia por un solo motivo. `RestCountriesMapper` solo
  convierte el JSON de la API en objetos `Pais`, `PaisDAOSqlServer` solo habla con SQL Server,
  `PaisServicio` concentra las reglas (validar, evitar duplicados, decidir el fallback) y
  `PaisControlador` solo recibe la petición HTTP y elige la vista. Si mañana cambia el formato del
  JSON, se toca el mapper y nada más.
- **Abierto/cerrado (OCP).** Para cambiar de proveedor de países basta escribir otra clase que
  implemente `IPaisApiCliente`; ni el servicio ni los controladores se modifican. Así se agregó la
  API de universidades: con interfaces nuevas (`IUniversidadApiCliente`, `IUniversidadDAO`) y sin
  tocar lo que ya funcionaba con países.
- **Sustitución de Liskov (LSP).** En producción `PaisServicio` usa `PaisDAOSqlServer` y en las
  pruebas usa `PaisDAOEnMemoria`. Las dos cumplen el mismo contrato y el servicio funciona igual con
  cualquiera, por eso las 46 pruebas corren sin SQL Server ni red.
- **Segregación de interfaces (ISP).** `IPaisApiCliente` tiene solo los métodos que el servicio
  necesita (traer todos y buscar por nombre). Los reportes tienen su propio controlador
  (`ReporteControlador`), separado del CRUD.
- **Inversión de dependencias (DIP).** Controladores y servicios reciben interfaces por constructor.
  `Program.cs` es el único archivo que conoce las clases concretas y las registra
  (`AddScoped<IPaisDAO, PaisDAOSqlServer>()`, `AddHttpClient<IPaisApiCliente, RestCountriesCliente>()`).

### ¿Qué responsabilidades tiene cada capa del patrón MVC?

- **Modelo** (`ec.edu.monster.modelo` y `servicio`): las entidades `Pais` y `Universidad`, el acceso
  a datos con EF Core (`AppDbContext` y los DAO), los clientes de las dos API y las reglas de negocio
  de `PaisServicio` y `UniversidadServicio`. No sabe nada de HTML ni de botones.
- **Vista** (`ec.edu.monster.vista`): las páginas Razor. Muestran los datos que reciben en su
  `VistaModelo`, capturan lo que escribe el usuario y piden confirmación antes de importar o
  eliminar. Nunca consultan la base ni llaman a una API.
- **Controlador** (`ec.edu.monster.controlador`): recibe la petición (por ejemplo
  `POST /paises/importar`), convierte el formulario en objetos del modelo, llama al servicio y decide
  qué vista mostrar o a dónde redirigir. No tiene SQL ni reglas de negocio.

Un ejemplo completo: al pulsar "Importar desde la API", la vista envía el formulario,
`PaisControlador.Importar` llama a `PaisServicio.ImportarDesdeApiAsync`, el servicio pide los países
a `RestCountriesCliente` y los guarda con `PaisDAOSqlServer`, y el controlador redirige a `/paises`
con el resumen "N recibidos, X nuevos, Y actualizados".

### ¿Por qué usar Entity Framework en lugar de SQL incrustado en el controlador?

En este proyecto ningún controlador escribe SQL. Toda la persistencia está en los DAO y se escribe
con LINQ, por ejemplo la búsqueda de países:

```csharp
consulta = consulta.Where(p => p.Nombre!.Contains(texto) || p.Codigo == codigo);
```

EF Core traduce eso a una consulta parametrizada, así que un texto como `' OR 1=1 --` se trata
como dato y no como SQL. Otras ventajas que se notaron al hacer el taller:

- La estructura de la base sale del modelo: `AppDbContext` define el índice único en `Codigo`, el de
  universidades (país + nombre) y las restricciones `Poblacion >= 0` y `Area >= 0`, y la migración
  `InitialCreate` crea todo con `Update-Database`.
- El upsert de la importación usa una transacción (`BeginTransactionAsync`): si algo falla, no queda
  la mitad de los países guardados.
- Los reportes del enunciado (`GROUP BY Region`, `TOP 5 ... ORDER BY Poblacion DESC`, `AVG`) se
  escriben con `GroupBy`, `OrderByDescending` y `Average`, y el compilador revisa los nombres de las
  columnas.

### ¿Qué ventajas ofrece SQL Server en entornos empresariales?

Las que se usaron en el proyecto:

- Integración directa con .NET: el proveedor `UseSqlServer` de EF Core y las migraciones funcionan
  sin configuración adicional.
- Autenticación de Windows (`Trusted_Connection=True`): la aplicación se conecta sin guardar
  contraseñas en `appsettings.json`.
- Integridad garantizada por el motor: índices únicos, restricciones `CHECK` y transacciones, de
  modo que ni un error del código puede guardar un país duplicado o con población negativa.
- SQL Server Management Studio para revisar las tablas y probar las consultas de
  `bdd/02_consultas_reportes.sql`.

Además, SQL Server tiene ediciones gratuitas (Express y Developer, la que usamos es Express 2022) y
versiones administradas en la nube, como Azure SQL Database o Amazon RDS, con la misma sintaxis.

### ¿Cómo podría migrarse esta aplicación MVC a una API REST con ASP.NET Core Web API?

La mayor parte del código se reutiliza sin cambios: entidades, `AppDbContext`, los DAO, los
clientes de las API y los servicios. Lo que cambia es la capa web:

1. Los controladores pasan a heredar de `ControllerBase` con `[ApiController]` y devuelven JSON
   (`Ok(paises)`, `NotFound()`, `CreatedAtAction(...)`) en lugar de vistas.
2. Las rutas actuales se convierten en recursos REST: `GET /api/paises` (con `?pagina=&tamano=`),
   `GET /api/paises/{id}`, `POST /api/paises`, `PUT /api/paises/{id}`, `DELETE /api/paises/{id}`,
   `POST /api/paises/importar` y `GET /api/reportes`.
3. Se agregan DTO para no exponer las entidades directamente. `FormularioPais` ya cumple ese papel
   para la entrada de datos.
4. Se agrega Swagger (`AddSwaggerGen`) para documentar y probar los endpoints.
5. Las vistas Razor se quitan o se convierten en un cliente aparte (web, escritorio o móvil) que
   consume la API.

Como `Program.cs` ya registra todo con inyección de dependencias, el cambio queda en los
controladores.

### ¿Cómo se podría desplegar en Azure App Service con Azure SQL Database?

1. Crear un Azure SQL Database (por ejemplo `ApiPaisesDB`) y abrir su firewall para App Service.
2. Aplicar el esquema con `Update-Database` apuntando a la base de Azure, o ejecutar
   `bdd/01_esquema_api_paises.sql`.
3. Crear un App Service con el runtime .NET 8 y publicar el proyecto desde Visual Studio
   (*Publish → Azure App Service*).
4. Configurar los secretos en *Configuration → Application settings*, no en `appsettings.json`:
   `ConnectionStrings__SqlServerConnection` con la cadena de Azure SQL (con usuario y contraseña, o
   mejor con una identidad administrada) y `RestCountries__Clave` con la clave de la API.

El código no cambia: la aplicación ya lee la cadena de conexión y la clave desde la configuración,
y `Program.cs` ya activa HSTS y la página de error fuera del entorno de desarrollo. El grupo ya
hizo algo parecido en AWS con la RDS SQL Server `grupo03-mssql`; también ahí lo único que cambia
es la cadena de conexión.

### ¿Cómo implementar autenticación JWT y autorización por roles?

Hoy la aplicación no tiene usuarios. Para agregarlos en la versión Web API:

1. Crear la entidad `Usuario` (usuario, hash de la contraseña con BCrypt y rol) y su tabla con una
   migración.
2. Agregar `Microsoft.AspNetCore.Authentication.JwtBearer`, configurar en `Program.cs` la emisión y
   validación del token (emisor, audiencia y clave de firma guardada como secreto, no en el código)
   y llamar a `app.UseAuthentication()` antes de `app.UseAuthorization()`.
3. Crear un `POST /api/auth/login` que valide las credenciales y devuelva el token con el rol.
4. Proteger los endpoints según lo que hace cada uno:
   - `[Authorize(Roles = "Administrador")]` para importar desde la API, eliminar países e importar
     universidades, porque modifican muchos datos o gastan cuota de la API;
   - `[Authorize(Roles = "Administrador,Operador")]` para registrar y editar países;
   - acceso con cualquier usuario autenticado para consultar, buscar y ver reportes.

El cliente envía el token en cada petición con `Authorization: Bearer <token>`.

### ¿Qué ventajas tendría pasar a microservicios con Docker?

El proyecto se podría dividir en tres servicios, cada uno en su contenedor y con su propia base:

- **Países**: importación desde REST Countries, CRUD, búsquedas y filtros.
- **Universidades**: importación desde Hipolabs y consultas.
- **Reportes**: estadísticas a partir de los datos de países.

Las ventajas serían escalar solo el servicio con más carga (por ejemplo, las consultas de países),
desplegar uno sin detener los otros, y que una caída de Hipolabs no afecte a países ni a reportes.
Docker además asegura que la aplicación y SQL Server corran igual en las PC del grupo y en la nube
(`docker compose up` levantaría todo).

Para el tamaño actual del taller no hace falta: sería un solo usuario, poco tráfico y más
infraestructura que mantener (comunicación entre servicios, monitoreo, consistencia entre bases).
La separación en interfaces y servicios que ya tiene el proyecto deja preparado ese paso para cuando
haga falta.

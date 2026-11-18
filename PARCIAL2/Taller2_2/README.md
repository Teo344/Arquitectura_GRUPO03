# Taller 2.2 — Consumo de API pública con .NET, SQL Server y MVC

Versión .NET del Taller 2.1 (Java): ASP.NET Core MVC (.NET 8) que consume la API REST Countries,
guarda los países en SQL Server con Entity Framework Core y aplica el patrón MVC. Incluye la
actividad avanzada (API de universidades → entidad `Universidad`).

| Carpeta | Contenido |
|---------|-----------|
| `docs/casos_de_uso.md` | Actores, diagrama y especificación de CU-01 a CU-10 |
| `docs/arquitectura.md` | Capas MVC, secuencias, principios SOLID y diferencias con el enunciado |
| `bdd/` | Script del esquema `ApiPaisesDB` (igual a la migración) y consultas de los reportes |
| `ApiPaisesMVC_SQLServer/` | Proyecto web (.NET 8): vista Razor, controladores, servicio y modelo (EF Core) |
| `ApiPaisesMVC_SQLServer.Tests/` | Pruebas xUnit: no necesitan SQL Server ni red |

## Puesta en marcha

1. **Requisitos**: Visual Studio 2022 (o SDK de .NET 8 o superior), SQL Server 2022 o superior
   (Express o Developer) en `localhost` con autenticación de Windows, y SSMS.
2. **Base de datos**: abrir `ApiPaisesMVC_SQLServer.sln`, dejar `ApiPaisesMVC_SQLServer` como
   proyecto de inicio y en *Tools → NuGet Package Manager → Package Manager Console* ejecutar
   `Update-Database`. Crea `ApiPaisesDB` con las tablas `Paises` y `Universidades` a partir de la
   migración `InitialCreate`. También se puede ejecutar `bdd/01_esquema_api_paises.sql` en SSMS:
   es el mismo SQL y registra la migración, así que los dos caminos son compatibles.
3. **Configuración** (`ApiPaisesMVC_SQLServer/appsettings.json`):
   - `ConnectionStrings:SqlServerConnection`: la del enunciado (`Server=localhost;Database=ApiPaisesDB;
     Trusted_Connection=True;TrustServerCertificate=True;`). Para autenticación SQL Server, usar
     `User Id=...;Password=...` en lugar de `Trusted_Connection`.
   - `RestCountries:Clave`: la API v3.1 del enunciado fue retirada (hoy responde *"This API version
     has been deprecated"*). La v5 requiere una cuenta gratuita en <https://restcountries.com/sign-up>
     (1000 solicitudes al mes); va la misma clave del grupo que en el Taller 2.1. Importar todos los
     países usa unas 3 solicitudes. Con la clave demo `rc_live_demo` todo funciona, pero la API solo
     devuelve un país de muestra.
4. **Ejecutar**: F5 en Visual Studio (perfil `http`), o en consola desde `ApiPaisesMVC_SQLServer`:
   `dotnet run --launch-profile http` y abrir <http://localhost:8100>. La pantalla de inicio muestra
   si SQL Server responde y si la clave de la API es propia o la demo.
5. **Pruebas**: `dotnet test` desde esta carpeta (Test Explorer en Visual Studio). Usan dobles en
   memoria, como las del Taller 2.1, y comprueban que la migración coincide con el modelo.

## Pantallas y actividades

| Ruta | Pantalla | Actividad del enunciado |
|------|----------|-------------------------|
| `/` | Inicio: estado de SQL Server y de la API | — |
| `/paises` | Países: importar, CRUD, búsquedas y filtros por región | Base, 2, 3 y 4 |
| `/busqueda` | Buscar en la API por nombre y guardar | 1 |
| `/reportes` | Países por región, país más poblado, promedio por región | 5 |
| `/universidades` | Importar universidades por país y consultarlas | Avanzada |

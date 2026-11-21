# Taller 2.1 — Consumo de API pública con MVC (Java + MySQL y .NET + SQL Server)

La misma aplicación en dos tecnologías, con la misma arquitectura (vista, controlador,
servicio y modelo), los mismos casos de uso y el mismo diseño visual "Azul".

| Carpeta | Contenido |
|---------|-----------|
| `docs/casos_de_uso.md` | Actores, diagrama y especificación de CU-01 a CU-08 (comunes a ambas versiones) |
| `docs/arquitectura.md` | Capas, secuencias, principios SOLID, diseño de la vista y diferencias con el enunciado |
| `bdd/mysql/` | Scripts de MySQL: usuario de la aplicación y esquema `api_paises` (versión Java) |
| `bdd/sqlserver/` | Script de SQL Server: esquema `api_paises` (versión .NET) |
| `ApiPaisesMVC/` | Versión Java: proyecto Maven (JDK 21) con vista Swing |
| `ApiPaisesMVC.NET/` | Versión .NET: solución .NET 10 con vista MAUI Blazor Hybrid para Windows |

**API key**: la API v3.1 del enunciado fue retirada. La v5 requiere una cuenta gratuita en
<https://restcountries.com/sign-up> (1000 solicitudes al mes); ambas versiones ya traen la clave
del grupo. Importar todos los países usa unas 3 solicitudes. Con la clave demo `rc_live_demo`
todo funciona, pero la API solo devuelve un país de muestra.

## Versión Java (MySQL)

1. **Base de datos** (MySQL 8.0.19 o superior), como root: ejecutar
   `bdd/mysql/00_usuario_local.sql` (cambiar antes la clave del usuario) y luego
   `bdd/mysql/01_esquema_api_paises.sql`.
2. **Configuración**: copiar `ApiPaisesMVC/src/main/resources/config.properties.example` como
   `config.properties` en la misma carpeta y completar `db.clave`.
3. **Ejecutar**: abrir `ApiPaisesMVC` en NetBeans y pulsar *Run*, o `mvn compile exec:java`.
4. **Pruebas**: `mvn test` (no necesitan MySQL ni red).

## Versión .NET (SQL Server)

1. **Base de datos** (SQL Server 2019+ o Express), desde PowerShell en `bdd/sqlserver`:
   ```powershell
   sqlcmd -S .\SQLEXPRESS -E -f 65001 -i 01_esquema_api_paises.sql
   ```
   La aplicación usa autenticación de Windows, así que no hay usuario ni clave que crear.
   Si la instancia no se llama `SQLEXPRESS`, cambiar `cadenaConexion` en
   `ApiPaisesMVC.NET/ApiPaises.Desktop/Resources/Raw/configuracion.json`.
2. **Ejecutar**: abrir `ApiPaisesMVC.NET/ApiPaises.slnx` en Visual Studio 2026 (carga de trabajo
   .NET MAUI), elegir `ApiPaises.Desktop` como proyecto de inicio y pulsar F5. Desde consola:
   ```powershell
   dotnet build ApiPaises.Desktop/ApiPaises.Desktop.csproj
   & "$env:LOCALAPPDATA\MonsterBuild\Taller2_1\ApiPaises.Desktop\bin\Debug\net10.0-windows10.0.19041.0\win-x64\ApiPaises.Desktop.exe"
   ```
   Las carpetas `bin` y `obj` se generan en `%LOCALAPPDATA%\MonsterBuild` porque la ruta del
   repositorio es demasiado larga para MAUI (mismo arreglo que TG.1.4).
3. **Pruebas**: `dotnet test ApiPaises.Pruebas` (no necesitan SQL Server ni red).

En ambas versiones, la pantalla de inicio muestra si la base de datos responde y si la clave de
la API es propia o la demo.

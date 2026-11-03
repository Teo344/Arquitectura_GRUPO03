EurekaBank - Grupo 03 - Cliente web REST (.NET)
================================================

EurekaBank.Web es el cliente web de la API REST WS_EUREKA_DOTNET_REST (04. SERVIDOR). Es el
mismo cliente de escritorio MAUI (04. CLIESC) llevado al navegador: no copia nada, referencia la
biblioteca EurekaBank.Shared, que ya tiene el modelo, los controladores, el cliente REST y las
pantallas Razor con el diseño "Azul". El cliente móvil (04. CLIMOV) usa la misma biblioteca, y el
cliente web de TG.1.2 (SOAP) está hecho igual.

                 EurekaBank.Shared (04. CLIESC)
                 modelo · controlador · servicio REST · vista
                /                 |                  \
  EurekaBank.Desktop     EurekaBank.Mobile      EurekaBank.Web
  MAUI (BlazorWebView)   MAUI Android           ASP.NET Core (Blazor Web App)

Tecnologías: .NET 10, ASP.NET Core Blazor Web App con componentes interactivos en el servidor
(Interactive Server), JSON sobre HttpClient (ClienteRest de Shared, token en Authorization: Bearer).
Las llamadas a la API salen del servidor web, no del navegador.

Archivos del proyecto (todo lo demás está en EurekaBank.Shared)
  Program.cs              Lo que en el escritorio es MauiProgram.cs: registra los servicios de Shared
  Components/App.razor    Lo que en el escritorio es wwwroot/index.html (CSS de Shared, blazor.web.js)
  Components/Raiz.razor   Lo que en el escritorio es el BlazorWebView: muestra Rutas de Shared
  appsettings.json        Lo que en el escritorio es servidor.json (sección "Cliente")

Diferencias por ser una aplicación web (varios empleados en un mismo servidor)
  - Cada pestaña del navegador tiene su propio ámbito (Scoped): EstadoSesion, OpcionesCliente y los
    servicios REST no se comparten entre empleados. En MAUI son Singleton porque hay uno solo.
  - El "equipo" que el servidor guarda en logsession es la IP del navegador del empleado.
  - Recargar la página (F5) no cierra la sesión: se guarda cifrada en la pestaña
    (ProtectedSessionStorage) y se borra al salir. Si el token ya caducó, la primera operación da
    AUTH-003 y se vuelve al login. Una pestaña nueva o privada pide iniciar sesión.

Requisitos
  - SQL Server Express (localhost\SQLEXPRESS) con la BD eurekabank de 03. BDD.
  - La API REST corriendo en el puerto 8097, desde 04. SERVIDOR\WS_EUREKA_DOTNET_REST
    (con su conexion.json configurado):
      dotnet run

Ejecutar
  En Visual Studio: abrir EurekaBank.Web.csproj y F5 (perfil http).
  Por consola, desde 04. CLIWEB\EurekaBank.Web:
    dotnet run --launch-profile http
  Abrir: http://localhost:8099/        Usuario de prueba: MONSTER / MONSTER9
  Puerto 8099 porque 8097 es la API REST y 8098 el cliente web de TG.1.2.
  Perfil "red" (http://0.0.0.0:8099) para entrar desde otros equipos de la red.

Configuración
  appsettings.json, sección "Cliente":
    "UrlServidor": "http://localhost:8097/"   (API REST; cambiar la IP si está en otro equipo)
    "NombreCliente": "Cliente web"            (pie de la pantalla de acceso)
  bin y obj se generan en %LOCALAPPDATA%\MonsterBuild\TG.1.4 (Directory.Build.props) porque la
  ruta de esta carpeta es muy larga para Windows.

Prueba
  Con EurekaBank.Web corriendo, desde 04. CLIWEB:
    dotnet run PruebaClienteWeb.cs
  Comprueba que todas las rutas de las pantallas responden (también al recargar) y que se sirven el
  diseño compartido (CSS, fuentes, foto) y Blazor. Debe terminar en PRUEBA APROBADA.
  Las acciones (login, consultas, transacciones) usan la conexión interactiva de Blazor y se prueban
  en el navegador: login, menú, F5, consulta, depósito con confirmación y comprobante, error de
  saldo (MOV-002) y salir.

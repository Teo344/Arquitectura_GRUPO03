using ec.edu.monster.servicio;
using ec.edu.monster.vista;
using ec.edu.monster.web;

// Equivale a MauiProgram.cs del cliente de escritorio: registra los servicios de EurekaBank.Shared y
// aloja sus pantallas, aquí en un servidor web. Las llamadas SOAP salen de este servidor, no del navegador.
WebApplicationBuilder builder = WebApplication.CreateBuilder(args);

builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();

// Dirección del servidor SOAP y nombre del cliente: sección "Cliente" de appsettings.json (RNF-010),
// lo que en el escritorio es Resources/Raw/servidor.json.
OpcionesCliente configuradas = builder.Configuration.GetSection("Cliente").Get<OpcionesCliente>() ?? new OpcionesCliente();

// En MAUI todo es Singleton porque hay un solo empleado. En la web cada circuito (pestaña del navegador)
// tiene su propio ámbito: la sesión de un empleado y el equipo que envía no se comparten con otro.
builder.Services.AddSingleton(new HttpClient { Timeout = TimeSpan.FromSeconds(15) });
builder.Services.AddScoped(_ => new OpcionesCliente
{
    UrlServidor = configuradas.UrlServidor,
    NombreCliente = configuradas.NombreCliente,
    NombreEquipo = Environment.MachineName
});
builder.Services.AddScoped<ClienteSoap>();
builder.Services.AddScoped<AutenticacionSoapService>();
builder.Services.AddScoped<CuentaSoapService>();
builder.Services.AddScoped<TransaccionSoapService>();
builder.Services.AddScoped<EstadoSesion>();

WebApplication app = builder.Build();

app.UseAntiforgery();
app.MapStaticAssets();
// Las pantallas (/, /menu, /cuenta, /transaccion) están en EurekaBank.Shared: se registran sus rutas
// para que recargar o abrir cualquiera de ellas responda la aplicación y no un 404.
app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode()
    .AddAdditionalAssemblies(typeof(Rutas).Assembly);

app.Run();

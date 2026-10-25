using SistemaConversorREST.UI.Shared.Adaptadores;
using SistemaConversorREST.UI.Shared.Interfaces;
using SistemaConversorREST.UI.Shared.Modelos;
using SistemaConversorREST.UI.Web.Components;

var builder = WebApplication.CreateBuilder(args);

// Las vistas compartidas corren en el servidor web: las llamadas REST salen de aquí, no del navegador
builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();

// Dirección del servidor REST y nombre del cliente (sección "Cliente" de appsettings.json)
var opciones = builder.Configuration.GetSection("Cliente").Get<OpcionesCliente>() ?? new OpcionesCliente();
builder.Services.AddSingleton(opciones);

void ConfigurarCliente(HttpClient http)
{
    http.BaseAddress = new Uri(opciones.UrlServidor);
    http.Timeout = TimeSpan.FromSeconds(20);
}

builder.Services.AddHttpClient<IAutenticacionRest, AdaptadorAutenticacionRest>(ConfigurarCliente);
builder.Services.AddHttpClient<IConversionRest, AdaptadorConversionRest>(ConfigurarCliente);

var app = builder.Build();

app.UseAntiforgery();
app.MapStaticAssets();
app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode();

app.Run();

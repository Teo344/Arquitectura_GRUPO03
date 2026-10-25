using SistemaConversorUI.Shared.Adaptadores;
using SistemaConversorUI.Shared.Interfaces;
using SistemaConversorUI.Shared.Modelos;
using SistemaConversorUI.Web.Components;

var builder = WebApplication.CreateBuilder(args);

// Las vistas compartidas corren en el servidor web: las llamadas SOAP salen de aquí, no del navegador
builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();

// Dirección del servidor SOAP y nombre del cliente (sección "Cliente" de appsettings.json)
var opciones = builder.Configuration.GetSection("Cliente").Get<OpcionesCliente>() ?? new OpcionesCliente();
builder.Services.AddSingleton(opciones);

builder.Services.AddHttpClient<IAutenticacionSoap, AdaptadorAutenticacionSoap>();
builder.Services.AddHttpClient<IConversionSoap, AdaptadorConversionSoap>();

var app = builder.Build();

app.UseAntiforgery();
app.MapStaticAssets();
app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode();

app.Run();

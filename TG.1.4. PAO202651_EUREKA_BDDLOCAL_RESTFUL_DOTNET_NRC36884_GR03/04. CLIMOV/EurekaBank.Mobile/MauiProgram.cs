using System.Text.Json;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;
using Microsoft.Extensions.Logging;

namespace ec.edu.monster;

/// <summary>Registra los servicios de EurekaBank.Shared y la vista Blazor de la aplicación móvil.</summary>
public static class MauiProgram
{
    private const string ArchivoConfiguracion = "servidor.json";

    public static MauiApp CreateMauiApp()
    {
        MauiAppBuilder builder = MauiApp.CreateBuilder();
        builder.UseMauiApp<App>();

        builder.Services.AddSingleton(LeerOpciones());
        builder.Services.AddSingleton(new HttpClient { Timeout = TimeSpan.FromSeconds(15) });
        builder.Services.AddSingleton<ClienteRest>();
        builder.Services.AddSingleton<AutenticacionRestService>();
        builder.Services.AddSingleton<CuentaRestService>();
        builder.Services.AddSingleton<TransaccionRestService>();
        builder.Services.AddSingleton<EstadoSesion>();

        builder.Services.AddMauiBlazorWebView();
#if DEBUG
        builder.Services.AddBlazorWebViewDeveloperTools();
        builder.Logging.AddDebug();
#endif
        return builder.Build();
    }

    /// <summary>
    /// La URL del servidor se lee de Resources/Raw/servidor.json (RNF-010); si falta el archivo se
    /// usa http://localhost:8097/. En el emulador, 10.0.2.2 es el localhost del PC donde corre
    /// Kestrel (servidor.json ya apunta a http://10.0.2.2:8097/).
    /// </summary>
    private static OpcionesCliente LeerOpciones()
    {
        OpcionesCliente opciones = new()
        {
            // Nombre del dispositivo que el servidor guarda en logsession, como el móvil Java
            NombreEquipo = DeviceInfo.Current.Manufacturer + " " + DeviceInfo.Current.Model,
            NombreCliente = "Cliente móvil"
        };
        try
        {
            using Stream entrada = FileSystem.OpenAppPackageFileAsync(ArchivoConfiguracion).GetAwaiter().GetResult();
            using JsonDocument documento = JsonDocument.Parse(entrada);
            if (documento.RootElement.TryGetProperty("urlServidor", out JsonElement url)
                && !string.IsNullOrWhiteSpace(url.GetString()))
            {
                opciones.UrlServidor = url.GetString()!;
            }
        }
        catch (Exception ex) when (ex is FileNotFoundException || ex is JsonException)
        {
            // Se mantiene la URL por defecto
        }
        return opciones;
    }
}

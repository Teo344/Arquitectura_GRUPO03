using ec.edu.monster.config;
using ec.edu.monster.controlador;
using ec.edu.monster.modelo.api;
using ec.edu.monster.modelo.dao;
using ec.edu.monster.servicio;
using Microsoft.Extensions.Logging;

namespace ec.edu.monster;

/// <summary>
/// Raíz de composición: es el único lugar que conoce las implementaciones concretas (SQL Server,
/// REST Countries) y las registra en el contenedor de dependencias. El resto del código depende de
/// interfaces y recibe sus dependencias por constructor.
/// </summary>
public static class MauiProgram
{
    public static MauiApp CreateMauiApp()
    {
        MauiAppBuilder builder = MauiApp.CreateBuilder();
        builder.UseMauiApp<App>();

        Configuracion configuracion = LeerConfiguracion();
        builder.Services.AddSingleton(configuracion);
        builder.Services.AddSingleton(new HttpClient { Timeout = configuracion.ApiTimeout });

        // Modelo
        builder.Services.AddSingleton(proveedor =>
            new ConexionBD(configuracion.CadenaConexion, proveedor.GetService<ILogger<ConexionBD>>()));
        builder.Services.AddSingleton<IPaisDAO, PaisDAOSqlServer>();
        builder.Services.AddSingleton<RestCountriesMapper>();
        builder.Services.AddSingleton<IPaisApiCliente, RestCountriesCliente>();
        // Servicio
        builder.Services.AddSingleton<PaisServicio>();
        // Controladores
        builder.Services.AddSingleton<InicioControlador>();
        builder.Services.AddSingleton<PaisControlador>();
        builder.Services.AddSingleton<ReporteControlador>();

        builder.Services.AddMauiBlazorWebView();
#if DEBUG
        builder.Services.AddBlazorWebViewDeveloperTools();
        builder.Logging.AddDebug();
#endif
        return builder.Build();
    }

    /// <summary>
    /// Lee Resources/Raw/configuracion.json (cadena de conexión y API key) en lugar de tenerlos
    /// escritos en el código, como hacía la clase Conexion del enunciado.
    /// </summary>
    private static Configuracion LeerConfiguracion()
    {
        try
        {
            using Stream entrada = FileSystem.OpenAppPackageFileAsync(Configuracion.Archivo).GetAwaiter().GetResult();
            return Configuracion.Leer(entrada);
        }
        catch (FileNotFoundException e)
        {
            throw new InvalidOperationException(
                $"No se encontró {Configuracion.Archivo} en Resources/Raw del proyecto ApiPaises.Desktop.", e);
        }
    }
}

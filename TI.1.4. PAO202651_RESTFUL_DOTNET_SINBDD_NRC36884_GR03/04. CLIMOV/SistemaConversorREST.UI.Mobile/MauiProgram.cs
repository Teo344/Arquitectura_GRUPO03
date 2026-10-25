using Microsoft.Extensions.Logging;
using SistemaConversorREST.UI.Shared.Adaptadores;
using SistemaConversorREST.UI.Shared.Interfaces;
using SistemaConversorREST.UI.Shared.Modelos;

namespace SistemaConversorREST.UI.Mobile
{
    public static class MauiProgram
    {
        public static MauiApp CreateMauiApp()
        {
            var builder = MauiApp.CreateBuilder();

            // En el emulador Android, 10.0.2.2 apunta al localhost de la computadora anfitriona.
            const string apiBaseAddress = "http://10.0.2.2:8096/";

            var opciones = new OpcionesCliente
            {
                UrlServidor = apiBaseAddress,
                NombreCliente = "Cliente móvil"
            };
            builder.Services.AddSingleton(opciones);
            builder.Services.AddSingleton(new HttpClient
            {
                BaseAddress = new Uri(opciones.UrlServidor),
                Timeout = TimeSpan.FromSeconds(20)
            });
            builder.Services.AddSingleton<IAutenticacionRest, AdaptadorAutenticacionRest>();
            builder.Services.AddSingleton<IConversionRest, AdaptadorConversionRest>();
            builder
                .UseMauiApp<App>()
                .ConfigureFonts(fonts =>
                {
                    fonts.AddFont("OpenSans-Regular.ttf", "OpenSansRegular");
                });

            builder.Services.AddMauiBlazorWebView();

#if DEBUG
            builder.Services.AddBlazorWebViewDeveloperTools();
            builder.Logging.AddDebug();
#endif

            return builder.Build();
        }
    }
}

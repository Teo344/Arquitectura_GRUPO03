using Microsoft.Extensions.Logging;
using SistemaConversorUI.Shared.Adaptadores;
using SistemaConversorUI.Shared.Interfaces;
using SistemaConversorUI.Shared.Modelos;

namespace SistemaConversorUI.Mobile
{
    public static class MauiProgram
    {
        public static MauiApp CreateMauiApp()
        {
            var builder = MauiApp.CreateBuilder();
            builder.Services.AddSingleton(new OpcionesCliente
            {
                // Con "adb reverse tcp:8095 tcp:8095" el localhost del emulador llega al IIS Express del PC
                UrlServidor = "http://localhost:8095/",
                NombreCliente = "Cliente móvil"
            });
            builder.Services.AddSingleton<HttpClient>();
            builder.Services.AddSingleton<IAutenticacionSoap, AdaptadorAutenticacionSoap>();
            builder.Services.AddSingleton<IConversionSoap, AdaptadorConversionSoap>();
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

using Microsoft.Extensions.Logging;
using SistemaConversorUI.Shared.Adaptadores;
using SistemaConversorUI.Shared.Interfaces;
using SistemaConversorUI.Shared.Modelos;

namespace SistemaConversorUI.Desktop
{
    public static class MauiProgram
    {
        public static MauiApp CreateMauiApp()
        {
            var builder = MauiApp.CreateBuilder();
            builder.Services.AddSingleton(new OpcionesCliente
            {
                UrlServidor = "http://localhost:8095/",
                NombreCliente = "Cliente de escritorio"
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

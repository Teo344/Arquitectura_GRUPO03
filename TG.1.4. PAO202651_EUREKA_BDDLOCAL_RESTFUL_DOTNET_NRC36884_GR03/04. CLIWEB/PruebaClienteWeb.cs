using System;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;

/// <summary>
/// Prueba del cliente web publicado: la aplicación responde en todas las rutas de las pantallas
/// (también al recargar), y sirve el diseño compartido (CSS, fuentes, foto) y el script de Blazor.
/// Las acciones de las pantallas viajan por la conexión interactiva de Blazor (SignalR), así que se
/// prueban en el navegador. Con EurekaBank.Web corriendo, se ejecuta con: dotnet run PruebaClienteWeb.cs
/// </summary>
public class PruebaClienteWeb
{
    private const string Url = "http://localhost:8099/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaClienteWeb ===");
        (HttpStatusCode estado, string html) = Obtener("").Result;
        Comprobar("/ responde 200", estado == HttpStatusCode.OK);
        Comprobar("la página enlaza el diseño de EurekaBank.Shared", html.Contains("_content/EurekaBank.Shared/css/eureka.css"));
        Comprobar("la página carga Blazor (blazor.web.js)", html.Contains("_framework/blazor.web.js"));
        Comprobar("la raíz es interactiva en el servidor", html.Contains("<!--Blazor:"));

        foreach (string ruta in new[] { "menu", "cuenta", "cuenta/movimientos", "transaccion/deposito" })
        {
            Comprobar("/" + ruta + " responde 200 (recargar no da 404)", Obtener(ruta).Result.Estado == HttpStatusCode.OK);
        }

        (HttpStatusCode estadoCss, string css) = Obtener("_content/EurekaBank.Shared/css/eureka.css").Result;
        Comprobar("eureka.css responde 200 con la fuente Plus Jakarta Sans",
            estadoCss == HttpStatusCode.OK && css.Contains("Plus Jakarta Sans"));
        Comprobar("la fuente se descarga", Obtener("_content/EurekaBank.Shared/fuentes/plus_jakarta_sans_bold.ttf").Result.Estado == HttpStatusCode.OK);
        Comprobar("la foto del login se descarga", Obtener("_content/EurekaBank.Shared/imagenes/fotoLogin.jpg").Result.Estado == HttpStatusCode.OK);
        Comprobar("blazor.web.js se descarga", Obtener("_framework/blazor.web.js").Result.Estado == HttpStatusCode.OK);

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static async Task<(HttpStatusCode Estado, string Contenido)> Obtener(string ruta)
    {
        using HttpResponseMessage respuesta = await cliente.GetAsync(Url + ruta);
        return (respuesta.StatusCode, await respuesta.Content.ReadAsStringAsync());
    }

    private static void Comprobar(string descripcion, bool correcto)
    {
        if (!correcto)
        {
            fallidas++;
        }
        Console.WriteLine((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}

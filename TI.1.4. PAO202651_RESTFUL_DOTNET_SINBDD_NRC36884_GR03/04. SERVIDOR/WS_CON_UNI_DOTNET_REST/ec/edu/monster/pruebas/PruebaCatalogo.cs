using System;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>Prueba de la consulta del catálogo y de las unidades en el servidor REST. Se ejecuta con: dotnet run PruebaCatalogo.cs</summary>
public class PruebaCatalogo
{
    private const string Url = "http://localhost:8096/api/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaCatalogo ===");
        string login = Enviar("POST", "autenticacion/login", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.IndexOf("\"token\":\"") + 9;
        string token = login.Substring(inicio, login.IndexOf("\"", inicio) - inicio);

        string catalogo = Enviar("GET", "catalogo/categorias", token, null);
        Comprobar("el catálogo tiene 6 categorías", catalogo.Split("\"convertible\"").Length - 1 == 6);

        string masa = Enviar("GET", "catalogo/categorias/MASA/unidades", token, null);
        Comprobar("MASA tiene 5 unidades", masa.Split("\"simbolo\"").Length - 1 == 5);
        Comprobar("MASA incluye el quintal (QQ)", masa.Contains("\"codigo\":\"QQ\""));

        string inexistente = Enviar("GET", "catalogo/categorias/xyz/unidades", token, null);
        Comprobar("categoría 'xyz' da DATA-001", inexistente.Contains("DATA-001"));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Enviar(string metodo, string ruta, string token, string json)
    {
        HttpRequestMessage solicitud = new HttpRequestMessage(new HttpMethod(metodo), Url + ruta);
        if (token != null)
        {
            solicitud.Headers.Add("Authorization", "Bearer " + token);
        }
        if (json != null)
        {
            solicitud.Content = new StringContent(json, Encoding.UTF8, "application/json");
        }
        HttpResponseMessage respuesta = cliente.Send(solicitud);
        return new StreamReader(respuesta.Content.ReadAsStream()).ReadToEnd();
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

using System;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>Prueba de la consulta del catálogo y de las unidades en el servidor SOAP (WCF). Se ejecuta con: dotnet run PruebaCatalogo.cs</summary>
public class PruebaCatalogo
{
    private const string Url = "http://localhost:8095/";
    private const string Ns = "http://wservicios.controladores.monster.edu.ec/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaCatalogo ===");
        string login = Enviar("AutenticacionWS", "login", null, "<usuario>MONSTER</usuario><clave>MONSTER9</clave>");
        int inicio = login.IndexOf("<token>") + 7;
        string token = login.Substring(inicio, login.IndexOf("</token>") - inicio);

        string catalogo = Enviar("ConversionUnidadesWS", "consultarCatalogo", token, "");
        Comprobar("el catálogo tiene 6 categorías", catalogo.Split("<categoria>").Length - 1 == 6);

        string masa = Enviar("ConversionUnidadesWS", "consultarUnidades", token, "<categoria>MASA</categoria>");
        Comprobar("MASA tiene 5 unidades", masa.Split("<unidad>").Length - 1 == 5);
        Comprobar("MASA incluye el quintal (QQ)", masa.Contains("<codigo>QQ</codigo>"));

        string inexistente = Enviar("ConversionUnidadesWS", "consultarUnidades", token, "<categoria>xyz</categoria>");
        Comprobar("categoría 'xyz' da DATA-001", inexistente.Contains("DATA-001"));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Enviar(string servicio, string operacion, string token, string parametros)
    {
        string sobre = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Header>"
            + (token == null ? "" : "<token xmlns=\"" + Ns + "\">" + token + "</token>")
            + "</s:Header><s:Body><" + operacion + " xmlns=\"" + Ns + "\">" + parametros + "</" + operacion + ">"
            + "</s:Body></s:Envelope>";
        HttpRequestMessage solicitud = new HttpRequestMessage(HttpMethod.Post, Url + servicio + ".svc");
        solicitud.Headers.Add("SOAPAction", "\"" + Ns + servicio + "/" + operacion + "\"");
        solicitud.Content = new StringContent(sobre, Encoding.UTF8, "text/xml");
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

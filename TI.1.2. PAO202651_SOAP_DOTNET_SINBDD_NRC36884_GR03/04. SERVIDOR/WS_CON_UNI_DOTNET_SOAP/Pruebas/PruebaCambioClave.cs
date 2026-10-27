using System;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>Prueba del cambio de clave en el servidor SOAP (WCF); al final vuelve a dejar MONSTER9. Se ejecuta con: dotnet run PruebaCambioClave.cs</summary>
public class PruebaCambioClave
{
    private const string Url = "http://localhost:8095/AutenticacionWS.svc";
    private const string Ns = "http://wservicios.controladores.monster.edu.ec/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaCambioClave ===");

        Comprobar("clave actual incorrecta da AUTH-003", CambiarClave("clave-mala", "NUEVA123").Contains("AUTH-003"));
        Comprobar("clave nueva vacía da DATA-001", CambiarClave("MONSTER9", "").Contains("DATA-001"));
        Comprobar("cambio de clave correcto", CambiarClave("MONSTER9", "NUEVA123").Contains("<estado>OK</estado>"));
        Comprobar("la clave anterior ya no sirve", Login("MONSTER9").Contains("AUTH-003"));
        Comprobar("la clave nueva inicia sesión", Login("NUEVA123").Contains("<token>"));
        Comprobar("se restaura la clave MONSTER9", CambiarClave("NUEVA123", "MONSTER9").Contains("<estado>OK</estado>"));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string CambiarClave(string claveActual, string claveNueva)
    {
        return Enviar("cambiarClave", "<usuario>MONSTER</usuario><claveActual>" + claveActual + "</claveActual>"
            + "<claveNueva>" + claveNueva + "</claveNueva>");
    }

    private static string Login(string clave)
    {
        return Enviar("login", "<usuario>MONSTER</usuario><clave>" + clave + "</clave>");
    }

    private static string Enviar(string operacion, string parametros)
    {
        string sobre = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body>"
            + "<" + operacion + " xmlns=\"" + Ns + "\">" + parametros + "</" + operacion + ">"
            + "</s:Body></s:Envelope>";
        HttpRequestMessage solicitud = new HttpRequestMessage(HttpMethod.Post, Url);
        solicitud.Headers.Add("SOAPAction", "\"" + Ns + "AutenticacionWS/" + operacion + "\"");
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

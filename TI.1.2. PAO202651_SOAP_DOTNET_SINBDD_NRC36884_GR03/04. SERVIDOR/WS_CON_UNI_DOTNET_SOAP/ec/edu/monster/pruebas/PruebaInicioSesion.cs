using System;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>Prueba del inicio de sesión contra el servidor SOAP (WCF) en ejecución. Se ejecuta con: dotnet run PruebaInicioSesion.cs</summary>
public class PruebaInicioSesion
{
    private const string Url = "http://localhost:8095/AutenticacionWS.svc";
    private const string Ns = "http://wservicios.controladores.monster.edu.ec/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaInicioSesion ===");

        Comprobar("credenciales correctas devuelven token", Login("MONSTER", "MONSTER9").Contains("<token>"));
        Comprobar("clave incorrecta da AUTH-003", Login("MONSTER", "clave-mala").Contains("AUTH-003"));
        Comprobar("usuario incorrecto da AUTH-003", Login("OTRO", "MONSTER9").Contains("AUTH-003"));
        Comprobar("credenciales vacías dan AUTH-003", Login("", "").Contains("AUTH-003"));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Login(string usuario, string clave)
    {
        return Enviar("login", "<usuario>" + usuario + "</usuario><clave>" + clave + "</clave>");
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

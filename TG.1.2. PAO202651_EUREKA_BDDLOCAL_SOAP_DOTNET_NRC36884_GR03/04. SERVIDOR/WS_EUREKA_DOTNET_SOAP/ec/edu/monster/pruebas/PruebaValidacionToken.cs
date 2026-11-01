using System;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>
/// Prueba de que las operaciones protegidas exigen un token válido en la cabecera SOAP y de que
/// cerrarSesion lo invalida (RQF-002, RN-06). Se ejecuta con: dotnet run PruebaValidacionToken.cs
/// </summary>
public class PruebaValidacionToken
{
    private const string Url = "http://localhost:8096/";
    private const string Ns = "http://wservicios.controladores.monster.edu.ec/";
    private const string Consulta = "<cuenta>00100001</cuenta>";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaValidacionToken ===");
        string login = Enviar("AutenticacionWS", "iniciarSesion", null, "<usuario>MONSTER</usuario><clave>MONSTER9</clave>");
        int inicio = login.IndexOf("<token>") + 7;
        string token = login.Substring(inicio, login.IndexOf("</token>") - inicio);

        Comprobar("sin token da AUTH-003", Enviar("CuentaWS", "consultarCuenta", null, Consulta).Contains("AUTH-003"));
        Comprobar("token inventado da AUTH-003",
            Enviar("CuentaWS", "consultarCuenta", "token-inventado", Consulta).Contains("AUTH-003"));
        Comprobar("sin token no se puede depositar", Enviar("TransaccionWS", "depositar", null,
            "<cuenta>00100001</cuenta><importe>1.00</importe>").Contains("AUTH-003"));
        Comprobar("token válido permite consultar",
            Enviar("CuentaWS", "consultarCuenta", token, Consulta).Contains("<codigo>OK-000</codigo>"));
        Comprobar("cerrarSesion da OK-000",
            Enviar("AutenticacionWS", "cerrarSesion", token, "").Contains("<codigo>OK-000</codigo>"));
        Comprobar("después del cierre el token da AUTH-003",
            Enviar("CuentaWS", "consultarCuenta", token, Consulta).Contains("AUTH-003"));
        Comprobar("cerrar de nuevo la sesión da AUTH-003",
            Enviar("AutenticacionWS", "cerrarSesion", token, "").Contains("AUTH-003"));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Enviar(string servicio, string operacion, string? token, string parametros)
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

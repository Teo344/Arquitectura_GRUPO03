using System;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>Prueba del inicio de sesión (RQF-001) contra el servidor SOAP (WCF) en ejecución. Se ejecuta con: dotnet run PruebaInicioSesion.cs</summary>
public class PruebaInicioSesion
{
    private const string Url = "http://localhost:8096/";
    private const string Ns = "http://wservicios.controladores.monster.edu.ec/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaInicioSesion ===");
        string correcto = Login("MONSTER", "MONSTER9");
        Comprobar("MONSTER/MONSTER9 da OK-000", correcto.Contains("<codigo>OK-000</codigo>"));
        Comprobar("la respuesta trae el token", correcto.Contains("<token>"));
        Comprobar("la respuesta trae el empleado 0015", correcto.Contains("<codigoEmpleado>0015</codigoEmpleado>"));
        Comprobar("la respuesta trae la sucursal Sipan", correcto.Contains("<sucursal>Sipan</sucursal>"));
        Comprobar("clave incorrecta da AUTH-001", Login("MONSTER", "otra").Contains("AUTH-001"));
        Comprobar("usuario inexistente da AUTH-001", Login("nadie", "MONSTER9").Contains("AUTH-001"));
        Comprobar("usuario ANULADO (creyes) da AUTH-002", Login("creyes", "linda").Contains("AUTH-002"));
        Comprobar("usuario sin sucursal (internet) da AUTH-002", Login("internet", "internet").Contains("AUTH-002"));
        Comprobar("usuario vacío da DATA-001", Login("", "MONSTER9").Contains("DATA-001"));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Login(string usuario, string clave)
    {
        return Enviar("AutenticacionWS", "iniciarSesion", null, "<usuario>" + usuario + "</usuario><clave>" + clave
            + "</clave><equipo>PruebaInicioSesion</equipo>");
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

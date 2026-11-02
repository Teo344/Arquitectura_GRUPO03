using System;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>
/// Prueba de que los recursos protegidos exigen un token válido en la cabecera Authorization y de
/// que cerrar-sesion lo invalida (RQF-002, RN-06). Se ejecuta con: dotnet run PruebaValidacionToken.cs
/// </summary>
public class PruebaValidacionToken
{
    private const string Url = "http://localhost:8097/api/";
    private const string Consulta = "cuentas/00100001";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;
    private static int estadoHttp;

    public static void Main()
    {
        Console.WriteLine("=== PruebaValidacionToken ===");
        string login = Enviar("POST", "autenticacion/login", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.IndexOf("\"token\":\"") + 9;
        string token = login.Substring(inicio, login.IndexOf("\"", inicio) - inicio);

        Comprobar("sin token da 401 y AUTH-003", Enviar("GET", Consulta, null, null).Contains("AUTH-003") && estadoHttp == 401);
        Comprobar("token inventado da AUTH-003", Enviar("GET", Consulta, "token-inventado", null).Contains("AUTH-003"));
        Comprobar("sin token no se puede depositar", Enviar("POST", "transacciones/depositos", null,
            "{\"cuenta\":\"00100001\",\"importe\":1.00}").Contains("AUTH-003"));
        Comprobar("token válido permite consultar",
            Enviar("GET", Consulta, token, null).Contains("\"codigo\":\"OK-000\"") && estadoHttp == 200);
        Comprobar("cerrar-sesion da OK-000", Enviar("POST", "autenticacion/cerrar-sesion", token, null).Contains("\"codigo\":\"OK-000\""));
        Comprobar("después del cierre el token da AUTH-003", Enviar("GET", Consulta, token, null).Contains("AUTH-003"));
        Comprobar("cerrar de nuevo la sesión da 401 y AUTH-003",
            Enviar("POST", "autenticacion/cerrar-sesion", token, null).Contains("AUTH-003") && estadoHttp == 401);

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Enviar(string metodo, string ruta, string? token, string? json)
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
        estadoHttp = (int)respuesta.StatusCode;
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

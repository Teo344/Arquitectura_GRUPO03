using System;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>Prueba del inicio de sesión (RQF-001) contra el servidor REST en ejecución. Se ejecuta con: dotnet run PruebaInicioSesion.cs</summary>
public class PruebaInicioSesion
{
    private const string Url = "http://localhost:8097/api/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;
    private static int estadoHttp;

    public static void Main()
    {
        Console.WriteLine("=== PruebaInicioSesion ===");
        string correcto = Login("MONSTER", "MONSTER9");
        Comprobar("MONSTER/MONSTER9 da 200 y OK-000", estadoHttp == 200 && correcto.Contains("\"codigo\":\"OK-000\""));
        Comprobar("la respuesta trae el token", correcto.Contains("\"token\":\""));
        Comprobar("la respuesta trae el empleado 0015", correcto.Contains("\"codigoEmpleado\":\"0015\""));
        Comprobar("la respuesta trae la sucursal Sipan", correcto.Contains("\"sucursal\":\"Sipan\""));
        Comprobar("clave incorrecta da 401 y AUTH-001", Login("MONSTER", "otra").Contains("AUTH-001") && estadoHttp == 401);
        Comprobar("usuario inexistente da AUTH-001", Login("nadie", "MONSTER9").Contains("AUTH-001"));
        Comprobar("usuario ANULADO (creyes) da 401 y AUTH-002", Login("creyes", "linda").Contains("AUTH-002") && estadoHttp == 401);
        Comprobar("usuario sin sucursal (internet) da AUTH-002", Login("internet", "internet").Contains("AUTH-002"));
        Comprobar("usuario vacío da 400 y DATA-001", Login("", "MONSTER9").Contains("DATA-001") && estadoHttp == 400);
        Comprobar("JSON mal formado da 400 y DATA-001",
            Enviar("POST", "autenticacion/login", null, "{malo").Contains("DATA-001") && estadoHttp == 400);
        Comprobar("los errores llevan estado ERROR", Login("MONSTER", "otra").Contains("\"estado\":\"ERROR\""));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Login(string usuario, string clave)
    {
        return Enviar("POST", "autenticacion/login", null, "{\"usuario\":\"" + usuario + "\",\"clave\":\"" + clave
            + "\",\"equipo\":\"PruebaInicioSesion\"}");
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

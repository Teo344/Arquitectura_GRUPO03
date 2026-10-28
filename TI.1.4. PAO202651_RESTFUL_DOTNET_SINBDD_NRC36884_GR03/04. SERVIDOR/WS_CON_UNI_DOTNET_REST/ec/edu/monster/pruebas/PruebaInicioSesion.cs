using System;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>Prueba del inicio de sesión contra el servidor REST en ejecución. Se ejecuta con: dotnet run PruebaInicioSesion.cs</summary>
public class PruebaInicioSesion
{
    private const string Url = "http://localhost:8096/api/autenticacion/login";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaInicioSesion ===");

        Comprobar("credenciales correctas devuelven token", Login("MONSTER", "MONSTER9").Contains("\"token\""));
        Comprobar("clave incorrecta da AUTH-003", Login("MONSTER", "clave-mala").Contains("AUTH-003"));
        Comprobar("usuario incorrecto da AUTH-003", Login("OTRO", "MONSTER9").Contains("AUTH-003"));
        Comprobar("credenciales vacías dan AUTH-003", Login("", "").Contains("AUTH-003"));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Login(string usuario, string clave)
    {
        return Enviar("{\"usuario\":\"" + usuario + "\",\"clave\":\"" + clave + "\"}");
    }

    private static string Enviar(string json)
    {
        HttpRequestMessage solicitud = new HttpRequestMessage(HttpMethod.Post, Url);
        solicitud.Content = new StringContent(json, Encoding.UTF8, "application/json");
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

#:project ../../../../WS_CON_UNI_DOTNET_REST.csproj

using System;
using ec.edu.monster.modelo;
using ec.edu.monster.servicios;
using ec.edu.monster.vista;

/// <summary>Prueba del formato de la respuesta que devuelve el servidor. Se ejecuta con: dotnet run PruebaRespuestaMapeador.cs</summary>
public class PruebaRespuestaMapeador
{
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaRespuestaMapeador ===");

        RespuestaDto respuesta = RespuestaMapeador.DeConversion(
            new ResultadoConversion { Resultado = 1000, Origen = Unidad.KM, Destino = Unidad.M });
        Comprobar("estado OK", respuesta.Estado == "OK");
        Comprobar("código 200", respuesta.Codigo == "200");
        Comprobar("resultado 1000", respuesta.Resultado == 1000);
        Comprobar("unidades KM -> M", respuesta.UnidadOrigen == "KM" && respuesta.UnidadDestino == "M");
        Comprobar("sin token en una conversión", respuesta.Token == null);

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
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

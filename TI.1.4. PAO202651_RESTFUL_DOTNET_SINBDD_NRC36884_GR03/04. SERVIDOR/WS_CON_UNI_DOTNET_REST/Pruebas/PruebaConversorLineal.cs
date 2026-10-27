#:project ../WS_CON_UNI_DOTNET_REST.csproj

using System;
using System.Globalization;
using WS_CON_UNI_DOTNET_REST.Modelo;
using WS_CON_UNI_DOTNET_REST.Servicios.Conversores;

/// <summary>Prueba de la conversión de longitud y masa por factor. Se ejecuta con: dotnet run PruebaConversorLineal.cs</summary>
public class PruebaConversorLineal
{
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaConversorLineal ===");
        ConversorLineal conversor = new ConversorLineal();

        Comprobar("1 KM -> M = 1000", conversor.Convertir(1, Unidad.KM, Unidad.M), 1000);
        Comprobar("1 MI -> M = 1609.344", conversor.Convertir(1, Unidad.MI, Unidad.M), 1609.344);
        Comprobar("1 QQ -> LB = 100", conversor.Convertir(1, Unidad.QQ, Unidad.LB), 100);
        Comprobar("1000 G -> KG = 1", conversor.Convertir(1000, Unidad.G, Unidad.KG), 1);

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static void Comprobar(string descripcion, double obtenido, double esperado)
    {
        bool correcto = Math.Abs(obtenido - esperado) < 0.000001;
        if (!correcto)
        {
            fallidas++;
        }
        Console.WriteLine((correcto ? "[OK]    " : "[FALLA] ") + descripcion + " -> " + obtenido.ToString(CultureInfo.InvariantCulture));
    }
}

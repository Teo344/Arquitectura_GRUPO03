#:project ../../../../WS_CON_UNI_DOTNET_REST.csproj

using System;
using System.Globalization;
using ec.edu.monster.modelo;
using ec.edu.monster.servicios.conversores;

/// <summary>Prueba de la conversión de temperatura con fórmulas. Se ejecuta con: dotnet run PruebaConversorTemperatura.cs</summary>
public class PruebaConversorTemperatura
{
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaConversorTemperatura ===");
        ConversorTemperatura conversor = new ConversorTemperatura();

        Comprobar("0 C -> K = 273.15", conversor.Convertir(0, Unidad.C, Unidad.K), 273.15);
        Comprobar("100 C -> F = 212", conversor.Convertir(100, Unidad.C, Unidad.F), 212);
        Comprobar("-40 C -> F = -40", conversor.Convertir(-40, Unidad.C, Unidad.F), -40);
        Comprobar("212 F -> RE = 80", conversor.Convertir(212, Unidad.F, Unidad.RE), 80);

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

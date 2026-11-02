#:project ../../../../WS_EUREKA_DOTNET_REST.csproj

using System;
using ec.edu.monster.servicios;

/// <summary>Prueba de la validación de cuentas (DATA-001) e importes (MOV-001). Se ejecuta con: dotnet run PruebaValidacion.cs</summary>
public class PruebaValidacion
{
    private static int fallidas = 0;
    private static readonly ServicioValidacion validacion = new ServicioValidacion();

    public static void Main()
    {
        Console.WriteLine("=== PruebaValidacion ===");
        try
        {
            Comprobar("' 00100001 ' es una cuenta válida y se recorta", validacion.ValidarCuenta(" 00100001 ") == "00100001");
        }
        catch (ExcepcionServicio)
        {
            Comprobar("' 00100001 ' es una cuenta válida y se recorta", false);
        }
        CuentaInvalida("1234");
        CuentaInvalida("0010000A");
        CuentaInvalida(null);

        ImporteValido(10.50m);
        ImporteValido(10.500m);
        ImporteInvalido(0m);
        ImporteInvalido(-1m);
        ImporteInvalido(1.234m);
        ImporteInvalido(null);
        ImporteInvalido(10000000000.00m);

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static void CuentaInvalida(string? cuenta)
    {
        try
        {
            validacion.ValidarCuenta(cuenta);
            Comprobar("cuenta '" + cuenta + "' da DATA-001", false);
        }
        catch (ExcepcionServicio e)
        {
            Comprobar("cuenta '" + cuenta + "' da DATA-001", e.CodigoError.Codigo == "DATA-001");
        }
    }

    private static void ImporteValido(decimal importe)
    {
        try
        {
            validacion.ValidarImporte(importe);
            Comprobar("importe " + importe + " es válido", true);
        }
        catch (ExcepcionServicio)
        {
            Comprobar("importe " + importe + " es válido", false);
        }
    }

    private static void ImporteInvalido(decimal? importe)
    {
        try
        {
            validacion.ValidarImporte(importe);
            Comprobar("importe " + importe + " da MOV-001", false);
        }
        catch (ExcepcionServicio e)
        {
            Comprobar("importe " + importe + " da MOV-001", e.CodigoError.Codigo == "MOV-001");
        }
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

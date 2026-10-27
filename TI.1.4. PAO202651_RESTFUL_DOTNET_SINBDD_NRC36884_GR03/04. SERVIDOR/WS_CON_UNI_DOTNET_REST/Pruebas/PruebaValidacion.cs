#:project ../WS_CON_UNI_DOTNET_REST.csproj

using System;
using WS_CON_UNI_DOTNET_REST.Modelo;
using WS_CON_UNI_DOTNET_REST.Servicios;

/// <summary>Prueba de la validación de categorías, unidades y valores. Se ejecuta con: dotnet run PruebaValidacion.cs</summary>
public class PruebaValidacion
{
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaValidacion ===");
        ServicioValidacion validacion = new ServicioValidacion();

        try
        {
            Comprobar("'longitud' es una categoría válida", validacion.ValidarCategoria("longitud") == Categoria.Longitud);
        }
        catch (ExcepcionServicio)
        {
            Comprobar("'longitud' es una categoría válida", false);
        }

        try
        {
            validacion.ValidarCategoria("xyz");
            Comprobar("categoría 'xyz' da DATA-001", false);
        }
        catch (ExcepcionServicio e)
        {
            Comprobar("categoría 'xyz' da DATA-001", e.CodigoError.Codigo == "DATA-001");
        }

        try
        {
            validacion.ValidarConversion(Categoria.Longitud, -1, "KM", "M");
            Comprobar("valor negativo en longitud da DATA-001", false);
        }
        catch (ExcepcionServicio e)
        {
            Comprobar("valor negativo en longitud da DATA-001", e.CodigoError.Codigo == "DATA-001");
        }

        try
        {
            validacion.ValidarConversion(Categoria.Longitud, 1, "KG", "M");
            Comprobar("KG en longitud da UNIT-001", false);
        }
        catch (ExcepcionServicio e)
        {
            Comprobar("KG en longitud da UNIT-001", e.CodigoError.Codigo == "UNIT-001");
        }

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

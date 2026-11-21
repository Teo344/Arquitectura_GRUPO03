using System.Globalization;
using ec.edu.monster.modelo.excepcion;
using Microsoft.Extensions.Logging;

namespace ec.edu.monster.vista;

/// <summary>Formato de datos para mostrar (convenciones de Ecuador: punto de miles y coma decimal).</summary>
public static class Formato
{
    private static readonly CultureInfo Ecuador = CultureInfo.GetCultureInfo("es-EC");
    private const string SinDato = "—";

    /// <summary>18001000 → "18.001.000".</summary>
    public static string Entero(long valor) => valor.ToString("N0", Ecuador);

    /// <summary>0.236 → "23,6 %".</summary>
    public static string Porcentaje(double fraccion) => (fraccion * 100).ToString("0.0", Ecuador) + " %";

    /// <summary>Texto o una raya larga si no hay dato (así un vacío no parece un error).</summary>
    public static string Texto(string? valor) => string.IsNullOrWhiteSpace(valor) ? SinDato : valor;

    /// <summary>"1 país" / "250 países".</summary>
    public static string Paises(long cantidad) => Entero(cantidad) + (cantidad == 1 ? " país" : " países");

    /// <summary>Ordena textos con las reglas del español, sin distinguir mayúsculas ni tildes.</summary>
    public static int Comparar(string? a, string? b)
    {
        if (a is null || b is null)
        {
            // Los vacíos al final
            return a is null ? (b is null ? 0 : 1) : -1;
        }
        return Ecuador.CompareInfo.Compare(a, b, CompareOptions.IgnoreCase | CompareOptions.IgnoreNonSpace);
    }
}

/// <summary>Tipo de alerta: cada uno con su color semántico y su icono (nunca solo color).</summary>
public enum TipoAlerta
{
    Info,
    Exito,
    Advertencia,
    Error
}

/// <summary>Mensaje para el usuario con su tipo.</summary>
public sealed record Mensaje(string Texto, TipoAlerta Tipo);

/// <summary>Resultado de una llamada al controlador: el valor o un mensaje listo para mostrar.</summary>
public readonly record struct Resultado<T>(T? Valor, string? Error)
{
    public bool Exito => Error is null;
}

/// <summary>
/// Ejecuta una llamada al controlador y convierte cualquier falla en un mensaje para el usuario:
/// las ExcepcionAplicacion traen su propio mensaje; las inesperadas se registran en el log y se
/// informan con un mensaje genérico, para que la ventana nunca quede en un estado roto.
/// </summary>
public static class Intento
{
    private const string ErrorInesperado =
        "Ocurrió un error inesperado. Intente nuevamente; si persiste, revise el registro de la aplicación.";

    public static async Task<Resultado<T>> EjecutarAsync<T>(Func<Task<T>> trabajo, ILogger log)
    {
        try
        {
            return new Resultado<T>(await trabajo(), null);
        }
        catch (ExcepcionAplicacion e)
        {
            return new Resultado<T>(default, e.Message);
        }
        catch (Exception e)
        {
            log.LogError(e, "Error no controlado en la vista");
            return new Resultado<T>(default, ErrorInesperado);
        }
    }

    public static Task<Resultado<bool>> EjecutarAsync(Func<Task> trabajo, ILogger log)
    {
        return EjecutarAsync(async () =>
        {
            await trabajo();
            return true;
        }, log);
    }
}

/// <summary>Datos de un diálogo de confirmación.</summary>
public sealed record DatosConfirmacion(string Titulo, string Descripcion, IReadOnlyList<(string Etiqueta, string Valor)> Filas,
    string TextoConfirmar, bool Destructiva);

/// <summary>
/// Permite pedir una confirmación con <c>await</c>: la vista muestra el diálogo mientras
/// <see cref="Actual"/> no es null, y el botón elegido completa la tarea con true o false.
/// </summary>
public sealed class Confirmador
{
    private TaskCompletionSource<bool>? pendiente;

    public DatosConfirmacion? Actual { get; private set; }

    public Task<bool> PedirAsync(DatosConfirmacion datos)
    {
        pendiente?.TrySetResult(false);
        Actual = datos;
        pendiente = new TaskCompletionSource<bool>();
        return pendiente.Task;
    }

    public void Responder(bool confirmado)
    {
        Actual = null;
        TaskCompletionSource<bool>? actual = pendiente;
        pendiente = null;
        actual?.TrySetResult(confirmado);
    }
}

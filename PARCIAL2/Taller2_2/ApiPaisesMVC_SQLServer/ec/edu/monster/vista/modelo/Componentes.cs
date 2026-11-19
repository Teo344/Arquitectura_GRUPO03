namespace ec.edu.monster.vista.modelo
{
    /// <summary>Tipo de un mensaje o estado: define color e icono (nunca solo el color).</summary>
    public enum TipoAlerta
    {
        Info,
        Exito,
        Advertencia,
        Error
    }

    public static class TipoAlertaExtensiones
    {
        public static string Clase(this TipoAlerta tipo) => tipo.ToString().ToLowerInvariant();

        public static NombreIcono Icono(this TipoAlerta tipo) => tipo switch
        {
            TipoAlerta.Exito => NombreIcono.Exito,
            TipoAlerta.Advertencia => NombreIcono.Advertencia,
            TipoAlerta.Error => NombreIcono.Error,
            _ => NombreIcono.Info
        };
    }

    /// <summary>Mensaje con icono (componentes/_Alerta.cshtml).</summary>
    /// <param name="Detalles">lista opcional (p. ej. todos los errores del formulario)</param>
    public sealed record Alerta(TipoAlerta Tipo, string Mensaje, IReadOnlyList<string>? Detalles = null);

    /// <summary>Chip de estado con icono (componentes/_ChipEstado.cshtml).</summary>
    public sealed record ChipEstado(TipoAlerta Tipo, string Texto);

    /// <summary>Estado vacío con icono, título, texto y una acción opcional (componentes/_EstadoVacio.cshtml).</summary>
    public sealed record EstadoVacio(NombreIcono Icono, string Titulo, string Texto, string? TextoAccion = null,
        string? UrlAccion = null);

    /// <summary>Diálogo de confirmación (componentes/_Dialogo.cshtml); paises.js lo abre.</summary>
    /// <param name="Filas">pares etiqueta-valor que resumen lo que se va a hacer</param>
    /// <param name="Peligro">la acción es destructiva: botón rojo</param>
    /// <param name="ValoresDinamicos">
    /// el valor de cada fila es una clave: paises.js lo completa con el atributo data-detalle-{clave}
    /// del formulario que abre el diálogo (p. ej. el país de la fila que se elimina)
    /// </param>
    public sealed record Dialogo(string Id, string Titulo, string Texto, IReadOnlyList<KeyValuePair<string, string>> Filas,
        string TextoAceptar, bool Peligro, bool ValoresDinamicos = false);
}

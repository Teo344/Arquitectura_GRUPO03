namespace ec.edu.monster.modelo.excepcion;

/// <summary>
/// Raíz de las fallas que la aplicación conoce. El mensaje ya está redactado para mostrarse al
/// usuario, así la vista puede atrapar este único tipo y presentar <see cref="Exception.Message"/>
/// sin conocer el detalle técnico.
/// </summary>
public abstract class ExcepcionAplicacion : Exception
{
    protected ExcepcionAplicacion(string mensaje, Exception? causa = null) : base(mensaje, causa)
    {
    }
}

/// <summary>
/// La API pública no respondió, rechazó la solicitud o devolvió algo que no se pudo interpretar. El
/// servicio la usa para decidir el respaldo con la base de datos local.
/// </summary>
public sealed class ExcepcionApiExterna(string mensaje, Exception? causa = null) : ExcepcionAplicacion(mensaje, causa);

/// <summary>Falla al conectar o al operar con la base de datos.</summary>
public sealed class ExcepcionPersistencia(string mensaje, Exception? causa = null) : ExcepcionAplicacion(mensaje, causa);

/// <summary>Se pidió un registro que no existe (p. ej. actualizar un id que otro usuario borró).</summary>
public sealed class ExcepcionNoEncontrado(string mensaje) : ExcepcionAplicacion(mensaje);

/// <summary>
/// Los datos no cumplen las reglas del negocio. Reúne todos los problemas encontrados para que el
/// usuario los corrija de una sola vez.
/// </summary>
public sealed class ExcepcionValidacion : ExcepcionAplicacion
{
    public ExcepcionValidacion(string error) : this([error])
    {
    }

    public ExcepcionValidacion(IReadOnlyList<string> errores) : base(string.Join("\n", errores))
    {
        Errores = errores;
    }

    public IReadOnlyList<string> Errores { get; }
}

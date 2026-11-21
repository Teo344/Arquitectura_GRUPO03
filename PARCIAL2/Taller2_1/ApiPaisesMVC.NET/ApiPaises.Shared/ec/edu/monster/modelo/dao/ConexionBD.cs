using ec.edu.monster.modelo.excepcion;
using Microsoft.Data.SqlClient;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;

namespace ec.edu.monster.modelo.dao;

/// <summary>
/// Entrega conexiones abiertas a SQL Server. Cada llamada abre una conexión nueva (el pool de
/// SqlClient la reutiliza por debajo) que el llamador cierra con <c>await using</c>; las
/// transacciones necesitan su propia conexión. La cadena llega de configuracion.json en lugar de
/// estar escrita en el código como en el enunciado.
/// </summary>
public sealed class ConexionBD(string cadenaConexion, ILogger<ConexionBD>? logger = null)
{
    private readonly string cadena = string.IsNullOrWhiteSpace(cadenaConexion)
        ? throw new ArgumentException("Falta la cadena de conexión.", nameof(cadenaConexion))
        : cadenaConexion;

    private readonly ILogger log = (ILogger?)logger ?? NullLogger.Instance;

    /// <exception cref="ExcepcionPersistencia">si SQL Server no está disponible o rechaza el acceso</exception>
    public async Task<SqlConnection> AbrirAsync()
    {
        SqlConnection conexion = new(cadena);
        try
        {
            await conexion.OpenAsync();
            return conexion;
        }
        catch (SqlException e)
        {
            await conexion.DisposeAsync();
            log.LogError(e, "No se pudo conectar a SQL Server");
            throw new ExcepcionPersistencia(
                "No se pudo conectar a la base de datos. Verifique que SQL Server esté en ejecución.", e);
        }
    }
}

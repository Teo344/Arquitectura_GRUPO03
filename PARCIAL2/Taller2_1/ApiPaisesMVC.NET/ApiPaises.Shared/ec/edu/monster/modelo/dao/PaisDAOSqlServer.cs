using System.Data;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using Microsoft.Data.SqlClient;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;

namespace ec.edu.monster.modelo.dao;

/// <summary>
/// Implementación ADO.NET de <see cref="IPaisDAO"/> para SQL Server.
/// <list type="bullet">
/// <item>Solo consultas parametrizadas: los datos nunca se concatenan al SQL.</item>
/// <item><c>await using</c>: conexiones, comandos y lectores siempre se cierran, aun con error
/// (el DAO del enunciado no los cerraba).</item>
/// <item>La importación usa una transacción en una sola conexión, en lugar de abrir una por país.</item>
/// </list>
/// </summary>
public sealed class PaisDAOSqlServer(ConexionBD conexionBD, ILogger<PaisDAOSqlServer>? logger = null) : IPaisDAO
{
    /// <summary>Códigos de error de SQL Server para clave única o primaria duplicada.</summary>
    private static readonly int[] ErroresDuplicado = [2627, 2601];

    private const string Columnas = "id, codigo, nombre, capital, region, poblacion, moneda";

    private const string SqlInsertar =
        "INSERT INTO dbo.pais (codigo, nombre, capital, region, poblacion, moneda) OUTPUT INSERTED.id "
        + "VALUES (@codigo, @nombre, @capital, @region, @poblacion, @moneda)";
    private const string SqlActualizar =
        "UPDATE dbo.pais SET codigo = @codigo, nombre = @nombre, capital = @capital, region = @region, "
        + "poblacion = @poblacion, moneda = @moneda WHERE id = @id";
    private const string SqlActualizarPorCodigo =
        "UPDATE dbo.pais SET nombre = @nombre, capital = @capital, region = @region, poblacion = @poblacion, "
        + "moneda = @moneda WHERE codigo = @codigo";
    private const string SqlEliminar = "DELETE FROM dbo.pais WHERE id = @id";
    private const string SqlPorId = "SELECT " + Columnas + " FROM dbo.pais WHERE id = @id";
    private const string SqlPorCodigo = "SELECT " + Columnas + " FROM dbo.pais WHERE codigo = @codigo";
    private const string SqlTodos = "SELECT " + Columnas + " FROM dbo.pais ORDER BY nombre";
    private const string SqlPorRegion = "SELECT " + Columnas + " FROM dbo.pais WHERE region = @region ORDER BY nombre";
    // La intercalación CI_AI de la base ya ignora mayúsculas y tildes
    private const string SqlPorNombre =
        "SELECT " + Columnas + " FROM dbo.pais WHERE nombre LIKE @patron ESCAPE '\\' ORDER BY nombre";
    private const string SqlCodigos = "SELECT codigo FROM dbo.pais";
    private const string SqlContarPorRegion =
        "SELECT region, COUNT_BIG(*) AS total FROM dbo.pais GROUP BY region ORDER BY total DESC, region";
    private const string SqlMasPoblado =
        "SELECT TOP (1) " + Columnas + " FROM dbo.pais ORDER BY poblacion DESC, nombre";
    private const string SqlContar = "SELECT COUNT_BIG(*) FROM dbo.pais";

    private readonly ConexionBD conexionBD = conexionBD ?? throw new ArgumentNullException(nameof(conexionBD));
    private readonly ILogger log = (ILogger?)logger ?? NullLogger.Instance;

    public async Task<Pais> InsertarAsync(Pais pais)
    {
        try
        {
            await using SqlConnection con = await conexionBD.AbrirAsync();
            await using SqlCommand cmd = new(SqlInsertar, con);
            AsignarDatos(cmd, pais);
            int id = (int)(await cmd.ExecuteScalarAsync())!;
            return pais.ConId(id);
        }
        catch (SqlException e)
        {
            throw Traducir(e, "registrar el país", pais.Codigo);
        }
    }

    public async Task<bool> ActualizarAsync(Pais pais)
    {
        if (pais.Id is not int id)
        {
            throw new ArgumentException("Para actualizar, el país debe tener id.", nameof(pais));
        }
        try
        {
            await using SqlConnection con = await conexionBD.AbrirAsync();
            await using SqlCommand cmd = new(SqlActualizar, con);
            AsignarDatos(cmd, pais);
            cmd.Parameters.Add("@id", SqlDbType.Int).Value = id;
            return await cmd.ExecuteNonQueryAsync() > 0;
        }
        catch (SqlException e)
        {
            throw Traducir(e, "actualizar el país", pais.Codigo);
        }
    }

    public async Task<bool> EliminarAsync(int id)
    {
        try
        {
            await using SqlConnection con = await conexionBD.AbrirAsync();
            await using SqlCommand cmd = new(SqlEliminar, con);
            cmd.Parameters.Add("@id", SqlDbType.Int).Value = id;
            return await cmd.ExecuteNonQueryAsync() > 0;
        }
        catch (SqlException e)
        {
            throw Traducir(e, "eliminar el país", null);
        }
    }

    public async Task<Pais?> BuscarPorIdAsync(int id)
    {
        return (await ConsultarAsync(SqlPorId, cmd => cmd.Parameters.Add("@id", SqlDbType.Int).Value = id))
            .FirstOrDefault();
    }

    public async Task<Pais?> BuscarPorCodigoAsync(string codigo)
    {
        return (await ConsultarAsync(SqlPorCodigo, cmd => cmd.Parameters.Add("@codigo", SqlDbType.Char, 3).Value = codigo))
            .FirstOrDefault();
    }

    public Task<IReadOnlyList<Pais>> ListarTodosAsync() => ConsultarAsync(SqlTodos, _ => { });

    public Task<IReadOnlyList<Pais>> BuscarPorRegionAsync(Region region)
    {
        return ConsultarAsync(SqlPorRegion,
            cmd => cmd.Parameters.Add("@region", SqlDbType.VarChar, 20).Value = region.ValorBase());
    }

    public Task<IReadOnlyList<Pais>> BuscarPorNombreAsync(string fragmento)
    {
        string patron = "%" + EscaparLike(fragmento) + "%";
        return ConsultarAsync(SqlPorNombre, cmd => cmd.Parameters.Add("@patron", SqlDbType.NVarChar, 210).Value = patron);
    }

    public async Task<ResultadoImportacion> GuardarOActualizarAsync(IReadOnlyList<Pais> paises)
    {
        if (paises.Count == 0)
        {
            return ResultadoImportacion.Vacio;
        }
        try
        {
            await using SqlConnection con = await conexionBD.AbrirAsync();
            await using SqlTransaction transaccion = (SqlTransaction)await con.BeginTransactionAsync();
            try
            {
                HashSet<string> existentes = await LeerCodigosAsync(con, transaccion);
                await using SqlCommand insertar = new(SqlInsertar, con, transaccion);
                await using SqlCommand actualizar = new(SqlActualizarPorCodigo, con, transaccion);
                int nuevos = 0;
                foreach (Pais pais in paises)
                {
                    bool existe = existentes.Contains(pais.Codigo!);
                    SqlCommand comando = existe ? actualizar : insertar;
                    comando.Parameters.Clear();
                    AsignarDatos(comando, pais);
                    await comando.ExecuteNonQueryAsync();
                    if (!existe)
                    {
                        nuevos++;
                        existentes.Add(pais.Codigo!);
                    }
                }
                await transaccion.CommitAsync();
                return new ResultadoImportacion(paises.Count, nuevos, paises.Count - nuevos);
            }
            catch
            {
                await transaccion.RollbackAsync();
                throw;
            }
        }
        catch (SqlException e)
        {
            throw Traducir(e, "guardar los países importados", null);
        }
    }

    public async Task<IReadOnlyList<ConteoRegion>> ContarPorRegionAsync()
    {
        try
        {
            await using SqlConnection con = await conexionBD.AbrirAsync();
            await using SqlCommand cmd = new(SqlContarPorRegion, con);
            await using SqlDataReader lector = await cmd.ExecuteReaderAsync();
            List<ConteoRegion> conteo = [];
            while (await lector.ReadAsync())
            {
                conteo.Add(new ConteoRegion(Regiones.DesdeBase(lector.GetString(0)), lector.GetInt64(1)));
            }
            return conteo;
        }
        catch (SqlException e)
        {
            throw Traducir(e, "contar los países por región", null);
        }
    }

    public async Task<Pais?> ObtenerMasPobladoAsync() => (await ConsultarAsync(SqlMasPoblado, _ => { })).FirstOrDefault();

    public async Task<long> ContarAsync()
    {
        try
        {
            await using SqlConnection con = await conexionBD.AbrirAsync();
            await using SqlCommand cmd = new(SqlContar, con);
            return (long)(await cmd.ExecuteScalarAsync())!;
        }
        catch (SqlException e)
        {
            throw Traducir(e, "contar los países", null);
        }
    }

    // ------------------------------------------------------------------ apoyo

    private async Task<IReadOnlyList<Pais>> ConsultarAsync(string sql, Action<SqlCommand> parametros)
    {
        try
        {
            await using SqlConnection con = await conexionBD.AbrirAsync();
            await using SqlCommand cmd = new(sql, con);
            parametros(cmd);
            await using SqlDataReader lector = await cmd.ExecuteReaderAsync();
            List<Pais> paises = [];
            while (await lector.ReadAsync())
            {
                paises.Add(Mapear(lector));
            }
            return paises;
        }
        catch (SqlException e)
        {
            throw Traducir(e, "consultar los países", null);
        }
    }

    /// <summary>Parámetros @codigo, @nombre, @capital, @region, @poblacion y @moneda.</summary>
    private static void AsignarDatos(SqlCommand cmd, Pais pais)
    {
        cmd.Parameters.Add("@codigo", SqlDbType.Char, 3).Value = pais.Codigo;
        cmd.Parameters.Add("@nombre", SqlDbType.NVarChar, 100).Value = pais.Nombre;
        cmd.Parameters.Add("@capital", SqlDbType.NVarChar, 100).Value = (object?)pais.Capital ?? DBNull.Value;
        cmd.Parameters.Add("@region", SqlDbType.VarChar, 20).Value = pais.Region!.Value.ValorBase();
        cmd.Parameters.Add("@poblacion", SqlDbType.BigInt).Value = pais.Poblacion;
        cmd.Parameters.Add("@moneda", SqlDbType.NVarChar, 100).Value = (object?)pais.Moneda ?? DBNull.Value;
    }

    private static Pais Mapear(SqlDataReader lector)
    {
        return new Pais(
            lector.GetInt32(0),
            lector.GetString(1),
            lector.GetString(2),
            lector.IsDBNull(3) ? null : lector.GetString(3),
            Regiones.DesdeBase(lector.GetString(4)),
            lector.GetInt64(5),
            lector.IsDBNull(6) ? null : lector.GetString(6));
    }

    private static async Task<HashSet<string>> LeerCodigosAsync(SqlConnection con, SqlTransaction transaccion)
    {
        HashSet<string> codigos = new(StringComparer.OrdinalIgnoreCase);
        await using SqlCommand cmd = new(SqlCodigos, con, transaccion);
        await using SqlDataReader lector = await cmd.ExecuteReaderAsync();
        while (await lector.ReadAsync())
        {
            codigos.Add(lector.GetString(0));
        }
        return codigos;
    }

    /// <summary>
    /// Escapa los comodines de LIKE para que "%", "_" o "[" escritos por el usuario se busquen
    /// literalmente (la consulta declara "\" como carácter de escape).
    /// </summary>
    internal static string EscaparLike(string texto)
    {
        return texto.Replace("\\", "\\\\").Replace("%", "\\%").Replace("_", "\\_").Replace("[", "\\[");
    }

    /// <summary>Convierte la SqlException en una excepción de la aplicación; el detalle técnico queda en el log.</summary>
    private ExcepcionAplicacion Traducir(SqlException e, string operacion, string? codigo)
    {
        if (ErroresDuplicado.Contains(e.Number))
        {
            return new ExcepcionValidacion($"Ya existe un país con el código {codigo}.");
        }
        log.LogError(e, "Error de base de datos al {Operacion}", operacion);
        return new ExcepcionPersistencia($"No se pudo {operacion}. Intente nuevamente.", e);
    }
}

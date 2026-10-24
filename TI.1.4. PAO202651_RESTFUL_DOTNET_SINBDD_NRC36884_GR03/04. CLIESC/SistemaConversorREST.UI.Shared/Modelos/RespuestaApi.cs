namespace SistemaConversorREST.UI.Shared.Modelos;

/// <summary>Contrato JSON de las respuestas de éxito y error del servidor REST.</summary>
public sealed class RespuestaApi
{
    public string? Estado { get; set; }
    public string? Codigo { get; set; }
    public string? Mensaje { get; set; }
    public string? Token { get; set; }
    public string? ExpiraEn { get; set; }
    public double? Resultado { get; set; }
    public List<CategoriaCliente>? Categorias { get; set; }
    public List<UnidadCliente>? Unidades { get; set; }
}

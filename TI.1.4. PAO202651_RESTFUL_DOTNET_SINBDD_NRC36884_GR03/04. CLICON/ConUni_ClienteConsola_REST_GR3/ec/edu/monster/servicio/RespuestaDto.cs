using System.Text.Json.Serialization;

namespace ec.edu.monster.servicio
{
    internal sealed class RespuestaDto
    {
        [JsonPropertyName("estado")]
        public string? Estado { get; set; }
        [JsonPropertyName("codigo")]
        public string? Codigo { get; set; }
        [JsonPropertyName("mensaje")]
        public string? Mensaje { get; set; }
        [JsonPropertyName("resultado")]
        public double? Resultado { get; set; }
        [JsonPropertyName("unidadOrigen")]
        public string? UnidadOrigen { get; set; }
        [JsonPropertyName("unidadDestino")]
        public string? UnidadDestino { get; set; }
        [JsonPropertyName("token")]
        public string? Token { get; set; }
        [JsonPropertyName("expiraEn")]
        public DateTimeOffset? ExpiraEn { get; set; }
        [JsonPropertyName("categorias")]
        public List<CategoriaDto?>? Categorias { get; set; }
        [JsonPropertyName("unidades")]
        public List<UnidadDto?>? Unidades { get; set; }
    }
}

using System.Text.Json.Serialization;

namespace ec.edu.monster.servicio
{
    internal sealed class UnidadDto
    {
        [JsonPropertyName("codigo")]
        public string? Codigo { get; set; }
        [JsonPropertyName("nombre")]
        public string? Nombre { get; set; }
        [JsonPropertyName("simbolo")]
        public string? Simbolo { get; set; }
        [JsonPropertyName("categoria")]
        public string? Categoria { get; set; }
    }
}

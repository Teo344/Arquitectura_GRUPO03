using System.Text.Json.Serialization;

namespace ec.edu.monster.servicio
{
    internal sealed class CategoriaDto
    {
        [JsonPropertyName("codigo")]
        public string? Codigo { get; set; }
        [JsonPropertyName("nombre")]
        public string? Nombre { get; set; }
        [JsonPropertyName("convertible")]
        public bool? Convertible { get; set; }
    }
}

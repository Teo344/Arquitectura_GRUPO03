using System.Text.Json.Serialization;

namespace ec.edu.monster.servicio
{
    internal sealed class CredencialesDto
    {
        [JsonPropertyName("usuario")]
        public string Usuario { get; set; } = string.Empty;
        [JsonPropertyName("clave")]
        public string Clave { get; set; } = string.Empty;
    }
}

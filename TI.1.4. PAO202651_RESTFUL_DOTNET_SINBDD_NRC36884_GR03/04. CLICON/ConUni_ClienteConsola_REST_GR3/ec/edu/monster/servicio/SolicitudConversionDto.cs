using System.Text.Json.Serialization;

namespace ec.edu.monster.servicio
{
    internal sealed class SolicitudConversionDto
    {
        [JsonPropertyName("valor")]
        public double Valor { get; set; }
        [JsonPropertyName("unidadOrigen")]
        public string UnidadOrigen { get; set; } = string.Empty;
        [JsonPropertyName("unidadDestino")]
        public string UnidadDestino { get; set; } = string.Empty;
    }
}

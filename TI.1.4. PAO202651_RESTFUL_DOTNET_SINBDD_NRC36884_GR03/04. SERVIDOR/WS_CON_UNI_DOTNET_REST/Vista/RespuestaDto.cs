using System.Collections.Generic;

namespace WS_CON_UNI_DOTNET_REST.Vista
{
    /// <summary>
    /// Respuesta exitosa de todas las operaciones (RQF-009). Los campos que no aplican
    /// a la operación quedan nulos y no se incluyen en el JSON. El orden de las propiedades
    /// es el del JSON.
    /// </summary>
    public class RespuestaDto
    {
        public string Estado { get; set; }
        public string Codigo { get; set; }
        public string Mensaje { get; set; }
        public double? Resultado { get; set; }
        public string UnidadOrigen { get; set; }
        public string UnidadDestino { get; set; }
        public string Token { get; set; }
        public string ExpiraEn { get; set; }
        public List<CategoriaDto> Categorias { get; set; }
        public List<UnidadDto> Unidades { get; set; }
    }
}

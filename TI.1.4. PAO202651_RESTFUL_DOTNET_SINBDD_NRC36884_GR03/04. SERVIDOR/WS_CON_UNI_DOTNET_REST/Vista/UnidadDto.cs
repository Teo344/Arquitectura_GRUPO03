namespace WS_CON_UNI_DOTNET_REST.Vista
{
    /// <summary>Elemento de la lista de unidades. No expone el factor de conversión.</summary>
    public class UnidadDto
    {
        public string Codigo { get; set; }
        public string Nombre { get; set; }
        public string Simbolo { get; set; }
        public string Categoria { get; set; }
    }
}

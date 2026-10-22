namespace WS_CON_UNI_DOTNET_REST.Vista
{
    /// <summary>Cuerpo del cambio de clave.</summary>
    public class CambioClaveDto
    {
        public string Usuario { get; set; }
        public string ClaveActual { get; set; }
        public string ClaveNueva { get; set; }
    }
}

namespace ec.edu.monster.servicio
{
    /// <summary>
    /// Configuración que entrega cada aplicación (escritorio, móvil o web) al registrar los
    /// servicios (RNF-010): la URL del servidor y el nombre del equipo para logsession.
    /// </summary>
    public sealed class OpcionesCliente
    {
        /// <summary>URL base de WS_EUREKA_DOTNET_REST, por ejemplo http://localhost:8097/</summary>
        public string UrlServidor { get; set; } = "http://localhost:8097/";

        /// <summary>Nombre del equipo que el servidor guarda en logsession (RQF-001).</summary>
        public string? NombreEquipo { get; set; }

        /// <summary>Texto del pie de la pantalla de acceso: "Cliente de escritorio", "Cliente móvil"...</summary>
        public string NombreCliente { get; set; } = "Cliente de escritorio";
    }
}

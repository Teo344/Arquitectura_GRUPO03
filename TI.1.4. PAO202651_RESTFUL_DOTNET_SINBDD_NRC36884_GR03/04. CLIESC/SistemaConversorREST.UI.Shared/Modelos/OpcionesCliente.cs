namespace SistemaConversorREST.UI.Shared.Modelos;

/// <summary>
/// Configuración propia de cada cliente (escritorio, web, móvil): dónde está el servidor REST
/// y cómo se presenta el cliente en las vistas.
/// </summary>
public sealed class OpcionesCliente
{
    public string UrlServidor { get; set; } = "http://localhost:8096/";

    public string NombreCliente { get; set; } = "Cliente de escritorio";
}

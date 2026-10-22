using Microsoft.AspNetCore.Mvc;

namespace WS_CON_UNI_DOTNET_REST.Seguridad
{
    /// <summary>
    /// Marca un controlador como protegido: aplica <see cref="ManejadorAutenticacion"/> antes de
    /// cada acción. Es el equivalente de @Protegido (@NameBinding) en el servidor Java.
    /// </summary>
    public class ProtegidoAttribute : TypeFilterAttribute
    {
        public ProtegidoAttribute() : base(typeof(ManejadorAutenticacion))
        {
        }
    }
}

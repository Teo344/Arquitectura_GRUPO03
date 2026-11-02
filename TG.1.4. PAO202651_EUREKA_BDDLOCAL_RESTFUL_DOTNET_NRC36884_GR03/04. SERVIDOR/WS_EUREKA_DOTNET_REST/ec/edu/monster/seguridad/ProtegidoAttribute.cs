using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.seguridad
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

using SistemaConversorREST.UI.Shared.Modelos;

namespace SistemaConversorREST.UI.Shared.Interfaces;

public interface IAutenticacionRest
{
    Task<SesionCliente> IniciarSesionAsync(string usuario, string clave);
}

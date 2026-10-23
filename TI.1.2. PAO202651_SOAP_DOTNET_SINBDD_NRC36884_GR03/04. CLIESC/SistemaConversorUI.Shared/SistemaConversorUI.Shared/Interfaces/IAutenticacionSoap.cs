using System;
using System.Collections.Generic;
using System.Text;
using SistemaConversorUI.Shared.Modelos;

namespace SistemaConversorUI.Shared.Interfaces
{
    public interface IAutenticacionSoap
    {
        Task<SesionCliente> IniciarSesionAsync(string usuario, string clave);

        Task CambiarClaveAsync(
            string usuario,
            string claveActual,
            string claveNueva);
    }
}

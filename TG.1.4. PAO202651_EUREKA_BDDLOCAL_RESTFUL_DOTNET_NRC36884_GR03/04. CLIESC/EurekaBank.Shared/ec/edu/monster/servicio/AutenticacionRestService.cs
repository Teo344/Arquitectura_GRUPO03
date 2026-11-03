using System.Text.Json;
using System.Threading.Tasks;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    /// <summary>Acceso al recurso público de autenticación (/api/autenticacion): RQF-001 y RQF-002.</summary>
    public sealed class AutenticacionRestService
    {
        private readonly ClienteRest cliente;
        private readonly OpcionesCliente opciones;

        public AutenticacionRestService(ClienteRest cliente, OpcionesCliente opciones)
        {
            this.cliente = cliente;
            this.opciones = opciones;
        }

        /// <summary>El nombre del equipo se guarda en logsession.</summary>
        public async Task<SesionModelo> IniciarSesionAsync(string usuario, string clave)
        {
            JsonElement respuesta = await cliente.PostAsync("/autenticacion/login", null,
                new { usuario, clave, equipo = opciones.NombreEquipo });

            string? token = ClienteRest.Texto(respuesta, "token");
            if (string.IsNullOrWhiteSpace(token))
            {
                throw new ErrorServicioModelo(null, "El servidor no devolvió el token de sesión.");
            }
            return new SesionModelo(token, ClienteRest.Texto(respuesta, "expiraEn"),
                ClienteRest.Texto(respuesta, "codigoEmpleado"), ClienteRest.Texto(respuesta, "nombreEmpleado"),
                ClienteRest.Texto(respuesta, "codigoSucursal"), ClienteRest.Texto(respuesta, "sucursal"));
        }

        /// <summary>Invalida el token en el servidor y registra la salida en logsession.</summary>
        public async Task CerrarSesionAsync(string token)
        {
            await cliente.PostAsync("/autenticacion/cerrar-sesion", token, null);
        }
    }
}

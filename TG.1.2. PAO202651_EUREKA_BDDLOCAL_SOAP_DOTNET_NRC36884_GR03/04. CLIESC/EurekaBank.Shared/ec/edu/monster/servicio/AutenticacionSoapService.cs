using System.Threading.Tasks;
using System.Xml.Linq;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    /// <summary>Acceso al servicio SOAP público de autenticación (AutenticacionWS): RQF-001 y RQF-002.</summary>
    public sealed class AutenticacionSoapService
    {
        private const string Servicio = "AutenticacionWS";

        private readonly ClienteSoap cliente;
        private readonly OpcionesCliente opciones;

        public AutenticacionSoapService(ClienteSoap cliente, OpcionesCliente opciones)
        {
            this.cliente = cliente;
            this.opciones = opciones;
        }

        /// <summary>El nombre del equipo se guarda en logsession.</summary>
        public async Task<SesionModelo> IniciarSesionAsync(string usuario, string clave)
        {
            XElement respuesta = await cliente.InvocarAsync(Servicio, "iniciarSesion", null,
                ("usuario", usuario), ("clave", clave), ("equipo", opciones.NombreEquipo));

            string? token = ClienteSoap.Texto(respuesta, "token");
            if (string.IsNullOrWhiteSpace(token))
            {
                throw new ErrorServicioModelo(null, "El servidor no devolvió el token de sesión.");
            }
            return new SesionModelo(token, ClienteSoap.Texto(respuesta, "expiraEn"),
                ClienteSoap.Texto(respuesta, "codigoEmpleado"), ClienteSoap.Texto(respuesta, "nombreEmpleado"),
                ClienteSoap.Texto(respuesta, "codigoSucursal"), ClienteSoap.Texto(respuesta, "sucursal"));
        }

        /// <summary>Invalida el token en el servidor y registra la salida en logsession.</summary>
        public async Task CerrarSesionAsync(string token)
        {
            await cliente.InvocarAsync(Servicio, "cerrarSesion", token);
        }
    }
}

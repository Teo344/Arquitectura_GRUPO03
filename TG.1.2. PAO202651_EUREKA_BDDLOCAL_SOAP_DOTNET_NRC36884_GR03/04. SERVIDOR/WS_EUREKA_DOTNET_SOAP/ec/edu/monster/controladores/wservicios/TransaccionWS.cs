using System.ServiceModel;
using ec.edu.monster.seguridad;
using ec.edu.monster.servicios;
using ec.edu.monster.vista;

namespace ec.edu.monster.controladores.wservicios
{
    /// <summary>
    /// Servicio protegido por ManejadorAutenticacion: no tiene lógica, delega en ServicioTransaccion.
    /// El empleado de cada movimiento sale de la sesión que dejó el inspector, nunca de un
    /// parámetro (RN-06).
    /// </summary>
    [Protegido]
    public class TransaccionWS : ITransaccionWS
    {
        private readonly ServicioTransaccion servicioTransaccion = new ServicioTransaccion();

        public RespuestaDto Depositar(string cuenta, decimal? importe)
        {
            return RespuestaMapeador.DeDeposito(servicioTransaccion.Depositar(SesionActual(), cuenta, importe));
        }

        public RespuestaDto Retirar(string cuenta, decimal? importe)
        {
            return RespuestaMapeador.DeRetiro(servicioTransaccion.Retirar(SesionActual(), cuenta, importe));
        }

        public RespuestaDto Transferir(string cuentaOrigen, string cuentaDestino, decimal? importe)
        {
            return RespuestaMapeador.DeTransferencia(
                servicioTransaccion.Transferir(SesionActual(), cuentaOrigen, cuentaDestino, importe));
        }

        private static Sesion SesionActual()
        {
            OperationContext.Current.IncomingMessageProperties.TryGetValue(ManejadorAutenticacion.PropiedadSesion, out object sesion);
            return (Sesion)sesion;
        }
    }
}

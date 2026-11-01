using ec.edu.monster.seguridad;
using ec.edu.monster.servicios;
using ec.edu.monster.vista;

namespace ec.edu.monster.controladores.wservicios
{
    /// <summary>Servicio protegido por ManejadorAutenticacion: no tiene lógica, delega en ServicioCuenta.</summary>
    [Protegido]
    public class CuentaWS : ICuentaWS
    {
        private readonly ServicioCuenta servicioCuenta = new ServicioCuenta();

        public RespuestaDto ConsultarCuenta(string cuenta)
        {
            return RespuestaMapeador.DeCuenta(servicioCuenta.ConsultarCuenta(cuenta));
        }

        public RespuestaDto ConsultarMovimientos(string cuenta)
        {
            return RespuestaMapeador.DeMovimientos(servicioCuenta.ConsultarMovimientos(cuenta));
        }
    }
}

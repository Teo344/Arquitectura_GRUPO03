using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ModelBinding;
using ec.edu.monster.seguridad;
using ec.edu.monster.servicios;
using ec.edu.monster.vista;

namespace ec.edu.monster.controladores.recursos
{
    /// <summary>
    /// Controlador protegido por ManejadorAutenticacion: depósito (RQF-005), retiro (RQF-006) y
    /// transferencia (RQF-007). El empleado de cada movimiento sale de la sesión que dejó el
    /// filtro, nunca del cuerpo de la petición (RN-06).
    /// </summary>
    [ApiController]
    [Route("api/transacciones")]
    [Protegido]
    public class TransaccionControlador : ControllerBase
    {
        private readonly ServicioTransaccion servicioTransaccion;

        public TransaccionControlador(ServicioTransaccion servicioTransaccion)
        {
            this.servicioTransaccion = servicioTransaccion;
        }

        [HttpPost("depositos")]
        public RespuestaDto Depositar([FromBody(EmptyBodyBehavior = EmptyBodyBehavior.Allow)] SolicitudTransaccionDto solicitud)
        {
            SolicitudTransaccionDto s = solicitud ?? new SolicitudTransaccionDto();
            return RespuestaMapeador.DeDeposito(servicioTransaccion.Depositar(SesionActual(), s.Cuenta, s.Importe));
        }

        [HttpPost("retiros")]
        public RespuestaDto Retirar([FromBody(EmptyBodyBehavior = EmptyBodyBehavior.Allow)] SolicitudTransaccionDto solicitud)
        {
            SolicitudTransaccionDto s = solicitud ?? new SolicitudTransaccionDto();
            return RespuestaMapeador.DeRetiro(servicioTransaccion.Retirar(SesionActual(), s.Cuenta, s.Importe));
        }

        [HttpPost("transferencias")]
        public RespuestaDto Transferir([FromBody(EmptyBodyBehavior = EmptyBodyBehavior.Allow)] SolicitudTransferenciaDto solicitud)
        {
            SolicitudTransferenciaDto s = solicitud ?? new SolicitudTransferenciaDto();
            return RespuestaMapeador.DeTransferencia(
                servicioTransaccion.Transferir(SesionActual(), s.CuentaOrigen, s.CuentaDestino, s.Importe));
        }

        private Sesion SesionActual()
        {
            return HttpContext.Items[ManejadorAutenticacion.PropiedadSesion] as Sesion;
        }
    }
}

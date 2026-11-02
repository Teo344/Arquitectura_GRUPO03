using Microsoft.AspNetCore.Mvc;
using ec.edu.monster.seguridad;
using ec.edu.monster.servicios;
using ec.edu.monster.vista;

namespace ec.edu.monster.controladores.recursos
{
    /// <summary>
    /// Controlador protegido por ManejadorAutenticacion: consulta de cuenta (RQF-003) y de
    /// movimientos (RQF-004). El token viaja en la cabecera Authorization y lo valida el filtro
    /// antes de llegar aquí.
    /// </summary>
    [ApiController]
    [Route("api/cuentas")]
    [Protegido]
    public class CuentaControlador : ControllerBase
    {
        private readonly ServicioCuenta servicioCuenta;

        public CuentaControlador(ServicioCuenta servicioCuenta)
        {
            this.servicioCuenta = servicioCuenta;
        }

        [HttpGet("{cuenta}")]
        public RespuestaDto ConsultarCuenta(string cuenta)
        {
            return RespuestaMapeador.DeCuenta(servicioCuenta.ConsultarCuenta(cuenta));
        }

        [HttpGet("{cuenta}/movimientos")]
        public RespuestaDto ConsultarMovimientos(string cuenta)
        {
            return RespuestaMapeador.DeMovimientos(servicioCuenta.ConsultarMovimientos(cuenta));
        }
    }
}

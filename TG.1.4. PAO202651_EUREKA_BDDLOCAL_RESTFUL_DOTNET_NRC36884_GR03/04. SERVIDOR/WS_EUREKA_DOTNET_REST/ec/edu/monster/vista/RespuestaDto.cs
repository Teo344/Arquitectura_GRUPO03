using System.Collections.Generic;

namespace ec.edu.monster.vista
{
    /// <summary>
    /// Respuesta exitosa de todas las operaciones (sección 8.2.7 del ERS). Los campos que no
    /// aplican a la operación quedan nulos y no se incluyen en el JSON. El orden de las
    /// propiedades es el del JSON.
    /// </summary>
    public class RespuestaDto
    {
        public string Estado { get; set; }
        public string Codigo { get; set; }
        public string Mensaje { get; set; }
        public string Token { get; set; }
        public string ExpiraEn { get; set; }
        public string CodigoEmpleado { get; set; }
        public string NombreEmpleado { get; set; }
        public string CodigoSucursal { get; set; }
        public string Sucursal { get; set; }
        public CuentaDto Cuenta { get; set; }
        public List<MovimientoDto> Movimientos { get; set; }
        public decimal? NuevoSaldo { get; set; }
        public int? NumeroMovimiento { get; set; }
        public int? NumeroMovimientoDestino { get; set; }
    }
}

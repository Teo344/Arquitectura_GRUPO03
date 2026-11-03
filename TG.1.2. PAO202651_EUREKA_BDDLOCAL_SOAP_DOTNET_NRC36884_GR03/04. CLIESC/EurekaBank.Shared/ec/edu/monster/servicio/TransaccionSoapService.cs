using System.Threading.Tasks;
using System.Xml.Linq;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    /// <summary>
    /// Acceso al servicio SOAP protegido de transacciones (TransaccionWS): RQF-005 a RQF-007.
    /// El empleado del movimiento lo toma el servidor del token, nunca de un parámetro (RN-06).
    /// </summary>
    public sealed class TransaccionSoapService
    {
        private const string Servicio = "TransaccionWS";

        private readonly ClienteSoap cliente;

        public TransaccionSoapService(ClienteSoap cliente)
        {
            this.cliente = cliente;
        }

        public async Task<ResultadoTransaccionModelo> DepositarAsync(string token, string cuenta, decimal importe)
        {
            return AResultado(await cliente.InvocarAsync(Servicio, "depositar", token,
                ("cuenta", cuenta), ("importe", ClienteSoap.Importe(importe))));
        }

        public async Task<ResultadoTransaccionModelo> RetirarAsync(string token, string cuenta, decimal importe)
        {
            return AResultado(await cliente.InvocarAsync(Servicio, "retirar", token,
                ("cuenta", cuenta), ("importe", ClienteSoap.Importe(importe))));
        }

        public async Task<ResultadoTransaccionModelo> TransferirAsync(string token, string cuentaOrigen,
            string cuentaDestino, decimal importe)
        {
            return AResultado(await cliente.InvocarAsync(Servicio, "transferir", token,
                ("cuentaOrigen", cuentaOrigen), ("cuentaDestino", cuentaDestino),
                ("importe", ClienteSoap.Importe(importe))));
        }

        private static ResultadoTransaccionModelo AResultado(XElement respuesta)
        {
            return new ResultadoTransaccionModelo(ClienteSoap.Texto(respuesta, "mensaje"),
                ClienteSoap.Decimal(respuesta, "nuevoSaldo"), ClienteSoap.Entero(respuesta, "numeroMovimiento"),
                ClienteSoap.Entero(respuesta, "numeroMovimientoDestino"));
        }
    }
}

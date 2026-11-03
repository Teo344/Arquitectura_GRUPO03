using System.Text.Json;
using System.Threading.Tasks;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    /// <summary>
    /// Acceso al recurso protegido de transacciones (/api/transacciones): RQF-005 a RQF-007. El
    /// empleado del movimiento lo toma el servidor del token, nunca de un parámetro (RN-06).
    /// </summary>
    public sealed class TransaccionRestService
    {
        private readonly ClienteRest cliente;

        public TransaccionRestService(ClienteRest cliente)
        {
            this.cliente = cliente;
        }

        public async Task<ResultadoTransaccionModelo> DepositarAsync(string token, string cuenta, decimal importe)
        {
            return AResultado(await cliente.PostAsync("/transacciones/depositos", token, new { cuenta, importe }));
        }

        public async Task<ResultadoTransaccionModelo> RetirarAsync(string token, string cuenta, decimal importe)
        {
            return AResultado(await cliente.PostAsync("/transacciones/retiros", token, new { cuenta, importe }));
        }

        public async Task<ResultadoTransaccionModelo> TransferirAsync(string token, string cuentaOrigen,
            string cuentaDestino, decimal importe)
        {
            return AResultado(await cliente.PostAsync("/transacciones/transferencias", token,
                new { cuentaOrigen, cuentaDestino, importe }));
        }

        private static ResultadoTransaccionModelo AResultado(JsonElement respuesta)
        {
            return new ResultadoTransaccionModelo(ClienteRest.Texto(respuesta, "mensaje"),
                ClienteRest.Decimal(respuesta, "nuevoSaldo"), ClienteRest.Entero(respuesta, "numeroMovimiento"),
                ClienteRest.Entero(respuesta, "numeroMovimientoDestino"));
        }
    }
}

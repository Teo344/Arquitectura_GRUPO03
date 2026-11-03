using System.Collections.Generic;
using System.Linq;
using System.Text.Json;
using System.Threading.Tasks;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    /// <summary>Acceso al recurso protegido de consultas (/api/cuentas): RQF-003 y RQF-004.</summary>
    public sealed class CuentaRestService
    {
        private readonly ClienteRest cliente;

        public CuentaRestService(ClienteRest cliente)
        {
            this.cliente = cliente;
        }

        public async Task<CuentaModelo> ConsultarCuentaAsync(string token, string cuenta)
        {
            JsonElement respuesta = await cliente.GetAsync("/cuentas/" + ClienteRest.Segmento(cuenta), token);
            JsonElement? datos = ClienteRest.Objeto(respuesta, "cuenta");
            if (datos == null)
            {
                throw new ErrorServicioModelo(null, "El servidor no devolvió los datos de la cuenta.");
            }
            JsonElement d = datos.Value;
            return new CuentaModelo(ClienteRest.Texto(d, "codigo"), ClienteRest.Texto(d, "titular"),
                ClienteRest.Texto(d, "moneda"), ClienteRest.Decimal(d, "saldo"), ClienteRest.Texto(d, "estado"),
                ClienteRest.Texto(d, "sucursal"), ClienteRest.Texto(d, "fechaCreacion"));
        }

        /// <summary>Una cuenta sin movimientos devuelve una lista vacía.</summary>
        public async Task<IReadOnlyList<MovimientoModelo>> ConsultarMovimientosAsync(string token, string cuenta)
        {
            JsonElement respuesta = await cliente.GetAsync("/cuentas/" + ClienteRest.Segmento(cuenta) + "/movimientos", token);
            return ClienteRest.Arreglo(respuesta, "movimientos")
                .Select(m => new MovimientoModelo(ClienteRest.Entero(m, "numero") ?? 0,
                    ClienteRest.Texto(m, "fecha"), ClienteRest.Texto(m, "tipo"), ClienteRest.Texto(m, "accion"),
                    ClienteRest.Decimal(m, "importe"), ClienteRest.Texto(m, "cuentaReferencia")))
                .ToList();
        }
    }
}

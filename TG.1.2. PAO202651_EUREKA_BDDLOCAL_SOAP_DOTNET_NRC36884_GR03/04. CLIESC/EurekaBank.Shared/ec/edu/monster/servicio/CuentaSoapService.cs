using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Xml.Linq;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    /// <summary>Acceso al servicio SOAP protegido de consultas (CuentaWS): RQF-003 y RQF-004.</summary>
    public sealed class CuentaSoapService
    {
        private const string Servicio = "CuentaWS";

        private readonly ClienteSoap cliente;

        public CuentaSoapService(ClienteSoap cliente)
        {
            this.cliente = cliente;
        }

        public async Task<CuentaModelo> ConsultarCuentaAsync(string token, string cuenta)
        {
            XElement respuesta = await cliente.InvocarAsync(Servicio, "consultarCuenta", token, ("cuenta", cuenta));
            XElement? datos = ClienteSoap.Hijo(respuesta, "cuenta");
            if (datos == null)
            {
                throw new ErrorServicioModelo(null, "El servidor no devolvió los datos de la cuenta.");
            }
            return new CuentaModelo(ClienteSoap.Texto(datos, "codigo"), ClienteSoap.Texto(datos, "titular"),
                ClienteSoap.Texto(datos, "moneda"), ClienteSoap.Decimal(datos, "saldo"),
                ClienteSoap.Texto(datos, "estado"), ClienteSoap.Texto(datos, "sucursal"),
                ClienteSoap.Texto(datos, "fechaCreacion"));
        }

        /// <summary>Una cuenta sin movimientos devuelve una lista vacía.</summary>
        public async Task<IReadOnlyList<MovimientoModelo>> ConsultarMovimientosAsync(string token, string cuenta)
        {
            XElement respuesta = await cliente.InvocarAsync(Servicio, "consultarMovimientos", token, ("cuenta", cuenta));
            return ClienteSoap.Hijos(ClienteSoap.Hijo(respuesta, "movimientos"), "movimiento")
                .Select(e => new MovimientoModelo(ClienteSoap.Entero(e, "numero") ?? 0,
                    ClienteSoap.Texto(e, "fecha"), ClienteSoap.Texto(e, "tipo"), ClienteSoap.Texto(e, "accion"),
                    ClienteSoap.Decimal(e, "importe"), ClienteSoap.Texto(e, "cuentaReferencia")))
                .ToList();
        }
    }
}

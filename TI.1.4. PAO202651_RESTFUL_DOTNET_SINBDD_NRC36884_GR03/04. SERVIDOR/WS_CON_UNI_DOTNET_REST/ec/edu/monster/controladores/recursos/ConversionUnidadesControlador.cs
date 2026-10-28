using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ModelBinding;
using ec.edu.monster.modelo;
using ec.edu.monster.seguridad;
using ec.edu.monster.servicios;
using ec.edu.monster.vista;

namespace ec.edu.monster.controladores.recursos
{
    /// <summary>
    /// Controlador protegido por ManejadorAutenticacion: catálogo (RQF-002, RQF-003) y
    /// conversiones (RQF-004 a RQF-006). El token viaja en la cabecera Authorization y lo
    /// valida el filtro antes de llegar aquí.
    /// </summary>
    [ApiController]
    [Route("api")]
    [Protegido]
    public class ConversionUnidadesControlador : ControllerBase
    {
        private readonly ServicioCatalogo servicioCatalogo;
        private readonly ServicioConversion servicioConversion;

        public ConversionUnidadesControlador(ServicioCatalogo servicioCatalogo, ServicioConversion servicioConversion)
        {
            this.servicioCatalogo = servicioCatalogo;
            this.servicioConversion = servicioConversion;
        }

        [HttpGet("catalogo/categorias")]
        public RespuestaDto ConsultarCatalogo()
        {
            return RespuestaMapeador.DeCatalogo(servicioCatalogo.Categorias());
        }

        [HttpGet("catalogo/categorias/{categoria}/unidades")]
        public RespuestaDto ConsultarUnidades(string categoria)
        {
            return RespuestaMapeador.DeUnidades(servicioCatalogo.Unidades(categoria));
        }

        [HttpPost("conversiones/longitud")]
        public RespuestaDto ConvertirLongitud([FromBody(EmptyBodyBehavior = EmptyBodyBehavior.Allow)] SolicitudConversionDto solicitud)
            => Convertir(Categoria.Longitud, solicitud);

        [HttpPost("conversiones/masa")]
        public RespuestaDto ConvertirMasa([FromBody(EmptyBodyBehavior = EmptyBodyBehavior.Allow)] SolicitudConversionDto solicitud)
            => Convertir(Categoria.Masa, solicitud);

        [HttpPost("conversiones/temperatura")]
        public RespuestaDto ConvertirTemperatura([FromBody(EmptyBodyBehavior = EmptyBodyBehavior.Allow)] SolicitudConversionDto solicitud)
            => Convertir(Categoria.Temperatura, solicitud);

        private RespuestaDto Convertir(Categoria categoria, SolicitudConversionDto solicitud)
        {
            SolicitudConversionDto s = solicitud ?? new SolicitudConversionDto();
            return RespuestaMapeador.DeConversion(
                servicioConversion.Convertir(categoria, s.Valor, s.UnidadOrigen, s.UnidadDestino));
        }
    }
}

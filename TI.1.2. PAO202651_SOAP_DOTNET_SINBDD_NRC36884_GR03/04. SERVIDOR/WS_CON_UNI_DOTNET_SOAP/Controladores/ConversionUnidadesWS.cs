using WS_CON_UNI_DOTNET_SOAP.Modelo;
using WS_CON_UNI_DOTNET_SOAP.Seguridad;
using WS_CON_UNI_DOTNET_SOAP.Servicios;
using WS_CON_UNI_DOTNET_SOAP.Vista;

namespace WS_CON_UNI_DOTNET_SOAP.Controladores
{
    /// <summary>Servicio protegido por ManejadorAutenticacion: no tiene lógica, delega en los servicios.</summary>
    [Protegido]
    public class ConversionUnidadesWS : IConversionUnidadesWS
    {
        private readonly ServicioCatalogo servicioCatalogo = new ServicioCatalogo();
        private readonly ServicioConversion servicioConversion = new ServicioConversion();

        public RespuestaDto ConsultarCatalogo()
        {
            return RespuestaMapeador.DeCatalogo(servicioCatalogo.Categorias());
        }

        public RespuestaDto ConsultarUnidades(string categoria)
        {
            return RespuestaMapeador.DeUnidades(servicioCatalogo.Unidades(categoria));
        }

        public RespuestaDto ConvertirLongitud(SolicitudConversionDto solicitud) => Convertir(Categoria.Longitud, solicitud);

        public RespuestaDto ConvertirMasa(SolicitudConversionDto solicitud) => Convertir(Categoria.Masa, solicitud);

        public RespuestaDto ConvertirTemperatura(SolicitudConversionDto solicitud) => Convertir(Categoria.Temperatura, solicitud);

        private RespuestaDto Convertir(Categoria categoria, SolicitudConversionDto solicitud)
        {
            SolicitudConversionDto s = solicitud ?? new SolicitudConversionDto();
            return RespuestaMapeador.DeConversion(
                servicioConversion.Convertir(categoria, s.Valor, s.UnidadOrigen, s.UnidadDestino));
        }
    }
}

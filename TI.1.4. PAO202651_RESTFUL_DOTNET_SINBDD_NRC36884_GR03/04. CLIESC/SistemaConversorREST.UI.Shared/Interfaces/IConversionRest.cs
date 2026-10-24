using SistemaConversorREST.UI.Shared.Modelos;

namespace SistemaConversorREST.UI.Shared.Interfaces;

public interface IConversionRest
{
    Task<IReadOnlyList<CategoriaCliente>> ConsultarCatalogoAsync(string token);

    Task<IReadOnlyList<UnidadCliente>> ConsultarUnidadesAsync(string categoria, string token);

    Task<double> ConvertirLongitudAsync(SolicitudConversionCliente solicitud, string token);

    Task<double> ConvertirMasaAsync(SolicitudConversionCliente solicitud, string token);

    Task<double> ConvertirTemperaturaAsync(SolicitudConversionCliente solicitud, string token);
}

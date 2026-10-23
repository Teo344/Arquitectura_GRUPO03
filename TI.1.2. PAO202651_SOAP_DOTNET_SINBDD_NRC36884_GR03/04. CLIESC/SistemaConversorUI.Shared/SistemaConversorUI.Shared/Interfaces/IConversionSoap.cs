using SistemaConversorUI.Shared.Modelos;
using System;
using System.Collections.Generic;
using System.Text;

namespace SistemaConversorUI.Shared.Interfaces
{
    public interface IConversionSoap
    {
        Task<IReadOnlyList<CategoriaCliente>> ConsultarCatalogoAsync(string token);

        Task<IReadOnlyList<UnidadCliente>> ConsultarUnidadesAsync(
            string categoria,
            string token);

        Task<double> ConvertirLongitudAsync(
            SolicitudConversionCliente solicitud,
            string token);

        Task<double> ConvertirMasaAsync(
            SolicitudConversionCliente solicitud,
            string token);

        Task<double> ConvertirTemperaturaAsync(
            SolicitudConversionCliente solicitud,
            string token);
    }
}

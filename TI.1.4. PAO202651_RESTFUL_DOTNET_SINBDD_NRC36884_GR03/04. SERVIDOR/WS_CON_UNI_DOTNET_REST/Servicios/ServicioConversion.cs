using System;
using System.Collections.Generic;
using System.Linq;
using WS_CON_UNI_DOTNET_REST.Modelo;
using WS_CON_UNI_DOTNET_REST.Servicios.Conversores;

namespace WS_CON_UNI_DOTNET_REST.Servicios
{
    /// <summary>Resultado de una conversión, ya redondeado.</summary>
    public class ResultadoConversion
    {
        public double Resultado { get; set; }
        public Unidad Origen { get; set; }
        public Unidad Destino { get; set; }
    }

    /// <summary>Orquesta validación, elección del conversor y redondeo (RQF-004 a RQF-006).</summary>
    public class ServicioConversion
    {
        private const int Decimales = 4;

        private readonly ServicioValidacion validacion = new ServicioValidacion();

        /// <summary>Estrategias disponibles (Strategy): agregar un cálculo nuevo es agregar un IConversor.</summary>
        private readonly IList<IConversor> conversores = new List<IConversor>
        {
            new ConversorLineal(),
            new ConversorTemperatura()
        };

        public ResultadoConversion Convertir(Categoria categoria, double? valor, string origen, string destino)
        {
            var unidades = validacion.ValidarConversion(categoria, valor, origen, destino);
            IConversor conversor = conversores.FirstOrDefault(c => c.Soporta(categoria))
                ?? throw new ExcepcionServicio(CodigoError.Data001);
            double resultado = conversor.Convertir(valor.Value, unidades.Origen, unidades.Destino);
            return new ResultadoConversion
            {
                Resultado = Math.Round(resultado, Decimales, MidpointRounding.AwayFromZero),
                Origen = unidades.Origen,
                Destino = unidades.Destino
            };
        }
    }
}

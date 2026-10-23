using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador
{
    public class ConversionControlador
    {
        private readonly ClienteRESTConversor clienteRest;

        public ConversionControlador(ClienteRESTConversor clienteRest)
        {
            this.clienteRest = clienteRest;
        }

        public Task<double> ConvertirAsync(ConversionModelo modelo)
        {
            ValidarSolicitud(modelo);
            return modelo.TipoConversion switch
            {
                TipoConversion.Longitud => clienteRest.ConvertirLongitudAsync(modelo.Valor, modelo.UnidadInicial, modelo.UnidadFinal),
                TipoConversion.Masa => clienteRest.ConvertirMasaAsync(modelo.Valor, modelo.UnidadInicial, modelo.UnidadFinal),
                TipoConversion.Temperatura => clienteRest.ConvertirTemperaturaAsync(modelo.Valor, modelo.UnidadInicial, modelo.UnidadFinal),
                _ => throw new ArgumentException("Debe seleccionar el tipo de conversión.")
            };
        }

        private static void ValidarSolicitud(ConversionModelo modelo)
        {
            if (modelo == null)
                throw new ArgumentException("La solicitud de conversión es obligatoria.");
            if (!modelo.TipoConversion.HasValue || !Enum.IsDefined(modelo.TipoConversion.Value))
                throw new ArgumentException("Debe seleccionar el tipo de conversión.");
            if (!double.IsFinite(modelo.Valor))
                throw new ArgumentException("El valor debe ser un número finito.");
            if (string.IsNullOrWhiteSpace(modelo.UnidadInicial))
                throw new ArgumentException("La unidad inicial es obligatoria.");
            if (string.IsNullOrWhiteSpace(modelo.UnidadFinal))
                throw new ArgumentException("La unidad final es obligatoria.");
        }
    }
}

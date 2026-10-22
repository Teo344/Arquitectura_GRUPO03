using System;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador
{
    public class ConversionControlador
    {
        private readonly ClienteSOAPConversor clienteSoap;

        public ConversionControlador(ClienteSOAPConversor clienteSoap)
        {
            this.clienteSoap = clienteSoap;
        }

        public double Convertir(ConversionModelo modelo)
        {
            ValidarSolicitud(modelo);
            switch (modelo.TipoConversion.Value)
            {
                case TipoConversion.Longitud:
                    return clienteSoap.ConvertirLongitud(modelo.Valor, modelo.UnidadInicial, modelo.UnidadFinal);
                case TipoConversion.Masa:
                    return clienteSoap.ConvertirMasa(modelo.Valor, modelo.UnidadInicial, modelo.UnidadFinal);
                default:
                    return clienteSoap.ConvertirTemperatura(modelo.Valor, modelo.UnidadInicial, modelo.UnidadFinal);
            }
        }

        private void ValidarSolicitud(ConversionModelo modelo)
        {
            if (modelo == null)
                throw new ArgumentException("La solicitud de conversión es obligatoria.");
            if (!modelo.TipoConversion.HasValue || !Enum.IsDefined(typeof(TipoConversion), modelo.TipoConversion.Value))
                throw new ArgumentException("Debe seleccionar el tipo de conversión.");
            if (double.IsNaN(modelo.Valor) || double.IsInfinity(modelo.Valor))
                throw new ArgumentException("El valor debe ser un número finito.");
            if (string.IsNullOrWhiteSpace(modelo.UnidadInicial))
                throw new ArgumentException("La unidad inicial es obligatoria.");
            if (string.IsNullOrWhiteSpace(modelo.UnidadFinal))
                throw new ArgumentException("La unidad final es obligatoria.");
        }
    }
}

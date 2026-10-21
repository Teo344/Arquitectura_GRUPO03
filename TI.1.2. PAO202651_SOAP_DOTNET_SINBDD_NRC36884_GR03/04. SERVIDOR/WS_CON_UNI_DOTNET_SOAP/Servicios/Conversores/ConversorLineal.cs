using WS_CON_UNI_DOTNET_SOAP.Modelo;

namespace WS_CON_UNI_DOTNET_SOAP.Servicios.Conversores
{
    /// <summary>Conversión por factor a la unidad base: longitud (RQF-004) y masa (RQF-005).</summary>
    public class ConversorLineal : IConversor
    {
        public bool Soporta(Categoria categoria) => categoria == Categoria.Longitud || categoria == Categoria.Masa;

        public double Convertir(double valor, Unidad origen, Unidad destino)
        {
            return valor * origen.Factor.Value / destino.Factor.Value;
        }
    }
}

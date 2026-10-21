using WS_CON_UNI_DOTNET_SOAP.Modelo;

namespace WS_CON_UNI_DOTNET_SOAP.Servicios.Conversores
{
    /// <summary>Estrategia de cálculo para una o más categorías.</summary>
    public interface IConversor
    {
        bool Soporta(Categoria categoria);

        double Convertir(double valor, Unidad origen, Unidad destino);
    }
}

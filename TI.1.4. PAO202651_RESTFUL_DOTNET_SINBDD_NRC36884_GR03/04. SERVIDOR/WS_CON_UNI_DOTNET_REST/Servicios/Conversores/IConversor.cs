using WS_CON_UNI_DOTNET_REST.Modelo;

namespace WS_CON_UNI_DOTNET_REST.Servicios.Conversores
{
    /// <summary>Estrategia de cálculo para una o más categorías.</summary>
    public interface IConversor
    {
        bool Soporta(Categoria categoria);

        double Convertir(double valor, Unidad origen, Unidad destino);
    }
}

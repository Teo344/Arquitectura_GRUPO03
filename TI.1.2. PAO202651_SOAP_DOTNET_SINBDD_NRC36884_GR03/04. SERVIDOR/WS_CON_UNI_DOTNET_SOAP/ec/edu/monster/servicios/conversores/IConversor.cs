using ec.edu.monster.modelo;

namespace ec.edu.monster.servicios.conversores
{
    /// <summary>Estrategia de cálculo para una o más categorías.</summary>
    public interface IConversor
    {
        bool Soporta(Categoria categoria);

        double Convertir(double valor, Unidad origen, Unidad destino);
    }
}

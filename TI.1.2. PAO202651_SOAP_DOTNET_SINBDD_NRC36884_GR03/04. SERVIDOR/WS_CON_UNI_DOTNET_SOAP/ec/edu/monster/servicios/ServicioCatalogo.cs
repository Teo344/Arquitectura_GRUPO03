using System.Collections.Generic;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicios
{
    /// <summary>Catálogo de categorías (RQF-002) y unidades por categoría (RQF-003).</summary>
    public class ServicioCatalogo
    {
        private readonly ServicioValidacion validacion = new ServicioValidacion();

        public IEnumerable<Categoria> Categorias() => Categoria.Todas;

        public IEnumerable<Unidad> Unidades(string categoria)
        {
            return Unidad.DeCategoria(validacion.ValidarCategoria(categoria));
        }
    }
}

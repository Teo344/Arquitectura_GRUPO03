using System.Collections.Generic;
using WS_CON_UNI_DOTNET_REST.Modelo;

namespace WS_CON_UNI_DOTNET_REST.Servicios
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

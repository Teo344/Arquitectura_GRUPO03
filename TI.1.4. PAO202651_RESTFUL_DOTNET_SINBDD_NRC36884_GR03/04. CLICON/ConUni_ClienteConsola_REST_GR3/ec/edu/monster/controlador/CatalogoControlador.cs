using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador
{
    public class CatalogoControlador
    {
        private readonly ClienteRESTConversor clienteRest;

        public CatalogoControlador(ClienteRESTConversor clienteRest)
        {
            this.clienteRest = clienteRest;
        }

        public Task<List<CategoriaModelo>> ConsultarCategoriasAsync() => clienteRest.ConsultarCatalogoAsync();

        public Task<List<UnidadModelo>> ConsultarUnidadesAsync(string categoria)
        {
            if (string.IsNullOrWhiteSpace(categoria))
                throw new ArgumentException("Debe seleccionar una categoría.");
            return clienteRest.ConsultarUnidadesAsync(categoria);
        }
    }
}

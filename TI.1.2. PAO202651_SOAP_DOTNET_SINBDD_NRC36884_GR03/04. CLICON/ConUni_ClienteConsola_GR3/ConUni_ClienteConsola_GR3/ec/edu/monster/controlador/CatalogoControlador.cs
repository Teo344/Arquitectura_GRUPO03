using System;
using System.Collections.Generic;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador
{
    public class CatalogoControlador
    {
        private readonly ClienteSOAPConversor clienteSoap;

        public CatalogoControlador(ClienteSOAPConversor clienteSoap)
        {
            this.clienteSoap = clienteSoap;
        }

        public List<CategoriaModelo> ConsultarCategorias()
        {
            return clienteSoap.ConsultarCatalogo();
        }

        public List<UnidadModelo> ConsultarUnidades(string categoria)
        {
            if (string.IsNullOrWhiteSpace(categoria))
                throw new ArgumentException("Debe seleccionar una categoría.");
            return clienteSoap.ConsultarUnidades(categoria);
        }
    }
}

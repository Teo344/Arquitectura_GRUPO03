using System.Collections.Generic;
using ec.edu.monster.controlador;
using ec.edu.monster.modelo;
using ec.edu.monster.util;

namespace ec.edu.monster.vista
{
    public class CatalogoVista
    {
        private readonly MensajesVista mensajes = new MensajesVista();
        private readonly CatalogoControlador catalogoControlador;

        public CatalogoVista(CatalogoControlador catalogoControlador)
        {
            this.catalogoControlador = catalogoControlador;
        }

        public void MostrarCatalogo()
        {
            List<CategoriaModelo> categorias = catalogoControlador.ConsultarCategorias();
            if (categorias.Count == 0)
            {
                mensajes.MostrarMensaje("El servidor no devolvio categorias.");
                return;
            }

            int opcion;
            do
            {
                mensajes.MostrarTitulo("CATALOGO DE UNIDADES");
                for (int indice = 0; indice < categorias.Count; indice++)
                {
                    CategoriaModelo categoria = categorias[indice];
                    string detalle = categoria.Convertible ? "" : " (solo consulta)";
                    mensajes.MostrarMensaje((indice + 1) + ". " + categoria.Nombre + detalle);
                }
                mensajes.MostrarMensaje("0. Volver");
                opcion = EntradaUtil.LeerEntero("Seleccione una categoria: ");
                if (opcion >= 1 && opcion <= categorias.Count)
                    MostrarUnidades(categorias[opcion - 1]);
                else if (opcion != 0)
                    mensajes.MostrarError("Categoria no valida. Elija una opcion de la lista.");
            } while (opcion != 0);
        }

        private void MostrarUnidades(CategoriaModelo categoria)
        {
            List<UnidadModelo> unidades = catalogoControlador.ConsultarUnidades(categoria.Codigo);
            mensajes.MostrarTitulo("UNIDADES DE " + categoria.Nombre.ToUpperInvariant());
            foreach (UnidadModelo unidad in unidades)
                mensajes.MostrarMensaje(string.Format("{0,-5} {1} ({2})", unidad.Codigo, unidad.Nombre, unidad.Simbolo));
        }
    }
}

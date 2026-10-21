using WS_CON_UNI_DOTNET_SOAP.Modelo;

namespace WS_CON_UNI_DOTNET_SOAP.Servicios
{
    /// <summary>Valida categoría, unidades y valor de una solicitud (RQF-008).</summary>
    public class ServicioValidacion
    {
        public Categoria ValidarCategoria(string codigo)
        {
            return Categoria.DesdeCodigo(codigo) ?? throw new ExcepcionServicio(CodigoError.Data001);
        }

        /// <summary>Devuelve (origen, destino) si la solicitud es válida para la categoría.</summary>
        public (Unidad Origen, Unidad Destino) ValidarConversion(Categoria categoria, double? valor, string origen, string destino)
        {
            if (categoria == null || valor == null || double.IsNaN(valor.Value) || double.IsInfinity(valor.Value))
            {
                throw new ExcepcionServicio(CodigoError.Data001);
            }
            Unidad unidadOrigen = ValidarUnidad(origen, categoria);
            Unidad unidadDestino = ValidarUnidad(destino, categoria);
            if (!categoria.Convertible)
            {
                throw new ExcepcionServicio(CodigoError.Data001);
            }
            bool admiteNegativos = categoria == Categoria.Temperatura;
            if (!admiteNegativos && valor.Value < 0)
            {
                throw new ExcepcionServicio(CodigoError.Data001);
            }
            return (unidadOrigen, unidadDestino);
        }

        /// <summary>Una unidad desconocida o de otra categoría es UNIT-001.</summary>
        private static Unidad ValidarUnidad(string codigo, Categoria categoria)
        {
            Unidad unidad = Unidad.DesdeCodigo(codigo);
            if (unidad == null || unidad.Categoria != categoria)
            {
                throw new ExcepcionServicio(CodigoError.Unit001);
            }
            return unidad;
        }
    }
}

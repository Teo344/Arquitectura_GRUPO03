using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;

namespace ec.edu.monster.modelo.api
{
    public class RestCountriesMapperTest
    {
        private static readonly string PaginaEjemplo =
            File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "restcountries", "pagina.json"));

        private readonly RestCountriesMapper mapper = new RestCountriesMapper();

        [Fact]
        public void LeePaginaYDescartaPaisesIncompletos()
        {
            RestCountriesMapper.Pagina pagina = mapper.LeerPagina(PaginaEjemplo);

            Assert.Equal(3, pagina.Paises.Count); // sin código y con región desconocida se descartan
            Assert.Equal(5, pagina.Recibidos);    // el offset avanza con todos los objetos recibidos
            Assert.True(pagina.HayMas);
            Assert.False(pagina.EsDemo);
        }

        [Fact]
        public void MapeaTodosLosCamposDeUnPaisCompleto()
        {
            Pais ecuador = mapper.LeerPagina(PaginaEjemplo).Paises[0];

            Assert.True(ecuador.EsNuevo);
            Assert.Equal("ECU", ecuador.Codigo);
            Assert.Equal("Ecuador", ecuador.Nombre);
            Assert.Equal("Quito", ecuador.Capital);
            Assert.Equal(Region.AMERICAS, ecuador.Region);
            Assert.Equal("South America", ecuador.SubRegion);
            Assert.Equal(18_001_000L, ecuador.Poblacion);
            Assert.Equal(276_841d, ecuador.Area);
            Assert.Equal("United States dollar", ecuador.Moneda);
        }

        [Fact]
        public void EligeLaCapitalMarcadaComoPrincipal()
        {
            Pais bolivia = mapper.LeerPagina(PaginaEjemplo).Paises[1];

            Assert.Equal("La Paz", bolivia.Capital);
        }

        [Fact]
        public void AceptaPaisSinCapitalNiMoneda()
        {
            Pais antartida = mapper.LeerPagina(PaginaEjemplo).Paises[2];

            Assert.Equal(Region.ANTARTIDA, antartida.Region);
            Assert.Null(antartida.Capital);
            Assert.Null(antartida.Moneda);
            Assert.Null(antartida.SubRegion);
        }

        [Fact]
        public void AceptaMonedasComoMapaCapitalesComoTextoYAreaComoNumero()
        {
            string json = """
                {"data":{"objects":[{"names":{"common":"Peru"},"codes":{"alpha_3":"per"},
                "capitals":["Lima"],"region":"Americas","population":34000000,"area":1285216,
                "currencies":{"PEN":{"name":"Peruvian sol"}}}]}}
                """;

            Pais peru = mapper.LeerPagina(json).Paises[0];

            Assert.Equal("PER", peru.Codigo); // el código se normaliza a mayúsculas
            Assert.Equal("Lima", peru.Capital);
            Assert.Equal("Peruvian sol", peru.Moneda);
            Assert.Equal(1_285_216d, peru.Area);
        }

        [Fact]
        public void SinMetaNoHayMasPaginas()
        {
            RestCountriesMapper.Pagina pagina = mapper.LeerPagina("{\"data\":{\"objects\":[]}}");

            Assert.Empty(pagina.Paises);
            Assert.False(pagina.HayMas);
        }

        [Fact]
        public void DetectaRespuestaDeLaClaveDemo()
        {
            RestCountriesMapper.Pagina pagina = mapper.LeerPagina("{\"data\":{\"_demo\":{\"message\":\"demo\"},\"objects\":[]}}");

            Assert.True(pagina.EsDemo);
        }

        [Fact]
        public void JsonInvalidoLanzaExcepcionApi()
        {
            Assert.Throws<ExcepcionApiExterna>(() => mapper.LeerPagina("<html>no es json"));
        }

        [Fact]
        public void RespuestaDeLaVersionRetiradaLanzaExcepcionApi()
        {
            // Lo que responde hoy https://restcountries.com/v3.1/all
            string retirada = "{\"success\":false,\"data\":null,\"errors\":[{\"message\":\"This API version has been deprecated.\"}]}";

            Assert.Throws<ExcepcionApiExterna>(() => mapper.LeerPagina(retirada));
        }
    }
}

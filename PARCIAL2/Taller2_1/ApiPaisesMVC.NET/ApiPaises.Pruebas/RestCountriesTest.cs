using ec.edu.monster.config;
using ec.edu.monster.modelo.api;
using ec.edu.monster.modelo.dao;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using Xunit;

namespace ec.edu.monster.pruebas;

public class RestCountriesMapperTest
{
    private static readonly string PaginaEjemplo = File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "Recursos", "pagina.json"));
    private readonly RestCountriesMapper mapper = new();

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

        Assert.Equal(new Pais(null, "ECU", "Ecuador", "Quito", Region.Americas, 18_001_000, "United States dollar"), ecuador);
    }

    [Fact]
    public void EligeLaCapitalMarcadaComoPrincipal()
    {
        Assert.Equal("La Paz", mapper.LeerPagina(PaginaEjemplo).Paises[1].Capital);
    }

    [Fact]
    public void AceptaPaisSinCapitalNiMoneda()
    {
        Pais antartida = mapper.LeerPagina(PaginaEjemplo).Paises[2];

        Assert.Equal(Region.Antartida, antartida.Region);
        Assert.Null(antartida.Capital);
        Assert.Null(antartida.Moneda);
    }

    [Fact]
    public void AceptaMonedasComoMapaYCapitalesComoTexto()
    {
        const string json = """
            {"data":{"objects":[{"names":{"common":"Peru"},"codes":{"alpha_3":"per"},
            "capitals":["Lima"],"region":"Americas","population":34000000,
            "currencies":{"PEN":{"name":"Peruvian sol"}}}]}}
            """;

        Pais peru = mapper.LeerPagina(json).Paises[0];

        Assert.Equal("PER", peru.Codigo); // el código se normaliza a mayúsculas
        Assert.Equal("Lima", peru.Capital);
        Assert.Equal("Peruvian sol", peru.Moneda);
    }

    [Fact]
    public void SinMetaNoHayMasPaginas()
    {
        RestCountriesMapper.Pagina pagina = mapper.LeerPagina("""{"data":{"objects":[]}}""");

        Assert.Empty(pagina.Paises);
        Assert.False(pagina.HayMas);
    }

    [Fact]
    public void DetectaRespuestaDeLaClaveDemo()
    {
        Assert.True(mapper.LeerPagina("""{"data":{"_demo":{"message":"demo"},"objects":[]}}""").EsDemo);
    }

    [Fact]
    public void JsonInvalidoLanzaExcepcionApi()
    {
        Assert.Throws<ExcepcionApiExterna>(() => mapper.LeerPagina("<html>no es json"));
    }

    [Fact]
    public void RespuestaSinDataLanzaExcepcionApi()
    {
        Assert.Throws<ExcepcionApiExterna>(() => mapper.LeerPagina("""{"errors":[{"message":"API version deprecated"}]}"""));
    }
}

public class RestCountriesClienteTest
{
    private static Configuracion Config(string clave) =>
        new("Server=x", "https://api.restcountries.com/countries/v5", clave, TimeSpan.FromSeconds(5));

    [Fact]
    public void ExigeApiKey()
    {
        Assert.Throws<ArgumentException>(() => new RestCountriesCliente(new HttpClient(), Config(" "), new RestCountriesMapper()));
    }

    [Fact]
    public async Task NoEnviaBusquedasVaciasALaApi()
    {
        RestCountriesCliente cliente = new(new HttpClient(), Config("clave"), new RestCountriesMapper());

        await Assert.ThrowsAsync<ArgumentException>(() => cliente.BuscarPorNombreAsync("   "));
    }

    [Fact]
    public void DetectaLaClaveDemo()
    {
        Assert.True(new RestCountriesCliente(new HttpClient(), Config("rc_live_demo"), new RestCountriesMapper()).EsModoDemostracion);
        Assert.False(new RestCountriesCliente(new HttpClient(), Config("propia"), new RestCountriesMapper()).EsModoDemostracion);
    }

    [Theory]
    [InlineData(401, "API key")]
    [InlineData(403, "cuota")]
    [InlineData(429, "Espere")]
    [InlineData(503, "no está disponible")]
    public void TraduceLosCodigosHttpAMensajesClaros(int estado, string fragmento)
    {
        Assert.Contains(fragmento, RestCountriesCliente.MensajePorEstado(estado));
    }

    [Fact]
    public void EscapaLosComodinesDeLike()
    {
        Assert.Equal("100\\%", PaisDAOSqlServer.EscaparLike("100%"));
        Assert.Equal("a\\_b", PaisDAOSqlServer.EscaparLike("a_b"));
        Assert.Equal("\\[x]", PaisDAOSqlServer.EscaparLike("[x]"));
        Assert.Equal("Ecuador", PaisDAOSqlServer.EscaparLike("Ecuador"));
    }

    [Fact]
    public void LeeLaConfiguracionYExigeLosDatosObligatorios()
    {
        const string valido = """{"baseDatos":{"cadenaConexion":"Server=x"},"api":{"urlBase":"https://a","clave":"clave-secreta"}}""";
        Configuracion configuracion = Configuracion.Leer(new MemoryStream(System.Text.Encoding.UTF8.GetBytes(valido)));

        Assert.Equal(TimeSpan.FromSeconds(15), configuracion.ApiTimeout);
        Assert.DoesNotContain("clave-secreta", configuracion.ToString()); // la clave no llega a los logs
        Assert.Throws<InvalidOperationException>(() =>
            Configuracion.Leer(new MemoryStream(System.Text.Encoding.UTF8.GetBytes("""{"api":{"clave":"k"}}"""))));
    }
}

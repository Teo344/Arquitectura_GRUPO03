using ec.edu.monster.modelo.api;
using ec.edu.monster.modelo.dao;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador
{
    /// <summary>
    /// Prueba la conversión del formulario; no llega al servicio, por eso sus dependencias fallan si
    /// se usan.
    /// </summary>
    public class PaisControladorTest
    {
        private sealed class ApiNoUsar : IPaisApiCliente
        {
            public Task<IReadOnlyList<Pais>> ObtenerTodosAsync() => throw new InvalidOperationException("No se esperaba llamar a la API");
            public Task<IReadOnlyList<Pais>> BuscarPorNombreAsync(string nombre) => throw new InvalidOperationException("No se esperaba llamar a la API");
        }

        private sealed class DaoNoUsar : PaisDAOEnMemoria
        {
            public override Task<Pais> InsertarAsync(Pais pais) => throw new InvalidOperationException("No se esperaba usar el DAO");
        }

        private readonly PaisControlador controlador = new PaisControlador(new PaisServicio(new DaoNoUsar(), new ApiNoUsar()));

        [Fact]
        public void ConvierteElFormularioEnPais()
        {
            FormularioPais formulario = new FormularioPais(" ecu ", " Ecuador ", "Quito", "america", "18.001.000", "",
                " South America ", "276841,5");

            Pais pais = controlador.Convertir(7, formulario);

            Assert.Equal(7, pais.Id);
            Assert.Equal("ECU", pais.Codigo);
            Assert.Equal("Ecuador", pais.Nombre);
            Assert.Equal("Quito", pais.Capital);
            Assert.Equal(Region.AMERICAS, pais.Region);
            Assert.Equal("South America", pais.SubRegion);
            Assert.Equal(18_001_000L, pais.Poblacion);
            Assert.Equal(276_841.5, pais.Area);
            Assert.Null(pais.Moneda);
        }

        [Fact]
        public void AceptaRegionesEnEspanolConOSinTildeYEnIngles()
        {
            Assert.Equal(Region.OCEANIA, RegionExtensiones.DesdeTexto("Oceanía"));
            Assert.Equal(Region.OCEANIA, RegionExtensiones.DesdeTexto("oceania"));
            Assert.Equal(Region.EUROPA, RegionExtensiones.DesdeTexto("Europe"));
            Assert.Equal(Region.AFRICA, RegionExtensiones.DesdeTexto("ÁFRICA"));
            Assert.Equal(Region.AMERICAS, RegionExtensiones.DesdeTexto("AMERICAS"));
        }

        [Fact]
        public void ReuneLosErroresDeRegionPoblacionYArea()
        {
            FormularioPais formulario = new FormularioPais("ECU", "Ecuador", null, "Atlántida", "muchos", null, null, "grande");

            ExcepcionValidacion error = Assert.Throws<ExcepcionValidacion>(() => controlador.Convertir(null, formulario));

            Assert.Equal(3, error.Errores.Count);
        }

        [Fact]
        public void PoblacionAceptaSeparadoresDeMiles()
        {
            Assert.Equal(18_001_000L, PaisControlador.ConvertirPoblacion("18,001,000"));
            Assert.Equal(18_001_000L, PaisControlador.ConvertirPoblacion("18 001 000"));
            Assert.Throws<ExcepcionValidacion>(() => PaisControlador.ConvertirPoblacion(""));
            Assert.Throws<ExcepcionValidacion>(() => PaisControlador.ConvertirPoblacion("12abc"));
        }

        [Fact]
        public void AreaVaciaEsCeroYAceptaPuntoOComa()
        {
            Assert.Equal(0, PaisControlador.ConvertirArea("  "));
            Assert.Equal(0.44, PaisControlador.ConvertirArea("0,44"));
            Assert.Equal(1_285_216.2, PaisControlador.ConvertirArea("1 285 216.2"));
        }

        [Fact]
        public void CapitalVaciaQuedaNula()
        {
            Pais pais = controlador.Convertir(null, new FormularioPais("ECU", "Ecuador", "  ", "Asia", "1", null));

            Assert.Null(pais.Capital);
            Assert.True(pais.EsNuevo);
        }

        [Fact]
        public void FiltroIgnoraValoresDesconocidos()
        {
            FiltroPaises filtro = PaisControlador.Filtro("capital", "  quito ", "europa");
            FiltroPaises porDefecto = PaisControlador.Filtro("xyz", " ", "atlantida");

            Assert.Equal(new FiltroPaises(CampoBusqueda.CAPITAL, "quito", Region.EUROPA), filtro);
            Assert.True(porDefecto.EstaVacio);
            Assert.Equal(CampoBusqueda.NOMBRE, porDefecto.Campo);
        }
    }
}

using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;

namespace ec.edu.monster.modelo.api
{
    public class HipolabsMapperTest
    {
        private static readonly string ListaEjemplo =
            File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "hipolabs", "universidades.json"));

        private readonly HipolabsMapper mapper = new HipolabsMapper();

        [Fact]
        public void LeeNombreDominioSitioYPais()
        {
            IReadOnlyList<Universidad> universidades = mapper.LeerLista(ListaEjemplo);

            Assert.Equal(2, universidades.Count); // la que no tiene nombre se descarta
            Universidad epn = universidades[0];
            Assert.Equal("Escuela Politécnica Nacional", epn.Nombre);
            Assert.Equal("epn.edu.ec", epn.Dominio);
            Assert.Equal("http://www.epn.edu.ec/", epn.SitioWeb);
            Assert.Equal("Ecuador", epn.Pais);
        }

        [Fact]
        public void AceptaUniversidadSinDominioNiSitio()
        {
            Universidad sinDatos = mapper.LeerLista(ListaEjemplo)[1];

            Assert.Null(sinDatos.Dominio);
            Assert.Null(sinDatos.SitioWeb);
        }

        [Fact]
        public void RespuestaQueNoEsListaLanzaExcepcionApi()
        {
            Assert.Throws<ExcepcionApiExterna>(() => mapper.LeerLista("{\"error\":\"x\"}"));
            Assert.Throws<ExcepcionApiExterna>(() => mapper.LeerLista("<html>"));
        }
    }
}

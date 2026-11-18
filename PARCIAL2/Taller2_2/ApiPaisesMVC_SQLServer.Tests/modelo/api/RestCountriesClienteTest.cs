using ec.edu.monster.config;
using Microsoft.Extensions.Logging.Abstractions;

namespace ec.edu.monster.modelo.api
{
    public class RestCountriesClienteTest
    {
        private static RestCountriesCliente Crear(string clave)
        {
            Configuracion configuracion = new Configuracion
            {
                ApiUrlBase = "https://api.restcountries.com/countries/v5",
                ApiClave = clave,
                ApiTimeout = TimeSpan.FromSeconds(5),
                UniversidadesUrlBase = "http://universities.hipolabs.com"
            };
            return new RestCountriesCliente(new HttpClient(), configuracion, new RestCountriesMapper(),
                NullLogger<RestCountriesCliente>.Instance);
        }

        [Fact]
        public void ExigeApiKey()
        {
            Assert.Throws<ArgumentException>(() => Crear(" "));
        }

        [Fact]
        public async Task NoEnviaBusquedasVaciasALaApi()
        {
            RestCountriesCliente cliente = Crear("clave");

            await Assert.ThrowsAsync<ArgumentException>(() => cliente.BuscarPorNombreAsync("   "));
        }

        [Fact]
        public void DetectaLaClaveDemo()
        {
            Assert.True(Crear("rc_live_demo").EsModoDemostracion);
            Assert.False(Crear("clave").EsModoDemostracion);
        }

        [Fact]
        public void TraduceLosCodigosHttpAMensajesClaros()
        {
            Assert.Contains("API key", RestCountriesCliente.MensajePorEstado(401));
            Assert.Contains("cuota", RestCountriesCliente.MensajePorEstado(403));
            Assert.Contains("Espere", RestCountriesCliente.MensajePorEstado(429));
            Assert.Contains("no está disponible", RestCountriesCliente.MensajePorEstado(503));
        }
    }
}

package ec.edu.monster.modelo.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RestCountriesClienteTest {

    @Test
    void exigeApiKey() {
        assertThrows(IllegalArgumentException.class, () -> new RestCountriesCliente(
                "https://api.restcountries.com/countries/v5", " ", Duration.ofSeconds(5), new RestCountriesMapper()));
    }

    @Test
    void noEnviaBusquedasVaciasALaApi() {
        RestCountriesCliente cliente = new RestCountriesCliente(
                "https://api.restcountries.com/countries/v5", "clave", Duration.ofSeconds(5), new RestCountriesMapper());

        assertThrows(IllegalArgumentException.class, () -> cliente.buscarPorNombre("   "));
    }

    @Test
    void traduceLosCodigosHttpAMensajesClaros() {
        assertTrue(RestCountriesCliente.mensajePorEstado(401).contains("API key"));
        assertTrue(RestCountriesCliente.mensajePorEstado(403).contains("cuota"));
        assertTrue(RestCountriesCliente.mensajePorEstado(429).contains("Espere"));
        assertTrue(RestCountriesCliente.mensajePorEstado(503).contains("no está disponible"));
    }
}

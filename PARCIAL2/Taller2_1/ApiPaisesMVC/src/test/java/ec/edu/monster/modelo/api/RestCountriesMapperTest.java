package ec.edu.monster.modelo.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelo.api.RestCountriesMapper.Pagina;
import ec.edu.monster.modelo.entidad.Pais;
import ec.edu.monster.modelo.entidad.Region;
import ec.edu.monster.modelo.excepcion.ExcepcionApiExterna;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RestCountriesMapperTest {

    private static String paginaEjemplo;
    private final RestCountriesMapper mapper = new RestCountriesMapper();

    @BeforeAll
    static void cargarEjemplo() throws IOException {
        try (InputStream entrada = RestCountriesMapperTest.class.getResourceAsStream("/restcountries/pagina.json")) {
            paginaEjemplo = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void leePaginaYDescartaPaisesIncompletos() {
        Pagina pagina = mapper.leerPagina(paginaEjemplo);

        assertEquals(3, pagina.paises().size(), "sin código y con región desconocida se descartan");
        assertEquals(5, pagina.recibidos(), "el offset avanza con todos los objetos recibidos");
        assertTrue(pagina.hayMas());
        assertFalse(pagina.esDemo());
    }

    @Test
    void mapeaTodosLosCamposDeUnPaisCompleto() {
        Pais ecuador = mapper.leerPagina(paginaEjemplo).paises().get(0);

        assertNull(ecuador.id());
        assertEquals("ECU", ecuador.codigo());
        assertEquals("Ecuador", ecuador.nombre());
        assertEquals("Quito", ecuador.capital());
        assertEquals(Region.AMERICAS, ecuador.region());
        assertEquals(18_001_000L, ecuador.poblacion());
        assertEquals("United States dollar", ecuador.moneda());
    }

    @Test
    void eligeLaCapitalMarcadaComoPrincipal() {
        Pais bolivia = mapper.leerPagina(paginaEjemplo).paises().get(1);

        assertEquals("La Paz", bolivia.capital());
    }

    @Test
    void aceptaPaisSinCapitalNiMoneda() {
        Pais antartida = mapper.leerPagina(paginaEjemplo).paises().get(2);

        assertEquals(Region.ANTARTIDA, antartida.region());
        assertNull(antartida.capital());
        assertNull(antartida.moneda());
    }

    @Test
    void aceptaMonedasComoMapaYCapitalesComoTexto() {
        String json = """
                {"data":{"objects":[{"names":{"common":"Peru"},"codes":{"alpha_3":"per"},
                "capitals":["Lima"],"region":"Americas","population":34000000,
                "currencies":{"PEN":{"name":"Peruvian sol"}}}]}}
                """;

        Pais peru = mapper.leerPagina(json).paises().get(0);

        assertEquals("PER", peru.codigo(), "el código se normaliza a mayúsculas");
        assertEquals("Lima", peru.capital());
        assertEquals("Peruvian sol", peru.moneda());
    }

    @Test
    void sinMetaNoHayMasPaginas() {
        Pagina pagina = mapper.leerPagina("{\"data\":{\"objects\":[]}}");

        assertTrue(pagina.paises().isEmpty());
        assertFalse(pagina.hayMas());
    }

    @Test
    void detectaRespuestaDeLaClaveDemo() {
        Pagina pagina = mapper.leerPagina("{\"data\":{\"_demo\":{\"message\":\"demo\"},\"objects\":[]}}");

        assertTrue(pagina.esDemo());
    }

    @Test
    void jsonInvalidoLanzaExcepcionApi() {
        assertThrows(ExcepcionApiExterna.class, () -> mapper.leerPagina("<html>no es json"));
    }

    @Test
    void respuestaSinDataLanzaExcepcionApi() {
        assertThrows(ExcepcionApiExterna.class,
                () -> mapper.leerPagina("{\"errors\":[{\"message\":\"API version deprecated\"}]}"));
    }
}

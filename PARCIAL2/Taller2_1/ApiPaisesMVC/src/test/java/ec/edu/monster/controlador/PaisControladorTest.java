package ec.edu.monster.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ec.edu.monster.modelo.api.PaisApiCliente;
import ec.edu.monster.modelo.dao.PaisDAO;
import ec.edu.monster.modelo.entidad.Pais;
import ec.edu.monster.modelo.entidad.Region;
import ec.edu.monster.modelo.excepcion.ExcepcionValidacion;
import ec.edu.monster.servicio.PaisServicio;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;

/**
 * Prueba la conversión del formulario; no llega al servicio, por eso sus
 * dependencias son proxies que fallan si se usan.
 */
class PaisControladorTest {

    private final PaisControlador controlador = new PaisControlador(
            new PaisServicio(noUsar(PaisDAO.class), noUsar(PaisApiCliente.class)));

    @Test
    void convierteElFormularioEnPais() {
        FormularioPais formulario = new FormularioPais(" ecu ", " Ecuador ", "Quito", "america", "18.001.000", "");

        Pais pais = controlador.convertir(7, formulario);

        assertEquals(new Pais(7, "ECU", "Ecuador", "Quito", Region.AMERICAS, 18_001_000, null), pais);
    }

    @Test
    void aceptaRegionesEnEspanolConOSinTildeYEnIngles() {
        assertEquals(Region.OCEANIA, Region.desdeTexto("Oceanía"));
        assertEquals(Region.OCEANIA, Region.desdeTexto("oceania"));
        assertEquals(Region.EUROPA, Region.desdeTexto("Europe"));
        assertEquals(Region.AFRICA, Region.desdeTexto("ÁFRICA"));
    }

    @Test
    void reuneLosErroresDeRegionYPoblacion() {
        FormularioPais formulario = new FormularioPais("ECU", "Ecuador", null, "Atlántida", "muchos", null);

        ExcepcionValidacion error = assertThrows(ExcepcionValidacion.class,
                () -> controlador.convertir(null, formulario));

        assertEquals(2, error.getErrores().size(), error.getMessage());
    }

    @Test
    void poblacionAceptaSeparadoresDeMiles() {
        assertEquals(18_001_000L, PaisControlador.convertirPoblacion("18,001,000"));
        assertEquals(18_001_000L, PaisControlador.convertirPoblacion("18 001 000"));
        assertThrows(ExcepcionValidacion.class, () -> PaisControlador.convertirPoblacion(""));
        assertThrows(ExcepcionValidacion.class, () -> PaisControlador.convertirPoblacion("12abc"));
    }

    @Test
    void capitalVaciaQuedaNula() {
        Pais pais = controlador.convertir(null, new FormularioPais("ECU", "Ecuador", "  ", "Asia", "1", null));

        assertNull(pais.capital());
    }

    @SuppressWarnings("unchecked")
    private static <T> T noUsar(Class<T> tipo) {
        return (T) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, (proxy, metodo, argumentos) -> {
            throw new AssertionError("No se esperaba llamar a " + metodo.getName());
        });
    }
}

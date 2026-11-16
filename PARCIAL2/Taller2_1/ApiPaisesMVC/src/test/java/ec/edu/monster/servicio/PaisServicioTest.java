package ec.edu.monster.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.modelo.api.PaisApiCliente;
import ec.edu.monster.modelo.entidad.EstadisticasPaises;
import ec.edu.monster.modelo.entidad.EstadoSistema;
import ec.edu.monster.modelo.entidad.OrigenDatos;
import ec.edu.monster.modelo.entidad.Pais;
import ec.edu.monster.modelo.entidad.Region;
import ec.edu.monster.modelo.entidad.ResultadoBusqueda;
import ec.edu.monster.modelo.entidad.ResultadoImportacion;
import ec.edu.monster.modelo.excepcion.ExcepcionApiExterna;
import ec.edu.monster.modelo.excepcion.ExcepcionNoEncontrado;
import ec.edu.monster.modelo.excepcion.ExcepcionPersistencia;
import ec.edu.monster.modelo.excepcion.ExcepcionValidacion;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaisServicioTest {

    private static final Pais ECUADOR = Pais.nuevo("ECU", "Ecuador", "Quito", Region.AMERICAS, 18_000_000, "Dólar");
    private static final Pais PERU = Pais.nuevo("PER", "Perú", "Lima", Region.AMERICAS, 34_000_000, "Sol");
    private static final Pais ESPANA = Pais.nuevo("ESP", "España", "Madrid", Region.EUROPA, 48_000_000, "Euro");

    /** API falsa: devuelve la lista configurada o falla si se indica. */
    private static final class ApiFalsa implements PaisApiCliente {

        List<Pais> paises = new ArrayList<>();
        boolean caida = false;

        @Override
        public List<Pais> obtenerTodos() {
            fallarSiEstaCaida();
            return paises;
        }

        @Override
        public List<Pais> buscarPorNombre(String nombre) {
            fallarSiEstaCaida();
            return paises.stream().filter(p -> p.nombre().toLowerCase().contains(nombre.toLowerCase())).toList();
        }

        private void fallarSiEstaCaida() {
            if (caida) {
                throw new ExcepcionApiExterna("La API REST Countries no respondió a tiempo.");
            }
        }
    }

    private PaisDAOEnMemoria dao;
    private ApiFalsa api;
    private PaisServicio servicio;

    @BeforeEach
    void preparar() {
        dao = new PaisDAOEnMemoria();
        api = new ApiFalsa();
        servicio = new PaisServicio(dao, api);
    }

    // ---------------------------------------------------------- CU-01

    @Test
    void reimportarActualizaEnLugarDeDuplicar() {
        api.paises = List.of(ECUADOR, PERU);
        ResultadoImportacion primera = servicio.importarDesdeApi();

        Pais ecuadorActualizado = Pais.nuevo("ECU", "Ecuador", "Quito", Region.AMERICAS, 18_500_000, "Dólar");
        api.paises = List.of(ecuadorActualizado, PERU, ESPANA);
        ResultadoImportacion segunda = servicio.importarDesdeApi();

        assertEquals(new ResultadoImportacion(2, 2, 0), primera);
        assertEquals(new ResultadoImportacion(3, 1, 2), segunda);
        assertEquals(3, dao.cantidad());
        assertEquals(18_500_000, dao.porCodigo().get("ECU").poblacion());
    }

    @Test
    void importarOmiteInvalidosYCodigosRepetidos() {
        Pais sinNombre = Pais.nuevo("XXA", " ", null, Region.ASIA, 10, null);
        api.paises = List.of(ECUADOR, sinNombre, ECUADOR);

        ResultadoImportacion resultado = servicio.importarDesdeApi();

        assertEquals(1, resultado.recibidos());
        assertEquals(1, dao.cantidad());
    }

    @Test
    void siLaApiFallaLaImportacionNoTocaLaBase() {
        dao.insertar(ECUADOR);
        api.caida = true;

        assertThrows(ExcepcionApiExterna.class, servicio::importarDesdeApi);
        assertEquals(1, dao.cantidad());
    }

    // ---------------------------------------------------------- CU-02

    @Test
    void buscarPorNombreConsultaLaApi() {
        api.paises = List.of(ECUADOR, PERU);

        ResultadoBusqueda resultado = servicio.buscarPorNombre("  ecua ");

        assertEquals(OrigenDatos.API_EXTERNA, resultado.origen());
        assertEquals(List.of(ECUADOR), resultado.paises());
        assertNull(resultado.aviso());
    }

    @Test
    void siLaApiFallaLaBusquedaRespondeConLaBaseLocal() {
        dao.insertar(ECUADOR);
        api.caida = true;

        ResultadoBusqueda resultado = servicio.buscarPorNombre("ecua");

        assertEquals(OrigenDatos.BASE_LOCAL, resultado.origen());
        assertEquals("ECU", resultado.paises().get(0).codigo());
        assertTrue(resultado.aviso().contains("guardados localmente"));
    }

    @Test
    void busquedaVaciaSeRechazaSinLlamarALaApi() {
        api.caida = true;

        assertThrows(ExcepcionValidacion.class, () -> servicio.buscarPorNombre("  "));
    }

    // ---------------------------------------------------------- CU-03 a CU-06

    @Test
    void registrarAsignaId() {
        Pais registrado = servicio.registrar(ECUADOR);

        assertNotNull(registrado.id());
        assertEquals(registrado, servicio.obtenerPorId(registrado.id()));
    }

    @Test
    void registrarReuneTodosLosErrores() {
        Pais invalido = Pais.nuevo("EC", "", null, null, -1, null);

        ExcepcionValidacion error = assertThrows(ExcepcionValidacion.class, () -> servicio.registrar(invalido));

        assertEquals(4, error.getErrores().size(), error.getMessage());
        assertEquals(0, dao.cantidad());
    }

    @Test
    void registrarRechazaCodigoDuplicado() {
        servicio.registrar(ECUADOR);

        assertThrows(ExcepcionValidacion.class, () -> servicio.registrar(ECUADOR));
    }

    @Test
    void actualizarCambiaLosDatos() {
        Pais registrado = servicio.registrar(ECUADOR);

        servicio.actualizar(new Pais(registrado.id(), "ECU", "Ecuador", "Guayaquil", Region.AMERICAS, 18_000_000, "Dólar"));

        assertEquals("Guayaquil", servicio.obtenerPorId(registrado.id()).capital());
    }

    @Test
    void actualizarRechazaElCodigoDeOtroPais() {
        servicio.registrar(ECUADOR);
        Pais peru = servicio.registrar(PERU);

        Pais conCodigoAjeno = new Pais(peru.id(), "ECU", "Perú", "Lima", Region.AMERICAS, 1, null);

        assertThrows(ExcepcionValidacion.class, () -> servicio.actualizar(conCodigoAjeno));
    }

    @Test
    void actualizarUnIdInexistenteFalla() {
        assertThrows(ExcepcionNoEncontrado.class, () -> servicio.actualizar(ECUADOR.conId(99)));
    }

    @Test
    void eliminarUnIdInexistenteFalla() {
        assertThrows(ExcepcionNoEncontrado.class, () -> servicio.eliminar(99));
    }

    // ---------------------------------------------------------- CU-07 y CU-08

    @Test
    void buscarPorRegionFiltra() {
        servicio.registrar(ECUADOR);
        servicio.registrar(ESPANA);

        assertEquals(List.of("España"),
                servicio.buscarPorRegion(Region.EUROPA).stream().map(Pais::nombre).toList());
        assertThrows(ExcepcionValidacion.class, () -> servicio.buscarPorRegion(null));
    }

    @Test
    void generaEstadisticas() {
        servicio.registrar(ESPANA);
        servicio.registrar(ECUADOR);
        servicio.registrar(PERU);

        EstadisticasPaises estadisticas = servicio.generarEstadisticas();

        assertEquals(3, estadisticas.totalPaises());
        assertEquals(List.of(Region.AMERICAS, Region.EUROPA), List.copyOf(estadisticas.paisesPorRegion().keySet()));
        assertEquals(2L, estadisticas.paisesPorRegion().get(Region.AMERICAS));
        assertEquals(Region.AMERICAS, estadisticas.regionConMasPaises());
        assertEquals("ESP", estadisticas.paisMasPoblado().codigo());
    }

    @Test
    void estadoInformaLaBaseDisponible() {
        servicio.registrar(ECUADOR);

        EstadoSistema estado = servicio.verificarEstado();

        assertTrue(estado.baseDisponible());
        assertEquals(1, estado.totalPaises());
        assertNull(estado.detalleBase());
    }

    @Test
    void estadoInformaLaBaseCaidaSinLanzarExcepcion() {
        PaisDAOEnMemoria caida = new PaisDAOEnMemoria() {
            @Override
            public long contar() {
                throw new ExcepcionPersistencia("No se pudo conectar a la base de datos.", null);
            }
        };

        EstadoSistema estado = new PaisServicio(caida, api).verificarEstado();

        assertFalse(estado.baseDisponible());
        assertEquals("No se pudo conectar a la base de datos.", estado.detalleBase());
    }

    @Test
    void estadisticasSinPaises() {
        EstadisticasPaises estadisticas = servicio.generarEstadisticas();

        assertTrue(estadisticas.estaVacio());
        assertNull(estadisticas.paisMasPoblado());
        assertNull(estadisticas.regionConMasPaises());
    }
}

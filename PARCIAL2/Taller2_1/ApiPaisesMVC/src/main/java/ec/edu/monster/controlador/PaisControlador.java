package ec.edu.monster.controlador;

import ec.edu.monster.modelo.entidad.Pais;
import ec.edu.monster.modelo.entidad.Region;
import ec.edu.monster.modelo.entidad.ResultadoBusqueda;
import ec.edu.monster.modelo.entidad.ResultadoImportacion;
import ec.edu.monster.modelo.excepcion.ExcepcionValidacion;
import ec.edu.monster.servicio.PaisServicio;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Controlador de la gestión de países (CU-01 a CU-07). Recibe las acciones de
 * la vista, convierte la entrada de texto en objetos del modelo y delega en
 * {@link PaisServicio}. No contiene SQL, JSON ni reglas de negocio.
 * <p>
 * Las fallas llegan a la vista como ExcepcionAplicacion con un mensaje listo
 * para mostrar. Las operaciones que usan la red o la base de datos pueden
 * tardar: la vista Swing debe llamarlas desde un SwingWorker, no desde el
 * hilo de eventos (EDT).
 *
 * @author carlo
 */
public class PaisControlador {

    private final PaisServicio servicio;

    public PaisControlador(PaisServicio servicio) {
        this.servicio = Objects.requireNonNull(servicio, "servicio");
    }

    /** CU-01 */
    public ResultadoImportacion importarPaises() {
        return servicio.importarDesdeApi();
    }

    /** CU-02 (Actividad 1) */
    public ResultadoBusqueda buscarEnApi(String nombre) {
        return servicio.buscarPorNombre(nombre);
    }

    /** CU-02: guarda un país elegido de los resultados de la búsqueda. */
    public ResultadoImportacion guardarResultado(Pais pais) {
        return servicio.guardarResultado(pais);
    }

    /** CU-03 */
    public Pais registrarPais(FormularioPais formulario) {
        return servicio.registrar(convertir(null, formulario));
    }

    /** CU-04 (Actividad 3: datos de la JTable) */
    public List<Pais> listarPaises() {
        return servicio.listarTodos();
    }

    /** CU-04 */
    public Pais obtenerPais(int id) {
        return servicio.obtenerPorId(id);
    }

    /** CU-05 */
    public Pais actualizarPais(int id, FormularioPais formulario) {
        return servicio.actualizar(convertir(id, formulario));
    }

    /** CU-06 */
    public void eliminarPais(int id) {
        servicio.eliminar(id);
    }

    /** CU-07 (Actividad 4) */
    public List<Pais> buscarPorRegion(Region region) {
        return servicio.buscarPorRegion(region);
    }

    /** Opciones para el combo de regiones. */
    public List<Region> regiones() {
        return List.of(Region.values());
    }

    /**
     * Convierte el formulario en un Pais. Reúne los errores de formato de
     * región y población; las demás reglas las valida el servicio.
     */
    Pais convertir(Integer id, FormularioPais formulario) {
        Objects.requireNonNull(formulario, "formulario");
        List<String> errores = new ArrayList<>();

        Region region = null;
        try {
            region = Region.desdeTexto(formulario.region());
        } catch (ExcepcionValidacion e) {
            errores.addAll(e.getErrores());
        }

        long poblacion = 0;
        try {
            poblacion = convertirPoblacion(formulario.poblacion());
        } catch (ExcepcionValidacion e) {
            errores.addAll(e.getErrores());
        }

        if (!errores.isEmpty()) {
            throw new ExcepcionValidacion(errores);
        }
        return new Pais(id, formulario.codigo(), formulario.nombre(), formulario.capital(),
                region, poblacion, formulario.moneda());
    }

    /**
     * Acepta separadores de miles comunes ("18.001.000", "18,001,000",
     * "18 001 000"): la población siempre es un número entero.
     */
    static long convertirPoblacion(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ExcepcionValidacion("La población es obligatoria.");
        }
        String digitos = texto.strip().replaceAll("[\\s.,'_]", "");
        try {
            return Long.parseLong(digitos);
        } catch (NumberFormatException e) {
            throw new ExcepcionValidacion("La población debe ser un número entero.");
        }
    }
}

package ec.edu.monster.servicio;

import ec.edu.monster.modelo.api.PaisApiCliente;
import ec.edu.monster.modelo.dao.PaisDAO;
import ec.edu.monster.modelo.entidad.EstadisticasPaises;
import ec.edu.monster.modelo.entidad.EstadoSistema;
import ec.edu.monster.modelo.entidad.Pais;
import ec.edu.monster.modelo.entidad.Region;
import ec.edu.monster.modelo.entidad.ResultadoBusqueda;
import ec.edu.monster.modelo.entidad.ResultadoImportacion;
import ec.edu.monster.modelo.excepcion.ExcepcionApiExterna;
import ec.edu.monster.modelo.excepcion.ExcepcionNoEncontrado;
import ec.edu.monster.modelo.excepcion.ExcepcionPersistencia;
import ec.edu.monster.modelo.excepcion.ExcepcionValidacion;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Reglas de negocio de los países: valida, orquesta la API y la base de datos
 * y arma los reportes. Recibe sus dependencias por constructor como
 * interfaces, así se prueba con dobles en memoria sin red ni MySQL.
 *
 * @author carlo
 */
public class PaisServicio {

    private static final Logger LOG = Logger.getLogger(PaisServicio.class.getName());

    /** Cuántos países muestra el ranking de los más poblados. */
    static final int TOP_POBLADOS = 5;

    /** Tamaño de las columnas VARCHAR(100) de la tabla pais. */
    static final int LONGITUD_MAXIMA = 100;
    private static final Pattern CODIGO_ISO = Pattern.compile("[A-Z]{3}");

    private final PaisDAO dao;
    private final PaisApiCliente api;

    public PaisServicio(PaisDAO dao, PaisApiCliente api) {
        this.dao = Objects.requireNonNull(dao, "dao");
        this.api = Objects.requireNonNull(api, "api");
    }

    /**
     * Comprueba la base de datos e informa cómo está configurada la API. No
     * consulta la API para no gastar la cuota mensual de solicitudes.
     */
    public EstadoSistema verificarEstado() {
        boolean demostracion = api.esModoDemostracion();
        try {
            return new EstadoSistema(true, dao.contar(), null, demostracion);
        } catch (ExcepcionPersistencia e) {
            return new EstadoSistema(false, 0, e.getMessage(), demostracion);
        }
    }

    /**
     * CU-01: trae todos los países de la API y los guarda. Reimportar no
     * duplica: los países ya registrados (mismo código) se actualizan.
     *
     * @throws ExcepcionApiExterna si la API falla; en ese caso la base no se toca
     */
    public ResultadoImportacion importarDesdeApi() {
        Map<String, Pais> porCodigo = new LinkedHashMap<>();
        for (Pais pais : api.obtenerTodos()) {
            List<String> errores = validar(pais);
            if (errores.isEmpty()) {
                porCodigo.putIfAbsent(pais.codigo(), pais);
            } else {
                LOG.log(Level.WARNING, "Se omite {0} de la importación: {1}", new Object[]{pais.nombre(), errores});
            }
        }
        return dao.guardarOActualizar(List.copyOf(porCodigo.values()));
    }

    /**
     * CU-02 (Actividad 1): busca en la API. Si la API no está disponible,
     * responde con los países ya guardados (patrón fallback) e indica el
     * motivo en el resultado.
     */
    public ResultadoBusqueda buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ExcepcionValidacion("Ingrese el nombre del país a buscar.");
        }
        String texto = nombre.strip();
        try {
            return ResultadoBusqueda.desdeApi(api.buscarPorNombre(texto));
        } catch (ExcepcionApiExterna e) {
            LOG.log(Level.WARNING, "API no disponible, se responde con la base local: {0}", e.getMessage());
            return ResultadoBusqueda.desdeBaseLocal(dao.buscarPorNombre(texto),
                    e.getMessage() + " Se muestran los países guardados localmente.");
        }
    }

    /**
     * CU-02 (opcional): guarda un resultado de la búsqueda en la API. Si el
     * código ya existe, actualiza sus datos.
     */
    public ResultadoImportacion guardarResultado(Pais pais) {
        validarOLanzar(pais);
        return dao.guardarOActualizar(List.of(pais));
    }

    /**
     * CU-03: registra un país ingresado por el usuario.
     */
    public Pais registrar(Pais pais) {
        Objects.requireNonNull(pais, "pais");
        if (!pais.esNuevo()) {
            throw new ExcepcionValidacion("Un país nuevo no debe tener id.");
        }
        validarOLanzar(pais);
        if (dao.buscarPorCodigo(pais.codigo()).isPresent()) {
            throw codigoDuplicado(pais.codigo());
        }
        return dao.insertar(pais);
    }

    /**
     * CU-04: país por id.
     */
    public Pais obtenerPorId(int id) {
        return dao.buscarPorId(id).orElseThrow(() -> noEncontrado(id));
    }

    /**
     * CU-04 (Actividad 3): todos los países, ordenados por nombre.
     */
    public List<Pais> listarTodos() {
        return dao.listarTodos();
    }

    /**
     * CU-05: actualiza un país existente.
     */
    public Pais actualizar(Pais pais) {
        Objects.requireNonNull(pais, "pais");
        if (pais.esNuevo()) {
            throw new ExcepcionValidacion("Seleccione el país que desea actualizar.");
        }
        validarOLanzar(pais);
        boolean codigoDeOtroPais = dao.buscarPorCodigo(pais.codigo())
                .filter(otro -> !otro.id().equals(pais.id()))
                .isPresent();
        if (codigoDeOtroPais) {
            throw codigoDuplicado(pais.codigo());
        }
        if (!dao.actualizar(pais)) {
            throw noEncontrado(pais.id());
        }
        return pais;
    }

    /**
     * CU-06: elimina un país.
     */
    public void eliminar(int id) {
        if (!dao.eliminar(id)) {
            throw noEncontrado(id);
        }
    }

    /**
     * CU-07 (Actividad 4): países de una región.
     */
    public List<Pais> buscarPorRegion(Region region) {
        if (region == null) {
            throw new ExcepcionValidacion("Seleccione una región.");
        }
        return dao.buscarPorRegion(region);
    }

    /**
     * CU-08 (Actividad 5): países por región, los cinco países más poblados
     * y la región con más países.
     */
    public EstadisticasPaises generarEstadisticas() {
        // Se ordena aquí (y no se confía en el orden del DAO): mayor cantidad
        // primero y, ante empate, el orden del enum
        Map<Region, Long> porRegion = new LinkedHashMap<>();
        dao.contarPorRegion().entrySet().stream()
                .sorted(Map.Entry.<Region, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(e -> porRegion.put(e.getKey(), e.getValue()));

        long total = porRegion.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) {
            return new EstadisticasPaises(Map.of(), 0, List.of(), null);
        }
        Region regionConMasPaises = porRegion.keySet().iterator().next();
        return new EstadisticasPaises(porRegion, total, dao.listarMasPoblados(TOP_POBLADOS), regionConMasPaises);
    }

    // ------------------------------------------------------------ validación

    /**
     * Reglas de un país válido. Devuelve todos los errores encontrados (lista
     * vacía si es válido).
     */
    List<String> validar(Pais pais) {
        List<String> errores = new ArrayList<>();
        if (pais.codigo() == null || !CODIGO_ISO.matcher(pais.codigo()).matches()) {
            errores.add("El código debe tener 3 letras (ISO alfa-3, p. ej. ECU).");
        }
        if (pais.nombre() == null) {
            errores.add("El nombre es obligatorio.");
        }
        if (pais.region() == null) {
            errores.add("La región es obligatoria.");
        }
        if (pais.poblacion() < 0) {
            errores.add("La población no puede ser negativa.");
        }
        validarLongitud(pais.nombre(), "El nombre", errores);
        validarLongitud(pais.capital(), "La capital", errores);
        validarLongitud(pais.moneda(), "La moneda", errores);
        return errores;
    }

    private void validarOLanzar(Pais pais) {
        Objects.requireNonNull(pais, "pais");
        List<String> errores = validar(pais);
        if (!errores.isEmpty()) {
            throw new ExcepcionValidacion(errores);
        }
    }

    private static void validarLongitud(String valor, String campo, List<String> errores) {
        if (valor != null && valor.length() > LONGITUD_MAXIMA) {
            errores.add(campo + " no puede superar " + LONGITUD_MAXIMA + " caracteres.");
        }
    }

    private static ExcepcionValidacion codigoDuplicado(String codigo) {
        return new ExcepcionValidacion("Ya existe un país con el código " + codigo + ".");
    }

    private static ExcepcionNoEncontrado noEncontrado(int id) {
        return new ExcepcionNoEncontrado("No existe un país con id " + id + ".");
    }
}

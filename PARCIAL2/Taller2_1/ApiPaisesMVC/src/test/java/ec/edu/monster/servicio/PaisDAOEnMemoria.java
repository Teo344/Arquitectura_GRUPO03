package ec.edu.monster.servicio;

import ec.edu.monster.modelo.dao.PaisDAO;
import ec.edu.monster.modelo.entidad.Pais;
import ec.edu.monster.modelo.entidad.Region;
import ec.edu.monster.modelo.entidad.ResultadoImportacion;
import ec.edu.monster.modelo.excepcion.ExcepcionValidacion;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Doble de prueba de {@link PaisDAO}: guarda los países en un mapa. Gracias a
 * que el servicio depende de la interfaz, las pruebas no necesitan MySQL.
 */
class PaisDAOEnMemoria implements PaisDAO {

    private final Map<Integer, Pais> filas = new TreeMap<>();
    private int secuencia = 0;

    @Override
    public Pais insertar(Pais pais) {
        if (buscarPorCodigo(pais.codigo()).isPresent()) {
            throw new ExcepcionValidacion("Ya existe un país con el código " + pais.codigo() + ".");
        }
        Pais guardado = pais.conId(++secuencia);
        filas.put(guardado.id(), guardado);
        return guardado;
    }

    @Override
    public boolean actualizar(Pais pais) {
        return filas.replace(pais.id(), pais) != null;
    }

    @Override
    public boolean eliminar(int id) {
        return filas.remove(id) != null;
    }

    @Override
    public Optional<Pais> buscarPorId(int id) {
        return Optional.ofNullable(filas.get(id));
    }

    @Override
    public Optional<Pais> buscarPorCodigo(String codigo) {
        return filas.values().stream().filter(p -> p.codigo().equals(codigo)).findFirst();
    }

    @Override
    public List<Pais> listarTodos() {
        return filas.values().stream().sorted(Comparator.comparing(Pais::nombre)).toList();
    }

    @Override
    public List<Pais> buscarPorRegion(Region region) {
        return listarTodos().stream().filter(p -> p.region() == region).toList();
    }

    @Override
    public List<Pais> buscarPorNombre(String fragmento) {
        String buscado = fragmento.toLowerCase(Locale.ROOT);
        return listarTodos().stream()
                .filter(p -> p.nombre().toLowerCase(Locale.ROOT).contains(buscado))
                .toList();
    }

    @Override
    public ResultadoImportacion guardarOActualizar(List<Pais> paises) {
        int nuevos = 0;
        for (Pais pais : paises) {
            Optional<Pais> existente = buscarPorCodigo(pais.codigo());
            if (existente.isPresent()) {
                filas.put(existente.get().id(), pais.conId(existente.get().id()));
            } else {
                insertar(pais);
                nuevos++;
            }
        }
        return new ResultadoImportacion(paises.size(), nuevos, paises.size() - nuevos);
    }

    @Override
    public Map<Region, Long> contarPorRegion() {
        // Sin orden a propósito: el servicio es quien ordena
        return filas.values().stream()
                .collect(Collectors.groupingBy(Pais::region, LinkedHashMap::new, Collectors.counting()));
    }

    @Override
    public Optional<Pais> obtenerMasPoblado() {
        return filas.values().stream().max(Comparator.comparingLong(Pais::poblacion));
    }

    @Override
    public long contar() {
        return filas.size();
    }

    int cantidad() {
        return filas.size();
    }

    Map<String, Pais> porCodigo() {
        return filas.values().stream().collect(Collectors.toMap(Pais::codigo, Function.identity()));
    }
}

package ec.edu.monster.modelo.entidad;

import ec.edu.monster.modelo.excepcion.ExcepcionValidacion;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Regiones que maneja REST Countries. Cada constante guarda el valor en inglés
 * que envía la API y el nombre en español que verá el usuario. En la base de
 * datos se guarda {@link #name()}, que no cambia aunque cambie la traducción.
 *
 * @author carlo
 */
public enum Region {

    AFRICA("Africa", "África"),
    AMERICAS("Americas", "América"),
    ANTARTIDA("Antarctic", "Antártida"),
    ASIA("Asia", "Asia"),
    EUROPA("Europe", "Europa"),
    OCEANIA("Oceania", "Oceanía");

    private final String valorApi;
    private final String nombre;

    Region(String valorApi, String nombre) {
        this.valorApi = valorApi;
        this.nombre = nombre;
    }

    public String getValorApi() {
        return valorApi;
    }

    public String getNombre() {
        return nombre;
    }

    /**
     * Traduce el valor de la API. Devuelve vacío si la API envía una región
     * desconocida, para que quien llama decida si descarta el registro.
     */
    public static Optional<Region> desdeApi(String valor) {
        if (valor == null) {
            return Optional.empty();
        }
        String buscado = valor.trim();
        return Arrays.stream(values())
                .filter(r -> r.valorApi.equalsIgnoreCase(buscado))
                .findFirst();
    }

    /**
     * Interpreta lo que escribe o elige el usuario: acepta el nombre en
     * español (con o sin tildes), el valor de la API o el nombre de la
     * constante.
     *
     * @throws ExcepcionValidacion si el texto no corresponde a ninguna región
     */
    public static Region desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ExcepcionValidacion("La región es obligatoria.");
        }
        String buscado = normalizar(texto);
        return Arrays.stream(values())
                .filter(r -> normalizar(r.nombre).equals(buscado)
                || normalizar(r.valorApi).equals(buscado)
                || normalizar(r.name()).equals(buscado))
                .findFirst()
                .orElseThrow(() -> new ExcepcionValidacion("Región desconocida: " + texto.trim()));
    }

    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toUpperCase(Locale.ROOT);
    }

    /**
     * Nombre en español: es lo que muestran directamente JComboBox y JTable.
     */
    @Override
    public String toString() {
        return nombre;
    }
}

package ec.edu.monster.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

/**
 * Parámetros externos de la aplicación, leídos de {@code config.properties}.
 * Sacarlos del código evita credenciales escritas en las clases (el enunciado
 * tenía root/12345 en Conexion) y permite cambiar de entorno sin recompilar.
 *
 * @author carlo
 */
public record Configuracion(String dbUrl, String dbUsuario, String dbClave,
        String apiUrlBase, String apiClave, Duration apiTimeout) {

    public static final String ARCHIVO = "/config.properties";
    private static final int TIMEOUT_POR_DEFECTO = 15;

    /**
     * @throws IllegalStateException si falta el archivo o una clave obligatoria
     */
    public static Configuracion cargar() {
        Properties propiedades = new Properties();
        try (InputStream entrada = Configuracion.class.getResourceAsStream(ARCHIVO)) {
            if (entrada == null) {
                throw new IllegalStateException("No se encontró " + ARCHIVO.substring(1)
                        + ". Copie src/main/resources/config.properties.example como config.properties y complételo.");
            }
            propiedades.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + ARCHIVO, e);
        }
        return desde(propiedades);
    }

    static Configuracion desde(Properties propiedades) {
        return new Configuracion(
                requerido(propiedades, "db.url"),
                requerido(propiedades, "db.usuario"),
                propiedades.getProperty("db.clave", ""),
                requerido(propiedades, "api.url.base"),
                requerido(propiedades, "api.clave"),
                Duration.ofSeconds(entero(propiedades, "api.timeout.segundos", TIMEOUT_POR_DEFECTO)));
    }

    private static String requerido(Properties propiedades, String clave) {
        String valor = propiedades.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la propiedad " + clave + " en config.properties");
        }
        return valor.strip();
    }

    private static int entero(Properties propiedades, String clave, int porDefecto) {
        String valor = propiedades.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        try {
            int numero = Integer.parseInt(valor.strip());
            if (numero <= 0) {
                throw new NumberFormatException();
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("La propiedad " + clave + " debe ser un entero positivo", e);
        }
    }

    /**
     * No muestra las claves si el objeto llega a imprimirse en un log.
     */
    @Override
    public String toString() {
        return "Configuracion[dbUrl=" + dbUrl + ", dbUsuario=" + dbUsuario
                + ", apiUrlBase=" + apiUrlBase + ", apiTimeout=" + apiTimeout + "]";
    }
}

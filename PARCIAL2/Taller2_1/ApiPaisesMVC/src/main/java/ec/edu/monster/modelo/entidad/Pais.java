package ec.edu.monster.modelo.entidad;

import java.util.Locale;

/**
 * País almacenado en la tabla {@code pais}. Es inmutable: para cambiar un dato
 * se crea otra instancia, así ningún componente modifica por sorpresa un
 * objeto que otra capa sigue usando (p. ej. el modelo de una JTable).
 * <p>
 * El constructor solo normaliza los textos (recorta espacios, código en
 * mayúsculas, vacíos como {@code null}); las reglas de negocio se validan en
 * PaisServicio.
 *
 * @param id        clave generada por la base de datos; {@code null} si aún no se guarda
 * @param codigo    código ISO 3166-1 alfa-3 (p. ej. ECU), único
 * @param nombre    nombre común
 * @param capital   capital principal; {@code null} si no tiene
 * @param region    región geográfica
 * @param poblacion número de habitantes
 * @param moneda    nombre de la moneda principal; {@code null} si no tiene
 * @author carlo
 */
public record Pais(Integer id, String codigo, String nombre, String capital,
        Region region, long poblacion, String moneda) {

    public Pais {
        codigo = limpiar(codigo);
        if (codigo != null) {
            codigo = codigo.toUpperCase(Locale.ROOT);
        }
        nombre = limpiar(nombre);
        capital = limpiar(capital);
        moneda = limpiar(moneda);
    }

    /**
     * País que todavía no existe en la base de datos.
     */
    public static Pais nuevo(String codigo, String nombre, String capital,
            Region region, long poblacion, String moneda) {
        return new Pais(null, codigo, nombre, capital, region, poblacion, moneda);
    }

    public Pais conId(int nuevoId) {
        return new Pais(nuevoId, codigo, nombre, capital, region, poblacion, moneda);
    }

    public boolean esNuevo() {
        return id == null;
    }

    private static String limpiar(String texto) {
        if (texto == null) {
            return null;
        }
        String recortado = texto.strip();
        return recortado.isEmpty() ? null : recortado;
    }
}

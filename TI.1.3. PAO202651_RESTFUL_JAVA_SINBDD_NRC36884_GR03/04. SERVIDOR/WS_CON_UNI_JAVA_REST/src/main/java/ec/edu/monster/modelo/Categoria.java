package ec.edu.monster.modelo;

import java.util.Locale;
import java.util.Optional;

/**
 * Categorías de unidades del catálogo (RQF-002). Solo las convertibles tienen
 * operación de conversión.
 *
 * @author carlo
 */
public enum Categoria {

    LONGITUD("Longitud", true),
    MASA("Peso (masa)", true),
    VOLUMEN("Volumen", false),
    TIEMPO("Tiempo", false),
    TEMPERATURA("Temperatura", true),
    AREA("Área", false);

    private final String nombre;
    private final boolean convertible;

    Categoria(String nombre, boolean convertible) {
        this.nombre = nombre;
        this.convertible = convertible;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isConvertible() {
        return convertible;
    }

    /**
     * Busca la categoría por el nombre del enum, ignorando espacios y
     * mayúsculas.
     */
    public static Optional<Categoria> desdeCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        String normalizado = codigo.trim().toUpperCase(Locale.ROOT);
        for (Categoria categoria : values()) {
            if (categoria.name().equals(normalizado)) {
                return Optional.of(categoria);
            }
        }
        return Optional.empty();
    }
}

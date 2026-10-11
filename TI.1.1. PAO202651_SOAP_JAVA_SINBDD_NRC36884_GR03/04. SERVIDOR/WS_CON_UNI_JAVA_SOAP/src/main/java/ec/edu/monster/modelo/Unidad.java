package ec.edu.monster.modelo;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Unidades de todas las categorías (RQF-003). El factor lleva la unidad a la
 * base de su categoría; en temperatura es {@code null} porque se usan fórmulas.
 * Los códigos son únicos en todo el enum.
 *
 * @author carlo
 */
public enum Unidad {

    // Longitud (base: metro)
    M("M", "Metro", "m", Categoria.LONGITUD, 1.0),
    CM("CM", "Centímetro", "cm", Categoria.LONGITUD, 0.01),
    KM("KM", "Kilómetro", "km", Categoria.LONGITUD, 1000.0),
    FT("FT", "Pie", "ft", Categoria.LONGITUD, 0.3048),
    YD("YD", "Yarda", "yd", Categoria.LONGITUD, 0.9144),
    MI("MI", "Milla", "mi", Categoria.LONGITUD, 1609.344),

    // Masa (base: kilogramo)
    G("G", "Gramo", "g", Categoria.MASA, 0.001),
    KG("KG", "Kilogramo", "kg", Categoria.MASA, 1.0),
    OZ("OZ", "Onza", "oz", Categoria.MASA, 0.028349523125),
    LB("LB", "Libra", "lb", Categoria.MASA, 0.45359237),
    QQ("QQ", "Quintal", "qq", Categoria.MASA, 45.359237),

    // Temperatura (vía Celsius, sin factor)
    C("C", "Celsius", "°C", Categoria.TEMPERATURA, null),
    K("K", "Kelvin", "K", Categoria.TEMPERATURA, null),
    F("F", "Fahrenheit", "°F", Categoria.TEMPERATURA, null),
    N("N", "Newton", "°N", Categoria.TEMPERATURA, null),
    RE("RE", "Réaumur", "°Ré", Categoria.TEMPERATURA, null),

    // Volumen (base: litro), solo catálogo
    L("L", "Litro", "L", Categoria.VOLUMEN, 1.0),
    ML("ML", "Mililitro", "mL", Categoria.VOLUMEN, 0.001),
    M3("M3", "Metro cúbico", "m³", Categoria.VOLUMEN, 1000.0),
    GAL("GAL", "Galón (US)", "gal", Categoria.VOLUMEN, 3.785411784),
    FT3("FT3", "Pie cúbico", "ft³", Categoria.VOLUMEN, 28.316846592),

    // Tiempo (base: segundo), solo catálogo
    S("S", "Segundo", "s", Categoria.TIEMPO, 1.0),
    MIN("MIN", "Minuto", "min", Categoria.TIEMPO, 60.0),
    H("H", "Hora", "h", Categoria.TIEMPO, 3600.0),
    D("D", "Día", "d", Categoria.TIEMPO, 86400.0),
    SEM("SEM", "Semana", "sem", Categoria.TIEMPO, 604800.0),

    // Área (base: metro cuadrado), solo catálogo
    M2("M2", "Metro cuadrado", "m²", Categoria.AREA, 1.0),
    CM2("CM2", "Centímetro cuadrado", "cm²", Categoria.AREA, 0.0001),
    KM2("KM2", "Kilómetro cuadrado", "km²", Categoria.AREA, 1000000.0),
    HA("HA", "Hectárea", "ha", Categoria.AREA, 10000.0),
    FT2("FT2", "Pie cuadrado", "ft²", Categoria.AREA, 0.09290304);

    private final String codigo;
    private final String nombre;
    private final String simbolo;
    private final Categoria categoria;
    private final Double factor;

    Unidad(String codigo, String nombre, String simbolo, Categoria categoria, Double factor) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.simbolo = simbolo;
        this.categoria = categoria;
        this.factor = factor;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Double getFactor() {
        return factor;
    }

    /**
     * Busca la unidad por código, ignorando espacios y mayúsculas.
     */
    public static Optional<Unidad> desdeCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        String normalizado = codigo.trim().toUpperCase(Locale.ROOT);
        for (Unidad unidad : values()) {
            if (unidad.codigo.equals(normalizado)) {
                return Optional.of(unidad);
            }
        }
        return Optional.empty();
    }

    public static List<Unidad> deCategoria(Categoria categoria) {
        return Arrays.stream(values())
                .filter(unidad -> unidad.categoria == categoria)
                .toList();
    }
}

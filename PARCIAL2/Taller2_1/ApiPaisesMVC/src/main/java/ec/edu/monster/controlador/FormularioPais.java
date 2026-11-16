package ec.edu.monster.controlador;

/**
 * Datos tal como los escribe el usuario en el formulario de país (todo texto).
 * La vista solo copia los campos de la pantalla aquí; convertirlos y
 * validarlos es trabajo del controlador y del servicio.
 *
 * @author carlo
 */
public record FormularioPais(String codigo, String nombre, String capital,
        String region, String poblacion, String moneda) {
}

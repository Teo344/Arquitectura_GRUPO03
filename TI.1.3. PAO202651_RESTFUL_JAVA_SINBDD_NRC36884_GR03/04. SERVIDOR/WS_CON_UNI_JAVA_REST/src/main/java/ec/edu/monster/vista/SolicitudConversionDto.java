package ec.edu.monster.vista;

/**
 * Cuerpo JSON de las peticiones de conversión. El token no va aquí: viaja en
 * la cabecera Authorization.
 *
 * @author carlo
 */
public class SolicitudConversionDto {

    public Double valor;
    public String unidadOrigen;
    public String unidadDestino;
}

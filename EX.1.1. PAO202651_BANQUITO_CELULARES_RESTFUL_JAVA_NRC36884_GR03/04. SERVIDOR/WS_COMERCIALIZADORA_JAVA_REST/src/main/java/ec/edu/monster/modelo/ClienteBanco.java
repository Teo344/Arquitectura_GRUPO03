package ec.edu.monster.modelo;

/**
 * Cliente tal como lo devuelve el Core (GET /clientes?cedula=). Campos
 * públicos para que JSON-B lo lea de la respuesta.
 *
 * @author carlo
 */
public class ClienteBanco {

    public int codCliente;
    public String cedula;
    public String nombre;
}

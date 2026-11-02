package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

/**
 * Datos públicos de un cliente: GET /clientes?cedula=. No incluye género ni
 * fecha de nacimiento, que solo usa la evaluación de crédito.
 *
 * @author carlo
 */
@JsonbPropertyOrder({"codCliente", "cedula", "nombre"})
public class ClienteDto {

    public int codCliente;
    public String cedula;
    public String nombre;
}

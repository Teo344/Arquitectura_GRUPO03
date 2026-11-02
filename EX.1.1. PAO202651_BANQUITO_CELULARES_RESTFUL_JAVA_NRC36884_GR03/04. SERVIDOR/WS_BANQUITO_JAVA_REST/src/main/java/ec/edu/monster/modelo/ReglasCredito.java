package ec.edu.monster.modelo;

/**
 * Las reglas 2 a 4 de un cliente tal como las calcula la base de datos (la
 * regla 1, ser cliente, es que el cliente exista).
 *
 * @author carlo
 */
public record ReglasCredito(boolean depositoUltimoMes, boolean hombreMenor25, boolean creditoActivo) {
}

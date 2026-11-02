package ec.edu.monster.modelo;

import java.time.LocalDate;

/**
 * Cliente del banco (tabla CLIENTE). genero es M o F.
 *
 * @author carlo
 */
public record Cliente(int codigo, String cedula, String nombre, String genero, LocalDate fechaNacimiento) {
}

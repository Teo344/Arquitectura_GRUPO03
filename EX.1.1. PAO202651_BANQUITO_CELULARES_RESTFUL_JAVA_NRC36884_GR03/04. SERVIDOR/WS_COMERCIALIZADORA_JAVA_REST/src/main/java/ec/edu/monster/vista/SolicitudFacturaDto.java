package ec.edu.monster.vista;

import java.util.List;

/**
 * Cuerpo JSON de POST /facturas. numCuotas solo se usa si formaPago es
 * CREDITO. Las líneas no llevan precio: el servidor lo toma del catálogo.
 *
 * @author carlo
 */
public class SolicitudFacturaDto {

    public static class LineaDto {

        public Integer codTelefono;
        public Integer cantidad;
    }

    public String cedula;
    public String formaPago;
    public Integer numCuotas;
    public List<LineaDto> detalles;
}

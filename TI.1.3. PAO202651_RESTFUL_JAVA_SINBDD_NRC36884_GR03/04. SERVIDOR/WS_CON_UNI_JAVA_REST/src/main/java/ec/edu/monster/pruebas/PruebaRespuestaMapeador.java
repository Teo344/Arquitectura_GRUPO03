package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.Unidad;
import ec.edu.monster.servicios.ServicioConversion.ResultadoConversion;
import ec.edu.monster.vista.RespuestaDto;
import ec.edu.monster.vista.RespuestaMapeador;

/**
 * Prueba del formato de la respuesta que devuelve el servidor.
 *
 * @author carlo
 */
public class PruebaRespuestaMapeador {

    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("=== PruebaRespuestaMapeador ===");
        RespuestaMapeador mapeador = new RespuestaMapeador();

        RespuestaDto respuesta = mapeador.deConversion(new ResultadoConversion(1000.0, Unidad.KM, Unidad.M));
        comprobar("estado OK", "OK".equals(respuesta.estado));
        comprobar("código 200", "200".equals(respuesta.codigo));
        comprobar("resultado 1000", respuesta.resultado == 1000.0);
        comprobar("unidades KM -> M", "KM".equals(respuesta.unidadOrigen) && "M".equals(respuesta.unidadDestino));
        comprobar("sin token en una conversión", respuesta.token == null);

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}

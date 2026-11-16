package ec.edu.monster.modelo.entidad;

import java.util.List;

/**
 * Resultado de buscar países por nombre (CU-02). Indica el origen para que la
 * vista avise al usuario cuando los datos vienen del respaldo local.
 *
 * @param paises coincidencias encontradas (puede estar vacía)
 * @param origen API o base local
 * @param aviso  motivo del respaldo; {@code null} si respondió la API
 * @author carlo
 */
public record ResultadoBusqueda(List<Pais> paises, OrigenDatos origen, String aviso) {

    public ResultadoBusqueda {
        paises = List.copyOf(paises);
    }

    public static ResultadoBusqueda desdeApi(List<Pais> paises) {
        return new ResultadoBusqueda(paises, OrigenDatos.API_EXTERNA, null);
    }

    public static ResultadoBusqueda desdeBaseLocal(List<Pais> paises, String aviso) {
        return new ResultadoBusqueda(paises, OrigenDatos.BASE_LOCAL, aviso);
    }

    public boolean estaVacio() {
        return paises.isEmpty();
    }
}

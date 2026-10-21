package ec.edu.monster.servicio;

import java.io.IOException;

/** Respuesta de error del servidor REST: código HTTP más el código del cuerpo JSON (AUTH-001, DATA-001...). */
public final class ErrorApi extends IOException {
    private final int estadoHttp;
    private final String codigo;

    public ErrorApi(int estadoHttp, String codigo, String mensaje) {
        super(mensaje);
        this.estadoHttp = estadoHttp;
        this.codigo = codigo;
    }

    public int getEstadoHttp() { return estadoHttp; }
    public String getCodigo() { return codigo; }
}

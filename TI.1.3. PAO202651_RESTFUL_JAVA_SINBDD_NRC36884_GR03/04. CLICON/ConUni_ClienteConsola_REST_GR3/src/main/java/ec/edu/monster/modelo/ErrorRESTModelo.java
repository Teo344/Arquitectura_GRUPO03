package ec.edu.monster.modelo;

/** Error de una respuesta REST que el controlador puede presentar en consola. */
public class ErrorRESTModelo extends RuntimeException {

    private final int estadoHttp;
    private final String codigo;

    public ErrorRESTModelo(int estadoHttp, String codigo, String mensaje) {
        super(mensaje);
        this.estadoHttp = estadoHttp;
        this.codigo = codigo;
    }

    public ErrorRESTModelo(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.estadoHttp = 0;
        this.codigo = null;
    }

    public int getEstadoHttp() {
        return estadoHttp;
    }

    public String getCodigo() {
        return codigo;
    }

    public boolean requiereNuevoLogin() {
        return estadoHttp == 401
                && ("AUTH-001".equals(codigo) || "AUTH-002".equals(codigo));
    }
}

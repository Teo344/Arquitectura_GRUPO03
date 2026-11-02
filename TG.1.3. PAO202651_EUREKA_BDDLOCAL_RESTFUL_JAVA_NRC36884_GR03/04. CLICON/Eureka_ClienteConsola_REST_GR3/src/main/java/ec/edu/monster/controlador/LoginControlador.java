package ec.edu.monster.controlador;

import ec.edu.monster.modelo.LoginModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.ClienteRESTEureka;

/** Coordina el inicio y cierre de sesión y comparte el cliente REST con los demás controladores. */
public class LoginControlador {

    private final ClienteRESTEureka clienteRest;

    public LoginControlador() {
        this.clienteRest = new ClienteRESTEureka();
    }

    public SesionModelo autenticar(LoginModelo modelo) {
        if (modelo == null || modelo.getUsuario() == null || modelo.getUsuario().isBlank()
                || modelo.getContrasenia() == null || modelo.getContrasenia().isEmpty()) {
            throw new IllegalArgumentException("Usuario y contraseña son obligatorios.");
        }
        return clienteRest.iniciarSesion(modelo.getUsuario().trim(), modelo.getContrasenia());
    }

    public CuentaControlador crearControladorCuenta() {
        return new CuentaControlador(clienteRest);
    }

    public TransaccionControlador crearControladorTransaccion() {
        return new TransaccionControlador(clienteRest);
    }

    public SesionModelo getSesion() {
        return clienteRest.getSesion();
    }

    public String getEndpointActivo() {
        return clienteRest.getEndpointActivo();
    }

    public void cerrarSesion() {
        clienteRest.cerrarSesion();
    }
}

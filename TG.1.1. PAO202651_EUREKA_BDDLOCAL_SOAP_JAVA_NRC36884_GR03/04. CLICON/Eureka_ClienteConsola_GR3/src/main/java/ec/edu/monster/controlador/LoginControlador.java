package ec.edu.monster.controlador;

import ec.edu.monster.modelo.LoginModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.ClienteWSEureka;

/** Coordina el inicio y cierre de sesión y comparte el cliente SOAP con los demás controladores. */
public class LoginControlador {

    private final ClienteWSEureka clienteWs;

    public LoginControlador() {
        this.clienteWs = new ClienteWSEureka();
    }

    public SesionModelo autenticar(LoginModelo modelo) {
        if (modelo == null || modelo.getUsuario() == null || modelo.getUsuario().isBlank()
                || modelo.getContrasenia() == null || modelo.getContrasenia().isEmpty()) {
            throw new IllegalArgumentException("Usuario y contraseña son obligatorios.");
        }
        return clienteWs.iniciarSesion(modelo.getUsuario().trim(), modelo.getContrasenia());
    }

    public CuentaControlador crearControladorCuenta() {
        return new CuentaControlador(clienteWs);
    }

    public TransaccionControlador crearControladorTransaccion() {
        return new TransaccionControlador(clienteWs);
    }

    public SesionModelo getSesion() {
        return clienteWs.getSesion();
    }

    public String getEndpointActivo() {
        return clienteWs.getEndpointActivo();
    }

    public void cerrarSesion() {
        clienteWs.cerrarSesion();
    }
}

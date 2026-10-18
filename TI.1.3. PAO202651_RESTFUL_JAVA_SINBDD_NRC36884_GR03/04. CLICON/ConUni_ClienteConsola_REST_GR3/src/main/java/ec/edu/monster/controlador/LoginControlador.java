package ec.edu.monster.controlador;

import ec.edu.monster.modelo.LoginModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.ClienteRESTConversor;

/** Coordina el login y comparte una sola sesión con los demás controladores. */
public class LoginControlador {

    private final ClienteRESTConversor clienteRest;

    public LoginControlador() {
        clienteRest = new ClienteRESTConversor();
    }

    public SesionModelo autenticar(LoginModelo modelo) {
        if (modelo == null || modelo.getUsuario() == null
                || modelo.getUsuario().isBlank()
                || modelo.getContrasenia() == null
                || modelo.getContrasenia().isBlank()) {
            throw new IllegalArgumentException("Usuario y contraseña son obligatorios.");
        }
        return clienteRest.login(modelo.getUsuario(), modelo.getContrasenia());
    }

    public ConversionControlador crearControladorConversion() {
        return new ConversionControlador(clienteRest);
    }

    public CatalogoControlador crearControladorCatalogo() {
        return new CatalogoControlador(clienteRest);
    }

    public String getEndpointActivo() {
        return clienteRest.getEndpointActivo();
    }

    public void cerrarSesion() {
        clienteRest.cerrarSesion();
    }
}

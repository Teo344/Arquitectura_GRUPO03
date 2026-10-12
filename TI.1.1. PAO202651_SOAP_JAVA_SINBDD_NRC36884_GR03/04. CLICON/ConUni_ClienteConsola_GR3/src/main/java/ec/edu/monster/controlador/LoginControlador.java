package ec.edu.monster.controlador;

import client.ws.ClienteWSConversor;
import ec.edu.monster.modelo.LoginModelo;

public class LoginControlador {

    private final ClienteWSConversor clienteWs;

    public LoginControlador() {
        this.clienteWs = new ClienteWSConversor();
    }

    public String autenticar(LoginModelo loginModelo) {
        return clienteWs.login(loginModelo.getUsuario(), loginModelo.getContrasenia());
    }

    public ConversionControlador crearControladorConversion() {
        return new ConversionControlador(clienteWs);
    }

    public CatalogoControlador crearControladorCatalogo() {
        return new CatalogoControlador(clienteWs);
    }

    public String getEndpointActivo() {
        return clienteWs.getEndpointActivo();
    }

    public void cerrarSesion() {
        clienteWs.cerrarSesion();
    }
}

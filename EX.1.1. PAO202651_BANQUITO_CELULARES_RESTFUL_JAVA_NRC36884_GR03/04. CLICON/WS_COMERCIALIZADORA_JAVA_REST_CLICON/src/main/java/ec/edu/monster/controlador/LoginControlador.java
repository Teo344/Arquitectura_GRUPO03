package ec.edu.monster.controlador;

import ec.edu.monster.modelo.LoginModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;

/** Coordina el inicio y cierre de sesión (CU-01 y CU-02) y comparte el cliente REST con los demás controladores. */
public class LoginControlador {

    private final ClienteRESTMonsterCell clienteRest;

    public LoginControlador() {
        this.clienteRest = new ClienteRESTMonsterCell();
    }

    public SesionModelo autenticar(LoginModelo modelo) {
        if (modelo == null || modelo.getUsuario() == null || modelo.getUsuario().isBlank()
                || modelo.getContrasenia() == null || modelo.getContrasenia().isEmpty()) {
            throw new IllegalArgumentException("Ingresa tu usuario y contraseña.");
        }
        return clienteRest.iniciarSesion(modelo.getUsuario().trim(), modelo.getContrasenia());
    }

    public VentaControlador crearControladorVenta() {
        return new VentaControlador(clienteRest);
    }

    public FacturaControlador crearControladorFactura() {
        return new FacturaControlador(clienteRest);
    }

    public TelefonoControlador crearControladorTelefono() {
        return new TelefonoControlador(clienteRest);
    }

    public CreditoControlador crearControladorCredito() {
        return new CreditoControlador(clienteRest);
    }

    public SimuladorControlador crearControladorSimulador() {
        return new SimuladorControlador(clienteRest);
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

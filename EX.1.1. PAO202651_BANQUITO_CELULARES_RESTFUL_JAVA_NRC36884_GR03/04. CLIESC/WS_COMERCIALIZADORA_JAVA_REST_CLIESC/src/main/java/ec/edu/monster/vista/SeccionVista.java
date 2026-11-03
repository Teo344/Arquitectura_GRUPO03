package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.SUPERFICIE;

import ec.edu.monster.controlador.ControladorBase;
import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 * Pantalla que se muestra a la derecha de la barra lateral (venta, facturas,
 * catálogo, crédito, simulador). Si el token caduca, vuelve al login a través
 * de la ventana principal.
 */
abstract class SeccionVista extends JPanel implements ControladorBase.Vista {

    protected final PrincipalVista principal;

    protected SeccionVista(PrincipalVista principal) {
        super(new BorderLayout());
        this.principal = principal;
        setBackground(SUPERFICIE);
    }

    @Override
    public void volverAlLogin(String mensaje) {
        principal.volverAlLogin(mensaje + ". Inicia sesión nuevamente.", true);
    }

    /** La sección deja de mostrarse: su controlador descarta las respuestas que lleguen después. */
    abstract void detener();
}

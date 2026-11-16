package ec.edu.monster;

import ec.edu.monster.config.Configuracion;
import ec.edu.monster.controlador.InicioControlador;
import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.controlador.ReporteControlador;
import ec.edu.monster.modelo.api.PaisApiCliente;
import ec.edu.monster.modelo.api.RestCountriesCliente;
import ec.edu.monster.modelo.api.RestCountriesMapper;
import ec.edu.monster.modelo.dao.ConexionBD;
import ec.edu.monster.modelo.dao.PaisDAO;
import ec.edu.monster.modelo.dao.PaisDAOMySQL;
import ec.edu.monster.servicio.PaisServicio;
import ec.edu.monster.vista.Ventana;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada y raíz de composición: es el único lugar que conoce las
 * implementaciones concretas (MySQL, REST Countries) y las inyecta por
 * constructor. El resto del código depende de interfaces.
 *
 * @author carlo
 */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        Configuracion configuracion;
        try {
            configuracion = Configuracion.cargar();
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Falta la configuración", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        PaisServicio servicio = crearServicio(configuracion);
        InicioControlador inicioControlador = new InicioControlador(servicio);
        PaisControlador paisControlador = new PaisControlador(servicio);
        ReporteControlador reporteControlador = new ReporteControlador(servicio);

        // Swing exige crear y modificar la interfaz en su hilo de eventos
        SwingUtilities.invokeLater(() ->
                new Ventana(inicioControlador, paisControlador, reporteControlador).abrirInicio());
    }

    static PaisServicio crearServicio(Configuracion configuracion) {
        ConexionBD conexionBD = new ConexionBD(
                configuracion.dbUrl(), configuracion.dbUsuario(), configuracion.dbClave());
        PaisDAO dao = new PaisDAOMySQL(conexionBD);
        PaisApiCliente api = new RestCountriesCliente(configuracion.apiUrlBase(), configuracion.apiClave(),
                configuracion.apiTimeout(), new RestCountriesMapper());
        return new PaisServicio(dao, api);
    }
}

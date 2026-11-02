package ec.edu.monster.servicios;

import ec.edu.monster.datos.ConexionBD;
import ec.edu.monster.datos.UsuarioDao;
import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.modelo.Usuario;
import ec.edu.monster.seguridad.GestorToken;
import ec.edu.monster.seguridad.GestorToken.TokenEmitido;
import ec.edu.monster.seguridad.Sesion;
import ec.edu.monster.seguridad.UtilHash;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inicio y cierre de sesión de los empleados de la comercializadora.
 *
 * @author carlo
 */
@ApplicationScoped
public class ServicioAutenticacion {

    private static final Logger LOG = Logger.getLogger(ServicioAutenticacion.class.getName());

    public record SesionIniciada(TokenEmitido token, Sesion sesion) {
    }

    @Inject
    private UsuarioDao usuarioDao;

    @Inject
    private GestorToken gestorToken;

    /**
     * AUTH-001 no indica si falló el usuario o la clave.
     */
    public SesionIniciada iniciarSesion(String usuario, String clave) throws ExcepcionServicio {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isEmpty()) {
            throw new ExcepcionServicio(CodigoError.DATA_001);
        }
        try (Connection cn = ConexionBD.getInstancia().obtenerConexion()) {
            Usuario encontrado = usuarioDao.buscar(cn, usuario.trim(), UtilHash.sha256(clave))
                    .orElseThrow(() -> new ExcepcionServicio(CodigoError.AUTH_001));
            if (!encontrado.activo()) {
                throw new ExcepcionServicio(CodigoError.AUTH_002);
            }
            Sesion sesion = new Sesion(encontrado.codigo(), encontrado.usuario(), encontrado.nombre());
            return new SesionIniciada(gestorToken.generar(sesion), sesion);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos al iniciar sesión", e);
            throw new ExcepcionServicio(CodigoError.SYS_001, e);
        }
    }

    public void cerrarSesion(String token) throws ExcepcionServicio {
        gestorToken.invalidar(token);
    }
}

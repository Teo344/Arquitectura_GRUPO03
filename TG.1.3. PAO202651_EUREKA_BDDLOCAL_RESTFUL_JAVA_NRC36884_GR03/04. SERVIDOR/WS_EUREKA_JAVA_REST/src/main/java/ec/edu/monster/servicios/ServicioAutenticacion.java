package ec.edu.monster.servicios;

import ec.edu.monster.datos.ConexionBD;
import ec.edu.monster.datos.LogSesionDao;
import ec.edu.monster.datos.UsuarioDao;
import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.modelo.Empleado;
import ec.edu.monster.modelo.Estado;
import ec.edu.monster.seguridad.GestorToken;
import ec.edu.monster.seguridad.GestorToken.TokenEmitido;
import ec.edu.monster.seguridad.Sesion;
import ec.edu.monster.seguridad.UtilHash;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inicio (RQF-001) y cierre (RQF-002) de sesión del empleado.
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
    private LogSesionDao logSesionDao;

    @Inject
    private GestorToken gestorToken;

    /**
     * AUTH-001 no indica si falló el usuario o la clave. Un usuario sin
     * sucursal vigente no puede atender en ventanilla: AUTH-002.
     */
    public SesionIniciada iniciarSesion(String usuario, String clave, String ip, String equipo)
            throws ExcepcionServicio {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isEmpty()) {
            throw new ExcepcionServicio(CodigoError.DATA_001);
        }
        try (Connection cn = ConexionBD.getInstancia().obtenerConexion()) {
            Empleado empleado = usuarioDao.buscar(cn, usuario.trim(), UtilHash.sha256(clave))
                    .orElseThrow(() -> new ExcepcionServicio(CodigoError.AUTH_001));
            if (!Estado.esActivo(empleado.estado()) || empleado.codigoSucursal() == null) {
                throw new ExcepcionServicio(CodigoError.AUTH_002);
            }
            int logId = logSesionDao.registrarIngreso(cn, empleado.codigo(), ip, equipo);
            Sesion sesion = new Sesion(empleado.codigo(), empleado.usuario(), empleado.nombre(),
                    empleado.codigoSucursal(), empleado.sucursal(), logId);
            return new SesionIniciada(gestorToken.generar(sesion), sesion);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos al iniciar sesión", e);
            throw new ExcepcionServicio(CodigoError.SYS_001, e);
        }
    }

    /**
     * Invalida el token y registra la salida. Un token ya inválido da AUTH-003.
     */
    public void cerrarSesion(String token) throws ExcepcionServicio {
        Sesion sesion = gestorToken.invalidar(token);
        try (Connection cn = ConexionBD.getInstancia().obtenerConexion()) {
            logSesionDao.registrarSalida(cn, sesion.logId(), Instant.now());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos al cerrar sesión", e);
            throw new ExcepcionServicio(CodigoError.SYS_001, e);
        }
    }
}

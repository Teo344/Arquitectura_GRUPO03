package ec.edu.monster.servicios;

import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.seguridad.GestorToken;
import ec.edu.monster.seguridad.GestorToken.TokenEmitido;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Login y cambio de clave (RQF-001). Es el único servicio que usa
 * {@link GestorToken}.
 *
 * @author carlo
 */
@ApplicationScoped
public class ServicioAutenticacion {

    @Inject
    private GestorToken gestorToken;

    /**
     * login y cambiarClave están sincronizados para que un login con la clave
     * vieja no quede vivo si coincide con un cambio de clave.
     */
    public synchronized TokenEmitido login(String usuario, String clave) throws ExcepcionServicio {
        validarCredenciales(usuario, clave);
        return gestorToken.generar(usuario.trim());
    }

    /**
     * La clave actual autentica al usuario, así que no requiere token. Al
     * cambiarla se cierran todas las sesiones.
     */
    public synchronized void cambiarClave(String usuario, String claveActual, String claveNueva)
            throws ExcepcionServicio {
        validarCredenciales(usuario, claveActual);
        if (claveNueva == null || claveNueva.isBlank()) {
            throw new ExcepcionServicio(CodigoError.DATA_001);
        }
        gestorToken.cambiarClave(claveNueva);
    }

    private void validarCredenciales(String usuario, String clave) throws ExcepcionServicio {
        if (usuario == null || clave == null || !gestorToken.credencialesValidas(usuario.trim(), clave)) {
            throw new ExcepcionServicio(CodigoError.AUTH_003);
        }
    }
}

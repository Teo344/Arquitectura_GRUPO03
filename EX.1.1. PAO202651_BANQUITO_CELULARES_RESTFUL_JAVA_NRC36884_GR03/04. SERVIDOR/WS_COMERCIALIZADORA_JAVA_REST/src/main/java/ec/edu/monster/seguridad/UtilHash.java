package ec.edu.monster.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hash de contraseñas. Devuelve el SHA-256 en hexadecimal en
 * minúsculas, el mismo formato que SHA2(clave, 256) de MySQL con el que se
 * cargaron los usuarios.
 *
 * @author carlo
 */
public final class UtilHash {

    private UtilHash() {
    }

    public static String sha256(String texto) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("La JVM no soporta SHA-256", e);
        }
    }
}

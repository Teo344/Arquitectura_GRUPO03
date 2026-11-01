package ec.edu.monster.pruebas;

import ec.edu.monster.seguridad.UtilHash;

/**
 * Prueba de que el hash de las claves coincide con SHA2(clave, 256) de MySQL.
 *
 * @author carlo
 */
public class PruebaHash {

    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("=== PruebaHash ===");
        comprobar("SHA-256 de 'abc' coincide con el vector conocido", UtilHash.sha256("abc")
                .equals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"));
        String hash = UtilHash.sha256("MONSTER9");
        comprobar("el hash tiene 64 caracteres", hash.length() == 64);
        comprobar("el hash está en minúsculas", hash.equals(hash.toLowerCase()));
        comprobar("claves distintas dan hashes distintos", !hash.equals(UtilHash.sha256("monster9")));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}

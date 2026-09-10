package essalud.gob.pe.seguroshijomenormayor.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtil {

    private HashUtil() {
    }

    public static String calcularSha256(byte[] contenido) {
        if (contenido == null) {
            throw new IllegalArgumentException("No se puede calcular hash de contenido nulo.");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenido);

            StringBuilder hexadecimal = new StringBuilder();

            for (byte b : hash) {
                String valorHexadecimal = Integer.toHexString(0xff & b);

                if (valorHexadecimal.length() == 1) {
                    hexadecimal.append('0');
                }

                hexadecimal.append(valorHexadecimal);
            }

            return hexadecimal.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo calcular el hash SHA-256.", e);
        }
    }
}
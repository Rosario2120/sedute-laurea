package util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash delle password con PBKDF2-HMAC-SHA256 e sale casuale (solo JDK, nessuna libreria esterna).
 * Formato salvato nel database: pbkdf2_sha256$iterazioni$saleBase64$hashBase64
 */
public final class PasswordHasher {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String PREFISSO = "pbkdf2_sha256";
    private static final int ITERAZIONI = 65536;
    private static final int LUNGHEZZA_SALE = 16;
    private static final int LUNGHEZZA_HASH_BIT = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String password) {
        byte[] sale = new byte[LUNGHEZZA_SALE];
        RANDOM.nextBytes(sale);
        byte[] hash = pbkdf2(password, sale, ITERAZIONI);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFISSO + "$" + ITERAZIONI + "$" + b64.encodeToString(sale) + "$" + b64.encodeToString(hash);
    }

    public static boolean verifica(String password, String hashSalvato) {
        if (password == null || hashSalvato == null) {
            return false;
        }
        String[] parti = hashSalvato.split("\\$");
        if (parti.length != 4 || !PREFISSO.equals(parti[0])) {
            return false;
        }
        try {
            int iterazioni = Integer.parseInt(parti[1]);
            byte[] sale = Base64.getDecoder().decode(parti[2]);
            byte[] atteso = Base64.getDecoder().decode(parti[3]);
            // confronto a tempo costante
            return MessageDigest.isEqual(atteso, pbkdf2(password, sale, iterazioni));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(String password, byte[] sale, int iterazioni) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), sale, iterazioni, LUNGHEZZA_HASH_BIT);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Algoritmo " + ALGORITMO + " non disponibile", e);
        } finally {
            spec.clearPassword();
        }
    }
}

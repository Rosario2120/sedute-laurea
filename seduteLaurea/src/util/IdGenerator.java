package util;

import java.util.Locale;
import java.util.UUID;

/**
 * Genera gli identificativi testuali usati come chiave primaria (es. "TESI-3F9A1C02").
 * Sostituisce il vecchio prefisso + System.currentTimeMillis(), che poteva collidere.
 */
public final class IdGenerator {

    private IdGenerator() {
    }

    public static String nuovoId(String prefisso) {
        String casuale = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return prefisso + "-" + casuale.toUpperCase(Locale.ROOT);
    }
}

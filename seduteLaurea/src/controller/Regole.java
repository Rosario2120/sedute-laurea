package controller;

/**
 * Controlli di validazione condivisi dai controller.
 */
final class Regole {

    private Regole() {
    }

    // Restituisce il valore senza spazi iniziali/finali, o segnala il campo mancante
    static String obbligatorio(String valore, String nomeCampo) throws OperazioneNonValidaException {
        if (valore == null || valore.isBlank()) {
            throw new OperazioneNonValidaException("Il campo \"" + nomeCampo + "\" è obbligatorio.");
        }
        return valore.trim();
    }

    static void verifica(boolean condizione, String messaggio) throws OperazioneNonValidaException {
        if (!condizione) {
            throw new OperazioneNonValidaException(messaggio);
        }
    }
}

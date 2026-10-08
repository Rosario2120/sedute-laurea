package controller;

/**
 * Violazione di una regola di dominio (es. "seduta al completo").
 * Il messaggio è pensato per essere mostrato così com'è all'utente.
 */
public class OperazioneNonValidaException extends RuntimeException {

    public OperazioneNonValidaException(String messaggio) {
        super(messaggio);
    }
}

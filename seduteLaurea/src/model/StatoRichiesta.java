package model;

/**
 * Stato di una richiesta di tirocinio o di tesi.
 * Una richiesta nasce IN_ATTESA e viene APPROVATA o RIFIUTATA dal docente.
 */
public enum StatoRichiesta {
    IN_ATTESA,
    APPROVATA,
    RIFIUTATA
}

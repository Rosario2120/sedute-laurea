package controller;

/**
 * Raccoglie tutti i controller dell'applicazione, così le GUI ricevono
 * un solo parametro invece di una lunga lista di controller.
 */
public record Controllers(
        UserController utenti,
        StudenteController studenti,
        DocenteController docenti,
        TirocinioController tirocini,
        RichiestaTirocinioController richieste,
        TesiController tesi,
        CommissioneController commissioni,
        SedutaLaureaController sedute
) {
}

package controller;

import model.Ruolo;
import model.Studente;
import model.User;
import org.junit.jupiter.api.Test;
import support.DatabaseTest;

import static org.junit.jupiter.api.Assertions.*;
import static support.DatiDemo.*;

class UserControllerTest extends DatabaseTest {

    @Test
    void loginConCredenzialiCorretteRestituisceUtenteERuolo() {
        assertEquals(Ruolo.COORDINATORE, c.utenti().login("coordinatore", PASSWORD).getRuolo());
        assertEquals(Ruolo.DOCENTE, c.utenti().login("mrossi", PASSWORD).getRuolo());
        assertEquals(Ruolo.STUDENTE, c.utenti().login("aesposito", PASSWORD).getRuolo());
    }

    @Test
    void loginRifiutaPasswordErrataEUtenteInesistenteConLoStessoMessaggio() {
        var errata = assertThrows(OperazioneNonValidaException.class, () -> c.utenti().login("mrossi", "sbagliata"));
        var inesistente = assertThrows(OperazioneNonValidaException.class, () -> c.utenti().login("nessuno", PASSWORD));
        assertEquals(errata.getMessage(), inesistente.getMessage());
    }

    @Test
    void loginRifiutaUsernameVuoto() {
        assertThrows(OperazioneNonValidaException.class, () -> c.utenti().login("  ", PASSWORD));
    }

    @Test
    void registraStudenteCreaAccountEAnagraficaCollegati() {
        Studente nuovo = c.utenti().registraStudente("gneri", "segreta1", "n86009999", "Giulia", "Neri",
                "GIULIA.NERI@studenti.unina.it");

        assertEquals("N86009999", nuovo.getMatricola());
        User user = c.utenti().login("gneri", "segreta1");
        assertEquals(Ruolo.STUDENTE, user.getRuolo());
        assertEquals("giulia.neri@studenti.unina.it", c.studenti().getStudenteByIdUser(user.getIdUser()).getEmail());
    }

    @Test
    void registraStudenteRifiutaDuplicatiEDatiNonValidi() {
        assertThrows(OperazioneNonValidaException.class, () ->
                c.utenti().registraStudente("aesposito", "segreta1", "N86000000", "A", "B", "a@b.it"));
        assertThrows(OperazioneNonValidaException.class, () ->
                c.utenti().registraStudente("nuovo", "segreta1", ESPOSITO, "A", "B", "a@b.it"));
        assertThrows(OperazioneNonValidaException.class, () ->
                c.utenti().registraStudente("nuovo", "corta", "N86000000", "A", "B", "a@b.it"));
        assertThrows(OperazioneNonValidaException.class, () ->
                c.utenti().registraStudente("nuovo", "segreta1", "N86000000", "A", "B", "non-una-email"));
    }

    @Test
    void emailGiaUsataVieneRifiutataSenzaCreareLAccount() {
        var e = assertThrows(OperazioneNonValidaException.class, () -> c.utenti().registraStudente("doppione",
                "segreta1", "N86000000", "A", "B", "AN.ESPOSITO@studenti.unina.it"));
        assertEquals("Email già registrata.", e.getMessage());
        assertThrows(OperazioneNonValidaException.class, () -> c.utenti().login("doppione", "segreta1"));
        assertThrows(OperazioneNonValidaException.class, () -> c.utenti().registraDocente("doppione", "segreta1",
                "A", "B", "mario.rossi@unina.it", false));
    }

    @Test
    void loginNonDistingueMaiuscoleNelloUsername() {
        c.utenti().registraStudente("GNeri", "segreta1", "N86009999", "Giulia", "Neri", "g.neri@studenti.unina.it");
        assertDoesNotThrow(() -> c.utenti().login("GNeri", "segreta1"));
        assertDoesNotThrow(() -> c.utenti().login("gneri", "segreta1"));
    }

    @Test
    void registraDocenteEcoordinatore() {
        c.utenti().registraDocente("pgialli", "segreta1", "Paolo", "Gialli", "p.gialli@unina.it", false);
        c.utenti().registraDocente("ccoord", "segreta1", "Carla", "Coord", "c.coord@unina.it", true);

        User docente = c.utenti().login("pgialli", "segreta1");
        assertEquals(Ruolo.DOCENTE, docente.getRuolo());
        assertNotNull(c.docenti().getDocenteByIdUser(docente.getIdUser()));
        assertEquals(Ruolo.COORDINATORE, c.utenti().login("ccoord", "segreta1").getRuolo());
    }

    @Test
    void cambiaPasswordRichiedeQuellaAttuale() {
        User user = c.utenti().login("mrossi", PASSWORD);
        assertThrows(OperazioneNonValidaException.class, () -> c.utenti().cambiaPassword(user, "sbagliata", "nuova123"));

        c.utenti().cambiaPassword(user, PASSWORD, "nuova123");
        assertDoesNotThrow(() -> c.utenti().login("mrossi", "nuova123"));
        assertThrows(OperazioneNonValidaException.class, () -> c.utenti().login("mrossi", PASSWORD));
    }

    @Test
    void eliminaUtente() {
        User coordinatore = c.utenti().login("coordinatore", PASSWORD);
        User studente = c.utenti().login("fcolombo", PASSWORD);
        User docenteConTirocini = c.utenti().login("mrossi", PASSWORD);

        assertThrows(OperazioneNonValidaException.class,
                () -> c.utenti().eliminaUtente(coordinatore, coordinatore.getIdUser()));
        // un docente con tirocini, tesi e commissioni non può essere eliminato
        var e = assertThrows(OperazioneNonValidaException.class,
                () -> c.utenti().eliminaUtente(coordinatore, docenteConTirocini.getIdUser()));
        assertTrue(e.getMessage().contains("collegati"));

        c.utenti().eliminaUtente(coordinatore, studente.getIdUser());
        assertNull(c.studenti().getStudenteByMatricola(COLOMBO)); // eliminato a cascata
    }
}

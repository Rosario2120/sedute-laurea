package controller;

import model.Docente;
import model.RichiestaTirocinio;
import model.StatoRichiesta;
import model.Studente;
import model.Tirocinio;
import org.junit.jupiter.api.Test;
import support.DatabaseTest;

import static org.junit.jupiter.api.Assertions.*;
import static support.DatiDemo.*;

class TirocinioControllerTest extends DatabaseTest {

    private Docente docente(String id) {
        return c.docenti().getDocenteById(id);
    }

    private Studente studente(String matricola) {
        return c.studenti().getStudenteByMatricola(matricola);
    }

    @Test
    void ilDocenteGestisceSoloIPropriTirocini() {
        Tirocinio nuovo = c.tirocini().creaTirocinio(docente(DOC_BIANCHI), "  Cloud computing ", "Deploy su Kubernetes");
        assertEquals("Cloud computing", nuovo.getTitolo());

        c.tirocini().modificaTirocinio(docente(DOC_BIANCHI), nuovo.getIdTirocinio(), "Cloud", "Nuova descrizione");
        assertEquals("Cloud", c.tirocini().getTirocinioById(nuovo.getIdTirocinio()).getTitolo());

        assertThrows(OperazioneNonValidaException.class,
                () -> c.tirocini().modificaTirocinio(docente(DOC_ROSSI), nuovo.getIdTirocinio(), "X", ""));
        assertThrows(OperazioneNonValidaException.class,
                () -> c.tirocini().creaTirocinio(docente(DOC_ROSSI), " ", "senza titolo"));
    }

    @Test
    void nonSiEliminaUnTirocinioConStudentiApprovati() {
        assertThrows(OperazioneNonValidaException.class,
                () -> c.tirocini().eliminaTirocinio(docente(DOC_ROSSI), TIR_ANDROID));

        // TIR-003 ha solo una richiesta in attesa: si elimina insieme alla richiesta
        c.tirocini().eliminaTirocinio(docente(DOC_BIANCHI), TIR_RETI);
        assertNull(c.tirocini().getTirocinioById(TIR_RETI));
        assertTrue(c.richieste().getRichiesteByStudente(ROMANO).isEmpty());
    }

    @Test
    void loStudenteInviaERitiraUnaRichiesta() {
        RichiestaTirocinio r = c.richieste().inviaRichiesta(studente(COLOMBO), TIR_ML);
        assertEquals(StatoRichiesta.IN_ATTESA, r.getStato());
        assertEquals(2, c.richieste().getRichiesteByStudente(COLOMBO).size());

        // doppia richiesta per lo stesso tirocinio
        assertThrows(OperazioneNonValidaException.class, () -> c.richieste().inviaRichiesta(studente(COLOMBO), TIR_ML));

        c.richieste().ritiraRichiesta(studente(COLOMBO), r.getIdRichiesta());
        assertEquals(1, c.richieste().getRichiesteByStudente(COLOMBO).size());
    }

    @Test
    void conUnTirocinioApprovatoNonSeNeRichiedonoAltri() {
        assertThrows(OperazioneNonValidaException.class, () -> c.richieste().inviaRichiesta(studente(ESPOSITO), TIR_ML));
    }

    @Test
    void dopoUnRifiutoSiPuoRichiedereDiNuovoLoStessoTirocinio() {
        c.richieste().rifiutaRichiesta(docente(DOC_ROSSI), RICH_COLOMBO_ANDROID);
        assertDoesNotThrow(() -> c.richieste().inviaRichiesta(studente(COLOMBO), TIR_ANDROID));
    }

    @Test
    void soloIlDocenteDelTirocinioValutaLaRichiesta() {
        assertThrows(OperazioneNonValidaException.class,
                () -> c.richieste().approvaRichiesta(docente(DOC_ROSSI), RICH_ROMANO_RETI));

        c.richieste().approvaRichiesta(docente(DOC_BIANCHI), RICH_ROMANO_RETI);
        assertEquals(StatoRichiesta.APPROVATA, c.richieste().getRichiesteByStudente(ROMANO).get(0).getStato());

        // già valutata
        assertThrows(OperazioneNonValidaException.class,
                () -> c.richieste().rifiutaRichiesta(docente(DOC_BIANCHI), RICH_ROMANO_RETI));
    }

    @Test
    void nonSiApprovaUnSecondoTirocinioPerLoStessoStudente() {
        RichiestaTirocinio seconda = c.richieste().inviaRichiesta(studente(COLOMBO), TIR_ML);
        c.richieste().approvaRichiesta(docente(DOC_ROSSI), RICH_COLOMBO_ANDROID);

        assertThrows(OperazioneNonValidaException.class,
                () -> c.richieste().approvaRichiesta(docente(DOC_VERDI), seconda.getIdRichiesta()));
    }

    @Test
    void loStudenteNonRitiraRichiesteAltruiOGiaValutate() {
        assertThrows(OperazioneNonValidaException.class,
                () -> c.richieste().ritiraRichiesta(studente(COLOMBO), RICH_ROMANO_RETI));
        assertThrows(OperazioneNonValidaException.class,
                () -> c.richieste().ritiraRichiesta(studente(ESPOSITO), "RICH-001"));
    }

    @Test
    void ilDocenteVedeLeRichiesteDeiSuoiTirocini() {
        assertEquals(3, c.richieste().getRichiesteByDocente(DOC_ROSSI).size()); // RICH-001, 003, 005
        assertEquals(1, c.richieste().getRichiesteByDocente(DOC_BIANCHI).size());
    }
}

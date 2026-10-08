package controller;

import model.Docente;
import model.StatoRichiesta;
import model.Studente;
import model.Tesi;
import org.junit.jupiter.api.Test;
import support.DatabaseTest;

import static org.junit.jupiter.api.Assertions.*;
import static support.DatiDemo.*;

class TesiControllerTest extends DatabaseTest {

    private Docente docente(String id) {
        return c.docenti().getDocenteById(id);
    }

    private Studente studente(String matricola) {
        return c.studenti().getStudenteByMatricola(matricola);
    }

    @Test
    void richiestaDiTesiInAttesa() {
        Tesi tesi = c.tesi().richiediTesi(studente(COLOMBO), "Algoritmi distribuiti", DOC_VERDI);
        assertEquals(StatoRichiesta.IN_ATTESA, tesi.getStato());
        assertEquals(tesi.getIdTesi(), c.tesi().getTesiCorrente(COLOMBO).getIdTesi());
        assertNotNull(c.tesi().getTesiCorrente(COLOMBO).getDataRichiesta());
    }

    @Test
    void unaSolaTesiAttivaPerStudente() {
        assertThrows(OperazioneNonValidaException.class,
                () -> c.tesi().richiediTesi(studente(ROMANO), "Seconda tesi", DOC_ROSSI));
        assertThrows(OperazioneNonValidaException.class,
                () -> c.tesi().richiediTesi(studente(ESPOSITO), "Altra tesi", DOC_ROSSI));
    }

    @Test
    void validaTitoloERelatore() {
        assertThrows(OperazioneNonValidaException.class, () -> c.tesi().richiediTesi(studente(COLOMBO), "", DOC_ROSSI));
        assertThrows(OperazioneNonValidaException.class, () -> c.tesi().richiediTesi(studente(COLOMBO), "T", "DOC-XYZ"));
        assertThrows(OperazioneNonValidaException.class, () -> c.tesi().richiediTesi(studente(COLOMBO), "T", null));
    }

    @Test
    void soloIlRelatoreValutaLaTesi() {
        assertThrows(OperazioneNonValidaException.class, () -> c.tesi().approvaTesi(docente(DOC_ROSSI), TESI_ROMANO));

        c.tesi().approvaTesi(docente(DOC_BIANCHI), TESI_ROMANO);
        assertTrue(c.tesi().haTesiApprovata(ROMANO));
        assertThrows(OperazioneNonValidaException.class, () -> c.tesi().rifiutaTesi(docente(DOC_BIANCHI), TESI_ROMANO));
    }

    @Test
    void dopoUnRifiutoLoStudentePuoProporreUnaNuovaTesi() {
        c.tesi().rifiutaTesi(docente(DOC_BIANCHI), TESI_ROMANO);
        assertNull(c.tesi().getTesiCorrente(ROMANO));

        c.tesi().richiediTesi(studente(ROMANO), "Nuovo argomento", DOC_VERDI);
        assertEquals(2, c.tesi().getTesiByStudente(ROMANO).size()); // lo storico conserva quella rifiutata
    }

    @Test
    void ritiroSoloDellaPropriaTesiInAttesa() {
        assertThrows(OperazioneNonValidaException.class, () -> c.tesi().ritiraTesi(studente(COLOMBO), TESI_ROMANO));
        assertThrows(OperazioneNonValidaException.class, () -> c.tesi().ritiraTesi(studente(ESPOSITO), "TESI-001"));

        c.tesi().ritiraTesi(studente(ROMANO), TESI_ROMANO);
        assertTrue(c.tesi().getTesiByStudente(ROMANO).isEmpty());
    }

    @Test
    void ilRelatoreVedeLeSueTesi() {
        assertEquals(2, c.tesi().getTesiByRelatore(DOC_ROSSI).size());
        assertEquals(3, c.tesi().getAllTesi().size());
    }
}

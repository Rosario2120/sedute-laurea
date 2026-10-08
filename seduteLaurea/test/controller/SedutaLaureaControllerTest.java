package controller;

import model.SedutaLaurea;
import model.Studente;
import org.junit.jupiter.api.Test;
import support.DatabaseTest;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static support.DatiDemo.*;

class SedutaLaureaControllerTest extends DatabaseTest {

    private static final LocalDate TRA_UN_MESE = LocalDate.now().plusMonths(1);
    private static final LocalTime ORE_DIECI = LocalTime.of(10, 0);

    private Studente studente(String matricola) {
        return c.studenti().getStudenteByMatricola(matricola);
    }

    @Test
    void creaEModificaUnaSeduta() {
        SedutaLaurea s = c.sedute().creaSeduta(TRA_UN_MESE, ORE_DIECI, " Aula 3 ", COMM_VERDI, 5);

        SedutaLaurea salvata = c.sedute().getSedutaById(s.getIdSeduta());
        assertEquals(TRA_UN_MESE, salvata.getDataSeduta());
        assertEquals(ORE_DIECI, salvata.getOraSeduta());
        assertEquals("Aula 3", salvata.getLuogo());

        c.sedute().modificaSeduta(s.getIdSeduta(), TRA_UN_MESE.plusDays(1), LocalTime.of(15, 30), "Aula 4", null, 8);
        salvata = c.sedute().getSedutaById(s.getIdSeduta());
        assertEquals(LocalTime.of(15, 30), salvata.getOraSeduta());
        assertNull(salvata.getIdCommissione());
        assertEquals(8, salvata.getMaxStudenti());
    }

    @Test
    void validazioneDatiSeduta() {
        assertThrows(OperazioneNonValidaException.class,
                () -> c.sedute().creaSeduta(LocalDate.now(), ORE_DIECI, "Aula", null, 5));
        assertThrows(OperazioneNonValidaException.class,
                () -> c.sedute().creaSeduta(TRA_UN_MESE, ORE_DIECI, " ", null, 5));
        assertThrows(OperazioneNonValidaException.class,
                () -> c.sedute().creaSeduta(TRA_UN_MESE, ORE_DIECI, "Aula", null, 0));
        assertThrows(OperazioneNonValidaException.class,
                () -> c.sedute().creaSeduta(TRA_UN_MESE, ORE_DIECI, "Aula", "COMM-XYZ", 5));
    }

    @Test
    void conPrenotatiNonSiRiduconoIPostiNeSiTogliLaCommissione() {
        assertThrows(OperazioneNonValidaException.class, () -> c.sedute().modificaSeduta(SED_TRA_30_GIORNI,
                TRA_UN_MESE, ORE_DIECI, "Aula Magna", COMM_FERRARA, 0));
        assertThrows(OperazioneNonValidaException.class, () -> c.sedute().modificaSeduta(SED_TRA_30_GIORNI,
                TRA_UN_MESE, ORE_DIECI, "Aula Magna", null, 10));
    }

    @Test
    void unaSedutaSvoltaNonSiModifica() {
        var e = assertThrows(OperazioneNonValidaException.class, () -> c.sedute().modificaSeduta(SED_PASSATA,
                TRA_UN_MESE, ORE_DIECI, "Aula Magna", COMM_FERRARA, 10));
        assertTrue(e.getMessage().contains("già svolta"));
    }

    @Test
    void prenotazioneEAnnullamento() {
        c.sedute().prenotaStudente(SED_TRA_60_GIORNI, studente(RICCI));
        assertEquals(SED_TRA_60_GIORNI, c.sedute().getSedutaPrenotata(RICCI).getIdSeduta());
        assertEquals(1, c.sedute().contaPrenotati(SED_TRA_60_GIORNI));

        c.sedute().annullaPrenotazione(SED_TRA_60_GIORNI, studente(RICCI));
        assertNull(c.sedute().getSedutaPrenotata(RICCI));
    }

    @Test
    void perPrenotarsiServeUnaTesiApprovata() {
        var e = assertThrows(OperazioneNonValidaException.class,
                () -> c.sedute().prenotaStudente(SED_TRA_60_GIORNI, studente(ROMANO))); // tesi in attesa
        assertTrue(e.getMessage().contains("tesi approvata"));
        assertThrows(OperazioneNonValidaException.class,
                () -> c.sedute().prenotaStudente(SED_TRA_60_GIORNI, studente(COLOMBO))); // nessuna tesi
    }

    @Test
    void unaSolaPrenotazionePerStudente() {
        var e = assertThrows(OperazioneNonValidaException.class,
                () -> c.sedute().prenotaStudente(SED_TRA_60_GIORNI, studente(ESPOSITO)));
        assertTrue(e.getMessage().contains("già prenotato"));
    }

    @Test
    void nonSiPrenotaSeduteSvolteSenzaCommissioneOAlCompleto() {
        assertThrows(OperazioneNonValidaException.class,
                () -> c.sedute().prenotaStudente(SED_PASSATA, studente(RICCI)));
        assertThrows(OperazioneNonValidaException.class,
                () -> c.sedute().prenotaStudente(SED_SENZA_COMMISSIONE, studente(RICCI)));

        SedutaLaurea piccola = c.sedute().creaSeduta(TRA_UN_MESE, ORE_DIECI, "Aula 1", COMM_VERDI, 1);
        c.sedute().prenotaStudente(piccola.getIdSeduta(), studente(RICCI));

        c.sedute().annullaPrenotazione(SED_TRA_30_GIORNI, studente(ESPOSITO));
        var e = assertThrows(OperazioneNonValidaException.class,
                () -> c.sedute().prenotaStudente(piccola.getIdSeduta(), studente(ESPOSITO)));
        assertEquals("La seduta è al completo.", e.getMessage());
    }

    @Test
    void seduteDisponibiliSoloFutureEConCommissione() {
        assertEquals(2, c.sedute().getSeduteDisponibili().size()); // SED-001 e SED-002
        assertEquals(4, c.sedute().getAllSedute().size());
    }

    @Test
    void ilCoordinatoreRimuoveUnaPrenotazioneEdEliminaUnaSeduta() {
        c.sedute().rimuoviPrenotazione(SED_TRA_30_GIORNI, studente(ESPOSITO));
        assertTrue(c.sedute().getStudentiPrenotati(SED_TRA_30_GIORNI).isEmpty());

        c.sedute().eliminaSeduta(SED_TRA_30_GIORNI);
        assertNull(c.sedute().getSedutaById(SED_TRA_30_GIORNI));
        assertThrows(OperazioneNonValidaException.class, () -> c.sedute().eliminaSeduta(SED_TRA_30_GIORNI));
    }

    @Test
    void ilDocenteVedeLeSeduteDelleSueCommissioni() {
        assertEquals(3, c.sedute().getSeduteByDocente(DOC_ROSSI).size()); // membro di entrambe le commissioni
        assertEquals(1, c.sedute().getSeduteByDocente(DOC_VERDI).size());
    }
}

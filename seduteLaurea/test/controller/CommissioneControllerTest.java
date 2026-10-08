package controller;

import model.Commissione;
import org.junit.jupiter.api.Test;
import support.DatabaseTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static support.DatiDemo.*;

class CommissioneControllerTest extends DatabaseTest {

    @Test
    void ilPresidenteEntraAutomaticamenteTraIMembri() {
        Commissione nuova = c.commissioni().creaCommissione(DOC_ROSSI, List.of(DOC_BIANCHI, DOC_VERDI));

        Commissione salvata = c.commissioni().getCommissioneById(nuova.getIdCommissione());
        assertEquals(3, salvata.getMembri().size());
        assertTrue(salvata.isMembro(DOC_ROSSI));
        assertEquals(DOC_ROSSI, salvata.getPresidente().getIdDocente());
    }

    @Test
    void servonoAlmenoTreMembri() {
        // il presidente ripetuto tra i membri non conta due volte
        assertThrows(OperazioneNonValidaException.class,
                () -> c.commissioni().creaCommissione(DOC_ROSSI, List.of(DOC_ROSSI, DOC_BIANCHI)));
        assertThrows(OperazioneNonValidaException.class,
                () -> c.commissioni().creaCommissione(null, List.of(DOC_ROSSI, DOC_BIANCHI, DOC_VERDI)));
    }

    @Test
    void conUnDocenteInesistenteNonVieneCreatoNulla() {
        int prima = c.commissioni().getAllCommissioni().size();
        assertThrows(OperazioneNonValidaException.class,
                () -> c.commissioni().creaCommissione(DOC_ROSSI, List.of(DOC_BIANCHI, "DOC-XYZ")));
        assertEquals(prima, c.commissioni().getAllCommissioni().size());
    }

    @Test
    void gestioneMembri() {
        c.commissioni().aggiungiMembro(COMM_FERRARA, DOC_VERDI);
        assertEquals(4, c.commissioni().getCommissioneById(COMM_FERRARA).getMembri().size());
        assertThrows(OperazioneNonValidaException.class, () -> c.commissioni().aggiungiMembro(COMM_FERRARA, DOC_VERDI));

        c.commissioni().rimuoviMembro(COMM_FERRARA, DOC_VERDI);
        assertFalse(c.commissioni().getCommissioneById(COMM_FERRARA).isMembro(DOC_VERDI));
    }

    @Test
    void nonSiRimuoveIlPresidenteNeSiScendeSottoIlMinimo() {
        assertThrows(OperazioneNonValidaException.class,
                () -> c.commissioni().rimuoviMembro(COMM_FERRARA, COORDINATRICE_FERRARA));
        assertThrows(OperazioneNonValidaException.class,
                () -> c.commissioni().rimuoviMembro(COMM_FERRARA, DOC_ROSSI)); // ha esattamente 3 membri
    }

    @Test
    void ilNuovoPresidenteDeveEssereMembro() {
        assertThrows(OperazioneNonValidaException.class, () -> c.commissioni().cambiaPresidente(COMM_FERRARA, DOC_VERDI));

        c.commissioni().cambiaPresidente(COMM_FERRARA, DOC_BIANCHI);
        assertEquals(DOC_BIANCHI, c.commissioni().getCommissioneById(COMM_FERRARA).getIdPresidente());
    }

    @Test
    void nonSiEliminaUnaCommissioneAssegnataAUnaSeduta() {
        assertThrows(OperazioneNonValidaException.class, () -> c.commissioni().eliminaCommissione(COMM_FERRARA));

        Commissione libera = c.commissioni().creaCommissione(DOC_VERDI, List.of(DOC_ROSSI, DOC_BIANCHI));
        c.commissioni().eliminaCommissione(libera.getIdCommissione());
        assertNull(c.commissioni().getCommissioneById(libera.getIdCommissione()));
    }

    @Test
    void commissioniDiUnDocente() {
        assertEquals(2, c.commissioni().getCommissioniByDocente(DOC_ROSSI).size());
        assertEquals(1, c.commissioni().getCommissioniByDocente(DOC_VERDI).size());
    }
}

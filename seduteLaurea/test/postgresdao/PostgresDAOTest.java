package postgresdao;

import model.Commissione;
import model.Docente;
import model.SedutaLaurea;
import model.Studente;
import org.junit.jupiter.api.Test;
import support.DatabaseTest;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static support.DatiDemo.*;

/**
 * Verifica i punti dei DAO che nel progetto originale erano difettosi.
 */
class PostgresDAOTest extends DatabaseTest {

    @Test
    void dateEOreVengonoSalvateNelleColonneDateTime() {
        SedutaLaureaPostgresDAO dao = new SedutaLaureaPostgresDAO(connection);
        SedutaLaurea s = new SedutaLaurea("SED-T1", LocalDate.of(2030, 2, 28), LocalTime.of(8, 45), "Aula", null, 3);

        assertTrue(dao.aggiungiSeduta(s)); // con setString PostgreSQL rifiutava l'inserimento
        SedutaLaurea letta = dao.getSedutaById("SED-T1");
        assertEquals(LocalDate.of(2030, 2, 28), letta.getDataSeduta());
        assertEquals(LocalTime.of(8, 45), letta.getOraSeduta());
    }

    @Test
    void getCoordinatoreUsaIlRuoloDellUtente() {
        Docente coordinatore = new DocentePostgresDAO(connection).getCoordinatore();
        assertEquals(COORDINATRICE_FERRARA, coordinatore.getIdDocente());
    }

    @Test
    void laCommissioneVieneCaricataConIMembri() {
        Commissione c = new CommissionePostgresDAO(connection).getCommissioneById(COMM_FERRARA);
        assertEquals(3, c.getMembri().size());
        assertEquals("Ferrara", c.getPresidente().getCognome());
    }

    @Test
    void laPrenotazioneNelDAONonSuperaIPosti() {
        SedutaLaureaPostgresDAO dao = new SedutaLaureaPostgresDAO(connection);
        StudentePostgresDAO studenti = new StudentePostgresDAO(connection);
        dao.aggiungiSeduta(new SedutaLaurea("SED-T2", LocalDate.of(2030, 1, 10), LocalTime.of(9, 0), "Aula", COMM_VERDI, 1));

        assertTrue(dao.prenotaStudente("SED-T2", studenti.getStudenteByMatricola(RICCI)));
        assertFalse(dao.prenotaStudente("SED-T2", studenti.getStudenteByMatricola(COLOMBO)));
        assertEquals(1, dao.contaPrenotati("SED-T2"));
        assertFalse(dao.prenotaStudente("SED-INESISTENTE", studenti.getStudenteByMatricola(COLOMBO)));
    }

    @Test
    void dueConnessioniNonPrenotanoEntrambeLUltimoPosto() throws Exception {
        for (int giro = 0; giro < 10; giro++) {
            String idSeduta = "SED-C" + giro;
            new SedutaLaureaPostgresDAO(connection).aggiungiSeduta(
                    new SedutaLaurea(idSeduta, LocalDate.of(2030, 1, 10), LocalTime.of(9, 0), "Aula", COMM_VERDI, 1));
            new SedutaLaureaPostgresDAO(connection).rimuoviStudente(SED_TRA_30_GIORNI,
                    new StudentePostgresDAO(connection).getStudenteByMatricola(ESPOSITO));

            ExecutorService pool = Executors.newFixedThreadPool(2);
            CountDownLatch via = new CountDownLatch(1);
            List<Future<Boolean>> esiti = new ArrayList<>();
            for (String matricola : List.of(RICCI, ESPOSITO)) {
                esiti.add(pool.submit(() -> {
                    try (Connection propria = nuovaConnessione()) {
                        SedutaLaureaPostgresDAO dao = new SedutaLaureaPostgresDAO(propria);
                        Studente studente = new StudentePostgresDAO(propria).getStudenteByMatricola(matricola);
                        via.await();
                        return dao.prenotaStudente(idSeduta, studente);
                    }
                }));
            }
            via.countDown();
            int riuscite = 0;
            for (Future<Boolean> esito : esiti) {
                riuscite += esito.get(10, TimeUnit.SECONDS) ? 1 : 0;
            }
            pool.shutdown();

            assertEquals(1, riuscite, "giro " + giro);
            assertEquals(1, new SedutaLaureaPostgresDAO(connection).contaPrenotati(idSeduta));
            // libera gli studenti per il giro successivo
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DELETE FROM prenotazioni_seduta WHERE id_seduta = '" + idSeduta + "'");
            }
        }
    }

    @Test
    void ricercheSenzaRisultatoRestituisconoNull() {
        assertNull(new UserPostgresDAO(connection).getUserByUsername("inesistente"));
        assertNull(new TesiPostgresDAO(connection).getTesiById("TESI-XYZ"));
        assertNull(new SedutaLaureaPostgresDAO(connection).getSedutaPrenotata(COLOMBO));
        assertNull(new CommissionePostgresDAO(connection).getCommissioneById(null));
    }
}

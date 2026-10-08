package controller;

import dao.DocenteDAO;
import dao.TesiDAO;
import model.Docente;
import model.StatoRichiesta;
import model.Studente;
import model.Tesi;
import util.IdGenerator;

import java.util.List;

import static controller.Regole.obbligatorio;
import static controller.Regole.verifica;

public class TesiController {

    private final TesiDAO tesiDAO;
    private final DocenteDAO docenteDAO;

    public TesiController(TesiDAO tesiDAO, DocenteDAO docenteDAO) {
        this.tesiDAO = tesiDAO;
        this.docenteDAO = docenteDAO;
    }

    // RICHIESTA TESI: una sola tesi attiva (in attesa o approvata) per studente
    public Tesi richiediTesi(Studente studente, String titolo, String idDocenteRelatore) {
        titolo = obbligatorio(titolo, "Titolo");
        verifica(idDocenteRelatore != null && docenteDAO.getDocenteByIdDocente(idDocenteRelatore) != null,
                "Docente relatore non trovato.");
        verifica(getTesiCorrente(studente.getMatricola()) == null,
                "Hai già una richiesta di tesi in attesa o approvata.");

        Tesi nuova = new Tesi(IdGenerator.nuovoId("TESI"), studente.getMatricola(), idDocenteRelatore,
                titolo, StatoRichiesta.IN_ATTESA);
        tesiDAO.aggiungiTesi(nuova);
        return nuova;
    }

    // RITIRO TESI: solo la propria e solo finché è in attesa
    public void ritiraTesi(Studente studente, String idTesi) {
        Tesi tesi = tesi(idTesi);
        verifica(tesi.getMatricolaStudente().equals(studente.getMatricola()), "Puoi ritirare solo la tua tesi.");
        verifica(tesi.getStato() == StatoRichiesta.IN_ATTESA, "Puoi ritirare solo una richiesta ancora in attesa.");
        tesiDAO.eliminaTesi(idTesi);
    }

    // APPROVAZIONE / RIFIUTO da parte del relatore
    public void approvaTesi(Docente relatore, String idTesi) {
        valuta(relatore, idTesi, StatoRichiesta.APPROVATA);
    }

    public void rifiutaTesi(Docente relatore, String idTesi) {
        valuta(relatore, idTesi, StatoRichiesta.RIFIUTATA);
    }

    // LISTA COMPLETA TESI
    public List<Tesi> getAllTesi() {
        return tesiDAO.getAllTesi();
    }

    // STORICO TESI DI UNO STUDENTE (dalla più recente)
    public List<Tesi> getTesiByStudente(String matricolaStudente) {
        return tesiDAO.getTesiByStudente(matricolaStudente);
    }

    // TESI DI CUI IL DOCENTE È RELATORE
    public List<Tesi> getTesiByRelatore(String idDocente) {
        return tesiDAO.getTesiByRelatore(idDocente);
    }

    // TESI ATTIVA (in attesa o approvata) dello studente, oppure null
    public Tesi getTesiCorrente(String matricolaStudente) {
        return tesiDAO.getTesiByStudente(matricolaStudente).stream()
                .filter(t -> t.getStato() != StatoRichiesta.RIFIUTATA)
                .findFirst()
                .orElse(null);
    }

    public boolean haTesiApprovata(String matricolaStudente) {
        Tesi corrente = getTesiCorrente(matricolaStudente);
        return corrente != null && corrente.getStato() == StatoRichiesta.APPROVATA;
    }

    private void valuta(Docente relatore, String idTesi, StatoRichiesta esito) {
        Tesi tesi = tesi(idTesi);
        verifica(tesi.getIdDocenteRelatore().equals(relatore.getIdDocente()),
                "Puoi valutare solo le tesi di cui sei relatore.");
        verifica(tesi.getStato() == StatoRichiesta.IN_ATTESA, "La tesi è già stata valutata.");
        tesiDAO.aggiornaStatoTesi(idTesi, esito);
    }

    private Tesi tesi(String idTesi) {
        Tesi tesi = tesiDAO.getTesiById(idTesi);
        verifica(tesi != null, "Tesi non trovata.");
        return tesi;
    }
}

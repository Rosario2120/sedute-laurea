package controller;

import dao.RichiestaTirocinioDAO;
import dao.TirocinioDAO;
import model.Docente;
import model.RichiestaTirocinio;
import model.StatoRichiesta;
import model.Studente;
import model.Tirocinio;
import util.IdGenerator;

import java.util.List;

import static controller.Regole.verifica;

public class RichiestaTirocinioController {

    private final RichiestaTirocinioDAO richiestaDAO;
    private final TirocinioDAO tirocinioDAO;

    public RichiestaTirocinioController(RichiestaTirocinioDAO richiestaDAO, TirocinioDAO tirocinioDAO) {
        this.richiestaDAO = richiestaDAO;
        this.tirocinioDAO = tirocinioDAO;
    }

    // INVIO RICHIESTA da parte dello studente
    public RichiestaTirocinio inviaRichiesta(Studente studente, String idTirocinio) {
        verifica(tirocinioDAO.getTirocinioById(idTirocinio) != null, "Tirocinio non trovato.");

        List<RichiestaTirocinio> mie = richiestaDAO.getRichiesteByStudente(studente.getMatricola());
        verifica(mie.stream().noneMatch(r -> r.getStato() == StatoRichiesta.APPROVATA),
                "Hai già un tirocinio approvato: non puoi richiederne altri.");
        verifica(mie.stream().noneMatch(r -> r.getIdTirocinio().equals(idTirocinio)
                        && r.getStato() == StatoRichiesta.IN_ATTESA),
                "Hai già una richiesta in attesa per questo tirocinio.");

        RichiestaTirocinio nuova = new RichiestaTirocinio(IdGenerator.nuovoId("RICH"),
                studente.getMatricola(), idTirocinio, StatoRichiesta.IN_ATTESA);
        richiestaDAO.aggiungiRichiesta(nuova);
        return nuova;
    }

    // RITIRO RICHIESTA: solo le proprie e solo finché sono in attesa
    public void ritiraRichiesta(Studente studente, String idRichiesta) {
        RichiestaTirocinio richiesta = richiesta(idRichiesta);
        verifica(richiesta.getMatricolaStudente().equals(studente.getMatricola()),
                "Puoi ritirare solo le tue richieste.");
        verifica(richiesta.getStato() == StatoRichiesta.IN_ATTESA,
                "Puoi ritirare solo richieste ancora in attesa.");
        richiestaDAO.eliminaRichiesta(idRichiesta);
    }

    // APPROVAZIONE / RIFIUTO da parte del docente che ha proposto il tirocinio
    public void approvaRichiesta(Docente docente, String idRichiesta) {
        valuta(docente, idRichiesta, StatoRichiesta.APPROVATA);
    }

    public void rifiutaRichiesta(Docente docente, String idRichiesta) {
        valuta(docente, idRichiesta, StatoRichiesta.RIFIUTATA);
    }

    // LISTA COMPLETA RICHIESTE
    public List<RichiestaTirocinio> getAllRichieste() {
        return richiestaDAO.getAllRichieste();
    }

    // RICHIESTE DI UNO STUDENTE
    public List<RichiestaTirocinio> getRichiesteByStudente(String matricolaStudente) {
        return richiestaDAO.getRichiesteByStudente(matricolaStudente);
    }

    // RICHIESTE RICEVUTE DA UN DOCENTE
    public List<RichiestaTirocinio> getRichiesteByDocente(String idDocente) {
        return richiestaDAO.getRichiesteByDocente(idDocente);
    }

    private void valuta(Docente docente, String idRichiesta, StatoRichiesta esito) {
        RichiestaTirocinio richiesta = richiesta(idRichiesta);
        Tirocinio tirocinio = tirocinioDAO.getTirocinioById(richiesta.getIdTirocinio());
        verifica(tirocinio != null && tirocinio.getIdDocente().equals(docente.getIdDocente()),
                "Puoi valutare solo le richieste per i tirocini che hai proposto.");
        verifica(richiesta.getStato() == StatoRichiesta.IN_ATTESA, "La richiesta è già stata valutata.");

        if (esito == StatoRichiesta.APPROVATA) {
            boolean giaApprovato = richiestaDAO.getRichiesteByStudente(richiesta.getMatricolaStudente()).stream()
                    .anyMatch(r -> r.getStato() == StatoRichiesta.APPROVATA);
            verifica(!giaApprovato, "Lo studente ha già un altro tirocinio approvato.");
        }
        richiestaDAO.aggiornaStatoRichiesta(idRichiesta, esito);
    }

    private RichiestaTirocinio richiesta(String idRichiesta) {
        RichiestaTirocinio richiesta = richiestaDAO.getRichiestaById(idRichiesta);
        verifica(richiesta != null, "Richiesta non trovata.");
        return richiesta;
    }
}

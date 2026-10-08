package controller;

import dao.RichiestaTirocinioDAO;
import dao.TirocinioDAO;
import model.Docente;
import model.StatoRichiesta;
import model.Tirocinio;
import util.IdGenerator;

import java.util.List;

import static controller.Regole.obbligatorio;
import static controller.Regole.verifica;

public class TirocinioController {

    private final TirocinioDAO tirocinioDAO;
    private final RichiestaTirocinioDAO richiestaDAO;

    public TirocinioController(TirocinioDAO tirocinioDAO, RichiestaTirocinioDAO richiestaDAO) {
        this.tirocinioDAO = tirocinioDAO;
        this.richiestaDAO = richiestaDAO;
    }

    // NUOVO TIROCINIO proposto da un docente
    public Tirocinio creaTirocinio(Docente docente, String titolo, String descrizione) {
        Tirocinio nuovo = new Tirocinio(IdGenerator.nuovoId("TIR"), obbligatorio(titolo, "Titolo"),
                descrizione == null ? "" : descrizione.trim(), docente.getIdDocente());
        tirocinioDAO.aggiungiTirocinio(nuovo);
        return nuovo;
    }

    // MODIFICA TIROCINIO: solo il docente che l'ha proposto
    public void modificaTirocinio(Docente docente, String idTirocinio, String titolo, String descrizione) {
        Tirocinio tirocinio = tirocinioDelDocente(docente, idTirocinio);
        tirocinio.setTitolo(obbligatorio(titolo, "Titolo"));
        tirocinio.setDescrizione(descrizione == null ? "" : descrizione.trim());
        tirocinioDAO.aggiornaTirocinio(tirocinio);
    }

    // ELIMINA TIROCINIO: vietato se c'è già uno studente approvato
    public void eliminaTirocinio(Docente docente, String idTirocinio) {
        tirocinioDelDocente(docente, idTirocinio);
        boolean conApprovati = richiestaDAO.getRichiesteByTirocinio(idTirocinio).stream()
                .anyMatch(r -> r.getStato() == StatoRichiesta.APPROVATA);
        verifica(!conApprovati, "Impossibile eliminare: il tirocinio ha già studenti approvati.");
        tirocinioDAO.eliminaTirocinio(idTirocinio); // le richieste in attesa/rifiutate vengono eliminate a cascata
    }

    // LISTA COMPLETA TIROCINI
    public List<Tirocinio> getAllTirocini() {
        return tirocinioDAO.getAllTirocini();
    }

    // TIROCINI DI UN DOCENTE
    public List<Tirocinio> getTirociniByDocente(String idDocente) {
        return tirocinioDAO.getTirociniByDocente(idDocente);
    }

    public Tirocinio getTirocinioById(String idTirocinio) {
        return tirocinioDAO.getTirocinioById(idTirocinio);
    }

    private Tirocinio tirocinioDelDocente(Docente docente, String idTirocinio) {
        Tirocinio tirocinio = tirocinioDAO.getTirocinioById(idTirocinio);
        verifica(tirocinio != null, "Tirocinio non trovato.");
        verifica(tirocinio.getIdDocente().equals(docente.getIdDocente()),
                "Puoi modificare solo i tirocini che hai proposto.");
        return tirocinio;
    }
}

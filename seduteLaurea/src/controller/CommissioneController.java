package controller;

import dao.CommissioneDAO;
import dao.DocenteDAO;
import dao.SedutaLaureaDAO;
import model.Commissione;
import model.Docente;
import util.IdGenerator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static controller.Regole.verifica;

public class CommissioneController {

    // Numero minimo di docenti in una commissione, presidente incluso
    public static final int MIN_MEMBRI = 3;

    private final CommissioneDAO commissioneDAO;
    private final DocenteDAO docenteDAO;
    private final SedutaLaureaDAO sedutaDAO;

    public CommissioneController(CommissioneDAO commissioneDAO, DocenteDAO docenteDAO, SedutaLaureaDAO sedutaDAO) {
        this.commissioneDAO = commissioneDAO;
        this.docenteDAO = docenteDAO;
        this.sedutaDAO = sedutaDAO;
    }

    // CREA NUOVA COMMISSIONE: il presidente viene aggiunto automaticamente ai membri
    public Commissione creaCommissione(String idPresidente, List<String> idMembri) {
        verifica(idPresidente != null, "Seleziona il presidente della commissione.");
        Set<String> ids = new LinkedHashSet<>();
        ids.add(idPresidente);
        ids.addAll(idMembri);
        verifica(ids.size() >= MIN_MEMBRI,
                "Una commissione deve avere almeno " + MIN_MEMBRI + " membri, presidente incluso.");

        List<Docente> membri = new ArrayList<>();
        for (String id : ids) {
            membri.add(docente(id));
        }

        Commissione nuova = new Commissione(IdGenerator.nuovoId("COMM"), idPresidente);
        commissioneDAO.aggiungiCommissione(nuova);
        try {
            for (Docente membro : membri) {
                commissioneDAO.aggiungiMembro(nuova.getIdCommissione(), membro);
                nuova.aggiungiMembro(membro);
            }
        } catch (RuntimeException e) {
            try {
                commissioneDAO.eliminaCommissione(nuova.getIdCommissione()); // i membri si cancellano a cascata
            } catch (RuntimeException annullamento) {
                e.addSuppressed(annullamento);
            }
            throw e;
        }
        return nuova;
    }

    // AGGIUNGI MEMBRO ALLA COMMISSIONE
    public void aggiungiMembro(String idCommissione, String idDocente) {
        Commissione commissione = commissione(idCommissione);
        verifica(!commissione.isMembro(idDocente), "Il docente fa già parte della commissione.");
        commissioneDAO.aggiungiMembro(idCommissione, docente(idDocente));
    }

    // RIMUOVI MEMBRO: non il presidente e senza scendere sotto il minimo
    public void rimuoviMembro(String idCommissione, String idDocente) {
        Commissione commissione = commissione(idCommissione);
        verifica(commissione.isMembro(idDocente), "Il docente non fa parte della commissione.");
        verifica(!commissione.getIdPresidente().equals(idDocente),
                "Non puoi rimuovere il presidente: nomina prima un altro presidente.");
        verifica(commissione.getMembri().size() > MIN_MEMBRI,
                "La commissione deve mantenere almeno " + MIN_MEMBRI + " membri.");
        commissioneDAO.rimuoviMembro(idCommissione, docente(idDocente));
    }

    // CAMBIA PRESIDENTE: deve essere già membro della commissione
    public void cambiaPresidente(String idCommissione, String idNuovoPresidente) {
        Commissione commissione = commissione(idCommissione);
        verifica(commissione.isMembro(idNuovoPresidente), "Il nuovo presidente deve essere membro della commissione.");
        commissione.setIdPresidente(idNuovoPresidente);
        commissioneDAO.aggiornaCommissione(commissione);
    }

    // ELIMINA COMMISSIONE: solo se non è assegnata a nessuna seduta
    public void eliminaCommissione(String idCommissione) {
        commissione(idCommissione);
        verifica(sedutaDAO.getSeduteByCommissione(idCommissione).isEmpty(),
                "La commissione è assegnata a una o più sedute: assegna prima un'altra commissione.");
        commissioneDAO.eliminaCommissione(idCommissione);
    }

    // LISTA COMPLETA COMMISSIONI
    public List<Commissione> getAllCommissioni() {
        return commissioneDAO.getAllCommissioni();
    }

    public Commissione getCommissioneById(String idCommissione) {
        return commissioneDAO.getCommissioneById(idCommissione);
    }

    // COMMISSIONI DI CUI IL DOCENTE FA PARTE
    public List<Commissione> getCommissioniByDocente(String idDocente) {
        return commissioneDAO.getCommissioniByDocente(idDocente);
    }

    private Commissione commissione(String idCommissione) {
        Commissione commissione = commissioneDAO.getCommissioneById(idCommissione);
        verifica(commissione != null, "Commissione non trovata.");
        return commissione;
    }

    private Docente docente(String idDocente) {
        Docente docente = docenteDAO.getDocenteByIdDocente(idDocente);
        verifica(docente != null, "Docente non trovato: " + idDocente);
        return docente;
    }
}

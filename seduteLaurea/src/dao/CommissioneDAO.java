package dao;

import model.Commissione;
import model.Docente;

import java.util.List;

public interface CommissioneDAO {

    // Aggiunge una nuova commissione
    boolean aggiungiCommissione(Commissione commissione);

    // Recupera una commissione (con i membri) tramite id_commissione
    Commissione getCommissioneById(String idCommissione);

    // Restituisce tutte le commissioni presenti nel sistema
    List<Commissione> getAllCommissioni();

    // Restituisce le commissioni di cui il docente fa parte
    List<Commissione> getCommissioniByDocente(String idDocente);

    // Aggiorna completamente una commissione
    boolean aggiornaCommissione(Commissione commissione);

    // Elimina una commissione dal sistema
    boolean eliminaCommissione(String idCommissione);

    // Aggiunge un docente alla commissione
    boolean aggiungiMembro(String idCommissione, Docente docente);

    // Rimuove un docente dalla commissione
    boolean rimuoviMembro(String idCommissione, Docente docente);

    // Restituisce tutti i membri della commissione
    List<Docente> getMembriCommissione(String idCommissione);
}

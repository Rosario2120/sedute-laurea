package dao;

import model.StatoRichiesta;
import model.Tesi;

import java.util.List;

public interface TesiDAO {

    // Aggiunge una nuova tesi nel sistema
    boolean aggiungiTesi(Tesi tesi);

    // Recupera una tesi tramite id_tesi
    Tesi getTesiById(String idTesi);

    // Restituisce tutte le tesi presenti nel sistema
    List<Tesi> getAllTesi();

    // Restituisce lo storico delle tesi di uno studente, dalla più recente
    List<Tesi> getTesiByStudente(String matricolaStudente);

    // Restituisce le tesi di cui il docente è relatore
    List<Tesi> getTesiByRelatore(String idDocente);

    // Aggiorna completamente una tesi
    boolean aggiornaTesi(Tesi tesi);

    // Aggiorna lo stato della tesi
    boolean aggiornaStatoTesi(String idTesi, StatoRichiesta nuovoStato);

    // Elimina una tesi dal sistema
    boolean eliminaTesi(String idTesi);
}

package dao;

import model.RichiestaTirocinio;
import model.StatoRichiesta;

import java.util.List;

public interface RichiestaTirocinioDAO {

    // Aggiunge una nuova richiesta di tirocinio
    boolean aggiungiRichiesta(RichiestaTirocinio richiesta);

    // Recupera una richiesta tramite id_richiesta
    RichiestaTirocinio getRichiestaById(String idRichiesta);

    // Restituisce tutte le richieste presenti nel sistema
    List<RichiestaTirocinio> getAllRichieste();

    // Restituisce tutte le richieste di un determinato studente
    List<RichiestaTirocinio> getRichiesteByStudente(String matricolaStudente);

    // Restituisce tutte le richieste relative a un tirocinio
    List<RichiestaTirocinio> getRichiesteByTirocinio(String idTirocinio);

    // Restituisce le richieste per i tirocini proposti da un docente
    List<RichiestaTirocinio> getRichiesteByDocente(String idDocente);

    // Aggiorna lo stato della richiesta
    boolean aggiornaStatoRichiesta(String idRichiesta, StatoRichiesta nuovoStato);

    // Aggiorna completamente una richiesta
    boolean aggiornaRichiesta(RichiestaTirocinio richiesta);

    // Elimina una richiesta
    boolean eliminaRichiesta(String idRichiesta);
}

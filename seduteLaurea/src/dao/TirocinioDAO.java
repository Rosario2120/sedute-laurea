package dao;

import model.Tirocinio;

import java.util.List;

public interface TirocinioDAO {

    // Aggiunge un nuovo tirocinio nel sistema
    boolean aggiungiTirocinio(Tirocinio tirocinio);

    // Recupera un tirocinio tramite id_tirocinio
    Tirocinio getTirocinioById(String idTirocinio);

    // Restituisce tutti i tirocini presenti nel sistema
    List<Tirocinio> getAllTirocini();

    // Restituisce i tirocini proposti da un docente
    List<Tirocinio> getTirociniByDocente(String idDocente);

    // Aggiorna i dati di un tirocinio
    boolean aggiornaTirocinio(Tirocinio tirocinio);

    // Elimina un tirocinio tramite id
    boolean eliminaTirocinio(String idTirocinio);
}

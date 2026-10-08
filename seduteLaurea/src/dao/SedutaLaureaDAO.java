package dao;

import model.SedutaLaurea;
import model.Studente;

import java.util.List;

public interface SedutaLaureaDAO {

    // Aggiunge una nuova seduta di laurea
    boolean aggiungiSeduta(SedutaLaurea seduta);

    // Recupera una seduta tramite id_seduta
    SedutaLaurea getSedutaById(String idSeduta);

    // Restituisce tutte le sedute presenti nel sistema, in ordine di data
    List<SedutaLaurea> getAllSedute();

    // Restituisce le sedute assegnate a una commissione
    List<SedutaLaurea> getSeduteByCommissione(String idCommissione);

    // Restituisce le sedute delle commissioni di cui il docente è membro
    List<SedutaLaurea> getSeduteByDocente(String idDocente);

    // Aggiorna completamente una seduta
    boolean aggiornaSeduta(SedutaLaurea seduta);

    // Elimina una seduta dal sistema (e le relative prenotazioni)
    boolean eliminaSeduta(String idSeduta);

    // Prenota uno studente a una seduta
    boolean prenotaStudente(String idSeduta, Studente studente);

    // Rimuove uno studente prenotato da una seduta
    boolean rimuoviStudente(String idSeduta, Studente studente);

    // Restituisce tutti gli studenti prenotati a una seduta
    List<Studente> getStudentiPrenotati(String idSeduta);

    // Numero di studenti prenotati a una seduta
    int contaPrenotati(String idSeduta);

    // Seduta a cui lo studente è prenotato, oppure null
    SedutaLaurea getSedutaPrenotata(String matricolaStudente);
}

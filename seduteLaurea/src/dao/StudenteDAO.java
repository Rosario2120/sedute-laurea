package dao;

import model.Studente;

import java.util.List;

public interface StudenteDAO {

    // Registra un nuovo studente nel sistema
    boolean registraStudente(Studente studente);

    // Recupera uno studente tramite matricola
    Studente getStudenteByMatricola(String matricola);

    // Recupera uno studente tramite id_user (collegamento a User)
    Studente getStudenteByIdUser(String idUser);

    // Restituisce tutti gli studenti registrati
    List<Studente> getAllStudenti();

    // Aggiorna i dati di uno studente
    boolean aggiornaStudente(Studente studente);

    // Elimina uno studente dal sistema
    boolean eliminaStudente(String matricola);
}

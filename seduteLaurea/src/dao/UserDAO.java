package dao;

import model.User;

import java.util.List;

public interface UserDAO {

    // Registra un nuovo utente nel sistema
    boolean registraUtente(User user);

    // Recupera un utente tramite il suo ID
    User getUserById(String idUser);

    // Recupera un utente tramite username (usato dal login per verificare l'hash)
    User getUserByUsername(String username);

    // Controlla se un username è già presente nel sistema
    boolean usernameEsiste(String username);

    // Restituisce tutti gli utenti registrati
    List<User> getAllUtenti();

    // Sostituisce l'hash della password
    boolean aggiornaPassword(String idUser, String nuovoPasswordHash);

    // Elimina un utente (e a cascata lo studente/docente collegato)
    boolean eliminaUtente(String idUser);
}

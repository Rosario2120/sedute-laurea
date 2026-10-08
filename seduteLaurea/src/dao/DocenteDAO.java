package dao;

import model.Docente;

import java.util.List;

public interface DocenteDAO {

    // Registra un nuovo docente nel sistema
    boolean registraDocente(Docente docente);

    // Recupera un docente tramite id_docente
    Docente getDocenteByIdDocente(String idDocente);

    // Recupera un docente tramite id_user (collegamento a User)
    Docente getDocenteByIdUser(String idUser);

    // Restituisce tutti i docenti
    List<Docente> getAllDocenti();

    // Restituisce il coordinatore del corso di laurea
    Docente getCoordinatore();

    // Aggiorna i dati del docente
    boolean aggiornaDocente(Docente docente);

    // Elimina un docente dal sistema
    boolean eliminaDocente(String idDocente);
}

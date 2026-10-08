package controller;

import dao.StudenteDAO;
import model.Studente;

import java.util.List;

public class StudenteController {

    private final StudenteDAO studenteDAO;

    public StudenteController(StudenteDAO studenteDAO) {
        this.studenteDAO = studenteDAO;
    }

    // RECUPERA STUDENTE PER ID USER
    public Studente getStudenteByIdUser(String idUser) {
        return studenteDAO.getStudenteByIdUser(idUser);
    }

    // RECUPERA STUDENTE PER MATRICOLA
    public Studente getStudenteByMatricola(String matricola) {
        return studenteDAO.getStudenteByMatricola(matricola);
    }

    // LISTA COMPLETA STUDENTI
    public List<Studente> getAllStudenti() {
        return studenteDAO.getAllStudenti();
    }
}

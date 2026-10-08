package controller;

import dao.DataAccessException;
import dao.DocenteDAO;
import dao.StudenteDAO;
import dao.UserDAO;
import model.Docente;
import model.Ruolo;
import model.Studente;
import model.User;
import util.IdGenerator;
import util.PasswordHasher;

import java.util.List;
import java.util.regex.Pattern;

import static controller.Regole.obbligatorio;
import static controller.Regole.verifica;

public class UserController {

    public static final int LUNGHEZZA_MINIMA_PASSWORD = 6;
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserDAO userDAO;
    private final StudenteDAO studenteDAO;
    private final DocenteDAO docenteDAO;

    public UserController(UserDAO userDAO, StudenteDAO studenteDAO, DocenteDAO docenteDAO) {
        this.userDAO = userDAO;
        this.studenteDAO = studenteDAO;
        this.docenteDAO = docenteDAO;
    }

    // LOGIN: verifica la password confrontandola con l'hash salvato
    public User login(String username, String password) {
        User user = userDAO.getUserByUsername(obbligatorio(username, "Username").toLowerCase());
        if (user == null || !PasswordHasher.verifica(password, user.getPasswordHash())) {
            // stesso messaggio nei due casi, per non rivelare quali username esistono
            throw new OperazioneNonValidaException("Username o password non corretti.");
        }
        return user;
    }

    // REGISTRAZIONE STUDENTE (account + anagrafica)
    public Studente registraStudente(String username, String password, String matricola,
                                     String nome, String cognome, String email) {
        matricola = obbligatorio(matricola, "Matricola").toUpperCase();
        nome = obbligatorio(nome, "Nome");
        cognome = obbligatorio(cognome, "Cognome");
        email = validaEmail(email);
        verifica(studenteDAO.getStudenteByMatricola(matricola) == null, "Matricola già registrata.");
        verificaEmailLibera(email);

        User user = creaUtente(username, password, Ruolo.STUDENTE);
        Studente studente = new Studente(matricola, nome, cognome, email, user.getIdUser());
        try {
            studenteDAO.registraStudente(studente);
        } catch (RuntimeException e) {
            annullaCreazione(user, e); // annulla la creazione dell'account
            throw e;
        }
        return studente;
    }

    // REGISTRAZIONE DOCENTE (o coordinatore, che è un docente con più permessi)
    public Docente registraDocente(String username, String password, String nome, String cognome,
                                   String email, boolean coordinatore) {
        nome = obbligatorio(nome, "Nome");
        cognome = obbligatorio(cognome, "Cognome");
        email = validaEmail(email);
        verificaEmailLibera(email);

        User user = creaUtente(username, password, coordinatore ? Ruolo.COORDINATORE : Ruolo.DOCENTE);
        Docente docente = new Docente(IdGenerator.nuovoId("DOC"), nome, cognome, email, user.getIdUser());
        try {
            docenteDAO.registraDocente(docente);
        } catch (RuntimeException e) {
            annullaCreazione(user, e);
            throw e;
        }
        return docente;
    }

    // LISTA COMPLETA UTENTI
    public List<User> getAllUtenti() {
        return userDAO.getAllUtenti();
    }

    // ELIMINA UTENTE: fallisce se il docente ha tirocini, tesi o commissioni collegati
    public void eliminaUtente(User richiedente, String idUser) {
        verifica(!richiedente.getIdUser().equals(idUser), "Non puoi eliminare il tuo stesso account.");
        try {
            verifica(userDAO.eliminaUtente(idUser), "Utente non trovato.");
        } catch (DataAccessException e) {
            if (e.isViolazioneChiaveEsterna()) {
                throw new OperazioneNonValidaException("Il docente ha tirocini, tesi o commissioni collegati: "
                        + "riassegnali o eliminali prima di eliminare l'account.");
            }
            throw e;
        }
    }

    // CAMBIO PASSWORD
    public void cambiaPassword(User user, String passwordAttuale, String nuovaPassword) {
        User salvato = userDAO.getUserById(user.getIdUser());
        verifica(salvato != null && PasswordHasher.verifica(passwordAttuale, salvato.getPasswordHash()),
                "La password attuale non è corretta.");
        validaPassword(nuovaPassword);

        String nuovoHash = PasswordHasher.hash(nuovaPassword);
        userDAO.aggiornaPassword(user.getIdUser(), nuovoHash);
        user.setPasswordHash(nuovoHash);
    }

    private void annullaCreazione(User user, RuntimeException errore) {
        try {
            userDAO.eliminaUtente(user.getIdUser());
        } catch (RuntimeException e) {
            errore.addSuppressed(e);
        }
    }

    private User creaUtente(String username, String password, Ruolo ruolo) {
        username = obbligatorio(username, "Username").toLowerCase();
        verifica(username.matches("[a-z0-9._-]{3,50}"),
                "Lo username deve avere 3-50 caratteri tra lettere, numeri, punto, trattino e underscore.");
        verifica(!userDAO.usernameEsiste(username), "Username già esistente.");
        validaPassword(password);

        User user = new User(IdGenerator.nuovoId("U"), username, PasswordHasher.hash(password), ruolo);
        userDAO.registraUtente(user);
        return user;
    }

    private void validaPassword(String password) {
        verifica(password != null && password.length() >= LUNGHEZZA_MINIMA_PASSWORD,
                "La password deve avere almeno " + LUNGHEZZA_MINIMA_PASSWORD + " caratteri.");
    }

    private void verificaEmailLibera(String email) {
        boolean usata = studenteDAO.getAllStudenti().stream().anyMatch(s -> s.getEmail().equalsIgnoreCase(email))
                || docenteDAO.getAllDocenti().stream().anyMatch(d -> d.getEmail().equalsIgnoreCase(email));
        verifica(!usata, "Email già registrata.");
    }

    private String validaEmail(String email) {
        email = obbligatorio(email, "Email").toLowerCase();
        verifica(EMAIL.matcher(email).matches(), "Indirizzo email non valido.");
        return email;
    }
}

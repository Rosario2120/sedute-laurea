package gui;

import controller.Controllers;
import model.Docente;
import model.Studente;
import model.Tirocinio;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Finestra base delle aree riservate: intestazione con utente, schede laterali,
 * pulsanti Aggiorna / Cambia password / Esci. Le sottoclassi aggiungono le schede
 * e implementano aggiorna(); al termine del costruttore devono chiamare avvia().
 */
abstract class AreaRiservataFrame extends JFrame {

    protected final User user;
    protected final Controllers controllers;
    protected final JTabbedPane schede = new JTabbedPane(SwingConstants.LEFT);

    private final Runnable onLogout;
    private final JLabel etichettaUtente = new JLabel();

    protected AreaRiservataFrame(String titolo, User user, Controllers controllers, Runnable onLogout) {
        this.user = user;
        this.controllers = controllers;
        this.onLogout = onLogout;

        setTitle("Sedute di Laurea - " + titolo);
        setSize(1100, 650);
        setMinimumSize(new Dimension(900, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        JButton aggiorna = new JButton("Aggiorna");
        JButton cambiaPassword = new JButton("Cambia password");
        JButton esci = new JButton("Esci");
        aggiorna.addActionListener(e -> aggiornaInSicurezza());
        cambiaPassword.addActionListener(e -> cambiaPassword());
        esci.addActionListener(e -> logout());

        JPanel pulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pulsanti.add(aggiorna);
        pulsanti.add(cambiaPassword);
        pulsanti.add(esci);

        JPanel intestazione = new JPanel(new BorderLayout());
        intestazione.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xC8CDD3)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        intestazione.add(etichettaUtente, BorderLayout.WEST);
        intestazione.add(pulsanti, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(intestazione, BorderLayout.NORTH);
        add(schede, BorderLayout.CENTER);
    }

    // Ricarica dal database tutti i dati mostrati nella finestra
    protected abstract void aggiorna();

    protected void impostaIntestazione(String nome, String dettaglio) {
        etichettaUtente.setText("<html><b style='font-size:13pt'>" + nome + "</b><br>" + dettaglio + "</html>");
    }

    // Da chiamare alla fine del costruttore della sottoclasse, quando tutti i campi sono pronti
    protected final void avvia() {
        schede.addChangeListener(e -> aggiornaInSicurezza());
        aggiornaInSicurezza();
    }

    protected void aggiornaInSicurezza() {
        GuiUtils.esegui(this, this::aggiorna, null);
    }

    // L'elemento selezionato è stato eliminato nel frattempo (es. da un altro utente)
    protected void elementoNonPiuDisponibile() {
        JOptionPane.showMessageDialog(this, "L'elemento selezionato non esiste più: i dati vengono aggiornati.",
                "Dati non aggiornati", JOptionPane.INFORMATION_MESSAGE);
        aggiornaInSicurezza();
    }

    // Mappe id -> oggetto (nell'ordine restituito dal DAO) per mostrare nomi al posto degli identificativi
    protected Map<String, Docente> mappaDocenti() {
        return controllers.docenti().getAllDocenti().stream()
                .collect(Collectors.toMap(Docente::getIdDocente, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    protected Map<String, Studente> mappaStudenti() {
        return controllers.studenti().getAllStudenti().stream()
                .collect(Collectors.toMap(Studente::getMatricola, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    protected Map<String, Tirocinio> mappaTirocini() {
        return controllers.tirocini().getAllTirocini().stream()
                .collect(Collectors.toMap(Tirocinio::getIdTirocinio, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    protected static String nomeDi(Docente docente) {
        return docente == null ? "-" : docente.getNomeCompleto();
    }

    protected static String nomeDi(Studente studente) {
        return studente == null ? "-" : studente.getNomeCompleto();
    }

    private void cambiaPassword() {
        JPasswordField attuale = new JPasswordField(20);
        JPasswordField nuova = new JPasswordField(20);
        JPasswordField ripeti = new JPasswordField(20);

        if (!GuiUtils.mostraForm(this, "Cambia password",
                "Password attuale", attuale, "Nuova password", nuova, "Ripeti nuova password", ripeti)) {
            return;
        }
        String nuovaPassword = new String(nuova.getPassword());
        if (!nuovaPassword.equals(new String(ripeti.getPassword()))) {
            JOptionPane.showMessageDialog(this, "Le due password nuove non coincidono.", "Cambia password",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        GuiUtils.esegui(this,
                () -> controllers.utenti().cambiaPassword(user, new String(attuale.getPassword()), nuovaPassword),
                "Password aggiornata.");
    }

    private void logout() {
        dispose();
        onLogout.run();
    }
}

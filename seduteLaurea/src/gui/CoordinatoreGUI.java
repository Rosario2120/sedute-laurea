package gui;

import controller.CommissioneController;
import controller.Controllers;
import model.Commissione;
import model.Docente;
import model.RichiestaTirocinio;
import model.SedutaLaurea;
import model.Studente;
import model.Tesi;
import model.Tirocinio;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Area coordinatore: tutte le funzioni del docente più la gestione di utenti,
 * commissioni e sedute di laurea e una panoramica di tesi e tirocini.
 */
public class CoordinatoreGUI extends DocenteGUI {

    private final TabellaDati tabellaUtenti = new TabellaDati("Username", "Ruolo", "Nome e cognome", "Matricola / ID docente", "Email");
    private final TabellaDati tabellaCommissioni = new TabellaDati("Presidente", "Membri", "Sedute assegnate");
    private final TabellaDati tabellaTutteSedute = new TabellaDati("Data", "Ora", "Luogo", "Commissione", "Prenotati", "Stato");
    private final TabellaDati tabellaTutteTesi = new TabellaDati("Studente", "Matricola", "Titolo", "Relatore", "Stato");
    private final TabellaDati tabellaTutteRichieste = new TabellaDati("Studente", "Matricola", "Tirocinio", "Docente", "Stato");

    public CoordinatoreGUI(User user, Controllers controllers, Runnable onLogout) {
        super("Area Coordinatore", user, controllers, onLogout);

        // Le schede di gestione vengono prima di quelle "da docente"
        schede.insertTab("Utenti", null, creaSchedaUtenti(), null, 0);
        schede.insertTab("Commissioni", null, creaSchedaCommissioni(), null, 1);
        schede.insertTab("Sedute di laurea", null, creaSchedaSedute(), null, 2);
        schede.insertTab("Panoramica", null, creaSchedaPanoramica(), null, 3);
        schede.setSelectedIndex(0);

        avvia();
    }

    // ============================
    // UTENTI
    // ============================
    private JPanel creaSchedaUtenti() {
        JButton nuovoStudente = new JButton("Nuovo studente");
        JButton nuovoDocente = new JButton("Nuovo docente");
        JButton elimina = new JButton("Elimina utente");

        nuovoStudente.addActionListener(e -> {
            JTextField username = new JTextField(20);
            JPasswordField password = new JPasswordField(20);
            JTextField matricola = new JTextField(20);
            JTextField nome = new JTextField(20);
            JTextField cognome = new JTextField(20);
            JTextField email = new JTextField(20);
            if (GuiUtils.mostraForm(this, "Nuovo studente", "Username", username, "Password iniziale", password,
                    "Matricola", matricola, "Nome", nome, "Cognome", cognome, "Email", email)
                    && GuiUtils.esegui(this, () -> controllers.utenti().registraStudente(username.getText(),
                    new String(password.getPassword()), matricola.getText(), nome.getText(), cognome.getText(),
                    email.getText()), "Studente registrato.")) {
                aggiornaInSicurezza();
            }
        });
        nuovoDocente.addActionListener(e -> {
            JTextField username = new JTextField(20);
            JPasswordField password = new JPasswordField(20);
            JTextField nome = new JTextField(20);
            JTextField cognome = new JTextField(20);
            JTextField email = new JTextField(20);
            JCheckBox coordinatore = new JCheckBox("Ha il ruolo di coordinatore");
            if (GuiUtils.mostraForm(this, "Nuovo docente", "Username", username, "Password iniziale", password,
                    "Nome", nome, "Cognome", cognome, "Email", email, "Ruolo", coordinatore)
                    && GuiUtils.esegui(this, () -> controllers.utenti().registraDocente(username.getText(),
                    new String(password.getPassword()), nome.getText(), cognome.getText(), email.getText(),
                    coordinatore.isSelected()), "Docente registrato.")) {
                aggiornaInSicurezza();
            }
        });
        elimina.addActionListener(e -> {
            String id = GuiUtils.selezione(this, tabellaUtenti, "un utente");
            if (id != null && GuiUtils.conferma(this, "Eliminare l'utente? Per uno studente verranno cancellate anche "
                    + "richieste, tesi e prenotazioni.")
                    && GuiUtils.esegui(this, () -> controllers.utenti().eliminaUtente(user, id), null)) {
                aggiornaInSicurezza();
            }
        });
        return GuiUtils.pannelloTabella(null, tabellaUtenti, nuovoStudente, nuovoDocente, elimina);
    }

    // ============================
    // COMMISSIONI
    // ============================
    private JPanel creaSchedaCommissioni() {
        JButton nuova = new JButton("Nuova commissione");
        JButton aggiungi = new JButton("Aggiungi membro");
        JButton rimuovi = new JButton("Rimuovi membro");
        JButton presidente = new JButton("Cambia presidente");
        JButton elimina = new JButton("Elimina");

        nuova.addActionListener(e -> nuovaCommissione());
        aggiungi.addActionListener(e -> {
            Commissione c = commissioneSelezionata();
            if (c == null) {
                return;
            }
            List<Docente> candidati = controllers.docenti().getAllDocenti().stream()
                    .filter(d -> !c.isMembro(d.getIdDocente())).toList();
            Docente scelto = scegliDocente("Aggiungi membro", "Docente da aggiungere", candidati);
            if (scelto != null && GuiUtils.esegui(this,
                    () -> controllers.commissioni().aggiungiMembro(c.getIdCommissione(), scelto.getIdDocente()), null)) {
                aggiornaInSicurezza();
            }
        });
        rimuovi.addActionListener(e -> {
            Commissione c = commissioneSelezionata();
            if (c == null) {
                return;
            }
            List<Docente> rimovibili = c.getMembri().stream()
                    .filter(d -> !d.getIdDocente().equals(c.getIdPresidente())).toList();
            Docente scelto = scegliDocente("Rimuovi membro", "Docente da rimuovere", rimovibili);
            if (scelto != null && GuiUtils.esegui(this,
                    () -> controllers.commissioni().rimuoviMembro(c.getIdCommissione(), scelto.getIdDocente()), null)) {
                aggiornaInSicurezza();
            }
        });
        presidente.addActionListener(e -> {
            Commissione c = commissioneSelezionata();
            if (c == null) {
                return;
            }
            Docente scelto = scegliDocente("Cambia presidente", "Nuovo presidente (tra i membri)", c.getMembri());
            if (scelto != null && GuiUtils.esegui(this,
                    () -> controllers.commissioni().cambiaPresidente(c.getIdCommissione(), scelto.getIdDocente()), null)) {
                aggiornaInSicurezza();
            }
        });
        elimina.addActionListener(e -> {
            String id = GuiUtils.selezione(this, tabellaCommissioni, "una commissione");
            if (id != null && GuiUtils.conferma(this, "Eliminare la commissione selezionata?")
                    && GuiUtils.esegui(this, () -> controllers.commissioni().eliminaCommissione(id), null)) {
                aggiornaInSicurezza();
            }
        });
        return GuiUtils.pannelloTabella(null, tabellaCommissioni, nuova, aggiungi, rimuovi, presidente, elimina);
    }

    private void nuovaCommissione() {
        List<Docente> docenti = controllers.docenti().getAllDocenti();
        JComboBox<Docente> presidente = new JComboBox<>(docenti.toArray(new Docente[0]));
        JList<Docente> membri = new JList<>(docenti.toArray(new Docente[0]));
        membri.setVisibleRowCount(Math.min(8, Math.max(3, docenti.size())));
        JScrollPane scroll = new JScrollPane(membri);
        scroll.setPreferredSize(new Dimension(300, 150));

        if (GuiUtils.mostraForm(this, "Nuova commissione", "Presidente", presidente,
                "Altri membri (Ctrl+clic per più scelte)", scroll,
                "Nota", new JLabel("Minimo " + CommissioneController.MIN_MEMBRI + " membri, presidente incluso."))) {
            Docente scelto = (Docente) presidente.getSelectedItem();
            List<String> idMembri = membri.getSelectedValuesList().stream().map(Docente::getIdDocente).toList();
            if (GuiUtils.esegui(this, () -> controllers.commissioni().creaCommissione(
                    scelto == null ? null : scelto.getIdDocente(), idMembri), "Commissione creata.")) {
                aggiornaInSicurezza();
            }
        }
    }

    private Commissione commissioneSelezionata() {
        String id = GuiUtils.selezione(this, tabellaCommissioni, "una commissione");
        Commissione commissione = id == null ? null : controllers.commissioni().getCommissioneById(id);
        if (id != null && commissione == null) {
            elementoNonPiuDisponibile();
        }
        return commissione;
    }

    private Docente scegliDocente(String titolo, String etichetta, List<Docente> scelte) {
        if (scelte.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessun docente disponibile per questa operazione.");
            return null;
        }
        JComboBox<Docente> combo = new JComboBox<>(scelte.toArray(new Docente[0]));
        return GuiUtils.mostraForm(this, titolo, etichetta, combo) ? (Docente) combo.getSelectedItem() : null;
    }

    // ============================
    // SEDUTE DI LAUREA
    // ============================
    private JPanel creaSchedaSedute() {
        JButton nuova = new JButton("Nuova seduta");
        JButton modifica = new JButton("Modifica");
        JButton elimina = new JButton("Elimina");
        JButton prenotati = new JButton("Studenti prenotati");

        nuova.addActionListener(e -> formSeduta(null));
        modifica.addActionListener(e -> {
            String id = GuiUtils.selezione(this, tabellaTutteSedute, "una seduta");
            SedutaLaurea seduta = id == null ? null : controllers.sedute().getSedutaById(id);
            if (seduta != null) {
                formSeduta(seduta);
            } else if (id != null) {
                elementoNonPiuDisponibile(); // mai formSeduta(null): creerebbe una nuova seduta
            }
        });
        elimina.addActionListener(e -> {
            String id = GuiUtils.selezione(this, tabellaTutteSedute, "una seduta");
            if (id == null) {
                return;
            }
            int prenotatiSeduta = controllers.sedute().contaPrenotati(id);
            String avviso = prenotatiSeduta > 0
                    ? "La seduta ha " + prenotatiSeduta + " studenti prenotati: le prenotazioni verranno cancellate. Continuare?"
                    : "Eliminare la seduta selezionata?";
            if (GuiUtils.conferma(this, avviso)
                    && GuiUtils.esegui(this, () -> controllers.sedute().eliminaSeduta(id), null)) {
                aggiornaInSicurezza();
            }
        });
        prenotati.addActionListener(e -> {
            String id = GuiUtils.selezione(this, tabellaTutteSedute, "una seduta");
            SedutaLaurea seduta = id == null ? null : controllers.sedute().getSedutaById(id);
            if (seduta != null) {
                new PrenotatiDialog(this, controllers, seduta, true).setVisible(true);
                aggiornaInSicurezza();
            } else if (id != null) {
                elementoNonPiuDisponibile();
            }
        });
        return GuiUtils.pannelloTabella(null, tabellaTutteSedute, nuova, modifica, elimina, prenotati);
    }

    // Form condiviso per creazione (seduta == null) e modifica
    private void formSeduta(SedutaLaurea seduta) {
        JTextField data = new JTextField(seduta == null ? "" : GuiUtils.formatta(seduta.getDataSeduta()), 10);
        JTextField ora = new JTextField(seduta == null ? "09:00" : GuiUtils.formatta(seduta.getOraSeduta()), 5);
        JTextField luogo = new JTextField(seduta == null ? "" : seduta.getLuogo(), 25);
        JSpinner maxStudenti = new JSpinner(new SpinnerNumberModel(seduta == null ? 10 : seduta.getMaxStudenti(), 1, 200, 1));

        JComboBox<Commissione> commissione = new JComboBox<>();
        commissione.addItem(null); // "da assegnare"
        for (Commissione c : controllers.commissioni().getAllCommissioni()) {
            commissione.addItem(c);
            if (seduta != null && c.getIdCommissione().equals(seduta.getIdCommissione())) {
                commissione.setSelectedItem(c);
            }
        }
        commissione.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, value == null ? "- da assegnare -" : value,
                        index, isSelected, cellHasFocus);
            }
        });

        String titolo = seduta == null ? "Nuova seduta di laurea" : "Modifica seduta di laurea";
        if (!GuiUtils.mostraForm(this, titolo, "Data (gg/mm/aaaa)", data, "Ora (hh:mm)", ora, "Luogo", luogo,
                "Commissione", commissione, "Numero massimo studenti", maxStudenti)) {
            return;
        }
        Commissione scelta = (Commissione) commissione.getSelectedItem();
        String idCommissione = scelta == null ? null : scelta.getIdCommissione();
        int max = (Integer) maxStudenti.getValue();

        boolean ok = GuiUtils.esegui(this, () -> {
            if (seduta == null) {
                controllers.sedute().creaSeduta(GuiUtils.leggiData(data.getText()), GuiUtils.leggiOra(ora.getText()),
                        luogo.getText(), idCommissione, max);
            } else {
                controllers.sedute().modificaSeduta(seduta.getIdSeduta(), GuiUtils.leggiData(data.getText()),
                        GuiUtils.leggiOra(ora.getText()), luogo.getText(), idCommissione, max);
            }
        }, seduta == null ? "Seduta creata." : "Seduta aggiornata.");
        if (ok) {
            aggiornaInSicurezza();
        }
    }

    // ============================
    // PANORAMICA (sola lettura)
    // ============================
    private JComponent creaSchedaPanoramica() {
        JTabbedPane sottoSchede = new JTabbedPane();
        sottoSchede.addTab("Tutte le tesi", GuiUtils.pannelloTabella(null, tabellaTutteTesi));
        sottoSchede.addTab("Tutte le richieste di tirocinio", GuiUtils.pannelloTabella(null, tabellaTutteRichieste));
        return sottoSchede;
    }

    // ============================
    // AGGIORNAMENTO DATI
    // ============================
    @Override
    protected void aggiorna() {
        super.aggiorna();

        Map<String, Docente> docentiPerId = mappaDocenti();
        Map<String, Docente> docentiPerUser = docentiPerId.values().stream()
                .collect(Collectors.toMap(Docente::getIdUser, d -> d));
        Map<String, Studente> studenti = mappaStudenti();
        Map<String, Studente> studentiPerUser = studenti.values().stream()
                .collect(Collectors.toMap(Studente::getIdUser, s -> s));
        Map<String, Tirocinio> tirocini = mappaTirocini();
        List<SedutaLaurea> sedute = controllers.sedute().getAllSedute();

        tabellaUtenti.svuota();
        for (User u : controllers.utenti().getAllUtenti()) {
            Studente s = studentiPerUser.get(u.getIdUser());
            Docente d = docentiPerUser.get(u.getIdUser());
            String nome = s != null ? s.getNomeCompleto() : d != null ? d.getNomeCompleto() : "-";
            String codice = s != null ? s.getMatricola() : d != null ? d.getIdDocente() : "-";
            String email = s != null ? s.getEmail() : d != null ? d.getEmail() : "-";
            tabellaUtenti.aggiungiRiga(u.getIdUser(), u.getUsername(), u.getRuolo(), nome, codice, email);
        }

        tabellaCommissioni.svuota();
        for (Commissione c : controllers.commissioni().getAllCommissioni()) {
            String membri = c.getMembri().stream().map(Docente::getNomeCompleto).collect(Collectors.joining(", "));
            long assegnate = sedute.stream().filter(s -> c.getIdCommissione().equals(s.getIdCommissione())).count();
            tabellaCommissioni.aggiungiRiga(c.getIdCommissione(), nomeDi(c.getPresidente()), membri, assegnate);
        }

        tabellaTutteSedute.svuota();
        for (SedutaLaurea s : sedute) {
            Commissione c = s.getIdCommissione() == null ? null
                    : controllers.commissioni().getCommissioneById(s.getIdCommissione());
            tabellaTutteSedute.aggiungiRiga(s.getIdSeduta(), GuiUtils.formatta(s.getDataSeduta()),
                    GuiUtils.formatta(s.getOraSeduta()), s.getLuogo(),
                    c == null ? "da assegnare" : "pres. " + nomeDi(c.getPresidente()),
                    controllers.sedute().contaPrenotati(s.getIdSeduta()) + " / " + s.getMaxStudenti(),
                    s.isFutura() ? "Programmata" : "Svolta");
        }

        tabellaTutteTesi.svuota();
        for (Tesi t : controllers.tesi().getAllTesi()) {
            tabellaTutteTesi.aggiungiRiga(t.getIdTesi(), nomeDi(studenti.get(t.getMatricolaStudente())),
                    t.getMatricolaStudente(), t.getTitolo(), nomeDi(docentiPerId.get(t.getIdDocenteRelatore())),
                    t.getStato());
        }

        tabellaTutteRichieste.svuota();
        for (RichiestaTirocinio r : controllers.richieste().getAllRichieste()) {
            Tirocinio t = tirocini.get(r.getIdTirocinio());
            tabellaTutteRichieste.aggiungiRiga(r.getIdRichiesta(), nomeDi(studenti.get(r.getMatricolaStudente())),
                    r.getMatricolaStudente(), t == null ? r.getIdTirocinio() : t.getTitolo(),
                    t == null ? "-" : nomeDi(docentiPerId.get(t.getIdDocente())), r.getStato());
        }
    }
}

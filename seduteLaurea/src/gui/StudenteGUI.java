package gui;

import controller.Controllers;
import controller.OperazioneNonValidaException;
import model.Commissione;
import model.Docente;
import model.RichiestaTirocinio;
import model.SedutaLaurea;
import model.StatoRichiesta;
import model.Studente;
import model.Tesi;
import model.Tirocinio;
import model.User;

import javax.swing.*;
import java.util.Map;

/**
 * Area studente: richiesta di tirocinio, richiesta di tesi, prenotazione alla seduta di laurea.
 */
public class StudenteGUI extends AreaRiservataFrame {

    private final Studente studente;

    private final TabellaDati tabellaTirocini = new TabellaDati("Titolo", "Docente", "Descrizione");
    private final TabellaDati tabellaRichieste = new TabellaDati("Tirocinio", "Docente", "Data richiesta", "Stato");
    private final TabellaDati tabellaTesi = new TabellaDati("Titolo", "Relatore", "Data richiesta", "Stato");
    private final TabellaDati tabellaSedute = new TabellaDati("Data", "Ora", "Luogo", "Presidente commissione", "Posti liberi");
    private final JLabel infoTesi = GuiUtils.riquadroInfo();
    private final JLabel infoSeduta = GuiUtils.riquadroInfo();

    public StudenteGUI(User user, Controllers controllers, Runnable onLogout) {
        super("Area Studente", user, controllers, onLogout);

        this.studente = controllers.studenti().getStudenteByIdUser(user.getIdUser());
        if (studente == null) {
            dispose();
            throw new OperazioneNonValidaException("Nessuno studente è associato all'account " + user.getUsername() + ".");
        }
        impostaIntestazione(studente.getNomeCompleto(), "Studente - matricola " + studente.getMatricola()
                + " - " + studente.getEmail());

        schede.addTab("Tirocini disponibili", creaSchedaTirocini());
        schede.addTab("Le mie richieste", creaSchedaRichieste());
        schede.addTab("Tesi", creaSchedaTesi());
        schede.addTab("Seduta di laurea", creaSchedaSedute());

        avvia();
    }

    // ============================
    // SCHEDE
    // ============================
    private JPanel creaSchedaTirocini() {
        JButton richiedi = new JButton("Richiedi il tirocinio selezionato");
        richiedi.addActionListener(e -> {
            String id = GuiUtils.selezione(this, tabellaTirocini, "un tirocinio");
            if (id != null && GuiUtils.esegui(this, () -> controllers.richieste().inviaRichiesta(studente, id),
                    "Richiesta inviata: il docente la valuterà a breve.")) {
                aggiornaInSicurezza();
            }
        });
        return GuiUtils.pannelloTabella(null, tabellaTirocini, richiedi);
    }

    private JPanel creaSchedaRichieste() {
        JButton ritira = new JButton("Ritira richiesta");
        ritira.addActionListener(e -> {
            String id = GuiUtils.selezione(this, tabellaRichieste, "una richiesta");
            if (id != null && GuiUtils.conferma(this, "Vuoi ritirare la richiesta selezionata?")
                    && GuiUtils.esegui(this, () -> controllers.richieste().ritiraRichiesta(studente, id), null)) {
                aggiornaInSicurezza();
            }
        });
        return GuiUtils.pannelloTabella(null, tabellaRichieste, ritira);
    }

    private JPanel creaSchedaTesi() {
        JButton richiedi = new JButton("Nuova richiesta di tesi");
        JButton ritira = new JButton("Ritira richiesta");
        richiedi.addActionListener(e -> richiediTesi());
        ritira.addActionListener(e -> {
            String id = GuiUtils.selezione(this, tabellaTesi, "una tesi");
            if (id != null && GuiUtils.conferma(this, "Vuoi ritirare la richiesta di tesi selezionata?")
                    && GuiUtils.esegui(this, () -> controllers.tesi().ritiraTesi(studente, id), null)) {
                aggiornaInSicurezza();
            }
        });
        return GuiUtils.pannelloTabella(infoTesi, tabellaTesi, richiedi, ritira);
    }

    private JPanel creaSchedaSedute() {
        JButton prenota = new JButton("Prenota la seduta selezionata");
        JButton annulla = new JButton("Annulla la mia prenotazione");
        prenota.addActionListener(e -> {
            String id = GuiUtils.selezione(this, tabellaSedute, "una seduta");
            if (id != null && GuiUtils.esegui(this, () -> controllers.sedute().prenotaStudente(id, studente),
                    "Prenotazione effettuata!")) {
                aggiornaInSicurezza();
            }
        });
        annulla.addActionListener(e -> {
            SedutaLaurea prenotata = controllers.sedute().getSedutaPrenotata(studente.getMatricola());
            if (prenotata == null) {
                JOptionPane.showMessageDialog(this, "Non hai prenotazioni attive.");
            } else if (GuiUtils.conferma(this, "Vuoi annullare la prenotazione alla seduta del "
                    + GuiUtils.formatta(prenotata.getDataSeduta()) + "?")
                    && GuiUtils.esegui(this, () -> controllers.sedute().annullaPrenotazione(prenotata.getIdSeduta(), studente),
                    "Prenotazione annullata.")) {
                aggiornaInSicurezza();
            }
        });
        return GuiUtils.pannelloTabella(infoSeduta, tabellaSedute, prenota, annulla);
    }

    // ============================
    // AZIONI
    // ============================
    private void richiediTesi() {
        JTextField titolo = new JTextField(30);
        JComboBox<Docente> relatore = new JComboBox<>(controllers.docenti().getAllDocenti().toArray(new Docente[0]));

        if (GuiUtils.mostraForm(this, "Nuova richiesta di tesi", "Titolo della tesi", titolo, "Docente relatore", relatore)) {
            Docente scelto = (Docente) relatore.getSelectedItem();
            if (GuiUtils.esegui(this, () -> controllers.tesi().richiediTesi(studente, titolo.getText(),
                    scelto == null ? null : scelto.getIdDocente()), "Richiesta di tesi inviata al relatore.")) {
                aggiornaInSicurezza();
            }
        }
    }

    // ============================
    // AGGIORNAMENTO DATI
    // ============================
    @Override
    protected void aggiorna() {
        Map<String, Docente> docenti = mappaDocenti();
        Map<String, Tirocinio> tirocini = mappaTirocini();

        tabellaTirocini.svuota();
        for (Tirocinio t : tirocini.values()) {
            tabellaTirocini.aggiungiRiga(t.getIdTirocinio(), t.getTitolo(), nomeDi(docenti.get(t.getIdDocente())),
                    t.getDescrizione());
        }

        tabellaRichieste.svuota();
        for (RichiestaTirocinio r : controllers.richieste().getRichiesteByStudente(studente.getMatricola())) {
            Tirocinio t = tirocini.get(r.getIdTirocinio());
            tabellaRichieste.aggiungiRiga(r.getIdRichiesta(), t == null ? r.getIdTirocinio() : t.getTitolo(),
                    t == null ? "-" : nomeDi(docenti.get(t.getIdDocente())),
                    GuiUtils.formatta(r.getDataRichiesta()), r.getStato());
        }

        tabellaTesi.svuota();
        for (Tesi t : controllers.tesi().getTesiByStudente(studente.getMatricola())) {
            tabellaTesi.aggiungiRiga(t.getIdTesi(), t.getTitolo(), nomeDi(docenti.get(t.getIdDocenteRelatore())),
                    GuiUtils.formatta(t.getDataRichiesta()), t.getStato());
        }
        Tesi corrente = controllers.tesi().getTesiCorrente(studente.getMatricola());
        boolean tesiApprovata = corrente != null && corrente.getStato() == StatoRichiesta.APPROVATA;
        infoTesi.setText(corrente == null
                ? "Non hai una tesi attiva. Invia una richiesta scegliendo titolo e relatore."
                : "<html>Tesi attuale: <b>" + corrente.getTitolo() + "</b> - stato: <b>"
                + GuiUtils.statoLeggibile(corrente.getStato()) + "</b></html>");

        tabellaSedute.svuota();
        for (SedutaLaurea s : controllers.sedute().getSeduteDisponibili()) {
            Commissione c = controllers.commissioni().getCommissioneById(s.getIdCommissione());
            int liberi = s.getMaxStudenti() - controllers.sedute().contaPrenotati(s.getIdSeduta());
            tabellaSedute.aggiungiRiga(s.getIdSeduta(), GuiUtils.formatta(s.getDataSeduta()),
                    GuiUtils.formatta(s.getOraSeduta()), s.getLuogo(),
                    c == null ? "-" : nomeDi(c.getPresidente()), liberi + " / " + s.getMaxStudenti());
        }
        SedutaLaurea prenotata = controllers.sedute().getSedutaPrenotata(studente.getMatricola());
        if (prenotata != null) {
            infoSeduta.setText("<html>Sei prenotato alla seduta del <b>" + GuiUtils.formatta(prenotata.getDataSeduta())
                    + "</b> alle <b>" + GuiUtils.formatta(prenotata.getOraSeduta()) + "</b> - " + prenotata.getLuogo()
                    + "</html>");
        } else if (!tesiApprovata) {
            infoSeduta.setText("Per prenotarti a una seduta di laurea serve una tesi approvata dal relatore.");
        } else {
            infoSeduta.setText("Non sei prenotato a nessuna seduta: scegline una dall'elenco.");
        }
    }
}

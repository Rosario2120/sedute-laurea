package gui;

import controller.Controllers;
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
import java.awt.*;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Area docente: gestione dei propri tirocini, valutazione di richieste di tirocinio
 * e di tesi, consultazione delle sedute in cui è in commissione.
 */
public class DocenteGUI extends AreaRiservataFrame {

    protected final Docente docente;

    private final TabellaDati tabellaTirocini = new TabellaDati("Titolo", "Descrizione", "Richieste in attesa");
    private final TabellaDati tabellaRichieste = new TabellaDati("Studente", "Matricola", "Tirocinio", "Data richiesta", "Stato");
    private final TabellaDati tabellaTesi = new TabellaDati("Studente", "Matricola", "Titolo", "Data richiesta", "Stato");
    private final TabellaDati tabellaSedute = new TabellaDati("Data", "Ora", "Luogo", "Ruolo in commissione", "Prenotati");
    private final JCheckBox soloRichiesteInAttesa = new JCheckBox("Mostra solo quelle in attesa", true);
    private final JCheckBox soloTesiInAttesa = new JCheckBox("Mostra solo quelle in attesa", true);

    public DocenteGUI(User user, Controllers controllers, Runnable onLogout) {
        this("Area Docente", user, controllers, onLogout);
        avvia();
    }

    // Usato da CoordinatoreGUI, che aggiunge le proprie schede prima di chiamare avvia()
    protected DocenteGUI(String titolo, User user, Controllers controllers, Runnable onLogout) {
        super(titolo, user, controllers, onLogout);

        this.docente = controllers.docenti().getDocenteByIdUser(user.getIdUser());
        if (docente == null) {
            dispose();
        }
        impostaIntestazione("Prof. " + docente.getNomeCompleto(), titolo + " - " + docente.getEmail());

        schede.addTab("I miei tirocini", creaSchedaTirocini());
        schede.addTab("Richieste di tirocinio", creaSchedaRichieste());
        schede.addTab("Tesi come relatore", creaSchedaTesi());
        schede.addTab("Le mie sedute", creaSchedaSedute());
    }

    // ============================
    // SCHEDE
    // ============================
    private JPanel creaSchedaTirocini() {
        JButton nuovo = new JButton("Nuovo tirocinio");
        JButton modifica = new JButton("Modifica");
        JButton elimina = new JButton("Elimina");

        nuovo.addActionListener(e -> {
            JTextField titolo = new JTextField(30);
            JTextArea descrizione = areaDescrizione("");
            if (GuiUtils.mostraForm(this, "Nuovo tirocinio", "Titolo", titolo, "Descrizione", new JScrollPane(descrizione))
                    && GuiUtils.esegui(this, () -> controllers.tirocini().creaTirocinio(docente, titolo.getText(),
                    descrizione.getText()), "Tirocinio pubblicato.")) {
                aggiornaInSicurezza();
            }
        });
        modifica.addActionListener(e -> {
            String id = GuiUtils.selezione(this, tabellaTirocini, "un tirocinio");
            if (id == null) {
                return;
            }
            Tirocinio t = controllers.tirocini().getTirocinioById(id);
            if (t == null) {
                elementoNonPiuDisponibile();
                return;
            }
            JTextField titolo = new JTextField(t.getTitolo(), 30);
            JTextArea descrizione = areaDescrizione(t.getDescrizione());
            if (GuiUtils.mostraForm(this, "Modifica tirocinio", "Titolo", titolo, "Descrizione", new JScrollPane(descrizione))
                    && GuiUtils.esegui(this, () -> controllers.tirocini().modificaTirocinio(docente, id, titolo.getText(),
                    descrizione.getText()), null)) {
                aggiornaInSicurezza();
            }
        });
        elimina.addActionListener(e -> {
            String id = GuiUtils.selezione(this, tabellaTirocini, "un tirocinio");
            if (id != null && GuiUtils.conferma(this, "Eliminare il tirocinio? Le richieste in attesa verranno cancellate.")
                    && GuiUtils.esegui(this, () -> controllers.tirocini().eliminaTirocinio(docente, id), null)) {
                aggiornaInSicurezza();
            }
        });
        return GuiUtils.pannelloTabella(null, tabellaTirocini, nuovo, modifica, elimina);
    }

    private JPanel creaSchedaRichieste() {
        soloRichiesteInAttesa.addActionListener(e -> aggiornaInSicurezza());
        return GuiUtils.pannelloTabella(soloRichiesteInAttesa, tabellaRichieste,
                pulsanteValutazione("Approva", tabellaRichieste, "una richiesta", controllers.richieste()::approvaRichiesta),
                pulsanteValutazione("Rifiuta", tabellaRichieste, "una richiesta", controllers.richieste()::rifiutaRichiesta));
    }

    private JPanel creaSchedaTesi() {
        soloTesiInAttesa.addActionListener(e -> aggiornaInSicurezza());
        return GuiUtils.pannelloTabella(soloTesiInAttesa, tabellaTesi,
                pulsanteValutazione("Approva", tabellaTesi, "una tesi", controllers.tesi()::approvaTesi),
                pulsanteValutazione("Rifiuta", tabellaTesi, "una tesi", controllers.tesi()::rifiutaTesi));
    }

    private JPanel creaSchedaSedute() {
        JButton prenotati = new JButton("Vedi studenti prenotati");
        prenotati.addActionListener(e -> {
            String id = GuiUtils.selezione(this, tabellaSedute, "una seduta");
            SedutaLaurea seduta = id == null ? null : controllers.sedute().getSedutaById(id);
            if (seduta != null) {
                new PrenotatiDialog(this, controllers, seduta, false).setVisible(true);
            } else if (id != null) {
                elementoNonPiuDisponibile();
            }
        });
        return GuiUtils.pannelloTabella(null, tabellaSedute, prenotati);
    }

    // Pulsante Approva/Rifiuta: applica l'azione all'elemento selezionato e ricarica
    private JButton pulsanteValutazione(String etichetta, TabellaDati tabella, String cosa,
                                        BiConsumer<Docente, String> azione) {
        JButton pulsante = new JButton(etichetta);
        pulsante.addActionListener(e -> {
            String id = GuiUtils.selezione(this, tabella, cosa);
            if (id != null && GuiUtils.esegui(this, () -> azione.accept(docente, id), null)) {
                aggiornaInSicurezza();
            }
        });
        return pulsante;
    }

    private static JTextArea areaDescrizione(String testo) {
        JTextArea area = new JTextArea(testo, 5, 30);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(UIManager.getFont("TextField.font"));
        area.setMinimumSize(new Dimension(200, 90));
        return area;
    }

    // ============================
    // AGGIORNAMENTO DATI
    // ============================
    @Override
    protected void aggiorna() {
        Map<String, Studente> studenti = mappaStudenti();
        Map<String, Tirocinio> tirocini = mappaTirocini();
        java.util.List<RichiestaTirocinio> richieste = controllers.richieste().getRichiesteByDocente(docente.getIdDocente());

        tabellaTirocini.svuota();
        for (Tirocinio t : controllers.tirocini().getTirociniByDocente(docente.getIdDocente())) {
            long inAttesa = richieste.stream()
                    .filter(r -> r.getIdTirocinio().equals(t.getIdTirocinio()) && r.getStato() == StatoRichiesta.IN_ATTESA)
                    .count();
            tabellaTirocini.aggiungiRiga(t.getIdTirocinio(), t.getTitolo(), t.getDescrizione(), inAttesa);
        }

        tabellaRichieste.svuota();
        for (RichiestaTirocinio r : richieste) {
            if (soloRichiesteInAttesa.isSelected() && r.getStato() != StatoRichiesta.IN_ATTESA) {
                continue;
            }
            Tirocinio t = tirocini.get(r.getIdTirocinio());
            tabellaRichieste.aggiungiRiga(r.getIdRichiesta(), nomeDi(studenti.get(r.getMatricolaStudente())),
                    r.getMatricolaStudente(), t == null ? r.getIdTirocinio() : t.getTitolo(),
                    GuiUtils.formatta(r.getDataRichiesta()), r.getStato());
        }

        tabellaTesi.svuota();
        for (Tesi t : controllers.tesi().getTesiByRelatore(docente.getIdDocente())) {
            if (soloTesiInAttesa.isSelected() && t.getStato() != StatoRichiesta.IN_ATTESA) {
                continue;
            }
            tabellaTesi.aggiungiRiga(t.getIdTesi(), nomeDi(studenti.get(t.getMatricolaStudente())),
                    t.getMatricolaStudente(), t.getTitolo(), GuiUtils.formatta(t.getDataRichiesta()), t.getStato());
        }

        tabellaSedute.svuota();
        for (SedutaLaurea s : controllers.sedute().getSeduteByDocente(docente.getIdDocente())) {
            Commissione c = controllers.commissioni().getCommissioneById(s.getIdCommissione());
            String ruolo = c != null && docente.getIdDocente().equals(c.getIdPresidente()) ? "Presidente" : "Membro";
            tabellaSedute.aggiungiRiga(s.getIdSeduta(), GuiUtils.formatta(s.getDataSeduta()),
                    GuiUtils.formatta(s.getOraSeduta()), s.getLuogo(), ruolo,
                    controllers.sedute().contaPrenotati(s.getIdSeduta()) + " / " + s.getMaxStudenti());
        }
    }
}

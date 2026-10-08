package gui;

import controller.Controllers;
import model.SedutaLaurea;
import model.Studente;

import javax.swing.*;
import java.awt.*;

/**
 * Elenco degli studenti prenotati a una seduta. In modalità modificabile
 * (coordinatore) permette anche di rimuovere una prenotazione.
 */
class PrenotatiDialog extends JDialog {

    private final Controllers controllers;
    private final SedutaLaurea seduta;
    private final TabellaDati tabella = new TabellaDati("Matricola", "Cognome", "Nome", "Email");
    private final JLabel riepilogo = new JLabel();

    PrenotatiDialog(Window owner, Controllers controllers, SedutaLaurea seduta, boolean modificabile) {
        super(owner, "Prenotati alla seduta del " + GuiUtils.formatta(seduta.getDataSeduta()), ModalityType.APPLICATION_MODAL);
        this.controllers = controllers;
        this.seduta = seduta;

        JButton chiudi = new JButton("Chiudi");
        chiudi.addActionListener(e -> dispose());

        JPanel contenuto;
        if (modificabile) {
            JButton rimuovi = new JButton("Rimuovi prenotazione");
            rimuovi.addActionListener(e -> rimuoviSelezionato());
            contenuto = GuiUtils.pannelloTabella(riepilogo, tabella, rimuovi, chiudi);
        } else {
            contenuto = GuiUtils.pannelloTabella(riepilogo, tabella, chiudi);
        }
        setContentPane(contenuto);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(owner);
        carica();
    }

    private void carica() {
        tabella.svuota();
        java.util.List<Studente> prenotati = controllers.sedute().getStudentiPrenotati(seduta.getIdSeduta());
        for (Studente s : prenotati) {
            tabella.aggiungiRiga(s.getMatricola(), s.getMatricola(), s.getCognome(), s.getNome(), s.getEmail());
        }
        riepilogo.setText(seduta.getLuogo() + " - ore " + GuiUtils.formatta(seduta.getOraSeduta()) + " - "
                + prenotati.size() + " prenotati su " + seduta.getMaxStudenti() + " posti");
    }

    private void rimuoviSelezionato() {
        String matricola = GuiUtils.selezione(this, tabella, "uno studente");
        if (matricola == null || !GuiUtils.conferma(this, "Rimuovere la prenotazione dello studente " + matricola + "?")) {
            return;
        }
        Studente studente = controllers.studenti().getStudenteByMatricola(matricola);
        if (studente == null) {
            carica(); // studente eliminato nel frattempo: la prenotazione è già sparita
            return;
        }
        if (GuiUtils.esegui(this, () -> controllers.sedute().rimuoviPrenotazione(seduta.getIdSeduta(), studente), null)) {
            carica();
        }
    }
}

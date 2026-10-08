package gui;

import app.App;
import controller.Controllers;
import database.DatabaseConnection;
import model.StatoRichiesta;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Giro automatico della GUI (eseguito da scripts/gui-tour.sh sotto Xvfb).
 *
 * Usa la GUI come un utente: digita le credenziali, preme i pulsanti, seleziona
 * righe. Verifica che le azioni arrivino al database e salva uno screenshot di
 * ogni schermata in docs/screenshots. Termina con codice 1 se una verifica fallisce.
 *
 * Lavora sul database di test, che viene ricreato con schema e dati demo.
 */
public class GuiTour {

    private static Path cartella;
    private static final List<String> errori = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        cartella = Path.of(args.length > 0 ? args[0] : "docs/screenshots");
        Files.createDirectories(cartella);

        Connection conn = DatabaseConnection.getConnection(
                env("DB_URL", "jdbc:postgresql://localhost:5432/sedutelaurea_test"),
                env("DB_USER", "postgres"), env("DB_PASSWORD", "postgres"));
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(Files.readString(Path.of("db/schema.sql")));
            stmt.execute(Files.readString(Path.of("db/seed.sql")));
        }
        Controllers c = App.creaControllers(conn);
        Aspetto.applica();

        LoginGUI[] contenitore = new LoginGUI[1];
        edt(() -> {
            contenitore[0] = new LoginGUI(c);
            contenitore[0].setVisible(true);
        });
        LoginGUI login = contenitore[0];
        salva(login, "01-login");

        // ---------- Login errato ----------
        accedi(login, "sricci", "sbagliata");
        salva(login, "02-login-errato");
        verifica(login.isVisible() && finestra(StudenteGUI.class) == null, "con password errata resta il login");

        // ---------- Studente con tesi approvata: prenota una seduta ----------
        accedi(login, "sricci", "password");
        StudenteGUI ricci = finestra(StudenteGUI.class);
        verifica(ricci != null, "login studente apre l'area studente");
        salvaSchede(ricci, "10-studente");

        mostraScheda(ricci, 3);
        selezionaPrimaRiga(ricci);
        cliccaInAttesa(ricci, "Prenota la seduta selezionata");
        chiudiDialog("13-studente-prenotazione-ok", null);
        verifica(c.sedute().getSedutaPrenotata("N86001004") != null, "la prenotazione dalla GUI è salvata nel database");
        salva(ricci, "14-studente-prenotato");

        cliccaInAttesa(ricci, "Annulla la mia prenotazione");
        rispondiDialog("15-studente-conferma-annullamento", "Sì");
        chiudiDialog("16-studente-annullata", null);
        verifica(c.sedute().getSedutaPrenotata("N86001004") == null, "l'annullamento confermato cancella la prenotazione");

        selezionaPrimaRiga(ricci);
        cliccaInAttesa(ricci, "Prenota la seduta selezionata");
        chiudiDialog(null, null);
        verifica(c.sedute().getSedutaPrenotata("N86001004") != null, "lo studente può prenotarsi di nuovo");

        clicca(ricci, "Esci");
        verifica(login.isVisible() && !ricci.isDisplayable(), "Esci chiude l'area e torna al login");

        // ---------- Studente senza tesi approvata: la regola blocca la prenotazione ----------
        accedi(login, "mromano", "password");
        StudenteGUI romano = finestra(StudenteGUI.class);
        mostraScheda(romano, 3);
        selezionaPrimaRiga(romano);
        cliccaInAttesa(romano, "Prenota la seduta selezionata");
        chiudiDialog("17-studente-prenotazione-negata", null);
        verifica(c.sedute().getSedutaPrenotata("N86001002") == null, "senza tesi approvata la prenotazione è rifiutata");
        clicca(romano, "Esci");

        // ---------- Docente: approva una richiesta di tirocinio ----------
        accedi(login, "mrossi", "password");
        DocenteGUI rossi = finestra(DocenteGUI.class);
        verifica(rossi != null && !(rossi instanceof CoordinatoreGUI), "login docente apre l'area docente");
        salvaSchede(rossi, "20-docente");

        mostraScheda(rossi, 1);
        selezionaPrimaRiga(rossi);
        clicca(rossi, "Approva");
        boolean approvata = c.richieste().getRichiesteByStudente("N86001003").stream()
                .anyMatch(r -> r.getStato() == StatoRichiesta.APPROVATA);
        verifica(approvata, "l'approvazione dalla GUI aggiorna lo stato della richiesta");
        clicca(rossi, "Esci");

        // ---------- Coordinatore ----------
        accedi(login, "coordinatore", "password");
        CoordinatoreGUI coordinatore = finestra(CoordinatoreGUI.class);
        verifica(coordinatore != null, "login coordinatore apre l'area coordinatore");
        salvaSchede(coordinatore, "30-coordinatore");

        mostraScheda(coordinatore, 1);
        cliccaInAttesa(coordinatore, "Nuova commissione");
        chiudiDialog("38-coordinatore-nuova-commissione", null);

        mostraScheda(coordinatore, 2);
        cliccaInAttesa(coordinatore, "Nuova seduta");
        chiudiDialog("39-coordinatore-nuova-seduta", null);

        selezionaRiga(coordinatore, 1); // seduta tra 30 giorni (righe in ordine di data)
        cliccaInAttesa(coordinatore, "Studenti prenotati");
        chiudiDialog("40-coordinatore-prenotati", d -> verifica(
                cercaTabella(d).getRowCount() == 2, "la seduta mostra i 2 prenotati (Esposito e Ricci)"));
        clicca(coordinatore, "Esci");

        edt(login::dispose);
        conn.close();

        if (errori.isEmpty()) {
            System.out.println("\nGUI TOUR OK - screenshot salvati in " + cartella.toAbsolutePath());
            System.exit(0);
        }
        System.out.println("\nGUI TOUR FALLITO:");
        errori.forEach(e -> System.out.println(" - " + e));
        System.exit(1);
    }

    // ============================
    // AZIONI SULLA GUI
    // ============================
    private static void accedi(LoginGUI login, String username, String password) throws Exception {
        edt(() -> {
            cerca(login.getContentPane(), JTextField.class, f -> !(f instanceof JPasswordField)).setText(username);
            cerca(login.getContentPane(), JPasswordField.class, f -> true).setText(password);
            pulsante(login.getContentPane(), "Accedi").doClick();
        });
    }

    private static void mostraScheda(AreaRiservataFrame frame, int indice) throws Exception {
        edt(() -> frame.schede.setSelectedIndex(indice));
    }

    private static void selezionaPrimaRiga(AreaRiservataFrame frame) throws Exception {
        selezionaRiga(frame, 0);
    }

    private static void selezionaRiga(AreaRiservataFrame frame, int riga) throws Exception {
        edt(() -> cercaTabella((Container) frame.schede.getSelectedComponent()).setRowSelectionInterval(riga, riga));
    }

    // Click che non apre finestre modali
    private static void clicca(Window finestra, String testo) throws Exception {
        edt(() -> pulsante(finestra, testo).doClick());
    }

    // Click che apre un dialog modale: va lanciato senza attendere
    private static void cliccaInAttesa(Window finestra, String testo) {
        SwingUtilities.invokeLater(() -> pulsante(finestra, testo).doClick());
    }

    private interface Controllo {
        void esegui(JDialog dialog);
    }

    // Attende il dialog aperto, lo fotografa (se nome != null), esegue un controllo opzionale e lo chiude
    private static void chiudiDialog(String nome, Controllo controllo) throws Exception {
        JDialog dialog = attendiDialog(nome);
        if (dialog == null) {
            return;
        }
        if (nome != null) {
            salvaImmagine(dialog.getRootPane(), nome);
        }
        if (controllo != null) {
            controllo.esegui(dialog);
        }
        edt(dialog::dispose);
    }

    // Fotografa il dialog aperto e preme uno dei suoi pulsanti (es. "Sì" di una conferma)
    private static void rispondiDialog(String nome, String pulsante) throws Exception {
        JDialog dialog = attendiDialog(nome);
        if (dialog == null) {
            return;
        }
        salvaImmagine(dialog.getRootPane(), nome);
        SwingUtilities.invokeLater(() -> pulsante(dialog, pulsante).doClick());
        for (int i = 0; i < 50 && dialog.isShowing(); i++) {
            Thread.sleep(100);
        }
    }

    private static JDialog attendiDialog(String nome) throws Exception {
        for (int i = 0; i < 50; i++) {
            Thread.sleep(100);
            for (Window w : Window.getWindows()) {
                if (w instanceof JDialog d && d.isShowing()) {
                    Thread.sleep(300);
                    return d;
                }
            }
        }
        verifica(false, "atteso un dialog" + (nome == null ? "" : " per " + nome));
        return null;
    }

    // ============================
    // SCREENSHOT
    // ============================
    private static void salvaSchede(AreaRiservataFrame frame, String prefisso) throws Exception {
        for (int i = 0; i < frame.schede.getTabCount(); i++) {
            int indice = i;
            edt(() -> frame.schede.setSelectedIndex(indice));
            String scheda = frame.schede.getTitleAt(i).toLowerCase().replaceAll("[^a-z]+", "-");
            salva(frame, prefisso + "-" + (i + 1) + "-" + scheda);
        }
    }

    private static void salva(JFrame frame, String nome) throws Exception {
        salvaImmagine(frame.getRootPane(), nome);
    }

    private static void salvaImmagine(JComponent componente, String nome) throws Exception {
        Thread.sleep(250);
        edt(() -> {
            BufferedImage img = new BufferedImage(componente.getWidth(), componente.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            componente.printAll(g);
            g.dispose();
            try {
                ImageIO.write(img, "png", cartella.resolve(nome + ".png").toFile());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
        System.out.println("  screenshot " + nome + ".png");
    }

    // ============================
    // SUPPORTO
    // ============================
    private static void edt(Runnable azione) throws Exception {
        SwingUtilities.invokeAndWait(azione);
    }

    private static void verifica(boolean condizione, String descrizione) {
        System.out.println((condizione ? "  [OK]   " : "  [FAIL] ") + descrizione);
        if (!condizione) {
            errori.add(descrizione);
        }
    }

    private static <T extends Window> T finestra(Class<T> tipo) throws Exception {
        Thread.sleep(300);
        for (Window w : Window.getWindows()) {
            if (tipo.isInstance(w) && w.isShowing()) {
                return tipo.cast(w);
            }
        }
        return null;
    }

    private static TabellaDati cercaTabella(Container radice) {
        return cerca(radice, TabellaDati.class, t -> true);
    }

    private static JButton pulsante(Container radice, String testo) {
        JButton b = cerca(radice, JButton.class, p -> testo.equals(p.getText()) && p.isShowing());
        if (b == null) {
            throw new IllegalStateException("Pulsante non trovato: " + testo);
        }
        return b;
    }

    private interface Filtro<T> {
        boolean accetta(T componente);
    }

    private static <T extends Component> T cerca(Container radice, Class<T> tipo, Filtro<T> filtro) {
        for (Component figlio : radice.getComponents()) {
            if (tipo.isInstance(figlio) && filtro.accetta(tipo.cast(figlio))) {
                return tipo.cast(figlio);
            }
            if (figlio instanceof Container contenitore) {
                T trovato = cerca(contenitore, tipo, filtro);
                if (trovato != null) {
                    return trovato;
                }
            }
        }
        return null;
    }

    private static String env(String nome, String predefinito) {
        String valore = System.getenv(nome);
        return valore == null || valore.isBlank() ? predefinito : valore;
    }
}

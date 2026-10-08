package gui;

import controller.OperazioneNonValidaException;
import dao.DataAccessException;
import model.StatoRichiesta;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Funzioni di supporto condivise dalle schermate Swing.
 */
final class GuiUtils {

    static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    static final DateTimeFormatter FORMATO_ORA = DateTimeFormatter.ofPattern("HH:mm");
    static final DateTimeFormatter FORMATO_DATA_ORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private GuiUtils() {
    }

    /**
     * Esegue un'azione dell'utente mostrando l'esito: un avviso per le regole di dominio
     * violate, un errore per i problemi del database. Restituisce true se è andata a buon fine.
     */
    static boolean esegui(Component parent, Runnable azione, String messaggioSuccesso) {
        try {
            azione.run();
            if (messaggioSuccesso != null) {
                JOptionPane.showMessageDialog(parent, messaggioSuccesso, "Operazione completata",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            return true;
        } catch (OperazioneNonValidaException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Operazione non consentita",
                    JOptionPane.WARNING_MESSAGE);
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Errore database", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Errore imprevisto: " + e.getMessage(), "Errore",
                    JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    static boolean conferma(Component parent, String domanda) {
        return JOptionPane.showConfirmDialog(parent, domanda, "Conferma", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // Avvisa se nessuna riga è selezionata; restituisce l'ID selezionato o null
    static String selezione(Component parent, TabellaDati tabella, String cosa) {
        String id = tabella.getIdSelezionato();
        if (id == null) {
            JOptionPane.showMessageDialog(parent, "Seleziona prima " + cosa + " nella tabella.", "Nessuna selezione",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        return id;
    }

    /**
     * Mostra un modulo con coppie etichetta/campo (es. "Titolo", campoTitolo, ...).
     * Restituisce true se l'utente ha premuto OK.
     */
    static boolean mostraForm(Component parent, String titolo, Object... etichetteECampi) {
        JPanel pannello = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        for (int i = 0; i < etichetteECampi.length; i += 2) {
            gbc.gridy = i / 2;
            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            pannello.add(new JLabel(etichetteECampi[i] + ":"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pannello.add((Component) etichetteECampi[i + 1], gbc);
        }
        pannello.setPreferredSize(new Dimension(Math.max(420, pannello.getPreferredSize().width),
                pannello.getPreferredSize().height));

        return JOptionPane.showConfirmDialog(parent, pannello, titolo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    // Tabella con barra dei pulsanti in basso ed eventuale componente in alto
    static JPanel pannelloTabella(JComponent sopra, TabellaDati tabella, JButton... pulsanti) {
        JPanel pannello = new JPanel(new BorderLayout(8, 8));
        pannello.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        if (sopra != null) {
            pannello.add(sopra, BorderLayout.NORTH);
        }
        pannello.add(new JScrollPane(tabella), BorderLayout.CENTER);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        for (JButton pulsante : pulsanti) {
            barra.add(pulsante);
        }
        pannello.add(barra, BorderLayout.SOUTH);
        return pannello;
    }

    // Etichetta informativa con bordo, usata sopra le tabelle
    static JLabel riquadroInfo() {
        JLabel label = new JLabel(" ");
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC8CDD3)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        label.setOpaque(true);
        label.setBackground(new Color(0xF4F6F8));
        return label;
    }

    static String statoLeggibile(StatoRichiesta stato) {
        return switch (stato) {
            case IN_ATTESA -> "In attesa";
            case APPROVATA -> "Approvata";
            case RIFIUTATA -> "Rifiutata";
        };
    }

    static String formatta(LocalDate data) {
        return data == null ? "" : data.format(FORMATO_DATA);
    }

    static String formatta(LocalTime ora) {
        return ora == null ? "" : ora.format(FORMATO_ORA);
    }

    static String formatta(LocalDateTime dataOra) {
        return dataOra == null ? "" : dataOra.format(FORMATO_DATA_ORA);
    }

    static LocalDate leggiData(String testo) {
        try {
            return LocalDate.parse(testo.trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new OperazioneNonValidaException("Data non valida: usa il formato gg/mm/aaaa (es. 15/03/2027).");
        }
    }

    static LocalTime leggiOra(String testo) {
        try {
            return LocalTime.parse(testo.trim(), FORMATO_ORA);
        } catch (DateTimeParseException e) {
            throw new OperazioneNonValidaException("Ora non valida: usa il formato hh:mm (es. 09:30).");
        }
    }
}

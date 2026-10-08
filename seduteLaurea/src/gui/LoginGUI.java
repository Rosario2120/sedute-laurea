package gui;

import controller.Controllers;
import controller.OperazioneNonValidaException;
import dao.DataAccessException;
import model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Schermata di accesso. Verifica le credenziali sul database e apre l'area
 * riservata corrispondente al ruolo; al logout la schermata riappare.
 */
public class LoginGUI extends JFrame {

    private final Controllers controllers;

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel messaggio = new JLabel(" ");

    public LoginGUI(Controllers controllers) {
        this.controllers = controllers;

        setTitle("Sedute di Laurea - Accesso");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JLabel titolo = new JLabel("Sedute di Laurea");
        titolo.setFont(titolo.getFont().deriveFont(Font.BOLD, 22f));
        JLabel sottotitolo = new JLabel("Tirocini, tesi e prenotazione alle sedute");
        sottotitolo.setForeground(Color.GRAY);
        messaggio.setForeground(new Color(0xB3261E));

        JButton loginButton = new JButton("Accedi");
        loginButton.addActionListener(e -> login());
        getRootPane().setDefaultButton(loginButton); // Invio = Accedi

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = 0;
        form.add(titolo, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 4, 16, 4);
        form.add(sottotitolo, gbc);

        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        form.add(new JLabel("Username"), gbc);
        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        form.add(new JLabel("Password"), gbc);
        gbc.gridx = 1;
        form.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        form.add(messaggio, gbc);

        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(loginButton, gbc);

        setContentPane(form);
        pack();
        setLocationRelativeTo(null);
    }

    // ============================
    // LOGIN
    // ============================
    private void login() {
        try {
            User user = controllers.utenti().login(usernameField.getText(), new String(passwordField.getPassword()));
            JFrame area = apriAreaRiservata(user);
            messaggio.setText(" ");
            setVisible(false);
            area.setVisible(true);
        } catch (OperazioneNonValidaException e) {
            messaggio.setText(e.getMessage());
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Errore database", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Sceglie la finestra in base al ruolo dell'utente
    JFrame apriAreaRiservata(User user) {
        Runnable alLogout = this::mostraLogin;
        return switch (user.getRuolo()) {
            case STUDENTE -> new StudenteGUI(user, controllers, alLogout);
            case DOCENTE -> new DocenteGUI(user, controllers, alLogout);
            case COORDINATORE -> new CoordinatoreGUI(user, controllers, alLogout);
        };
    }

    // ============================
    // LOGOUT
    // ============================
    private void mostraLogin() {
        passwordField.setText("");
        messaggio.setText(" ");
        setVisible(true);
        passwordField.requestFocusInWindow();
    }
}

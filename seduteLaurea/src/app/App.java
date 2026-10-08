package app;

import controller.*;
import dao.*;
import database.DatabaseConnection;
import gui.Aspetto;
import gui.LoginGUI;
import postgresdao.*;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class App {

    public static void main(String[] args) {
        Aspetto.applica();

        // Connessione al database
        Connection conn;
        try {
            conn = DatabaseConnection.getConnection();
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(null, e.getMessage()
                            + "\n\nAvvia il database con \"docker compose up -d db\" oppure configura"
                            + "\nDB_URL / DB_USER / DB_PASSWORD (vedi README).",
                    "Sedute di Laurea - database non raggiungibile", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> chiudi(conn)));

        Controllers controllers = creaControllers(conn);

        // GUI LOGIN (sempre sull'Event Dispatch Thread di Swing)
        SwingUtilities.invokeLater(() -> new LoginGUI(controllers).setVisible(true));
    }

    // Collega DAO PostgreSQL e controller; usato anche dai test
    public static Controllers creaControllers(Connection conn) {
        // DAO con connessione
        UserDAO userDAO = new UserPostgresDAO(conn);
        StudenteDAO studenteDAO = new StudentePostgresDAO(conn);
        DocenteDAO docenteDAO = new DocentePostgresDAO(conn);
        TirocinioDAO tirocinioDAO = new TirocinioPostgresDAO(conn);
        RichiestaTirocinioDAO richiestaDAO = new RichiestaTirocinioPostgresDAO(conn);
        TesiDAO tesiDAO = new TesiPostgresDAO(conn);
        SedutaLaureaDAO sedutaDAO = new SedutaLaureaPostgresDAO(conn);
        CommissioneDAO commissioneDAO = new CommissionePostgresDAO(conn);

        // CONTROLLER
        TesiController tesiController = new TesiController(tesiDAO, docenteDAO);
        return new Controllers(
                new UserController(userDAO, studenteDAO, docenteDAO),
                new StudenteController(studenteDAO),
                new DocenteController(docenteDAO),
                new TirocinioController(tirocinioDAO, richiestaDAO),
                new RichiestaTirocinioController(richiestaDAO, tirocinioDAO),
                tesiController,
                new CommissioneController(commissioneDAO, docenteDAO, sedutaDAO),
                new SedutaLaureaController(sedutaDAO, commissioneDAO, tesiController)
        );
    }

    private static void chiudi(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            // chiusura in uscita dal programma: nulla da fare
        }
    }
}

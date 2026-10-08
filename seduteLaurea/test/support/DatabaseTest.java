package support;

import app.App;
import controller.Controllers;
import database.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Base dei test di integrazione: prima di ogni test ricrea schema e dati demo
 * nel database di test, così ogni test parte dallo stesso stato noto.
 *
 * Il database si configura con DB_URL / DB_USER / DB_PASSWORD; per sicurezza
 * il nome deve contenere "test", per non cancellare per errore i dati reali.
 */
public abstract class DatabaseTest {

    private static final String URL = env("DB_URL", "jdbc:postgresql://localhost:5432/sedutelaurea_test");
    private static final String USER = env("DB_USER", "postgres");
    private static final String PASSWORD = env("DB_PASSWORD", "postgres");

    protected Connection connection;
    protected Controllers c;

    @BeforeEach
    void preparaDatabase() throws SQLException, IOException {
        if (!URL.contains("test")) {
            throw new IllegalStateException("DB_URL deve puntare a un database di test (nome con \"test\"): " + URL);
        }
        connection = nuovaConnessione();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(Files.readString(Path.of("db/schema.sql")));
            stmt.execute(Files.readString(Path.of("db/seed.sql")));
        }
        c = App.creaControllers(connection);
    }

    // Connessione aggiuntiva allo stesso database di test (per i test di concorrenza)
    protected static Connection nuovaConnessione() {
        return DatabaseConnection.getConnection(URL, USER, PASSWORD);
    }

    @AfterEach
    void chiudiConnessione() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    private static String env(String nome, String predefinito) {
        String valore = System.getenv(nome);
        return valore == null || valore.isBlank() ? predefinito : valore;
    }
}

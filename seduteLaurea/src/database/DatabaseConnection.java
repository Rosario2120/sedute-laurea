package database;

import dao.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 db.url=jdbc:postgresql://localhost:5432/sedutelaurea
 db.user=Studente
 db.password=201106

 *
 * Parametri letti, in ordine di priorità:
 *   1. variabili d'ambiente DB_URL, DB_USER, DB_PASSWORD
 *   2. file db.properties nella cartella di lavoro (chiavi db.url, db.user, db.password)
 *   3. valori predefiniti, coerenti con il database avviato da docker-compose.yml
 */
public class DatabaseConnection {

    private static final String URL_PREDEFINITO = "jdbc:postgresql://localhost:5432/sedutelaurea";
    private static final String USER_PREDEFINITO = "postgres";
    private static final String PASSWORD_PREDEFINITA = "201106";
    private static final String FILE_CONFIGURAZIONE = "db.properties";

    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        Properties file = leggiFileConfigurazione();
        String url = valore("DB_URL", file, "db.url", URL_PREDEFINITO);
        String user = valore("DB_USER", file, "db.user", USER_PREDEFINITO);
        String password = valore("DB_PASSWORD", file, "db.password", PASSWORD_PREDEFINITA);
        return getConnection(url, user, password);
    }

    public static Connection getConnection(String url, String user, String password) {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DataAccessException("Impossibile connettersi al database " + url + ": " + e.getMessage(), e);
        }
    }

    private static String valore(String variabileAmbiente, Properties file, String chiaveFile, String predefinito) {
        String daAmbiente = System.getenv(variabileAmbiente);
        if (daAmbiente != null && !daAmbiente.isBlank()) {
            return daAmbiente;
        }
        return file.getProperty(chiaveFile, predefinito);
    }

    private static Properties leggiFileConfigurazione() {
        Properties properties = new Properties();
        Path percorso = Path.of(FILE_CONFIGURAZIONE);
        if (Files.isRegularFile(percorso)) {
            try (InputStream in = Files.newInputStream(percorso)) {
                properties.load(in);
            } catch (IOException e) {
                System.err.println("Impossibile leggere " + FILE_CONFIGURAZIONE + ": " + e.getMessage());
            }
        }
        return properties;
    }
}

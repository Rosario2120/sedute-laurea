package dao;

import java.sql.SQLException;

/**
 * Errore di accesso ai dati. Sostituisce il vecchio "e.printStackTrace(); return false;"
 * dei DAO, così la GUI può mostrare all'utente il motivo reale del problema.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }

    // true se l'operazione è stata bloccata da una chiave esterna (dati collegati)
    public boolean isViolazioneChiaveEsterna() {
        return getCause() instanceof SQLException e && "23503".equals(e.getSQLState());
    }

    // Traduce i codici SQLSTATE più comuni di PostgreSQL in messaggi comprensibili
    public static DataAccessException da(SQLException e) {
        String stato = e.getSQLState() != null ? e.getSQLState() : "";
        String messaggio = switch (stato) {
            case "23505" -> "Dato già presente: è stato violato un vincolo di unicità.";
            case "23503" -> "Operazione non consentita: esistono dati collegati.";
            case "23514", "22007", "22008" -> "Valore non valido per il database.";
            case "08001", "08006", "57P01" -> "Connessione al database persa.";
            default -> "Errore del database: " + e.getMessage();
        };
        return new DataAccessException(messaggio, e);
    }
}

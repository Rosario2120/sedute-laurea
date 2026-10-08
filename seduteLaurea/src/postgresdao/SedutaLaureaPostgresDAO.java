package postgresdao;

import dao.DataAccessException;
import dao.SedutaLaureaDAO;
import model.SedutaLaurea;
import model.Studente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SedutaLaureaPostgresDAO implements SedutaLaureaDAO {

    private final Connection connection;

    public SedutaLaureaPostgresDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean aggiungiSeduta(SedutaLaurea seduta) {
        String query = "INSERT INTO sedute_laurea (id_seduta, data_seduta, ora_seduta, luogo, id_commissione, max_studenti) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, seduta.getIdSeduta());
            // setDate/setTime: con setString PostgreSQL rifiuta l'inserimento in colonne DATE/TIME
            stmt.setDate(2, Date.valueOf(seduta.getDataSeduta()));
            stmt.setTime(3, Time.valueOf(seduta.getOraSeduta()));
            stmt.setString(4, seduta.getLuogo());
            stmt.setString(5, seduta.getIdCommissione());
            stmt.setInt(6, seduta.getMaxStudenti());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public SedutaLaurea getSedutaById(String idSeduta) {
        List<SedutaLaurea> lista = trovaMolti("SELECT * FROM sedute_laurea WHERE id_seduta = ?", idSeduta);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public List<SedutaLaurea> getAllSedute() {
        return trovaMolti("SELECT * FROM sedute_laurea ORDER BY data_seduta, ora_seduta");
    }

    @Override
    public List<SedutaLaurea> getSeduteByCommissione(String idCommissione) {
        return trovaMolti("SELECT * FROM sedute_laurea WHERE id_commissione = ? ORDER BY data_seduta, ora_seduta",
                idCommissione);
    }

    @Override
    public List<SedutaLaurea> getSeduteByDocente(String idDocente) {
        return trovaMolti("SELECT s.* FROM sedute_laurea s JOIN membri_commissione mc ON s.id_commissione = mc.id_commissione " +
                "WHERE mc.id_docente = ? ORDER BY s.data_seduta, s.ora_seduta", idDocente);
    }

    @Override
    public boolean aggiornaSeduta(SedutaLaurea seduta) {
        String query = "UPDATE sedute_laurea SET data_seduta = ?, ora_seduta = ?, luogo = ?, id_commissione = ?, max_studenti = ? " +
                "WHERE id_seduta = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(seduta.getDataSeduta()));
            stmt.setTime(2, Time.valueOf(seduta.getOraSeduta()));
            stmt.setString(3, seduta.getLuogo());
            stmt.setString(4, seduta.getIdCommissione());
            stmt.setInt(5, seduta.getMaxStudenti());
            stmt.setString(6, seduta.getIdSeduta());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public boolean eliminaSeduta(String idSeduta) {
        String query = "DELETE FROM sedute_laurea WHERE id_seduta = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idSeduta);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    /**
     * Prenota solo se c'è ancora posto. La riga della seduta viene bloccata (FOR UPDATE)
     * in una transazione, così due prenotazioni contemporanee per l'ultimo posto non
     * possono riuscire entrambe. Restituisce false se la seduta è piena o non esiste.
     */
    @Override
    public boolean prenotaStudente(String idSeduta, Studente studente) {
        String blocca = "SELECT max_studenti FROM sedute_laurea WHERE id_seduta = ? FOR UPDATE";
        String inserisci = "INSERT INTO prenotazioni_seduta (id_seduta, matricola_studente) VALUES (?, ?)";

        try {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                int maxStudenti;
                try (PreparedStatement stmt = connection.prepareStatement(blocca)) {
                    stmt.setString(1, idSeduta);
                    try (ResultSet rs = stmt.executeQuery()) {
                        maxStudenti = rs.next() ? rs.getInt(1) : 0;
                    }
                }
                if (contaPrenotati(idSeduta) >= maxStudenti) {
                    connection.rollback();
                    return false;
                }
                try (PreparedStatement stmt = connection.prepareStatement(inserisci)) {
                    stmt.setString(1, idSeduta);
                    stmt.setString(2, studente.getMatricola());
                    stmt.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public boolean rimuoviStudente(String idSeduta, Studente studente) {
        String query = "DELETE FROM prenotazioni_seduta WHERE id_seduta = ? AND matricola_studente = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idSeduta);
            stmt.setString(2, studente.getMatricola());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public List<Studente> getStudentiPrenotati(String idSeduta) {
        String query = "SELECT s.matricola, s.nome, s.cognome, s.email, s.id_user " +
                "FROM prenotazioni_seduta p JOIN studenti s ON p.matricola_studente = s.matricola " +
                "WHERE p.id_seduta = ? ORDER BY p.data_prenotazione";

        List<Studente> lista = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idSeduta);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(StudentePostgresDAO.mappa(rs));
                }
            }

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }

        return lista;
    }

    @Override
    public int contaPrenotati(String idSeduta) {
        String query = "SELECT COUNT(*) FROM prenotazioni_seduta WHERE id_seduta = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idSeduta);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public SedutaLaurea getSedutaPrenotata(String matricolaStudente) {
        List<SedutaLaurea> lista = trovaMolti("SELECT s.* FROM sedute_laurea s JOIN prenotazioni_seduta p " +
                "ON s.id_seduta = p.id_seduta WHERE p.matricola_studente = ?", matricolaStudente);
        return lista.isEmpty() ? null : lista.get(0);
    }

    private List<SedutaLaurea> trovaMolti(String query, String... parametri) {
        List<SedutaLaurea> lista = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (int i = 0; i < parametri.length; i++) {
                stmt.setString(i + 1, parametri[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mappa(rs));
                }
            }

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }

        return lista;
    }

    private SedutaLaurea mappa(ResultSet rs) throws SQLException {
        return new SedutaLaurea(
                rs.getString("id_seduta"),
                rs.getDate("data_seduta").toLocalDate(),
                rs.getTime("ora_seduta").toLocalTime(),
                rs.getString("luogo"),
                rs.getString("id_commissione"),
                rs.getInt("max_studenti")
        );
    }
}

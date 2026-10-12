package postgresdao;

import dao.DataAccessException;
import dao.DocenteDAO;
import model.Docente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocentePostgresDAO implements DocenteDAO {

    private final Connection connection;

    public DocentePostgresDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean registraDocente(Docente docente) {
        String query = "INSERT INTO docenti (id_docente, nome, cognome, email, id_user) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, docente.getIdDocente());
            stmt.setString(2, docente.getNome());
            stmt.setString(3, docente.getCognome());
            stmt.setString(4, docente.getEmail());
            stmt.setString(5, docente.getIdUser());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public Docente getDocenteByIdDocente(String idDocente) {
        return trovaUno("SELECT * FROM docenti WHERE id_docente = ?", idDocente);
    }

    @Override
    public Docente getDocenteByIdUser(String idUser) {
        System.out.println("ID USER RICEVUTO: [" + idUser + "]");
        String query = "SELECT * FROM public.docenti WHERE id_user = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idUser);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mappa(rs);
                }
                System.out.println("Nessun DOCENTE TROVATO PER id_user = " + idUser);
                return null;
            }
        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public List<Docente> getAllDocenti() {
        String query = "SELECT * FROM docenti ORDER BY cognome, nome";
        List<Docente> docenti = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                docenti.add(mappa(rs));
            }

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }

        return docenti;
    }

    @Override
    public Docente getCoordinatore() {
        // Il ruolo è memorizzato in "utenti", non in "docenti": la query originale
        // (WHERE ruolo = 'COORDINATORE' su docenti) falliva per colonna inesistente.
        String query = "SELECT d.* FROM docenti d JOIN utenti u ON d.id_user = u.id_user " +
                "WHERE u.ruolo = 'COORDINATORE' ORDER BY d.id_docente LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? mappa(rs) : null;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public boolean aggiornaDocente(Docente docente) {
        String query = "UPDATE docenti SET nome = ?, cognome = ?, email = ?, id_user = ? WHERE id_docente = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, docente.getNome());
            stmt.setString(2, docente.getCognome());
            stmt.setString(3, docente.getEmail());
            stmt.setString(4, docente.getIdUser());
            stmt.setString(5, docente.getIdDocente());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public boolean eliminaDocente(String idDocente) {
        String query = "DELETE FROM docenti WHERE id_docente = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idDocente);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    private Docente trovaUno(String query, String parametro) {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, parametro);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mappa(rs) : null;
            }

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    // Riutilizzato anche da CommissionePostgresDAO
    static Docente mappa(ResultSet rs) throws SQLException {
        return new Docente(
                rs.getString("id_docente"),
                rs.getString("nome"),
                rs.getString("cognome"),
                rs.getString("email"),
                rs.getString("id_user")
        );
    }
}

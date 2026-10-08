package postgresdao;

import dao.DataAccessException;
import dao.StudenteDAO;
import model.Studente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentePostgresDAO implements StudenteDAO {

    private final Connection connection;

    public StudentePostgresDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean registraStudente(Studente studente) {
        String query = "INSERT INTO studenti (matricola, nome, cognome, email, id_user) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, studente.getMatricola());
            stmt.setString(2, studente.getNome());
            stmt.setString(3, studente.getCognome());
            stmt.setString(4, studente.getEmail());
            stmt.setString(5, studente.getIdUser());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public Studente getStudenteByMatricola(String matricola) {
        return trovaUno("SELECT * FROM studenti WHERE matricola = ?", matricola);
    }

    @Override
    public Studente getStudenteByIdUser(String idUser) {
        return trovaUno("SELECT * FROM studenti WHERE id_user = ?", idUser);
    }

    @Override
    public List<Studente> getAllStudenti() {
        String query = "SELECT * FROM studenti ORDER BY cognome, nome";
        List<Studente> studenti = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                studenti.add(mappa(rs));
            }

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }

        return studenti;
    }

    @Override
    public boolean aggiornaStudente(Studente studente) {
        String query = "UPDATE studenti SET nome = ?, cognome = ?, email = ?, id_user = ? WHERE matricola = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, studente.getNome());
            stmt.setString(2, studente.getCognome());
            stmt.setString(3, studente.getEmail());
            stmt.setString(4, studente.getIdUser());
            stmt.setString(5, studente.getMatricola());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public boolean eliminaStudente(String matricola) {
        String query = "DELETE FROM studenti WHERE matricola = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, matricola);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    private Studente trovaUno(String query, String parametro) {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, parametro);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mappa(rs) : null;
            }

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    // Riutilizzato anche da SedutaLaureaPostgresDAO
    static Studente mappa(ResultSet rs) throws SQLException {
        return new Studente(
                rs.getString("matricola"),
                rs.getString("nome"),
                rs.getString("cognome"),
                rs.getString("email"),
                rs.getString("id_user")
        );
    }
}

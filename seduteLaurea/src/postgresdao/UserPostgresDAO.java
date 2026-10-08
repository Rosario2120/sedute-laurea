package postgresdao;

import dao.DataAccessException;
import dao.UserDAO;
import model.Ruolo;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserPostgresDAO implements UserDAO {

    private final Connection connection;

    public UserPostgresDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean registraUtente(User user) {
        String query = "INSERT INTO utenti (id_user, username, password_hash, ruolo) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getIdUser());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getRuolo().name());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public User getUserById(String idUser) {
        return trovaUno("SELECT * FROM utenti WHERE id_user = ?", idUser);
    }

    @Override
    public User getUserByUsername(String username) {
        return trovaUno("SELECT * FROM utenti WHERE username = ?", username);
    }

    @Override
    public boolean usernameEsiste(String username) {
        return getUserByUsername(username) != null;
    }

    @Override
    public List<User> getAllUtenti() {
        String query = "SELECT * FROM utenti ORDER BY ruolo, username";
        List<User> lista = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mappa(rs));
            }

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }

        return lista;
    }

    @Override
    public boolean aggiornaPassword(String idUser, String nuovoPasswordHash) {
        String query = "UPDATE utenti SET password_hash = ? WHERE id_user = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nuovoPasswordHash);
            stmt.setString(2, idUser);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public boolean eliminaUtente(String idUser) {
        String query = "DELETE FROM utenti WHERE id_user = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idUser);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    private User trovaUno(String query, String parametro) {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, parametro);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mappa(rs) : null;
            }

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    private User mappa(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("id_user"),
                rs.getString("username"),
                rs.getString("password_hash"),
                Ruolo.valueOf(rs.getString("ruolo"))
        );
    }
}

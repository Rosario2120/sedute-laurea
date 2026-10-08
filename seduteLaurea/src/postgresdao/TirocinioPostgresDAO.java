package postgresdao;

import dao.DataAccessException;
import dao.TirocinioDAO;
import model.Tirocinio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TirocinioPostgresDAO implements TirocinioDAO {

    private final Connection connection;

    public TirocinioPostgresDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean aggiungiTirocinio(Tirocinio tirocinio) {
        String query = "INSERT INTO tirocini (id_tirocinio, titolo, descrizione, id_docente) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tirocinio.getIdTirocinio());
            stmt.setString(2, tirocinio.getTitolo());
            stmt.setString(3, tirocinio.getDescrizione());
            stmt.setString(4, tirocinio.getIdDocente());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public Tirocinio getTirocinioById(String idTirocinio) {
        String query = "SELECT * FROM tirocini WHERE id_tirocinio = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idTirocinio);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mappa(rs) : null;
            }

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public List<Tirocinio> getAllTirocini() {
        return trovaMolti("SELECT * FROM tirocini ORDER BY titolo");
    }

    @Override
    public List<Tirocinio> getTirociniByDocente(String idDocente) {
        return trovaMolti("SELECT * FROM tirocini WHERE id_docente = ? ORDER BY titolo", idDocente);
    }

    @Override
    public boolean aggiornaTirocinio(Tirocinio tirocinio) {
        String query = "UPDATE tirocini SET titolo = ?, descrizione = ?, id_docente = ? WHERE id_tirocinio = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tirocinio.getTitolo());
            stmt.setString(2, tirocinio.getDescrizione());
            stmt.setString(3, tirocinio.getIdDocente());
            stmt.setString(4, tirocinio.getIdTirocinio());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public boolean eliminaTirocinio(String idTirocinio) {
        String query = "DELETE FROM tirocini WHERE id_tirocinio = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idTirocinio);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    private List<Tirocinio> trovaMolti(String query, String... parametri) {
        List<Tirocinio> lista = new ArrayList<>();

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

    private Tirocinio mappa(ResultSet rs) throws SQLException {
        return new Tirocinio(
                rs.getString("id_tirocinio"),
                rs.getString("titolo"),
                rs.getString("descrizione"),
                rs.getString("id_docente")
        );
    }
}

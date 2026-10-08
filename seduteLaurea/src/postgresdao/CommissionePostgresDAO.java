package postgresdao;

import dao.CommissioneDAO;
import dao.DataAccessException;
import model.Commissione;
import model.Docente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommissionePostgresDAO implements CommissioneDAO {

    private final Connection connection;

    public CommissionePostgresDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean aggiungiCommissione(Commissione commissione) {
        String query = "INSERT INTO commissioni (id_commissione, id_presidente) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, commissione.getIdCommissione());
            stmt.setString(2, commissione.getIdPresidente());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public Commissione getCommissioneById(String idCommissione) {
        List<Commissione> lista = trovaMolti("SELECT * FROM commissioni WHERE id_commissione = ?", idCommissione);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public List<Commissione> getAllCommissioni() {
        return trovaMolti("SELECT * FROM commissioni ORDER BY id_commissione");
    }

    @Override
    public List<Commissione> getCommissioniByDocente(String idDocente) {
        return trovaMolti("SELECT c.* FROM commissioni c JOIN membri_commissione mc ON c.id_commissione = mc.id_commissione " +
                "WHERE mc.id_docente = ? ORDER BY c.id_commissione", idDocente);
    }

    @Override
    public boolean aggiornaCommissione(Commissione commissione) {
        String query = "UPDATE commissioni SET id_presidente = ? WHERE id_commissione = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, commissione.getIdPresidente());
            stmt.setString(2, commissione.getIdCommissione());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public boolean eliminaCommissione(String idCommissione) {
        String query = "DELETE FROM commissioni WHERE id_commissione = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idCommissione);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public boolean aggiungiMembro(String idCommissione, Docente docente) {
        String query = "INSERT INTO membri_commissione (id_commissione, id_docente) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idCommissione);
            stmt.setString(2, docente.getIdDocente());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public boolean rimuoviMembro(String idCommissione, Docente docente) {
        String query = "DELETE FROM membri_commissione WHERE id_commissione = ? AND id_docente = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idCommissione);
            stmt.setString(2, docente.getIdDocente());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public List<Docente> getMembriCommissione(String idCommissione) {
        String query = "SELECT d.id_docente, d.nome, d.cognome, d.email, d.id_user " +
                "FROM membri_commissione mc JOIN docenti d ON mc.id_docente = d.id_docente " +
                "WHERE mc.id_commissione = ? ORDER BY d.cognome, d.nome";

        List<Docente> lista = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idCommissione);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(DocentePostgresDAO.mappa(rs));
                }
            }

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }

        return lista;
    }

    private List<Commissione> trovaMolti(String query, String... parametri) {
        List<Commissione> lista = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (int i = 0; i < parametri.length; i++) {
                stmt.setString(i + 1, parametri[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Commissione(rs.getString("id_commissione"), rs.getString("id_presidente")));
                }
            }

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }

        // carica i membri (prima mancava: aggiungiMembro(Docente) nel model era vuoto)
        for (Commissione c : lista) {
            for (Docente membro : getMembriCommissione(c.getIdCommissione())) {
                c.aggiungiMembro(membro);
            }
        }

        return lista;
    }
}

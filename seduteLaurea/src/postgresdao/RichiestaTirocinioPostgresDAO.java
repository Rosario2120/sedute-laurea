package postgresdao;

import dao.DataAccessException;
import dao.RichiestaTirocinioDAO;
import model.RichiestaTirocinio;
import model.StatoRichiesta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RichiestaTirocinioPostgresDAO implements RichiestaTirocinioDAO {

    private final Connection connection;

    public RichiestaTirocinioPostgresDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean aggiungiRichiesta(RichiestaTirocinio richiesta) {
        // data_richiesta viene valorizzata dal DEFAULT del database
        String query = "INSERT INTO richieste_tirocinio (id_richiesta, matricola_studente, id_tirocinio, stato) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, richiesta.getIdRichiesta());
            stmt.setString(2, richiesta.getMatricolaStudente());
            stmt.setString(3, richiesta.getIdTirocinio());
            stmt.setString(4, richiesta.getStato().name());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public RichiestaTirocinio getRichiestaById(String idRichiesta) {
        List<RichiestaTirocinio> lista = trovaMolti("SELECT * FROM richieste_tirocinio WHERE id_richiesta = ?", idRichiesta);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public List<RichiestaTirocinio> getAllRichieste() {
        return trovaMolti("SELECT * FROM richieste_tirocinio ORDER BY data_richiesta DESC");
    }

    @Override
    public List<RichiestaTirocinio> getRichiesteByStudente(String matricolaStudente) {
        return trovaMolti("SELECT * FROM richieste_tirocinio WHERE matricola_studente = ? ORDER BY data_richiesta DESC",
                matricolaStudente);
    }

    @Override
    public List<RichiestaTirocinio> getRichiesteByTirocinio(String idTirocinio) {
        return trovaMolti("SELECT * FROM richieste_tirocinio WHERE id_tirocinio = ? ORDER BY data_richiesta DESC",
                idTirocinio);
    }

    @Override
    public List<RichiestaTirocinio> getRichiesteByDocente(String idDocente) {
        return trovaMolti("SELECT r.* FROM richieste_tirocinio r JOIN tirocini t ON r.id_tirocinio = t.id_tirocinio " +
                "WHERE t.id_docente = ? ORDER BY r.data_richiesta DESC", idDocente);
    }

    @Override
    public boolean aggiornaStatoRichiesta(String idRichiesta, StatoRichiesta nuovoStato) {
        String query = "UPDATE richieste_tirocinio SET stato = ? WHERE id_richiesta = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nuovoStato.name());
            stmt.setString(2, idRichiesta);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public boolean aggiornaRichiesta(RichiestaTirocinio richiesta) {
        String query = "UPDATE richieste_tirocinio SET matricola_studente = ?, id_tirocinio = ?, stato = ? WHERE id_richiesta = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, richiesta.getMatricolaStudente());
            stmt.setString(2, richiesta.getIdTirocinio());
            stmt.setString(3, richiesta.getStato().name());
            stmt.setString(4, richiesta.getIdRichiesta());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public boolean eliminaRichiesta(String idRichiesta) {
        String query = "DELETE FROM richieste_tirocinio WHERE id_richiesta = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idRichiesta);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    private List<RichiestaTirocinio> trovaMolti(String query, String... parametri) {
        List<RichiestaTirocinio> lista = new ArrayList<>();

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

    private RichiestaTirocinio mappa(ResultSet rs) throws SQLException {
        RichiestaTirocinio r = new RichiestaTirocinio(
                rs.getString("id_richiesta"),
                rs.getString("matricola_studente"),
                rs.getString("id_tirocinio"),
                StatoRichiesta.valueOf(rs.getString("stato"))
        );
        Timestamp data = rs.getTimestamp("data_richiesta");
        r.setDataRichiesta(data != null ? data.toLocalDateTime() : null);
        return r;
    }
}

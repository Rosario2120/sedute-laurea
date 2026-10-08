package postgresdao;

import dao.DataAccessException;
import dao.TesiDAO;
import model.StatoRichiesta;
import model.Tesi;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TesiPostgresDAO implements TesiDAO {

    private final Connection connection;

    public TesiPostgresDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean aggiungiTesi(Tesi tesi) {
        // data_richiesta viene valorizzata dal DEFAULT del database
        String query = "INSERT INTO tesi (id_tesi, matricola_studente, id_docente_relatore, titolo, stato) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tesi.getIdTesi());
            stmt.setString(2, tesi.getMatricolaStudente());
            stmt.setString(3, tesi.getIdDocenteRelatore());
            stmt.setString(4, tesi.getTitolo());
            stmt.setString(5, tesi.getStato().name());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public Tesi getTesiById(String idTesi) {
        List<Tesi> lista = trovaMolti("SELECT * FROM tesi WHERE id_tesi = ?", idTesi);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public List<Tesi> getAllTesi() {
        return trovaMolti("SELECT * FROM tesi ORDER BY data_richiesta DESC");
    }

    @Override
    public List<Tesi> getTesiByStudente(String matricolaStudente) {
        return trovaMolti("SELECT * FROM tesi WHERE matricola_studente = ? ORDER BY data_richiesta DESC",
                matricolaStudente);
    }

    @Override
    public List<Tesi> getTesiByRelatore(String idDocente) {
        return trovaMolti("SELECT * FROM tesi WHERE id_docente_relatore = ? ORDER BY data_richiesta DESC", idDocente);
    }

    @Override
    public boolean aggiornaTesi(Tesi tesi) {
        String query = "UPDATE tesi SET matricola_studente = ?, id_docente_relatore = ?, titolo = ?, stato = ? WHERE id_tesi = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tesi.getMatricolaStudente());
            stmt.setString(2, tesi.getIdDocenteRelatore());
            stmt.setString(3, tesi.getTitolo());
            stmt.setString(4, tesi.getStato().name());
            stmt.setString(5, tesi.getIdTesi());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public boolean aggiornaStatoTesi(String idTesi, StatoRichiesta nuovoStato) {
        String query = "UPDATE tesi SET stato = ? WHERE id_tesi = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nuovoStato.name());
            stmt.setString(2, idTesi);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    @Override
    public boolean eliminaTesi(String idTesi) {
        String query = "DELETE FROM tesi WHERE id_tesi = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, idTesi);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DataAccessException.da(e);
        }
    }

    private List<Tesi> trovaMolti(String query, String... parametri) {
        List<Tesi> lista = new ArrayList<>();

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

    private Tesi mappa(ResultSet rs) throws SQLException {
        Tesi t = new Tesi(
                rs.getString("id_tesi"),
                rs.getString("matricola_studente"),
                rs.getString("id_docente_relatore"),
                rs.getString("titolo"),
                StatoRichiesta.valueOf(rs.getString("stato"))
        );
        Timestamp data = rs.getTimestamp("data_richiesta");
        t.setDataRichiesta(data != null ? data.toLocalDateTime() : null);
        return t;
    }
}

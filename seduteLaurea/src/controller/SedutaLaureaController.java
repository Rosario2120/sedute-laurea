package controller;

import dao.CommissioneDAO;
import dao.SedutaLaureaDAO;
import model.SedutaLaurea;
import model.Studente;
import util.IdGenerator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static controller.Regole.obbligatorio;
import static controller.Regole.verifica;

public class SedutaLaureaController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SedutaLaureaDAO sedutaDAO;
    private final CommissioneDAO commissioneDAO;
    private final TesiController tesiController;

    public SedutaLaureaController(SedutaLaureaDAO sedutaDAO, CommissioneDAO commissioneDAO,
                                  TesiController tesiController) {
        this.sedutaDAO = sedutaDAO;
        this.commissioneDAO = commissioneDAO;
        this.tesiController = tesiController;
    }

    // NUOVA SEDUTA (la commissione può essere assegnata anche in seguito)
    public SedutaLaurea creaSeduta(LocalDate data, LocalTime ora, String luogo, String idCommissione, int maxStudenti) {
        validaDati(data, ora, idCommissione, maxStudenti);
        SedutaLaurea nuova = new SedutaLaurea(IdGenerator.nuovoId("SED"), data, ora,
                obbligatorio(luogo, "Luogo"), idCommissione, maxStudenti);
        sedutaDAO.aggiungiSeduta(nuova);
        return nuova;
    }

    // MODIFICA SEDUTA
    public void modificaSeduta(String idSeduta, LocalDate data, LocalTime ora, String luogo,
                               String idCommissione, int maxStudenti) {
        SedutaLaurea seduta = seduta(idSeduta);
        verifica(seduta.isFutura(), "Una seduta già svolta o in corso non può essere modificata.");
        validaDati(data, ora, idCommissione, maxStudenti);
        int prenotati = sedutaDAO.contaPrenotati(idSeduta);
        verifica(maxStudenti >= prenotati,
                "Ci sono già " + prenotati + " studenti prenotati: il numero massimo non può essere inferiore.");
        verifica(idCommissione != null || prenotati == 0,
                "Una seduta con studenti prenotati deve avere una commissione.");

        seduta.setDataSeduta(data);
        seduta.setOraSeduta(ora);
        seduta.setLuogo(obbligatorio(luogo, "Luogo"));
        seduta.setIdCommissione(idCommissione);
        seduta.setMaxStudenti(maxStudenti);
        sedutaDAO.aggiornaSeduta(seduta);
    }

    // ELIMINA SEDUTA (le prenotazioni vengono eliminate a cascata)
    public void eliminaSeduta(String idSeduta) {
        verifica(sedutaDAO.eliminaSeduta(idSeduta), "Seduta non trovata.");
    }

    // PRENOTA STUDENTE: tutte le regole di prenotazione sono qui
    public void prenotaStudente(String idSeduta, Studente studente) {
        SedutaLaurea seduta = seduta(idSeduta);
        verifica(seduta.isFutura(), "Le prenotazioni per questa seduta sono chiuse.");
        verifica(seduta.getIdCommissione() != null, "Alla seduta non è ancora stata assegnata una commissione.");
        verifica(tesiController.haTesiApprovata(studente.getMatricola()),
                "Per prenotarti a una seduta serve una tesi approvata dal relatore.");

        SedutaLaurea giaPrenotata = sedutaDAO.getSedutaPrenotata(studente.getMatricola());
        if (giaPrenotata != null) {
            throw new OperazioneNonValidaException("Sei già prenotato alla seduta del "
                    + giaPrenotata.getDataSeduta().format(FORMATO_DATA) + ": annulla prima quella prenotazione.");
        }
        // controllo ripetuto dal DAO in modo atomico, per le prenotazioni contemporanee
        verifica(sedutaDAO.contaPrenotati(idSeduta) < seduta.getMaxStudenti()
                && sedutaDAO.prenotaStudente(idSeduta, studente), "La seduta è al completo.");
    }

    // ANNULLA PRENOTAZIONE da parte dello studente (solo prima della seduta)
    public void annullaPrenotazione(String idSeduta, Studente studente) {
        SedutaLaurea seduta = seduta(idSeduta);
        verifica(seduta.isFutura(), "Non puoi annullare la prenotazione a una seduta già svolta o in corso.");
        verifica(sedutaDAO.rimuoviStudente(idSeduta, studente), "Non risulti prenotato a questa seduta.");
    }

    // RIMUOVI PRENOTAZIONE da parte del coordinatore (nessun vincolo di data)
    public void rimuoviPrenotazione(String idSeduta, Studente studente) {
        verifica(sedutaDAO.rimuoviStudente(idSeduta, studente), "Lo studente non è prenotato a questa seduta.");
    }

    // RECUPERA SEDUTA PER ID
    public SedutaLaurea getSedutaById(String idSeduta) {
        return sedutaDAO.getSedutaById(idSeduta);
    }

    // LISTA COMPLETA SEDUTE
    public List<SedutaLaurea> getAllSedute() {
        return sedutaDAO.getAllSedute();
    }

    // SEDUTE PRENOTABILI: future e con commissione assegnata
    public List<SedutaLaurea> getSeduteDisponibili() {
        return sedutaDAO.getAllSedute().stream()
                .filter(s -> s.isFutura() && s.getIdCommissione() != null)
                .toList();
    }

    // SEDUTE IN CUI IL DOCENTE È IN COMMISSIONE
    public List<SedutaLaurea> getSeduteByDocente(String idDocente) {
        return sedutaDAO.getSeduteByDocente(idDocente);
    }

    public List<Studente> getStudentiPrenotati(String idSeduta) {
        return sedutaDAO.getStudentiPrenotati(idSeduta);
    }

    public int contaPrenotati(String idSeduta) {
        return sedutaDAO.contaPrenotati(idSeduta);
    }

    public SedutaLaurea getSedutaPrenotata(String matricolaStudente) {
        return sedutaDAO.getSedutaPrenotata(matricolaStudente);
    }

    private void validaDati(LocalDate data, LocalTime ora, String idCommissione, int maxStudenti) {
        verifica(data != null, "Indica la data della seduta.");
        verifica(data.isAfter(LocalDate.now()), "La data della seduta deve essere successiva a oggi.");
        verifica(ora != null, "Indica l'ora della seduta.");
        verifica(maxStudenti > 0, "Il numero massimo di studenti deve essere maggiore di zero.");
        verifica(idCommissione == null || commissioneDAO.getCommissioneById(idCommissione) != null,
                "Commissione non trovata.");
    }

    private SedutaLaurea seduta(String idSeduta) {
        SedutaLaurea seduta = sedutaDAO.getSedutaById(idSeduta);
        verifica(seduta != null, "Seduta non trovata.");
        return seduta;
    }
}

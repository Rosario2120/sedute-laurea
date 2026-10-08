package support;

/**
 * Identificativi presenti in db/seed.sql, usati dai test.
 */
public final class DatiDemo {

    public static final String PASSWORD = "password";

    // Docenti
    public static final String COORDINATRICE_FERRARA = "DOC-001";
    public static final String DOC_ROSSI = "DOC-002";
    public static final String DOC_BIANCHI = "DOC-003";
    public static final String DOC_VERDI = "DOC-004";

    // Studenti
    public static final String ESPOSITO = "N86001001"; // tirocinio e tesi approvati, prenotato a SED-001
    public static final String ROMANO = "N86001002";   // richiesta tirocinio e tesi in attesa
    public static final String COLOMBO = "N86001003";  // richiesta tirocinio in attesa, nessuna tesi
    public static final String RICCI = "N86001004";    // tirocinio e tesi approvati, non prenotata

    // Tirocini (TIR-001 e TIR-002 di Rossi, TIR-003 di Bianchi, TIR-004 di Verdi)
    public static final String TIR_ANDROID = "TIR-001";
    public static final String TIR_DATI = "TIR-002";
    public static final String TIR_RETI = "TIR-003";
    public static final String TIR_ML = "TIR-004";

    // Richieste
    public static final String RICH_ROMANO_RETI = "RICH-002";   // in attesa, docente Bianchi
    public static final String RICH_COLOMBO_ANDROID = "RICH-003"; // in attesa, docente Rossi

    // Tesi
    public static final String TESI_ROMANO = "TESI-002"; // in attesa, relatore Bianchi

    // Commissioni
    public static final String COMM_FERRARA = "COMM-001"; // Ferrara (pres.), Rossi, Bianchi
    public static final String COMM_VERDI = "COMM-002";   // Verdi (pres.), Rossi, Bianchi

    // Sedute
    public static final String SED_TRA_30_GIORNI = "SED-001"; // COMM-001, 10 posti, Esposito prenotato
    public static final String SED_TRA_60_GIORNI = "SED-002"; // COMM-002, 2 posti
    public static final String SED_PASSATA = "SED-003";
    public static final String SED_SENZA_COMMISSIONE = "SED-004";

    private DatiDemo() {
    }
}

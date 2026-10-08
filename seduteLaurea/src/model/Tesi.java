package model;

import java.time.LocalDateTime;

public class Tesi {

    private String idTesi;
    private String matricolaStudente;
    private String idDocenteRelatore;
    private String titolo;
    private StatoRichiesta stato;
    private LocalDateTime dataRichiesta; // impostata dal database all'inserimento

    public Tesi(String idTesi, String matricolaStudente, String idDocenteRelatore, String titolo, StatoRichiesta stato) {
        this.idTesi = idTesi;
        this.matricolaStudente = matricolaStudente;
        this.idDocenteRelatore = idDocenteRelatore;
        this.titolo = titolo;
        this.stato = stato;
    }

    public String getIdTesi() {
        return idTesi;
    }

    public void setIdTesi(String idTesi) {
        this.idTesi = idTesi;
    }

    public String getMatricolaStudente() {
        return matricolaStudente;
    }

    public void setMatricolaStudente(String matricolaStudente) {
        this.matricolaStudente = matricolaStudente;
    }

    public String getIdDocenteRelatore() {
        return idDocenteRelatore;
    }

    public void setIdDocenteRelatore(String idDocenteRelatore) {
        this.idDocenteRelatore = idDocenteRelatore;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public StatoRichiesta getStato() {
        return stato;
    }

    public void setStato(StatoRichiesta stato) {
        this.stato = stato;
    }

    public LocalDateTime getDataRichiesta() {
        return dataRichiesta;
    }

    public void setDataRichiesta(LocalDateTime dataRichiesta) {
        this.dataRichiesta = dataRichiesta;
    }
}

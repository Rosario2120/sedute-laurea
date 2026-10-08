package model;

import java.time.LocalDateTime;

public class RichiestaTirocinio {

    private String idRichiesta;
    private String matricolaStudente;
    private String idTirocinio;
    private StatoRichiesta stato;
    private LocalDateTime dataRichiesta; // impostata dal database all'inserimento

    public RichiestaTirocinio(String idRichiesta, String matricolaStudente, String idTirocinio, StatoRichiesta stato) {
        this.idRichiesta = idRichiesta;
        this.matricolaStudente = matricolaStudente;
        this.idTirocinio = idTirocinio;
        this.stato = stato;
    }

    public String getIdRichiesta() {
        return idRichiesta;
    }

    public void setIdRichiesta(String idRichiesta) {
        this.idRichiesta = idRichiesta;
    }

    public String getMatricolaStudente() {
        return matricolaStudente;
    }

    public void setMatricolaStudente(String matricolaStudente) {
        this.matricolaStudente = matricolaStudente;
    }

    public String getIdTirocinio() {
        return idTirocinio;
    }

    public void setIdTirocinio(String idTirocinio) {
        this.idTirocinio = idTirocinio;
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

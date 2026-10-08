package model;

public class Tirocinio {

    private String idTirocinio;
    private String titolo;
    private String descrizione;
    private String idDocente; // docente che propone il tirocinio

    public Tirocinio(String idTirocinio, String titolo, String descrizione, String idDocente) {
        this.idTirocinio = idTirocinio;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.idDocente = idDocente;
    }

    public String getIdTirocinio() {
        return idTirocinio;
    }

    public void setIdTirocinio(String idTirocinio) {
        this.idTirocinio = idTirocinio;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(String idDocente) {
        this.idDocente = idDocente;
    }

    @Override
    public String toString() {
        return titolo;
    }
}

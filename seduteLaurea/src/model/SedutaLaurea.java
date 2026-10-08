package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class SedutaLaurea {

    private String idSeduta;
    private LocalDate dataSeduta;
    private LocalTime oraSeduta;
    private String luogo;
    private String idCommissione; // può essere null finché non viene assegnata
    private int maxStudenti;

    public SedutaLaurea(String idSeduta, LocalDate dataSeduta, LocalTime oraSeduta, String luogo,
                        String idCommissione, int maxStudenti) {
        this.idSeduta = idSeduta;
        this.dataSeduta = dataSeduta;
        this.oraSeduta = oraSeduta;
        this.luogo = luogo;
        this.idCommissione = idCommissione;
        this.maxStudenti = maxStudenti;
    }

    public String getIdSeduta() {
        return idSeduta;
    }

    public void setIdSeduta(String idSeduta) {
        this.idSeduta = idSeduta;
    }

    public LocalDate getDataSeduta() {
        return dataSeduta;
    }

    public void setDataSeduta(LocalDate dataSeduta) {
        this.dataSeduta = dataSeduta;
    }

    public LocalTime getOraSeduta() {
        return oraSeduta;
    }

    public void setOraSeduta(LocalTime oraSeduta) {
        this.oraSeduta = oraSeduta;
    }

    public String getLuogo() {
        return luogo;
    }

    public void setLuogo(String luogo) {
        this.luogo = luogo;
    }

    public String getIdCommissione() {
        return idCommissione;
    }

    public void setIdCommissione(String idCommissione) {
        this.idCommissione = idCommissione;
    }

    public int getMaxStudenti() {
        return maxStudenti;
    }

    public void setMaxStudenti(int maxStudenti) {
        this.maxStudenti = maxStudenti;
    }

    // Una seduta di oggi non è più prenotabile: le prenotazioni si chiudono il giorno prima
    public boolean isFutura() {
        return dataSeduta.isAfter(LocalDate.now());
    }
}

package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Commissione {

    private String idCommissione;
    private String idPresidente;
    private final List<Docente> membri; // il presidente è sempre incluso

    public Commissione(String idCommissione, String idPresidente) {
        this.idCommissione = idCommissione;
        this.idPresidente = idPresidente;
        this.membri = new ArrayList<>();
    }

    public String getIdCommissione() {
        return idCommissione;
    }

    public void setIdCommissione(String idCommissione) {
        this.idCommissione = idCommissione;
    }

    public String getIdPresidente() {
        return idPresidente;
    }

    public void setIdPresidente(String idPresidente) {
        this.idPresidente = idPresidente;
    }

    public List<Docente> getMembri() {
        return Collections.unmodifiableList(membri);
    }

    public void aggiungiMembro(Docente docente) {
        if (!isMembro(docente.getIdDocente())) {
            membri.add(docente);
        }
    }

    public void rimuoviMembro(String idDocente) {
        membri.removeIf(d -> d.getIdDocente().equals(idDocente));
    }

    public boolean isMembro(String idDocente) {
        return membri.stream().anyMatch(d -> d.getIdDocente().equals(idDocente));
    }

    public Docente getPresidente() {
        return membri.stream()
                .filter(d -> d.getIdDocente().equals(idPresidente))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        Docente presidente = getPresidente();
        return idCommissione + (presidente != null ? " - pres. " + presidente.getNomeCompleto() : "");
    }
}

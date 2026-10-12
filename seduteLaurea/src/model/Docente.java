package model;
public class Docente {
    private String idDocente;
    private String nome;
    private String cognome;
    private String email;
    private String idUser;
    public Docente(String idDocente, String nome, String cognome,
                   String email, String idUser) {
        this.idDocente = idDocente;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.idUser = idUser;
    }
    public String getIdDocente() {
        return idDocente;
    }
    public void setIdDocente(String idDocente) {
        this.idDocente = idDocente;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getCognome() {
        return cognome;
    }
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getIdUser() {
        return idUser;
    }
    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }
}
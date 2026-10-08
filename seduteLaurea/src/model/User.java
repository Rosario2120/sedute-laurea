package model;

public class User {

    private String idUser;
    private String username;
    private String passwordHash;
    private Ruolo ruolo;

    public User(String idUser, String username, String passwordHash, Ruolo ruolo) {
        this.idUser = idUser;
        this.username = username;
        this.passwordHash = passwordHash;
        this.ruolo = ruolo;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Ruolo getRuolo() {
        return ruolo;
    }

    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }
}

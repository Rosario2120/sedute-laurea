package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void verificaLaPasswordCorretta() {
        String hash = PasswordHasher.hash("segreta123");
        assertTrue(PasswordHasher.verifica("segreta123", hash));
        assertFalse(PasswordHasher.verifica("Segreta123", hash));
    }

    @Test
    void usaUnSaleDiversoAOgniHash() {
        assertNotEquals(PasswordHasher.hash("uguale"), PasswordHasher.hash("uguale"));
    }

    @Test
    void nonSalvaLaPasswordInChiaro() {
        String hash = PasswordHasher.hash("segreta123");
        assertFalse(hash.contains("segreta123"));
        assertTrue(hash.startsWith("pbkdf2_sha256$"));
    }

    @Test
    void rifiutaHashMalformatiSenzaEccezioni() {
        assertFalse(PasswordHasher.verifica("x", null));
        assertFalse(PasswordHasher.verifica(null, PasswordHasher.hash("x")));
        assertFalse(PasswordHasher.verifica("x", "testo-in-chiaro"));
        assertFalse(PasswordHasher.verifica("x", "pbkdf2_sha256$abc$!!$!!"));
    }

    @Test
    void eCompatibileConGliHashDelSeed() {
        // hash di "password" presente in db/seed.sql, generato fuori da Java
        String dalSeed = "pbkdf2_sha256$65536$olsH+Rg8Uq8f7zwF5Y8H2Q==$8jhJaUBnrAg680LBvW0s+XYXRX2K/murEAwgRvUJZvY=";
        assertTrue(PasswordHasher.verifica("password", dalSeed));
    }
}

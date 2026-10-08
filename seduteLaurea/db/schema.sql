-- =====================================================================
-- Schema del database "sedutelaurea"
--
-- Ricostruito a partire dalle query presenti in src/postgresdao/*.java
-- (nomi di tabelle e colonne invariati) e completato con chiavi esterne,
-- vincoli di unicità e di dominio che nel progetto originale mancavano.
--
-- Lo script è idempotente: elimina e ricrea tutte le tabelle.
-- =====================================================================

DROP TABLE IF EXISTS prenotazioni_seduta CASCADE;
DROP TABLE IF EXISTS sedute_laurea       CASCADE;
DROP TABLE IF EXISTS membri_commissione  CASCADE;
DROP TABLE IF EXISTS commissioni         CASCADE;
DROP TABLE IF EXISTS tesi                CASCADE;
DROP TABLE IF EXISTS richieste_tirocinio CASCADE;
DROP TABLE IF EXISTS tirocini            CASCADE;
DROP TABLE IF EXISTS docenti             CASCADE;
DROP TABLE IF EXISTS studenti            CASCADE;
DROP TABLE IF EXISTS utenti              CASCADE;

-- ---------------------------------------------------------------------
-- Account di accesso. Il ruolo determina l'area riservata mostrata.
-- Un COORDINATORE è anche un docente (ha una riga in "docenti").
-- ---------------------------------------------------------------------
CREATE TABLE utenti (
    id_user       VARCHAR(40)  PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    ruolo         VARCHAR(20)  NOT NULL
        CHECK (ruolo IN ('STUDENTE', 'DOCENTE', 'COORDINATORE'))
);

CREATE TABLE studenti (
    matricola VARCHAR(20)  PRIMARY KEY,
    nome      VARCHAR(50)  NOT NULL,
    cognome   VARCHAR(50)  NOT NULL,
    email     VARCHAR(100) NOT NULL UNIQUE,
    id_user   VARCHAR(40)  NOT NULL UNIQUE
        REFERENCES utenti (id_user) ON DELETE CASCADE
);

CREATE TABLE docenti (
    id_docente VARCHAR(20)  PRIMARY KEY,
    nome       VARCHAR(50)  NOT NULL,
    cognome    VARCHAR(50)  NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    id_user    VARCHAR(40)  NOT NULL UNIQUE
        REFERENCES utenti (id_user) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Tirocini proposti dai docenti e richieste degli studenti.
-- Un docente con tirocini, tesi o commissioni non può essere eliminato
-- (le FK verso "docenti" sono RESTRICT di default).
-- ---------------------------------------------------------------------
CREATE TABLE tirocini (
    id_tirocinio VARCHAR(40)  PRIMARY KEY,
    titolo       VARCHAR(150) NOT NULL,
    descrizione  TEXT,
    id_docente   VARCHAR(20)  NOT NULL REFERENCES docenti (id_docente)
);

CREATE TABLE richieste_tirocinio (
    id_richiesta       VARCHAR(40) PRIMARY KEY,
    matricola_studente VARCHAR(20) NOT NULL
        REFERENCES studenti (matricola) ON DELETE CASCADE,
    id_tirocinio       VARCHAR(40) NOT NULL
        REFERENCES tirocini (id_tirocinio) ON DELETE CASCADE,
    stato              VARCHAR(20) NOT NULL DEFAULT 'IN_ATTESA'
        CHECK (stato IN ('IN_ATTESA', 'APPROVATA', 'RIFIUTATA')),
    data_richiesta     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Una sola richiesta "viva" (non rifiutata) per coppia studente/tirocinio
CREATE UNIQUE INDEX uq_richiesta_attiva
    ON richieste_tirocinio (matricola_studente, id_tirocinio)
    WHERE stato <> 'RIFIUTATA';

-- Al massimo un tirocinio approvato per studente
CREATE UNIQUE INDEX uq_tirocinio_approvato
    ON richieste_tirocinio (matricola_studente)
    WHERE stato = 'APPROVATA';

CREATE INDEX idx_richieste_tirocinio ON richieste_tirocinio (id_tirocinio);

-- ---------------------------------------------------------------------
-- Tesi: richiesta dello studente al docente relatore.
-- ---------------------------------------------------------------------
CREATE TABLE tesi (
    id_tesi             VARCHAR(40)  PRIMARY KEY,
    matricola_studente  VARCHAR(20)  NOT NULL
        REFERENCES studenti (matricola) ON DELETE CASCADE,
    id_docente_relatore VARCHAR(20)  NOT NULL REFERENCES docenti (id_docente),
    titolo              VARCHAR(200) NOT NULL,
    stato               VARCHAR(20)  NOT NULL DEFAULT 'IN_ATTESA'
        CHECK (stato IN ('IN_ATTESA', 'APPROVATA', 'RIFIUTATA')),
    data_richiesta      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Una sola tesi non rifiutata per studente (dopo un rifiuto può riprovare)
CREATE UNIQUE INDEX uq_tesi_attiva
    ON tesi (matricola_studente)
    WHERE stato <> 'RIFIUTATA';

CREATE INDEX idx_tesi_relatore ON tesi (id_docente_relatore);

-- ---------------------------------------------------------------------
-- Commissioni di laurea. Il presidente è sempre anche membro
-- (regola applicata da CommissioneController).
-- ---------------------------------------------------------------------
CREATE TABLE commissioni (
    id_commissione VARCHAR(40) PRIMARY KEY,
    id_presidente  VARCHAR(20) NOT NULL REFERENCES docenti (id_docente)
);

CREATE TABLE membri_commissione (
    id_commissione VARCHAR(40) NOT NULL
        REFERENCES commissioni (id_commissione) ON DELETE CASCADE,
    id_docente     VARCHAR(20) NOT NULL REFERENCES docenti (id_docente),
    PRIMARY KEY (id_commissione, id_docente)
);

CREATE INDEX idx_membri_docente ON membri_commissione (id_docente);

-- ---------------------------------------------------------------------
-- Sedute di laurea e prenotazioni degli studenti.
-- Una commissione assegnata a una seduta non può essere eliminata.
-- ---------------------------------------------------------------------
CREATE TABLE sedute_laurea (
    id_seduta      VARCHAR(40)  PRIMARY KEY,
    data_seduta    DATE         NOT NULL,
    ora_seduta     TIME         NOT NULL,
    luogo          VARCHAR(100) NOT NULL,
    id_commissione VARCHAR(40)  REFERENCES commissioni (id_commissione),
    max_studenti   INTEGER      NOT NULL DEFAULT 10 CHECK (max_studenti > 0)
);

CREATE INDEX idx_sedute_commissione ON sedute_laurea (id_commissione);

CREATE TABLE prenotazioni_seduta (
    id_seduta          VARCHAR(40) NOT NULL
        REFERENCES sedute_laurea (id_seduta) ON DELETE CASCADE,
    matricola_studente VARCHAR(20) NOT NULL
        REFERENCES studenti (matricola) ON DELETE CASCADE,
    data_prenotazione  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_seduta, matricola_studente),
    -- uno studente può essere prenotato a una sola seduta
    CONSTRAINT uq_prenotazione_studente UNIQUE (matricola_studente)
);

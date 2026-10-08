-- Eseguito solo alla prima inizializzazione del container PostgreSQL.
-- Crea il database separato usato dai test automatici, che a ogni test
-- ricreano lo schema da zero senza toccare i dati di "sedutelaurea".
CREATE DATABASE sedutelaurea_test;

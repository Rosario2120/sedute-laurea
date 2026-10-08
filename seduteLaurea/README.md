# Sedute di Laurea

## Descrizione

Sedute di Laurea è un'applicazione desktop sviluppata in Java per supportare la gestione delle attività legate al percorso finale di uno studente universitario.

Il sistema permette di gestire richieste di tirocinio, assegnazioni di tesi e organizzazione delle sedute di laurea, coinvolgendo studenti, docenti e coordinatori.

---

## Requisiti del progetto

Per eseguire l'applicazione sono necessari:

### Software
- Java 17 o versione successiva
- PostgreSQL
- Maven
- IntelliJ IDEA (consigliato)

### Sistema operativo
- Windows
- Linux
- macOS


---

## Funzionalità

### Studente
- Richiedere un tirocinio.
- Richiedere una tesi a un docente.
- Consultare lo stato delle richieste effettuate.
- Prenotarsi a una seduta di laurea.

### Docente
- Visualizzare le richieste ricevute.
- Approvare o rifiutare richieste di tirocinio e tesi.
- Consultare le sedute di laurea assegnate.
- Partecipare alle commissioni.

### Coordinatore
- Gestire gli account del sistema.
- Organizzare commissioni e sedute di laurea.
- Monitorare l'andamento delle tesi e dei tirocini.
- Gestire le informazioni generali del sistema.

---

## Tecnologie utilizzate

Il progetto è stato sviluppato utilizzando:

- Java
- Java Swing
- PostgreSQL
- JDBC
- Maven

---

## Struttura del progetto

L'applicazione segue una struttura organizzata in diversi livelli:

- **GUI**: gestione dell'interfaccia grafica.
- **Controller**: gestione della logica applicativa.
- **DAO**: accesso e gestione dei dati nel database.
- **Entity**: rappresentazione degli oggetti del dominio.

Il progetto adotta inoltre il pattern DAO per separare la logica applicativa dalla gestione della persistenza dei dati.

---

## Avvio del progetto

1. Creare o avviare un database PostgreSQL.
2. Importare lo schema del database.
3. Configurare i parametri di connessione.
4. Aprire il progetto tramite IntelliJ IDEA.
5. Eseguire l'applicazione.

---

## Database

Il database PostgreSQL viene utilizzato per memorizzare le informazioni relative a:

- utenti;
- tesi;
- tirocini;
- commissioni;
- sedute di laurea.

---

## Obiettivo del progetto

Il progetto è stato realizzato nell'ambito del corso di Programmazione a Oggetti con l'obiettivo di applicare concetti di progettazione software, sviluppo di applicazioni desktop e gestione dei dati tramite database relazionale.

---

## Autore:Rosario Loffredo

Progetto sviluppato per il corso universitario di Programmazione a Oggetti.
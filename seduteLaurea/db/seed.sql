-- =====================================================================
-- Dati dimostrativi. Tutti gli account hanno password: password
--
-- Le date delle sedute sono relative a CURRENT_DATE, così i dati restano
-- validi (sedute future prenotabili, una seduta passata) in ogni momento.
-- =====================================================================

INSERT INTO utenti (id_user, username, password_hash, ruolo) VALUES
    ('U-001', 'coordinatore', 'pbkdf2_sha256$65536$olsH+Rg8Uq8f7zwF5Y8H2Q==$8jhJaUBnrAg680LBvW0s+XYXRX2K/murEAwgRvUJZvY=', 'COORDINATORE'),
    ('U-002', 'mrossi',       'pbkdf2_sha256$65536$O4BDTtuCtLFXHE8TVD7baA==$g1qfnpr2+McrUuzvp1gp2JgsbFViJ+LC6kizkrg4OBo=', 'DOCENTE'),
    ('U-003', 'lbianchi',     'pbkdf2_sha256$65536$20IdgQ+cYQ5RVV5UqYypIA==$uY8pnrHl9ilS0Ma4J3qQnV01GrFZciySyEldpoqHXMk=', 'DOCENTE'),
    ('U-004', 'gverdi',       'pbkdf2_sha256$65536$Phuckh4rcJL6aj4jXdvw7A==$iWXh00lOfzwYr+V2ZhuH7yFKFKO3DvnTdp6yxqf4b4w=', 'DOCENTE'),
    ('U-005', 'aesposito',    'pbkdf2_sha256$65536$0DRmIddryt0pqbCfeSymRg==$cdddAklOg26WFM5Z016Y6rzFTdcihEBKtAqjIKZslN0=', 'STUDENTE'),
    ('U-006', 'mromano',      'pbkdf2_sha256$65536$SEel0IqtoQlrHaiIZbAs5g==$aJFYNJwqcj3e4j5feW99FsNmqtyMPPB2pGFggaS/kU0=', 'STUDENTE'),
    ('U-007', 'fcolombo',     'pbkdf2_sha256$65536$pw8tMjPe0wXIUuBFaYOhzA==$wZpSW7z9ISj/cPFRN4Y8uHo8zpGqvoXaYzVbKsfYJsY=', 'STUDENTE'),
    ('U-008', 'sricci',       'pbkdf2_sha256$65536$A1U39b2tEn+Sy294rNJNoA==$uokcxkmNTmNc3uLzwOYE7ixbxLijo8BZGVSdc1kVWbY=', 'STUDENTE');

INSERT INTO docenti (id_docente, nome, cognome, email, id_user) VALUES
    ('DOC-001', 'Anna',     'Ferrara', 'anna.ferrara@unina.it',     'U-001'),
    ('DOC-002', 'Mario',    'Rossi',   'mario.rossi@unina.it',      'U-002'),
    ('DOC-003', 'Laura',    'Bianchi', 'laura.bianchi@unina.it',    'U-003'),
    ('DOC-004', 'Giovanni', 'Verdi',   'giovanni.verdi@unina.it',   'U-004');

INSERT INTO studenti (matricola, nome, cognome, email, id_user) VALUES
    ('N86001001', 'Antonio',   'Esposito', 'an.esposito@studenti.unina.it', 'U-005'),
    ('N86001002', 'Martina',   'Romano',   'ma.romano@studenti.unina.it',   'U-006'),
    ('N86001003', 'Francesco', 'Colombo',  'fr.colombo@studenti.unina.it',  'U-007'),
    ('N86001004', 'Sara',      'Ricci',    'sa.ricci@studenti.unina.it',    'U-008');

INSERT INTO tirocini (id_tirocinio, titolo, descrizione, id_docente) VALUES
    ('TIR-001', 'Sviluppo di un''app Android',  'Progettazione e sviluppo di un''applicazione mobile per la prenotazione delle aule.', 'DOC-002'),
    ('TIR-002', 'Analisi dei dati di ateneo',   'Pipeline di raccolta e visualizzazione dei dati sulle carriere degli studenti.',       'DOC-002'),
    ('TIR-003', 'Sicurezza delle reti',         'Analisi delle vulnerabilità della rete di dipartimento e proposta di mitigazioni.',    'DOC-003'),
    ('TIR-004', 'Machine learning applicato',   'Classificazione automatica di documenti amministrativi.',                              'DOC-004');

INSERT INTO richieste_tirocinio (id_richiesta, matricola_studente, id_tirocinio, stato) VALUES
    ('RICH-001', 'N86001001', 'TIR-001', 'APPROVATA'),
    ('RICH-002', 'N86001002', 'TIR-003', 'IN_ATTESA'),
    ('RICH-003', 'N86001003', 'TIR-001', 'IN_ATTESA'),
    ('RICH-004', 'N86001004', 'TIR-004', 'RIFIUTATA'),
    ('RICH-005', 'N86001004', 'TIR-002', 'APPROVATA');

INSERT INTO tesi (id_tesi, matricola_studente, id_docente_relatore, titolo, stato) VALUES
    ('TESI-001', 'N86001001', 'DOC-002', 'Progettazione di app mobili per servizi universitari', 'APPROVATA'),
    ('TESI-002', 'N86001002', 'DOC-003', 'Tecniche di intrusion detection nelle reti di ateneo',  'IN_ATTESA'),
    ('TESI-003', 'N86001004', 'DOC-002', 'Dashboard per l''analisi delle carriere studentesche',  'APPROVATA');

INSERT INTO commissioni (id_commissione, id_presidente) VALUES
    ('COMM-001', 'DOC-001'),
    ('COMM-002', 'DOC-004');

INSERT INTO membri_commissione (id_commissione, id_docente) VALUES
    ('COMM-001', 'DOC-001'),
    ('COMM-001', 'DOC-002'),
    ('COMM-001', 'DOC-003'),
    ('COMM-002', 'DOC-004'),
    ('COMM-002', 'DOC-002'),
    ('COMM-002', 'DOC-003');

INSERT INTO sedute_laurea (id_seduta, data_seduta, ora_seduta, luogo, id_commissione, max_studenti) VALUES
    ('SED-001', CURRENT_DATE + 30, '10:00', 'Aula Magna',            'COMM-001', 10),
    ('SED-002', CURRENT_DATE + 60, '14:30', 'Aula T1 - Monte S. Angelo', 'COMM-002', 2),
    ('SED-003', CURRENT_DATE - 20, '09:30', 'Aula Magna',            'COMM-001', 10),
    ('SED-004', CURRENT_DATE + 90, '11:00', 'Aula Leopardi',         NULL,       8);

INSERT INTO prenotazioni_seduta (id_seduta, matricola_studente) VALUES
    ('SED-001', 'N86001001');

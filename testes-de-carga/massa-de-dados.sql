-- ============================================================
-- UNILINK: Massa de dados para os testes de carga
-- Cria 2000 estudantes (com perfil) e 50 eventos futuros, para o
-- teste nao ficar rodando sempre em cima dos 5 estudantes da carga inicial.
-- Senha de todos: 123456 (mesmo hash MD5 da carga inicial)
-- Pode rodar de novo sem problema: o que ja existe e ignorado.
-- 2000 estudantes x 50 eventos = 100 mil confirmacoes de presenca possiveis.
-- ============================================================

SET NAMES utf8mb4;
USE unilink_db;
SET SESSION cte_max_recursion_depth = 5000;

-- Estudantes carga1@edu.unirio.br ate carga2000@edu.unirio.br
INSERT IGNORE INTO estudante (email_institucional, nome, data_nascimento, curso, campus_predio, senha_hash)
WITH RECURSIVE n (i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 2000)
SELECT CONCAT('carga', i, '@edu.unirio.br'),
       CONCAT('Estudante Carga ', i),
       DATE '2000-01-01' + INTERVAL (i * 3) DAY,
       ELT(1 + i % 4, 'Sistemas de Informação', 'Matemática', 'História', 'Biblioteconomia'),
       ELT(1 + i % 2, 'CCET', 'CCH'),
       'e10adc3949ba59abbe56e057f20f883e'
FROM n;

-- Um perfil (biografia) para cada estudante da massa
INSERT IGNORE INTO perfil (fk_email_estudante, tres_gostos, frase_marcante, mood_atual, funfact)
WITH RECURSIVE n (i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 2000)
SELECT CONCAT('carga', i, '@edu.unirio.br'),
       'Estudar, Ler, Correr', 'Frase de teste de carga', 'Testando o Unilink', CONCAT('Sou o estudante ', i)
FROM n;

-- 50 eventos futuros (todos aceitam confirmacao de presenca)
INSERT INTO evento (fk_email_organizador, nome_evento, data_hora_evento, local_campus, qtd_confirmados)
WITH RECURSIVE n (i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 50)
SELECT CONCAT('carga', i, '@edu.unirio.br'),
       CONCAT('Evento de carga ', i),
       TIMESTAMP '2027-01-01 18:00:00' + INTERVAL i DAY,
       ELT(1 + i % 2, 'CCET - Auditório', 'Campus Pasteur - CCH'),
       0
FROM n
WHERE NOT EXISTS (SELECT 1 FROM evento WHERE nome_evento LIKE 'Evento de carga %');

SELECT (SELECT COUNT(*) FROM estudante) AS estudantes, (SELECT COUNT(*) FROM evento) AS eventos;

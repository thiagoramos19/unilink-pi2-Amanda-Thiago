-- ============================================================
-- UNILINK: Massa de dados do feed (servico 4, incluido na medicao 2)
-- Cria 100 postagens com 3 comentarios cada (300 comentarios),
-- para o feed ter tamanho parecido com o de uma rede social pequena em uso.
-- Rode DEPOIS do massa-de-dados.sql (usa os estudantes carga1 a carga2000).
-- ============================================================

SET NAMES utf8mb4;
USE unilink_db;

INSERT INTO postagem (fk_email_autor, conteudo_texto, tipo_midia, data_hora_criacao)
WITH RECURSIVE n (i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 100)
SELECT CONCAT('carga', i, '@edu.unirio.br'),
       CONCAT('Postagem de carga ', i, ': alguém tem o material da prova de Banco de Dados?'),
       'NENHUMA',
       TIMESTAMP '2026-11-01 08:00:00' + INTERVAL i MINUTE
FROM n
WHERE NOT EXISTS (SELECT 1 FROM postagem WHERE conteudo_texto LIKE 'Postagem de carga %');

INSERT INTO comentario (fk_idpost, fk_email_autor, texto)
WITH RECURSIVE n (i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 3)
SELECT p.idpostagem, CONCAT('carga', 100 + p.idpostagem * 3 + n.i, '@edu.unirio.br'), CONCAT('Comentário de carga ', n.i)
FROM postagem p CROSS JOIN n
WHERE p.conteudo_texto LIKE 'Postagem de carga %'
  AND NOT EXISTS (SELECT 1 FROM comentario WHERE texto LIKE 'Comentário de carga %');

SELECT (SELECT COUNT(*) FROM postagem) AS postagens, (SELECT COUNT(*) FROM comentario) AS comentarios;

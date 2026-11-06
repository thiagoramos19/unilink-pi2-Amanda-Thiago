-- ============================================================
-- UNILINK: Carga Inicial de Dados (DML)
-- Populate inicial com dados de prototipos e testes da UNIRIO
-- Fonte: secao 10.2 da especificacao (PI2_C6_AmandaBrandes_ThiagoIvo.pdf)
-- Senha de todos os estudantes: 123456 (guardada como hash MD5)
-- ============================================================

SET NAMES utf8mb4;

USE unilink_db;

-- 1. Insercao de Estudantes
INSERT INTO estudante (email_institucional, nome, data_nascimento, curso, campus_predio, senha_hash) VALUES
('thiago.ivo@edu.unirio.br', 'Thiago Ramos Ivo', '2003-05-15', 'Sistemas de Informação', 'CCET', 'e10adc3949ba59abbe56e057f20f883e'),
('amanda.brandes@edu.unirio.br', 'Amanda Brandes', '2002-11-20', 'Sistemas de Informação', 'CCET', 'e10adc3949ba59abbe56e057f20f883e'),
('ciclano@edu.unirio.br', 'Ciclano Silva', '2005-02-10', 'Biblioteconomia', 'CCH', 'e10adc3949ba59abbe56e057f20f883e'),
('fulano@edu.unirio.br', 'Fulano de Tal', '2003-08-25', 'Matemática', 'CCET', 'e10adc3949ba59abbe56e057f20f883e'),
('beltrano@edu.unirio.br', 'Beltrano Santos', '2004-01-30', 'História', 'CCH', 'e10adc3949ba59abbe56e057f20f883e');

-- 2. Insercao de Perfis Individuais (1:1)
INSERT INTO perfil (fk_email_estudante, tres_gostos, frase_marcante, mood_atual, funfact) VALUES
('thiago.ivo@edu.unirio.br', 'Desenvolver interfaces, Jogar futebol, Ouvir podcasts', 'Código limpo é a alma do negócio.', 'Focado nas entregas do PI2', 'Consigo codar ouvindo música instrumental por 8h seguidas'),
('amanda.brandes@edu.unirio.br', 'Design IHC, Café expresso, Maratona de séries', 'A simplicidade é o último grau da sofisticação.', 'Animada para os testes heurísticos', 'Tenho uma coleção de cadernos de prototipação'),
('ciclano@edu.unirio.br', 'Organizar livros por cor, Cultura pop depressiva, Memes intelectuais', 'Leio tudo que vejo pela frente, inclusive entrelinhas de mensagens de bom dia.', 'Disposto a recomendar um livro pra cada pessoa que der match.', 'Ja li a biblioteca inteira do CCH'),
('fulano@edu.unirio.br', 'Cálculo e seus desafios, Sair a noite, Cuidar de planta', 'Minha vida é 80% concreto e 20% desespero.', 'Procurando alguém para me ajudar a entender Estatística.', 'Sei de cor os primeiros 50 dígitos do Pi'),
('beltrano@edu.unirio.br', 'Avaliar filmes no Letterbox, Música indie underground, Networking', 'Otimizando minha vida pessoal com a mesma eficiência de uma planilha.', 'Ansioso para o próximo churrasco da atlética.', 'Assisto filmes antigos em velocidade 1.5x');

-- 3. Insercao de Postagens no Feed
INSERT INTO postagem (fk_email_autor, conteudo_texto, tipo_midia, data_hora_criacao) VALUES
('ciclano@edu.unirio.br', 'Gente, tô fazendo uma pesquisa pra P1, podem responder se puderem?[link suspeito removido]', 'NENHUMA', NOW() - INTERVAL 2 HOUR),
('fulano@edu.unirio.br', 'Trilha sonora essencial pra estudar para a prova de Cálculo nesta semana!', 'MUSICA', NOW() - INTERVAL 1 HOUR),
('amanda.brandes@edu.unirio.br', 'Alguém no CCET tem o livro de IHC do Barbosa para emprestar?', 'LIVRO', NOW() - INTERVAL 30 MINUTE);

-- 4. Insercao de Comentarios
INSERT INTO comentario (fk_idpost, fk_email_autor, texto, data_hora) VALUES
(1, 'thiago.ivo@edu.unirio.br', 'Respondido! Boa sorte na pesquisa!', NOW() - INTERVAL 1 HOUR),
(2, 'beltrano@edu.unirio.br', 'Playlist excelente, ajudou muito na concentração!', NOW() - INTERVAL 45 MINUTE);

-- 5. Insercao de Stories (Com expiração calculada para 24h)
INSERT INTO story (fk_email_autor, hora_criacao, hora_fim) VALUES
('thiago.ivo@edu.unirio.br', NOW(), NOW() + INTERVAL 24 HOUR),
('ciclano@edu.unirio.br', NOW() - INTERVAL 2 HOUR, NOW() + INTERVAL 22 HOUR);

-- 6. Insercao de Solicitacoes de Amizade
INSERT INTO solicitacao_amizade (fk_email_solicitante, fk_email_solicitado, status, data_solicitacao, data_confirmacao) VALUES
('thiago.ivo@edu.unirio.br', 'amanda.brandes@edu.unirio.br', 'ACEITO', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 20 HOUR),
('ciclano@edu.unirio.br', 'thiago.ivo@edu.unirio.br', 'SOLICITADO', NOW() - INTERVAL 3 HOUR, NULL),
('fulano@edu.unirio.br', 'beltrano@edu.unirio.br', 'ACEITO', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY);

-- 7. Insercao de Mensagens Privadas (Chat entre amigos)
INSERT INTO mensagem (fk_email_remetente, fk_email_destinatario, texto_mensagem, data_hora_envio) VALUES
('thiago.ivo@edu.unirio.br', 'amanda.brandes@edu.unirio.br', 'Oi Amanda, ajustei as telas de protótipo do Unilink!', NOW() - INTERVAL 10 MINUTE),
('amanda.brandes@edu.unirio.br', 'thiago.ivo@edu.unirio.br', 'Perfeito Thiago! Vou atualizar o relatório técnico com as capturas.', NOW() - INTERVAL 5 MINUTE);

-- 8. Insercao de Eventos Acadêmicos
INSERT INTO evento (fk_email_organizador, nome_evento, data_hora_evento, local_campus, qtd_confirmados) VALUES
('thiago.ivo@edu.unirio.br', 'Acende o Farol - Atlética ADM', '2026-12-03 18:00:00', 'Campus Pasteur - CCH', 123),
('amanda.brandes@edu.unirio.br', 'Workshop de Prototipagem IHC', '2026-12-04 14:00:00', 'CCET - Auditório', 43);

-- 9. Insercao de Presencas em Eventos
INSERT INTO presenca_evento (fk_email_estudante, fk_id_evento, data_confirmacao) VALUES
('thiago.ivo@edu.unirio.br', 1, NOW() - INTERVAL 2 DAY),
('amanda.brandes@edu.unirio.br', 1, NOW() - INTERVAL 1 DAY),
('fulano@edu.unirio.br', 2, NOW() - INTERVAL 5 HOUR);

-- 10. Insercao de Produtos no "Joga pra Rolo"
INSERT INTO produto (fk_email_vendedor, fk_email_reserva, titulo, status, preco) VALUES
('beltrano@edu.unirio.br', NULL, 'Livro de História Contemporânea - Usado', 'DISPONÍVEL', 35.00),
('ciclano@edu.unirio.br', 'thiago.ivo@edu.unirio.br', 'Calculadora Científica Casio FX-82MS', 'RESERVADO', 50.00);

-- 11. Atualizacao Simulatória (Exemplo DML de mudanca de status de amizade)
UPDATE solicitacao_amizade
    SET status = 'ACEITO', data_confirmacao = NOW()
WHERE fk_email_solicitante = 'ciclano@edu.unirio.br'
  AND fk_email_solicitado = 'thiago.ivo@edu.unirio.br';

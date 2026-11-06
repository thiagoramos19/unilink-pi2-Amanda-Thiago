-- ============================================================
-- UNILINK: Criacao da Base de Dados e Esquema Fisico
-- SGBDR: MySQL 8.0+
-- Fonte: secao 10.1 da especificacao (PI2_C6_AmandaBrandes_ThiagoIvo.pdf)
-- ============================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS unilink_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE unilink_db;

-- ------------------------------------------------------------
-- Tabela: estudante
-- Entidade central do sistema
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS estudante (
    idestudante INT AUTO_INCREMENT PRIMARY KEY,
    email_institucional VARCHAR(100) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    data_nascimento DATE NOT NULL,
    curso VARCHAR(100) NOT NULL,
    campus_predio VARCHAR(45) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    CONSTRAINT chk_email_unirio CHECK (email_institucional LIKE '%@edu.unirio.br')
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Tabela: perfil
-- Relacionamento 1:1 exclusivo com estudante
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS perfil (
    idperfil INT AUTO_INCREMENT PRIMARY KEY,
    fk_email_estudante VARCHAR(100) NOT NULL UNIQUE,
    tres_gostos VARCHAR(255),
    frase_marcante VARCHAR(255),
    mood_atual VARCHAR(100),
    funfact VARCHAR(255),
    CONSTRAINT fk_perfil_estudante FOREIGN KEY (fk_email_estudante)
        REFERENCES estudante(email_institucional)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Tabela: postagem
-- Publicacoes no feed principal
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS postagem (
    idpostagem INT AUTO_INCREMENT PRIMARY KEY,
    fk_email_autor VARCHAR(100) NOT NULL,
    conteudo_texto TEXT,
    tipo_midia ENUM('IMAGEM', 'MUSICA', 'LIVRO', 'ARQUIVO', 'NENHUMA') NOT NULL DEFAULT 'NENHUMA',
    data_hora_criacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_postagem_autor FOREIGN KEY (fk_email_autor)
        REFERENCES estudante(email_institucional)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Tabela: comentario
-- Comentarios em postagens do feed
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comentario (
    idcomentario INT AUTO_INCREMENT PRIMARY KEY,
    fk_idpost INT NOT NULL,
    fk_email_autor VARCHAR(100) NOT NULL,
    texto VARCHAR(255) NOT NULL,
    data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comentario_post FOREIGN KEY (fk_idpost)
        REFERENCES postagem(idpostagem)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comentario_autor FOREIGN KEY (fk_email_autor)
        REFERENCES estudante(email_institucional)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Tabela: story
-- Midias temporarias no carrossel do feed
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS story (
    idstory INT AUTO_INCREMENT PRIMARY KEY,
    fk_email_autor VARCHAR(100) NOT NULL,
    hora_criacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hora_fim DATETIME NOT NULL,
    CONSTRAINT fk_story_autor FOREIGN KEY (fk_email_autor)
        REFERENCES estudante(email_institucional)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Tabela: mensagem
-- Chat privado entre dois estudantes
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mensagem (
    idmensagem INT AUTO_INCREMENT PRIMARY KEY,
    fk_email_remetente VARCHAR(100) NOT NULL,
    fk_email_destinatario VARCHAR(100) NOT NULL,
    texto_mensagem TEXT NOT NULL,
    data_hora_envio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mensagem_remetente FOREIGN KEY (fk_email_remetente)
        REFERENCES estudante(email_institucional)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_mensagem_destinatario FOREIGN KEY (fk_email_destinatario)
        REFERENCES estudante(email_institucional)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Tabela: solicitacao_amizade
-- Relacionamento N:M auto-referencial (Chave Composta)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS solicitacao_amizade (
    fk_email_solicitante VARCHAR(100) NOT NULL,
    fk_email_solicitado VARCHAR(100) NOT NULL,
    status ENUM('SOLICITADO', 'ACEITO', 'RECUSADO') NOT NULL DEFAULT 'SOLICITADO',
    data_solicitacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_confirmacao DATETIME NULL,
    PRIMARY KEY (fk_email_solicitante, fk_email_solicitado),
    CONSTRAINT fk_amizade_solicitante FOREIGN KEY (fk_email_solicitante)
        REFERENCES estudante(email_institucional)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_amizade_solicitado FOREIGN KEY (fk_email_solicitado)
        REFERENCES estudante(email_institucional)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Tabela: evento
-- Eventos universitarios
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS evento (
    idevento INT AUTO_INCREMENT PRIMARY KEY,
    fk_email_organizador VARCHAR(100) NOT NULL,
    nome_evento VARCHAR(100) NOT NULL,
    data_hora_evento DATETIME NOT NULL,
    local_campus VARCHAR(100) NOT NULL,
    qtd_confirmados INT NOT NULL DEFAULT 0,
    CONSTRAINT chk_qtd_confirmados CHECK (qtd_confirmados >= 0),
    CONSTRAINT fk_evento_organizador FOREIGN KEY (fk_email_organizador)
        REFERENCES estudante(email_institucional)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Tabela: presenca_evento
-- Tabela associativa para presencas em eventos (Chave Composta)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS presenca_evento (
    fk_email_estudante VARCHAR(100) NOT NULL,
    fk_id_evento INT NOT NULL,
    data_confirmacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (fk_email_estudante, fk_id_evento),
    CONSTRAINT fk_presenca_estudante FOREIGN KEY (fk_email_estudante)
        REFERENCES estudante(email_institucional)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_presenca_evento FOREIGN KEY (fk_id_evento)
        REFERENCES evento(idevento)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Tabela: produto
-- Mural de desapegos e classificados "Joga pra Rolo"
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS produto (
    idproduto INT AUTO_INCREMENT PRIMARY KEY,
    fk_email_vendedor VARCHAR(100) NOT NULL,
    fk_email_reserva VARCHAR(100) NULL,
    titulo VARCHAR(100) NOT NULL,
    status ENUM('DISPONÍVEL', 'RESERVADO', 'VENDIDO') NOT NULL DEFAULT 'DISPONÍVEL',
    preco DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_preco_positivo CHECK (preco >= 0),
    CONSTRAINT fk_produto_vendedor FOREIGN KEY (fk_email_vendedor)
        REFERENCES estudante(email_institucional)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_produto_reserva FOREIGN KEY (fk_email_reserva)
        REFERENCES estudante(email_institucional)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Indices de Otimizacao
-- ------------------------------------------------------------
CREATE INDEX idx_estudante_campus ON estudante(campus_predio);
CREATE INDEX idx_postagem_criacao ON postagem(data_hora_criacao DESC);
CREATE INDEX idx_story_expiracao ON story(hora_fim);
CREATE INDEX idx_mensagem_dialogo ON mensagem(fk_email_remetente, fk_email_destinatario);

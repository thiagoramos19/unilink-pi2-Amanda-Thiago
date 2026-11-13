package br.unirio.unilink.model;

import java.time.LocalDateTime;

/**
 * Uma linha da tabela comentario, com o nome do autor.
 */
public record Comentario(
        int id,
        String emailAutor,
        String nomeAutor,
        String texto,
        LocalDateTime dataHora) {
}

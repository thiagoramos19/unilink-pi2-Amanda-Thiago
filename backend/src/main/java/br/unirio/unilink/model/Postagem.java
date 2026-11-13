package br.unirio.unilink.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Uma linha da tabela postagem, com o nome, curso e campus do autor
 * (da tabela estudante) e os comentarios da postagem.
 */
public record Postagem(
        int id,
        String emailAutor,
        String nomeAutor,
        String cursoAutor,
        String campusAutor,
        String texto,
        String tipoMidia,
        LocalDateTime dataHoraCriacao,
        List<Comentario> comentarios) {
}

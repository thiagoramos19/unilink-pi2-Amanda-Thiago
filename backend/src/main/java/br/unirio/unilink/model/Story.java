package br.unirio.unilink.model;

import java.time.LocalDateTime;

/**
 * Um story que ainda nao expirou, com o nome de quem publicou.
 */
public record Story(
        int id,
        String emailAutor,
        String nomeAutor,
        LocalDateTime horaCriacao,
        LocalDateTime horaFim) {
}

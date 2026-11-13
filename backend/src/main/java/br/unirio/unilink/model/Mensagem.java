package br.unirio.unilink.model;

import java.time.LocalDateTime;

/**
 * Uma linha da tabela mensagem.
 */
public record Mensagem(
        int id,
        String remetente,
        String destinatario,
        String texto,
        LocalDateTime dataHora) {
}

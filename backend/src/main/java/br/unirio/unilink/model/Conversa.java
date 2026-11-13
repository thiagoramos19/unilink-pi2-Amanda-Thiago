package br.unirio.unilink.model;

import java.time.LocalDateTime;

/**
 * Um amigo na lista de conversas, com a ultima mensagem trocada (se houver).
 */
public record Conversa(
        String email,
        String nome,
        String ultimaMensagem,
        boolean ultimaFoiMinha,
        LocalDateTime dataHora) {
}

package br.unirio.unilink.model;

import java.time.LocalDateTime;

/**
 * Uma linha da tabela evento, com o nome do organizador e,
 * para o estudante que pediu a lista, se ele ja confirmou presenca.
 */
public record Evento(
        int id,
        String nome,
        LocalDateTime dataHora,
        String local,
        int confirmados,
        String organizador,
        boolean confirmei) {
}

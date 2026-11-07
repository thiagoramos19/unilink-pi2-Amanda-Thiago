package br.unirio.unilink.model;

/**
 * Quem esta fazendo o pedido, quando o site so precisa enviar o e-mail
 * (confirmar presenca, reservar ou comprar um produto).
 */
public record Solicitante(String email) {
}

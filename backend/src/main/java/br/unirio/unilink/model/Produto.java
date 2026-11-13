package br.unirio.unilink.model;

import java.math.BigDecimal;

/**
 * Uma linha da tabela produto ("Joga pra Rolo"), com o nome do vendedor.
 * Status: DISPONÍVEL, RESERVADO ou VENDIDO.
 */
public record Produto(
        int id,
        String titulo,
        BigDecimal preco,
        String status,
        String emailVendedor,
        String nomeVendedor,
        String emailReserva) {
}

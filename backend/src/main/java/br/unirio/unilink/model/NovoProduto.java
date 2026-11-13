package br.unirio.unilink.model;

import java.math.BigDecimal;

/**
 * O que o site envia ao anunciar um item (UC11).
 */
public record NovoProduto(String emailVendedor, String titulo, BigDecimal preco) {
}

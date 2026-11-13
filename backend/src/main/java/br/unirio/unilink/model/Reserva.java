package br.unirio.unilink.model;

/**
 * Resposta da reserva (UC05): o produto reservado e o codigo Pix copia e cola.
 */
public record Reserva(Produto produto, String codigoPix) {
}

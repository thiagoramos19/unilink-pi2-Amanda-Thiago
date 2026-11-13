package br.unirio.unilink.model;

/**
 * O que o site envia ao mandar uma mensagem no chat (UC04).
 */
public record NovaMensagem(String remetente, String destinatario, String texto) {
}

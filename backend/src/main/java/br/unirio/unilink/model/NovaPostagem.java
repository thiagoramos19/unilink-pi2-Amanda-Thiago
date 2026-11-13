package br.unirio.unilink.model;

/**
 * O que o site envia ao publicar uma postagem (UC09).
 */
public record NovaPostagem(String emailAutor, String texto, String tipoMidia) {
}

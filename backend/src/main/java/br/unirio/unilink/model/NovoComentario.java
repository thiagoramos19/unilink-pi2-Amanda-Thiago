package br.unirio.unilink.model;

/**
 * O que o site envia ao comentar uma postagem.
 */
public record NovoComentario(String emailAutor, String texto) {
}

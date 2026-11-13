package br.unirio.unilink.model;

/**
 * O que o site envia ao publicar um story (UC10).
 * O banco nao guarda o arquivo da foto, so quem publicou e o horario.
 */
public record NovoStory(String emailAutor) {
}

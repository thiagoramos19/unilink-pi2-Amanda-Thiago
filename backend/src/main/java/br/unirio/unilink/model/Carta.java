package br.unirio.unilink.model;

/**
 * Carta da tela Descobrir (UC03): frente com nome, idade, curso e campus;
 * verso com a biografia.
 */
public record Carta(
        String email,
        String nome,
        int idade,
        String curso,
        String campus,
        Biografia biografia) {
}

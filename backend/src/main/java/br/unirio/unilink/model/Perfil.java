package br.unirio.unilink.model;

/**
 * Tela "Seu perfil": dados do estudante, biografia e os contadores do painel.
 */
public record Perfil(
        Estudante estudante,
        Biografia biografia,
        int amigos,
        int eventos) {
}

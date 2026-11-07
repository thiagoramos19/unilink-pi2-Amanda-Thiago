package br.unirio.unilink.model;

import java.time.LocalDate;

/**
 * O que o site envia no cadastro (UC01): os tres passos juntos.
 */
public record NovoEstudante(
        String email,
        String senha,
        String nome,
        LocalDate dataNascimento,
        String curso,
        String campus,
        String gostos,
        String frase,
        String mood,
        String funfact) {
}

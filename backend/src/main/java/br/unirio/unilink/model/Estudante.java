package br.unirio.unilink.model;

import java.time.LocalDate;

/**
 * Dados basicos do estudante, devolvidos no login e no cadastro.
 */
public record Estudante(
        String email,
        String nome,
        LocalDate dataNascimento,
        String curso,
        String campus) {
}

package br.unirio.unilink.model;

/**
 * O que o site envia no login (UC02).
 */
public record Login(String email, String senha) {
}

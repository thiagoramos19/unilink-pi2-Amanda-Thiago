package br.unirio.unilink.model;

/**
 * O que o site envia ao clicar em "Adicionar amigo" (UC03).
 */
public record NovaAmizade(String solicitante, String solicitado) {
}

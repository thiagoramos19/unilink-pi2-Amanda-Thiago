package br.unirio.unilink.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Atalhos para recusar um pedido com uma mensagem que o site mostra ao usuario.
 */
final class Validacao {

    static final String DOMINIO = "@edu.unirio.br";

    private Validacao() {
    }

    static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
    }

    static boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }

    static ResponseStatusException naoEncontrado(String mensagem) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, mensagem);
    }

    static ResponseStatusException conflito(String mensagem) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensagem);
    }
}

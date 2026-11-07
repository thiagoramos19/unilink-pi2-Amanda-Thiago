package br.unirio.unilink.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Transforma a senha em hash antes de gravar ou comparar.
 * Usa MD5 para ser compativel com a carga inicial da especificacao (secao 10.2).
 */
final class Senhas {

    private Senhas() {
    }

    static String hash(String senha) {
        try {
            byte[] bytes = MessageDigest.getInstance("MD5").digest(senha.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

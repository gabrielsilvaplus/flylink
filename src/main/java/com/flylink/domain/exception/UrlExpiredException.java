package com.flylink.domain.exception;

/**
 * Exceção lançada quando uma URL encurtada é acessada após sua data limite
 * (expiresAt)
 * ou após atingir seu limite máximo de cliques (maxClicks).
 */
public class UrlExpiredException extends RuntimeException {

    public UrlExpiredException(String code) {
        super("A URL com o código '" + code + "' expirou e não está mais disponível.");
    }
}

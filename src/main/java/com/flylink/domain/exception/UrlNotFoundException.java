package com.flylink.domain.exception;

/**
 * Exceção lançada quando uma URL não é encontrada pelo código informado.
 * Resulta em HTTP 404 (Not Found).
 */
public class UrlNotFoundException extends RuntimeException {

    public UrlNotFoundException(String code) {
        super("URL não encontrada com o código: " + code);
    }
}

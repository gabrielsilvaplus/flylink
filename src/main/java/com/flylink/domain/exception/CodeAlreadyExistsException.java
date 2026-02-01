package com.flylink.domain.exception;

/**
 * Exceção lançada quando já existe uma URL com o código informado.
 * Resulta em HTTP 409 (Conflict).
 */
public class CodeAlreadyExistsException extends RuntimeException {

    public CodeAlreadyExistsException(String code) {
        super("Já existe uma URL com o código: " + code);
    }
}

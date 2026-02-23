package com.flylink.domain.exception;

/**
 * Lançada quando um registro é tentado com um email já cadastrado.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Já existe um usuário cadastrado com o email: " + email);
    }
}

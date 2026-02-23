package com.flylink.web.exception;

import com.flylink.domain.exception.EmailAlreadyExistsException;
import com.flylink.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handler de exceções relacionadas à autenticação e autorização.
 * Prioridade 2 — abaixo dos handlers de domínio, acima do global.
 */
@Slf4j
@RestControllerAdvice
@Order(2)
public class AuthExceptionHandler {

    /**
     * Email duplicado no registro → 409 Conflict.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        log.warn("Tentativa de registro com email duplicado: {}", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }

    /**
     * Credenciais inválidas no login → 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        log.warn("Tentativa de login com credenciais inválidas: {}", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .message("Credenciais inválidas")
                        .path(request.getRequestURI())
                        .build());
    }

    /**
     * Acesso negado (autenticado mas sem permissão) → 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Acesso negado ao endpoint: {}", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .message("Acesso negado")
                        .path(request.getRequestURI())
                        .build());
    }
}

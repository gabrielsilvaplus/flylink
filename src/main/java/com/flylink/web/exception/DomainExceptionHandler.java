package com.flylink.web.exception;

import com.flylink.domain.exception.CodeAlreadyExistsException;
import com.flylink.domain.exception.UrlNotFoundException;
import com.flylink.web.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handler para exceções do domínio da aplicação.
 * Trata erros de negócio específicos como URL não encontrada e código
 * duplicado.
 */
@RestControllerAdvice
@Order(1)
public class DomainExceptionHandler {

    /**
     * Trata exceção de URL não encontrada.
     * Retorna HTTP 404 (Not Found).
     */
    @ExceptionHandler(UrlNotFoundException.class)
    @ApiResponse(responseCode = "404", description = "URL não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleUrlNotFound(
            UrlNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Trata exceção de código já existente.
     * Retorna HTTP 409 (Conflict).
     */
    @ExceptionHandler(CodeAlreadyExistsException.class)
    @ApiResponse(responseCode = "409", description = "Código já existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleCodeAlreadyExists(
            CodeAlreadyExistsException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}

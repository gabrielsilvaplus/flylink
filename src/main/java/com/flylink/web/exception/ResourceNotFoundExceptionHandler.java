package com.flylink.web.exception;

import com.flylink.web.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Handler para recursos não encontrados (404).
 * Trata requisições para arquivos estáticos ou rotas inexistentes.
 */
@Slf4j
@RestControllerAdvice
@Order(4)
public class ResourceNotFoundExceptionHandler {

    /**
     * Trata requisições para recursos estáticos inexistentes.
     * Retorna HTTP 404 (Not Found) sem logar como erro (apenas debug).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        // Ignora favicon.ico silenciosamente
        if (request.getRequestURI().contains("favicon.ico")) {
            log.debug("Favicon não encontrado (ignorado)");
        } else {
            log.warn("Recurso não encontrado: {}", request.getRequestURI());
        }

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message("Recurso não encontrado: " + request.getRequestURI())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}

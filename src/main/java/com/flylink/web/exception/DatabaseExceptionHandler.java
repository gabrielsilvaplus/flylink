package com.flylink.web.exception;

import com.flylink.web.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handler para erros de integridade do banco de dados.
 * Converte exceções técnicas em mensagens amigáveis para o usuário.
 */
@Slf4j
@RestControllerAdvice
@Order(3)
public class DatabaseExceptionHandler {

    /**
     * Trata erros de violação de integridade do banco de dados.
     * Retorna HTTP 400 (Bad Request) com mensagem amigável.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ApiResponse(responseCode = "400", description = "Erro de integridade de dados", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.warn("Erro de integridade de dados no endpoint {}: {}",
                request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());

        String message = resolveMessage(ex);

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Resolve a mensagem amigável baseada no tipo de erro.
     */
    private String resolveMessage(DataIntegrityViolationException ex) {
        String rootMessage = ex.getMostSpecificCause().getMessage().toLowerCase();

        if (rootMessage.contains("unique") || rootMessage.contains("duplicate")) {
            return "Já existe um registro com esse valor";
        }

        if (rootMessage.contains("value too long")) {
            return "Valor excede o tamanho máximo permitido";
        }

        if (rootMessage.contains("not-null") || rootMessage.contains("cannot be null")) {
            return "Campo obrigatório não informado";
        }

        if (rootMessage.contains("foreign key") || rootMessage.contains("fk_")) {
            return "Registro referenciado não existe ou não pode ser removido";
        }

        return "Erro ao salvar os dados. Verifique as informações e tente novamente";
    }
}

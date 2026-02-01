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

/**
 * Handler global para exceções não tratadas.
 * Deve ter a menor prioridade (maior valor de Order) para servir como fallback.
 */
@Slf4j
@RestControllerAdvice
@Order(100)
public class GlobalExceptionHandler {

        /**
         * Trata exceções genéricas não capturadas pelos handlers específicos.
         * Retorna HTTP 500 (Internal Server Error).
         */
        @ExceptionHandler(Exception.class)
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex,
                        HttpServletRequest request) {

                log.error("Erro não tratado no endpoint {}: {}",
                                request.getRequestURI(),
                                ex.getMessage(),
                                ex);

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .message("Erro interno do servidor")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
}

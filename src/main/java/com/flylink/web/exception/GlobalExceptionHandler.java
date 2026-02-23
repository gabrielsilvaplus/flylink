package com.flylink.web.exception;

import com.flylink.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Handler global para exceções não tratadas pelos handlers específicos.
 *
 * Estende ResponseEntityExceptionHandler para herdar o tratamento automático
 * de exceções do framework Spring MVC (405, 415, 400 de parâmetros, etc.).
 *
 * Deve ter a menor prioridade (maior valor de Order) para servir como fallback.
 */
@Slf4j
@RestControllerAdvice
@Order(100)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        /**
         * Sobrescreve o método do Spring para padronizar TODAS as respostas
         * de exceções do framework no formato ErrorResponse.
         *
         * Cobre automaticamente: 405 (Method Not Allowed), 415 (Unsupported Media
         * Type),
         * 400 (Missing Params), 406 (Not Acceptable), entre outros.
         */
        @Override
        protected ResponseEntity<Object> handleExceptionInternal(
                        Exception ex,
                        Object body,
                        HttpHeaders headers,
                        HttpStatusCode statusCode,
                        WebRequest request) {
                String path = extractPath(request);

                log.warn("Exceção do framework no endpoint {}: {} ({})",
                                path, ex.getMessage(), statusCode.value());

                ErrorResponse error = ErrorResponse.builder()
                                .status(statusCode.value())
                                .message(resolveFrameworkMessage(ex, statusCode))
                                .path(path)
                                .build();

                return new ResponseEntity<>(error, headers, statusCode);
        }

        /**
         * Trata exceções genéricas não capturadas por nenhum handler.
         * Retorna HTTP 500 (Internal Server Error).
         */
        @ExceptionHandler(Exception.class)
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

        /**
         * Converte exceções do framework em mensagens amigáveis em PT-BR.
         */
        private String resolveFrameworkMessage(Exception ex, HttpStatusCode statusCode) {
                int status = statusCode.value();

                return switch (status) {
                        case 400 -> "Requisição inválida: verifique os parâmetros enviados";
                        case 405 -> "Método HTTP não permitido para este endpoint";
                        case 406 -> "Formato de resposta não suportado";
                        case 415 -> "Tipo de conteúdo não suportado. Use application/json";
                        default -> ex.getMessage();
                };
        }

        private String extractPath(WebRequest request) {
                if (request instanceof ServletWebRequest servletRequest) {
                        return servletRequest.getRequest().getRequestURI();
                }
                return "";
        }
}

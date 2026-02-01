package com.flylink.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO padrão para respostas de erro da API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de erro padronizada")
public class ErrorResponse {

    @Schema(description = "Código HTTP do erro", example = "404")
    private int status;

    @Schema(description = "Mensagem de erro", example = "URL não encontrada com o código: xyz")
    private String message;

    @Schema(description = "Endpoint que gerou o erro", example = "/api/v1/urls/xyz")
    private String path;

    @Schema(description = "Data e hora do erro")
    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now();

    @Schema(description = "Lista de erros de validação")
    private List<String> errors;
}

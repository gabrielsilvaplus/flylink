package com.flylink.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO de resposta com os dados de uma URL encurtada.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de uma URL encurtada")
public class UrlResponse {

    @Schema(description = "ID único no banco de dados", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Código curto da URL", example = "abc1234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "URL encurtada completa", example = "http://localhost:8080/abc1234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String shortUrl;

    @Schema(description = "URL original", example = "https://github.com/usuario/projeto", requiredMode = Schema.RequiredMode.REQUIRED)
    private String originalUrl;

    @Schema(description = "Número de cliques", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long clickCount;

    @Schema(description = "Data de criação", requiredMode = Schema.RequiredMode.REQUIRED)
    private OffsetDateTime createdAt;

    @Schema(description = "Data de expiração (pode ser null)", nullable = true)
    private OffsetDateTime expiresAt;

    @Schema(description = "Data do último clique/acesso", nullable = true)
    private OffsetDateTime lastClickAt;

    @Schema(description = "Indica se a URL está ativa", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isActive;

    @Schema(description = "Número máximo de cliques permitido (pode ser null)", example = "100", nullable = true)
    private Long maxClicks;
}

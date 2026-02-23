package com.flylink.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO com estatísticas detalhadas de uma URL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estatísticas de uma URL encurtada")
public class UrlStatsResponse {

    @Schema(description = "Código da URL", example = "abc1234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "URL original", requiredMode = Schema.RequiredMode.REQUIRED)
    private String originalUrl;

    @Schema(description = "Total de cliques", example = "150", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long clickCount;

    @Schema(description = "Data de criação", requiredMode = Schema.RequiredMode.REQUIRED)
    private OffsetDateTime createdAt;

    @Schema(description = "Último clique registrado", nullable = true)
    private OffsetDateTime lastClickAt;

    @Schema(description = "URL está ativa?", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isActive;

    @Schema(description = "Data de expiração", nullable = true)
    private OffsetDateTime expiresAt;

    @Schema(description = "Número máximo de cliques permitido (pode ser null)", example = "100", nullable = true)
    private Long maxClicks;
}

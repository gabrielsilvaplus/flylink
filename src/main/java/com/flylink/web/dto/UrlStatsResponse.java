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

    @Schema(description = "Código da URL", example = "abc1234")
    private String code;

    @Schema(description = "URL original")
    private String originalUrl;

    @Schema(description = "Total de cliques", example = "150")
    private Long clickCount;

    @Schema(description = "Data de criação")
    private OffsetDateTime createdAt;

    @Schema(description = "Último clique registrado")
    private OffsetDateTime lastClickAt;

    @Schema(description = "URL está ativa?", example = "true")
    private Boolean isActive;

    @Schema(description = "Data de expiração")
    private OffsetDateTime expiresAt;
}

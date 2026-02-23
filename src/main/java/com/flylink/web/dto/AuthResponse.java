package com.flylink.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta após registro ou login bem-sucedido.
 * Retorna o token JWT e dados básicos do usuário.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de autenticação com token JWT")
public class AuthResponse {

    @Schema(description = "Token JWT para autenticação nas requisições protegidas", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @Schema(description = "Nome do usuário", example = "Gabriel Silva", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Email do usuário", example = "gabriel@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}

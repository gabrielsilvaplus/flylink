package com.flylink.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.OffsetDateTime;

/**
 * DTO para atualização de URL encurtada.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para atualizar uma URL")
public class UpdateUrlRequest {

    @Schema(description = "Nova URL original", example = "https://github.com/novo-repo")
    @URL(message = "Formato de URL inválido")
    private String originalUrl;

    @Schema(description = "Nova data de expiração")
    @Future(message = "Data de expiração deve ser no futuro")
    private OffsetDateTime expiresAt;

    @Schema(description = "Novo código personalizado (opcional). Máximo 50 caracteres.", example = "novo-link-legal")
    @Size(max = 50, message = "O código deve ter no máximo 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9-_]*$", message = "O código deve conter apenas letras, números, hífen e underscore")
    private String customCode;
}

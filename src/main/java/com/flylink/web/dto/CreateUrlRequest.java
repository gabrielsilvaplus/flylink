package com.flylink.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.OffsetDateTime;

/**
 * DTO para requisição de criação de URL encurtada.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criar uma nova URL encurtada")
public class CreateUrlRequest {

    @Schema(description = "URL original que será encurtada", example = "https://github.com/usuario/projeto", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "A URL original é obrigatória")
    @URL(message = "Formato de URL inválido")
    private String originalUrl;

    @Schema(description = "Data limite de expiração da URL (opcional). Formato ISO-8601.", example = "2024-12-31T23:59:59Z", nullable = true)
    private OffsetDateTime expiresAt;

    @Schema(description = "Número máximo de cliques. Quando este valor for atingido, a URL expirará automaticamente.", example = "10", nullable = true)
    @Positive(message = "O limite de cliques deve ser positivo")
    private Long maxClicks;

    @Schema(description = "Código personalizado (opcional). Se não informado, será gerado automaticamente. Máximo 50 caracteres.", example = "meu-projeto", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 50, message = "O código deve ter no máximo 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9-_]*$", message = "O código deve conter apenas letras, números, hífen e underscore")
    private String customCode;
}

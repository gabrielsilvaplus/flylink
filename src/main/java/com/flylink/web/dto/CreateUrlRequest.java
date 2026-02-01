package com.flylink.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

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

    @Schema(description = "Código personalizado (opcional). Se não informado, será gerado automaticamente.", example = "meu-projeto", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String customCode;
}

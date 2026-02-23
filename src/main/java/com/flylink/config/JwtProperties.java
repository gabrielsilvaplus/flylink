package com.flylink.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propriedades do JWT externalizadas via application.yml.
 * Prefixo: app.jwt
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    // Chave secreta para assinar os tokens (mínimo 256 bits para HMAC-SHA)
    @NotBlank(message = "A chave secreta JWT é obrigatória")
    private String secret;

    // Tempo de expiração do token em milissegundos
    private long expirationMs = 86400000; // 24 horas padrão
}

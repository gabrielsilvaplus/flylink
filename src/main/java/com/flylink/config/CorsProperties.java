package com.flylink.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Propriedades de CORS externalizadas via application.yml.
 * Prefixo: app.cors
 *
 * Aceita uma string separada por vírgula (via .env) ou uma lista YAML.
 * Ex: CORS_ALLOWED_ORIGINS=http://localhost:5173,http://192.168.1.29:5173
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /**
     * Lista de origens permitidas para requisições cross-origin.
     */
    private List<String> allowedOrigins = List.of("http://localhost:5173");
}

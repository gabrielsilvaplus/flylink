package com.flylink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do OpenAPI/Swagger.
 * Define informações da API que aparecem na documentação.
 */
@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("FlyLink - URL Shortener API")
                                                .description("API REST para encurtamento de URLs. " +
                                                                "Permite criar, listar e gerenciar URLs encurtadas com redirecionamento automático.")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Gabriel Pereira")
                                                                .url("https://github.com/gabrielsilvaplus"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8080")
                                                                .description("Servidor de desenvolvimento")));
        }
}

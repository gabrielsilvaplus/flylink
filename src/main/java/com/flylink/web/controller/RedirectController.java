package com.flylink.web.controller;

import com.flylink.domain.service.UrlShortenerService;
import com.flylink.infrastructure.persistence.entity.ShortUrlEntity;
import com.flylink.web.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Controller para redirecionamento de URLs encurtadas.
 * Rota: GET /{code} → Redireciona para a URL original.
 * 
 * Regex aceita letras, números, hífens e underscores (sem extensões de
 * arquivo).
 */
@RestController
@RequiredArgsConstructor
@Hidden
public class RedirectController {

    private final UrlShortenerService urlService;

    /**
     * Redireciona o usuário para a URL original.
     * Incrementa o contador de cliques e retorna HTTP 302 (Found).
     * 
     * Regex {code:[a-zA-Z0-9_-]+} aceita códigos com letras, números, hífen e
     * underscore.
     * Evita interceptar arquivos .html, .js, .ico etc.
     * 
     * @param code Código da URL encurtada
     * @return Redirecionamento HTTP 302
     */
    @Operation(summary = "Redirecionamento Original", description = "Faz o redirecionamento (HTTP 302) para o destino com base no código.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirecionamento executado"),
            @ApiResponse(responseCode = "404", description = "Código não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "410", description = "Código expirado (limite de tempo ou cliques atingido)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{code:[a-zA-Z0-9_-]+}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        // Busca a URL pelo código
        ShortUrlEntity entity = urlService.findByCode(code);

        // Incrementa o contador de cliques
        urlService.incrementClickCount(code);

        // Redireciona para a URL original
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(entity.getOriginalUrl()));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}

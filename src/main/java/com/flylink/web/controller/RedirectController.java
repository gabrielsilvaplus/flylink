package com.flylink.web.controller;

import com.flylink.domain.service.UrlShortenerService;
import com.flylink.infrastructure.persistence.entity.ShortUrlEntity;
import io.swagger.v3.oas.annotations.Hidden;
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

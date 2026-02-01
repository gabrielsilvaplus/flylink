package com.flylink.web.controller;

import com.flylink.domain.service.UrlShortenerService;
import com.flylink.infrastructure.persistence.entity.ShortUrlEntity;
import com.flylink.web.dto.CreateUrlRequest;
import com.flylink.web.dto.ErrorResponse;
import com.flylink.web.dto.UrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operações CRUD de URLs encurtadas.
 */
@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
@Tag(name = "URLs", description = "Operações de criação, listagem e gerenciamento de URLs encurtadas")
public class UrlController {

    private final UrlShortenerService urlService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Operation(summary = "Criar URL encurtada", description = "Cria uma nova URL encurtada. Pode-se fornecer um código personalizado ou deixar o sistema gerar automaticamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "URL criada com sucesso", content = @Content(schema = @Schema(implementation = UrlResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Código já existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados para criar a URL encurtada", required = true, content = @Content(schema = @Schema(implementation = CreateUrlRequest.class), examples = {
            @ExampleObject(name = "URL simples", summary = "Criar com código automático", value = "{\"originalUrl\": \"https://github.com/usuario/projeto\"}"),
            @ExampleObject(name = "URL com código personalizado", summary = "Criar com código customizado", value = "{\"originalUrl\": \"https://linkedin.com/in/usuario\", \"customCode\": \"meu-linkedin\"}")
    }))
    @PostMapping
    public ResponseEntity<UrlResponse> createUrl(@Valid @RequestBody CreateUrlRequest request) {
        ShortUrlEntity entity = urlService.createShortUrl(
                request.getOriginalUrl(),
                request.getCustomCode());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(entity));
    }

    @Operation(summary = "Listar todas as URLs", description = "Retorna a lista de todas as URLs encurtadas cadastradas no sistema.")
    @ApiResponse(responseCode = "200", description = "Lista de URLs")
    @GetMapping
    public ResponseEntity<List<UrlResponse>> listAll() {
        List<UrlResponse> urls = urlService.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(urls);
    }

    @Operation(summary = "Buscar URL por código", description = "Retorna os detalhes de uma URL específica pelo seu código.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL encontrada"),
            @ApiResponse(responseCode = "404", description = "URL não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{code}")
    public ResponseEntity<UrlResponse> getByCode(
            @Parameter(description = "Código da URL encurtada", example = "abc1234") @PathVariable String code) {
        ShortUrlEntity entity = urlService.findByCode(code);
        return ResponseEntity.ok(toResponse(entity));
    }

    @Operation(summary = "Deletar URL", description = "Remove uma URL encurtada do sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "URL deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "URL não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Código da URL encurtada", example = "abc1234") @PathVariable String code) {
        urlService.deleteByCode(code);
        return ResponseEntity.noContent().build();
    }

    /**
     * Converte entidade para DTO de resposta.
     */
    private UrlResponse toResponse(ShortUrlEntity entity) {
        return UrlResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .shortUrl(baseUrl + "/" + entity.getCode())
                .originalUrl(entity.getOriginalUrl())
                .clickCount(entity.getClickCount())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .isActive(entity.getIsActive())
                .build();
    }
}

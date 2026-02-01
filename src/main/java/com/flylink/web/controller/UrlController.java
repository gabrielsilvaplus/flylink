package com.flylink.web.controller;

import com.flylink.domain.service.UrlShortenerService;
import com.flylink.infrastructure.persistence.entity.ShortUrlEntity;
import com.flylink.web.dto.CreateUrlRequest;
import com.flylink.web.dto.ErrorResponse;
import com.flylink.web.dto.UpdateUrlRequest;
import com.flylink.web.dto.UrlResponse;
import com.flylink.web.dto.UrlStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@RequestMapping(value = "/api/v1/urls", produces = "application/json")
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
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lista de URLs", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UrlResponse.class)))),
                        @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
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

        @Operation(summary = "Atualizar URL", description = "Atualiza a URL original, código personalizado ou data de expiração.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "URL atualizada"),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "URL não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Novo código já existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PutMapping("/{code}")
        public ResponseEntity<UrlResponse> updateUrl(
                        @Parameter(description = "Código da URL") @PathVariable String code,
                        @Valid @RequestBody UpdateUrlRequest request) {
                ShortUrlEntity entity = urlService.updateUrl(
                                code,
                                request.getOriginalUrl(),
                                request.getExpiresAt(),
                                request.getCustomCode());
                return ResponseEntity.ok(toResponse(entity));
        }

        @Operation(summary = "Estatísticas da URL", description = "Retorna estatísticas detalhadas de uma URL.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Estatísticas"),
                        @ApiResponse(responseCode = "404", description = "URL não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/{code}/stats")
        public ResponseEntity<UrlStatsResponse> getStats(
                        @Parameter(description = "Código da URL") @PathVariable String code) {
                ShortUrlEntity entity = urlService.findByCode(code);
                return ResponseEntity.ok(toStatsResponse(entity));
        }

        @Operation(summary = "Ativar/desativar URL", description = "Alterna o estado ativo/inativo de uma URL.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Estado alterado"),
                        @ApiResponse(responseCode = "404", description = "URL não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PatchMapping("/{code}/toggle")
        public ResponseEntity<UrlResponse> toggleActive(
                        @Parameter(description = "Código da URL") @PathVariable String code) {
                ShortUrlEntity entity = urlService.toggleActive(code);
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
                                .lastClickAt(entity.getLastClickAt())
                                .isActive(entity.getIsActive())
                                .build();
        }

        /**
         * Converte entidade para DTO de estatísticas.
         */
        private UrlStatsResponse toStatsResponse(ShortUrlEntity entity) {
                return UrlStatsResponse.builder()
                                .code(entity.getCode())
                                .originalUrl(entity.getOriginalUrl())
                                .clickCount(entity.getClickCount())
                                .createdAt(entity.getCreatedAt())
                                .lastClickAt(entity.getLastClickAt())
                                .isActive(entity.getIsActive())
                                .expiresAt(entity.getExpiresAt())
                                .build();
        }
}

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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operações CRUD de URLs encurtadas.
 * Todas as operações exigem autenticação JWT.
 */
@RestController
@RequestMapping(value = "/api/v1/urls", produces = "application/json")
@RequiredArgsConstructor
@Tag(name = "URLs", description = "Operações de criação, listagem e gerenciamento de URLs encurtadas")
@SecurityRequirement(name = "bearerAuth")
public class UrlController {

        private final UrlShortenerService urlService;

        @Value("${app.base-url:http://localhost:8080}")
        private String baseUrl;

        @Operation(summary = "Criar URL encurtada", description = "Cria uma nova URL encurtada vinculada ao usuário autenticado.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "URL criada com sucesso", content = @Content(schema = @Schema(implementation = UrlResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Código já existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PostMapping
        public ResponseEntity<UrlResponse> createUrl(
                        @Valid @RequestBody CreateUrlRequest request,
                        Authentication authentication) {
                Long userId = extractUserId(authentication);
                ShortUrlEntity entity = urlService.createShortUrl(
                                request.getOriginalUrl(),
                                request.getCustomCode(),
                                userId,
                                request.getMaxClicks(),
                                request.getExpiresAt());
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(toResponse(entity));
        }

        @Operation(summary = "Listar minhas URLs", description = "Retorna todas as URLs encurtadas do usuário autenticado.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lista de URLs", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UrlResponse.class)))),
                        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping
        public ResponseEntity<List<UrlResponse>> listAll(Authentication authentication) {
                Long userId = extractUserId(authentication);
                List<UrlResponse> urls = urlService.findAllByUser(userId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
                return ResponseEntity.ok(urls);
        }

        @Operation(summary = "Buscar URL por código", description = "Retorna os detalhes de uma URL específica do usuário autenticado.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "URL encontrada", content = @Content(schema = @Schema(implementation = UrlResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "URL não pertence ao usuário", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "URL não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "410", description = "URL expirada por tempo ou limite de cliques", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/{code}")
        public ResponseEntity<UrlResponse> getByCode(
                        @Parameter(description = "Código da URL encurtada", example = "abc1234") @PathVariable String code,
                        Authentication authentication) {
                Long userId = extractUserId(authentication);
                ShortUrlEntity entity = urlService.findExistingByCode(code, userId);
                return ResponseEntity.ok(toResponse(entity));
        }

        @Operation(summary = "Atualizar URL", description = "Atualiza a URL original, código personalizado ou data de expiração.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "URL atualizada", content = @Content(schema = @Schema(implementation = UrlResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "URL não pertence ao usuário", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "URL não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Novo código já existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PutMapping("/{code}")
        public ResponseEntity<UrlResponse> updateUrl(
                        @Parameter(description = "Código da URL") @PathVariable String code,
                        @Valid @RequestBody UpdateUrlRequest request,
                        Authentication authentication) {
                Long userId = extractUserId(authentication);
                ShortUrlEntity entity = urlService.updateUrl(
                                code,
                                request.getOriginalUrl(),
                                request.getExpiresAt(),
                                request.getCustomCode(),
                                request.getMaxClicks(),
                                userId);
                return ResponseEntity.ok(toResponse(entity));
        }

        @Operation(summary = "Estatísticas da URL", description = "Retorna estatísticas detalhadas de uma URL do usuário.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Estatísticas", content = @Content(schema = @Schema(implementation = UrlStatsResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "URL não pertence ao usuário", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "URL não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "410", description = "URL expirada por tempo ou limite de cliques", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/{code}/stats")
        public ResponseEntity<UrlStatsResponse> getStats(
                        @Parameter(description = "Código da URL") @PathVariable String code,
                        Authentication authentication) {
                Long userId = extractUserId(authentication);
                ShortUrlEntity entity = urlService.findExistingByCode(code, userId);
                return ResponseEntity.ok(toStatsResponse(entity));
        }

        @Operation(summary = "Ativar/desativar URL", description = "Alterna o estado ativo/inativo de uma URL.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Estado alterado", content = @Content(schema = @Schema(implementation = UrlResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "URL não pertence ao usuário", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "URL não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PatchMapping("/{code}/toggle")
        public ResponseEntity<UrlResponse> toggleActive(
                        @Parameter(description = "Código da URL") @PathVariable String code,
                        Authentication authentication) {
                Long userId = extractUserId(authentication);
                ShortUrlEntity entity = urlService.toggleActive(code, userId);
                return ResponseEntity.ok(toResponse(entity));
        }

        @Operation(summary = "Deletar URL", description = "Remove uma URL encurtada do sistema.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "URL deletada com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "URL não pertence ao usuário", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "URL não encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @DeleteMapping("/{code}")
        public ResponseEntity<Void> delete(
                        @Parameter(description = "Código da URL encurtada", example = "abc1234") @PathVariable String code,
                        Authentication authentication) {
                Long userId = extractUserId(authentication);
                urlService.deleteByCode(code, userId);
                return ResponseEntity.noContent().build();
        }

        /**
         * Extrai o userId do objeto Authentication.
         * O JwtAuthenticationFilter seta o userId como principal.
         */
        private Long extractUserId(Authentication authentication) {
                return (Long) authentication.getPrincipal();
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
                                .maxClicks(entity.getMaxClicks())
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
                                .maxClicks(entity.getMaxClicks())
                                .build();
        }
}

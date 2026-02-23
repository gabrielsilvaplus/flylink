package com.flylink.domain.service;

import com.flylink.domain.exception.CodeAlreadyExistsException;
import com.flylink.domain.exception.UrlExpiredException;
import com.flylink.infrastructure.persistence.entity.ShortUrlEntity;
import com.flylink.infrastructure.persistence.repository.ShortUrlJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private ShortUrlJpaRepository repository;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    @Test
    @DisplayName("Deve criar uma URL encurtada com código customizado")
    void shouldCreateShortUrlWithCustomCode() {
        // Arrange
        String originalUrl = "https://example.com";
        String customCode = "mysite";
        Long userId = 1L;

        when(repository.existsByCode(customCode)).thenReturn(false);
        when(repository.save(any(ShortUrlEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ShortUrlEntity result = urlShortenerService.createShortUrl(originalUrl, customCode, userId, null, null);

        // Assert
        assertEquals(customCode, result.getCode());
        assertEquals(originalUrl, result.getOriginalUrl());
        assertEquals(userId, result.getUserId());
        assertNull(result.getMaxClicks());
        assertNull(result.getExpiresAt());
    }

    @Test
    @DisplayName("Deve lançar CodeAlreadyExistsException quando o código customizado já existir")
    void shouldThrowExceptionWhenCustomCodeTaken() {
        // Arrange
        String customCode = "taken";
        when(repository.existsByCode(customCode)).thenReturn(true);

        // Act & Assert
        assertThrows(CodeAlreadyExistsException.class,
                () -> urlShortenerService.createShortUrl("https://example.com", customCode, 1L, null, null));
    }

    @Test
    @DisplayName("Deve criar uma URL encurtada com código gerado automaticamente")
    void shouldCreateShortUrlWithGeneratedCode() {
        // Arrange
        String originalUrl = "https://example.com";
        when(repository.existsByCode(anyString())).thenReturn(false);
        when(repository.save(any(ShortUrlEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ShortUrlEntity result = urlShortenerService.createShortUrl(originalUrl, null, 1L, null, null);

        // Assert
        assertNotNull(result.getCode());
        assertEquals(7, result.getCode().length());
        assertEquals(originalUrl, result.getOriginalUrl());
    }

    @Test
    @DisplayName("Deve buscar uma URL ativa pelo código")
    void shouldFindActiveUrlByCode() {
        // Arrange
        String code = "1234567";
        ShortUrlEntity entity = ShortUrlEntity.builder()
                .code(code)
                .expiresAt(null)
                .build();

        when(repository.findByCodeAndIsActiveTrue(code)).thenReturn(Optional.of(entity));

        // Act
        ShortUrlEntity result = urlShortenerService.findByCode(code);

        // Assert
        assertEquals(code, result.getCode());
    }

    @Test
    @DisplayName("Deve lançar UrlExpiredException quando a URL encontrada estiver expirada")
    void shouldThrowUrlExpiredExceptionWhenUrlExpired() {
        // Arrange
        String code = "1234567";
        ShortUrlEntity entity = ShortUrlEntity.builder()
                .code(code)
                .expiresAt(OffsetDateTime.now().minusDays(1)) // expired yesterday
                .isActive(true)
                .build();

        when(repository.findByCodeAndIsActiveTrue(code)).thenReturn(Optional.of(entity));

        // Act & Assert
        assertThrows(UrlExpiredException.class, () -> urlShortenerService.findByCode(code));
        verify(repository).save(entity); // Ensures entity was deactivated
        assertFalse(entity.getIsActive());
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException ao tentar atualizar a URL de outro usuário")
    void shouldThrowAccessDeniedExceptionWhenUpdatingUnownedUrl() {
        // Arrange
        String code = "1234567";
        ShortUrlEntity entity = ShortUrlEntity.builder()
                .code(code)
                .userId(2L) // Belong to user 2
                .build();

        when(repository.findByCode(code)).thenReturn(Optional.of(entity));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> urlShortenerService.findExistingByCode(code, 1L));
    }

    @Test
    @DisplayName("Deve lançar UrlExpiredException quando falhar ao incrementar cliques (limite atingido)")
    void shouldThrowExpiredExceptionWhenClickLimitReachedConcurrently() {
        // Arrange
        String code = "limit";
        // Returns 0 update counts, meaning URL is either inactive, limit reached, or
        // doesn't exist.
        when(repository.incrementClickCountSafely(code)).thenReturn(0);

        // Act & Assert
        assertThrows(UrlExpiredException.class, () -> urlShortenerService.incrementClickCount(code));
    }

    @Test
    @DisplayName("Deve incrementar a contagem de cliques com sucesso")
    void shouldIncrementClickCountSuccessfully() {
        String code = "1234567";
        when(repository.incrementClickCountSafely(code)).thenReturn(1);

        assertDoesNotThrow(() -> urlShortenerService.incrementClickCount(code));
    }

    @Test
    @DisplayName("Deve lançar UrlNotFoundException quando não encontrar a URL pelo código")
    void shouldThrowUrlNotFoundExceptionWhenCodeNotFound() {
        String code = "notfound";
        when(repository.findByCodeAndIsActiveTrue(code)).thenReturn(Optional.empty());

        assertThrows(com.flylink.domain.exception.UrlNotFoundException.class,
                () -> urlShortenerService.findByCode(code));
    }

    @Test
    @DisplayName("Deve retornar a entidade ao buscar uma URL existente pelo código")
    void shouldReturnEntityWhenFindingExistingByCode() {
        String code = "1234567";
        ShortUrlEntity entity = ShortUrlEntity.builder()
                .code(code)
                .userId(1L)
                .build();

        when(repository.findByCode(code)).thenReturn(Optional.of(entity));

        ShortUrlEntity result = urlShortenerService.findExistingByCode(code, 1L);
        assertEquals(entity, result);
    }

    @Test
    @DisplayName("Deve lançar UrlNotFoundException quando falhar ao buscar uma URL existente pelo código")
    void shouldThrowUrlNotFoundExceptionWhenFindingExistingByCodeFails() {
        String code = "notfound";
        when(repository.findByCode(code)).thenReturn(Optional.empty());

        assertThrows(com.flylink.domain.exception.UrlNotFoundException.class,
                () -> urlShortenerService.findExistingByCode(code, 1L));
    }

    @Test
    @DisplayName("Deve listar todas as URLs de um usuário")
    void shouldFindAllByUser() {
        Long userId = 1L;
        ShortUrlEntity e1 = ShortUrlEntity.builder().code("code1").userId(userId).build();
        ShortUrlEntity e2 = ShortUrlEntity.builder().code("code2").userId(userId).build();
        when(repository.findByUserId(userId)).thenReturn(java.util.List.of(e1, e2));

        java.util.List<ShortUrlEntity> result = urlShortenerService.findAllByUser(userId);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Deve deletar uma URL pelo código")
    void shouldDeleteUrlByCode() {
        String code = "1234567";
        Long userId = 1L;
        ShortUrlEntity entity = ShortUrlEntity.builder().code(code).userId(userId).build();
        when(repository.findByCode(code)).thenReturn(Optional.of(entity));
        doNothing().when(repository).delete(entity);

        assertDoesNotThrow(() -> urlShortenerService.deleteByCode(code, userId));
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("Deve alternar o estado ativo/inativo da URL")
    void shouldToggleActive() {
        String code = "1234567";
        Long userId = 1L;
        ShortUrlEntity entity = ShortUrlEntity.builder().code(code).userId(userId).isActive(true).build();
        when(repository.findByCode(code)).thenReturn(Optional.of(entity));
        when(repository.save(any(ShortUrlEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShortUrlEntity result = urlShortenerService.toggleActive(code, userId);
        assertFalse(result.getIsActive());
    }

    @Test
    @DisplayName("Deve atualizar a URL com sucesso")
    void shouldUpdateUrlSuccessfully() {
        String code = "1234567";
        Long userId = 1L;
        ShortUrlEntity entity = ShortUrlEntity.builder()
                .code(code)
                .userId(userId)
                .originalUrl("http://old.com")
                .isActive(false)
                .clickCount(0L)
                .build();

        when(repository.findByCode(code)).thenReturn(Optional.of(entity));
        when(repository.existsByCode("newcode")).thenReturn(false);
        when(repository.save(any(ShortUrlEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(1);
        ShortUrlEntity result = urlShortenerService.updateUrl(code, "http://new.com", expiresAt, "newcode", 10L,
                userId);

        assertEquals("newcode", result.getCode());
        assertEquals("http://new.com", result.getOriginalUrl());
        assertEquals(expiresAt, result.getExpiresAt());
        assertEquals(10L, result.getMaxClicks());
        assertTrue(result.getIsActive());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar um código customizado para um já existente")
    void shouldThrowExceptionWhenUpdatingToExistingCustomCode() {
        String code = "1234567";
        Long userId = 1L;
        ShortUrlEntity entity = ShortUrlEntity.builder().code(code).userId(userId).build();

        when(repository.findByCode(code)).thenReturn(Optional.of(entity));
        when(repository.existsByCode("existing")).thenReturn(true);

        assertThrows(CodeAlreadyExistsException.class,
                () -> urlShortenerService.updateUrl(code, null, null, "existing", null, userId));
    }
}

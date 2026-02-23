package com.flylink.domain.service;

import com.flylink.domain.exception.CodeAlreadyExistsException;
import com.flylink.domain.exception.UrlExpiredException;
import com.flylink.domain.exception.UrlNotFoundException;
import com.flylink.infrastructure.persistence.entity.ShortUrlEntity;
import com.flylink.infrastructure.persistence.repository.ShortUrlJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Serviço principal de encurtamento de URLs.
 * Contém toda a lógica de negócio da aplicação.
 */
@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private final ShortUrlJpaRepository repository;

    // Caracteres Base62 para geração de códigos
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 7;
    private final SecureRandom random = new SecureRandom();

    /**
     * Cria uma nova URL encurtada vinculada ao usuário autenticado.
     *
     * @param originalUrl URL original a ser encurtada
     * @param customCode  Código personalizado (opcional)
     * @param userId      ID do usuário autenticado
     * @return Entidade criada com o código gerado
     * @throws CodeAlreadyExistsException se o código customizado já existir
     */
    @Transactional
    public ShortUrlEntity createShortUrl(String originalUrl, String customCode, Long userId, Long maxClicks,
            OffsetDateTime expiresAt) {
        String code = (customCode != null && !customCode.isBlank())
                ? customCode
                : generateUniqueCode();

        if (repository.existsByCode(code)) {
            throw new CodeAlreadyExistsException(code);
        }

        ShortUrlEntity entity = ShortUrlEntity.builder()
                .code(code)
                .originalUrl(originalUrl)
                .userId(userId)
                .maxClicks(maxClicks)
                .expiresAt(expiresAt)
                .build();

        return repository.save(entity);
    }

    /**
     * Busca uma URL pelo código.
     * Retorna apenas URLs ativas (usado para redirecionamento).
     * 
     * @param code Código da URL encurtada
     * @return Entidade encontrada
     * @throws UrlNotFoundException se não encontrar ou estiver inativa
     */
    @Transactional(readOnly = true)
    public ShortUrlEntity findByCode(String code) {
        ShortUrlEntity entity = repository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new UrlNotFoundException(code));

        if (entity.getExpiresAt() != null && OffsetDateTime.now().isAfter(entity.getExpiresAt())) {
            entity.setIsActive(false);
            repository.save(entity);
            throw new UrlExpiredException(code);
        }

        return entity;
    }

    /**
     * Busca uma URL pelo código e valida ownership.
     * Usado para operações de gerenciamento (editar, deletar, visualizar detalhes).
     *
     * @param code   Código da URL encurtada
     * @param userId ID do usuário autenticado
     * @return Entidade encontrada
     * @throws UrlNotFoundException  se não encontrar
     * @throws AccessDeniedException se a URL não pertencer ao usuário
     */
    @Transactional(readOnly = true)
    public ShortUrlEntity findExistingByCode(String code, Long userId) {
        ShortUrlEntity entity = repository.findByCode(code)
                .orElseThrow(() -> new UrlNotFoundException(code));

        validateOwnership(entity, userId);
        return entity;
    }

    /**
     * Lista todas as URLs do usuário autenticado.
     *
     * @param userId ID do usuário autenticado
     * @return Lista de URLs do usuário
     */
    @Transactional(readOnly = true)
    public List<ShortUrlEntity> findAllByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    /**
     * Incrementa o contador de cliques e atualiza lastClickAt.
     * 
     * @param code Código da URL
     */
    @Transactional
    public void incrementClickCount(String code) {
        int updatedRows = repository.incrementClickCountSafely(code);

        if (updatedRows == 0) {
            // Se 0 linhas foram afetadas, ou não existe ativo, ou atingiu o limite de
            // cliques
            // na transação concorrente um milissegundo antes.
            throw new UrlExpiredException(code);
        }
    }

    /**
     * Deleta uma URL pelo código, validando ownership.
     *
     * @param code   Código da URL
     * @param userId ID do usuário autenticado
     * @throws UrlNotFoundException  se não encontrar
     * @throws AccessDeniedException se a URL não pertencer ao usuário
     */
    @Transactional
    public void deleteByCode(String code, Long userId) {
        ShortUrlEntity entity = findExistingByCode(code, userId);
        repository.delete(entity);
    }

    /**
     * Atualiza uma URL existente, validando ownership.
     *
     * @param code        Código da URL
     * @param originalUrl Nova URL original (opcional)
     * @param expiresAt   Nova data de expiração (opcional)
     * @param customCode  Novo código personalizado (opcional)
     * @param userId      ID do usuário autenticado
     * @return Entidade atualizada
     */
    @Transactional
    public ShortUrlEntity updateUrl(String code, String originalUrl, OffsetDateTime expiresAt, String customCode,
            Long maxClicks, Long userId) {
        ShortUrlEntity entity = findExistingByCode(code, userId);

        if (customCode != null && !customCode.isBlank() && !customCode.equals(entity.getCode())) {
            if (repository.existsByCode(customCode)) {
                throw new CodeAlreadyExistsException(customCode);
            }
            entity.setCode(customCode);
        }

        if (originalUrl != null && !originalUrl.isBlank()) {
            entity.setOriginalUrl(originalUrl);
        }

        entity.setExpiresAt(expiresAt); // Pode setar null para remover limite
        entity.setMaxClicks(maxClicks); // Pode setar null para remover limite

        // Reactivar automaticamente se os limites foram expandidos e estava inativa
        boolean isTemporalValid = entity.getExpiresAt() == null || OffsetDateTime.now().isBefore(entity.getExpiresAt());
        boolean isClicksValid = entity.getMaxClicks() == null || entity.getClickCount() < entity.getMaxClicks();

        if (!entity.getIsActive() && isTemporalValid && isClicksValid) {
            entity.setIsActive(true);
        }

        return repository.save(entity);
    }

    /**
     * Alterna o estado ativo/inativo de uma URL, validando ownership.
     *
     * @param code   Código da URL
     * @param userId ID do usuário autenticado
     * @return Entidade com estado atualizado
     */
    @Transactional
    public ShortUrlEntity toggleActive(String code, Long userId) {
        ShortUrlEntity entity = findExistingByCode(code, userId);

        entity.setIsActive(!entity.getIsActive());
        return repository.save(entity);
    }

    /**
     * Valida que a URL pertence ao usuário autenticado.
     * URLs sem dono (criadas antes da auth) não podem ser gerenciadas.
     */
    private void validateOwnership(ShortUrlEntity entity, Long userId) {
        if (entity.getUserId() == null || !entity.getUserId().equals(userId)) {
            throw new AccessDeniedException("Você não tem permissão para acessar esta URL");
        }
    }

    /**
     * Gera um código único Base62 de 7 caracteres.
     */
    private String generateUniqueCode() {
        String code;
        do {
            code = generateBase62Code();
        } while (repository.existsByCode(code));
        return code;
    }

    /**
     * Gera uma string aleatória em Base62.
     */
    private String generateBase62Code() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(BASE62.length());
            sb.append(BASE62.charAt(index));
        }
        return sb.toString();
    }
}

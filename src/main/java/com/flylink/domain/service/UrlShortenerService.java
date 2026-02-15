package com.flylink.domain.service;

import com.flylink.domain.exception.CodeAlreadyExistsException;
import com.flylink.domain.exception.UrlNotFoundException;
import com.flylink.infrastructure.persistence.entity.ShortUrlEntity;
import com.flylink.infrastructure.persistence.repository.ShortUrlJpaRepository;
import lombok.RequiredArgsConstructor;
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
     * Cria uma nova URL encurtada.
     * 
     * @param originalUrl URL original a ser encurtada
     * @param customCode  Código personalizado (opcional)
     * @return Entidade criada com o código gerado
     * @throws CodeAlreadyExistsException se o código customizado já existir
     */
    @Transactional
    public ShortUrlEntity createShortUrl(String originalUrl, String customCode) {
        // Se foi informado código customizado, verifica se já existe
        String code = (customCode != null && !customCode.isBlank())
                ? customCode
                : generateUniqueCode();

        if (repository.existsByCode(code)) {
            throw new CodeAlreadyExistsException(code);
        }

        ShortUrlEntity entity = ShortUrlEntity.builder()
                .code(code)
                .originalUrl(originalUrl)
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
        return repository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new UrlNotFoundException(code));
    }

    /**
     * Busca uma URL pelo código, incluindo URLs inativas.
     * Usado para operações de gerenciamento (editar, deletar, visualizar detalhes).
     * 
     * @param code Código da URL encurtada
     * @return Entidade encontrada
     * @throws UrlNotFoundException se não encontrar
     */
    @Transactional(readOnly = true)
    public ShortUrlEntity findExistingByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new UrlNotFoundException(code));
    }

    /**
     * Lista todas as URLs cadastradas.
     * 
     * @return Lista de todas as URLs
     */
    @Transactional(readOnly = true)
    public List<ShortUrlEntity> findAll() {
        return repository.findAll();
    }

    /**
     * Incrementa o contador de cliques e atualiza lastClickAt.
     * 
     * @param code Código da URL
     */
    @Transactional
    public void incrementClickCount(String code) {
        ShortUrlEntity entity = findByCode(code);
        entity.setClickCount(entity.getClickCount() + 1);
        entity.setLastClickAt(OffsetDateTime.now());
        repository.save(entity);
    }

    /**
     * Deleta uma URL pelo código.
     * 
     * @param code Código da URL
     * @throws UrlNotFoundException se não encontrar
     */
    @Transactional
    public void deleteByCode(String code) {
        ShortUrlEntity entity = findExistingByCode(code);
        repository.delete(entity);
    }

    /**
     * Atualiza uma URL existente.
     * 
     * @param code        Código da URL
     * @param originalUrl Nova URL original (opcional)
     * @param expiresAt   Nova data de expiração (opcional)
     * @return Entidade atualizada
     */
    @Transactional
    public ShortUrlEntity updateUrl(String code, String originalUrl, OffsetDateTime expiresAt, String customCode) {
        ShortUrlEntity entity = findExistingByCode(code);

        if (customCode != null && !customCode.isBlank() && !customCode.equals(entity.getCode())) {
            if (repository.existsByCode(customCode)) {
                throw new CodeAlreadyExistsException(customCode);
            }
            entity.setCode(customCode);
        }

        if (originalUrl != null && !originalUrl.isBlank()) {
            entity.setOriginalUrl(originalUrl);
        }
        if (expiresAt != null) {
            entity.setExpiresAt(expiresAt);
        }

        return repository.save(entity);
    }

    /**
     * Alterna o estado ativo/inativo de uma URL.
     * 
     * @param code Código da URL
     * @return Entidade com estado atualizado
     */
    @Transactional
    public ShortUrlEntity toggleActive(String code) {
        ShortUrlEntity entity = repository.findByCode(code)
                .orElseThrow(() -> new UrlNotFoundException(code));

        entity.setIsActive(!entity.getIsActive());
        return repository.save(entity);
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

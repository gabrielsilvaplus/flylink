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
     * Retorna apenas URLs ativas.
     * 
     * @param code Código da URL encurtada
     * @return Entidade encontrada
     * @throws UrlNotFoundException se não encontrar
     */
    @Transactional(readOnly = true)
    public ShortUrlEntity findByCode(String code) {
        return repository.findByCodeAndIsActiveTrue(code)
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
     * Incrementa o contador de cliques de uma URL.
     * 
     * @param code Código da URL
     */
    @Transactional
    public void incrementClickCount(String code) {
        repository.incrementClickCount(code);
    }

    /**
     * Deleta uma URL pelo código.
     * 
     * @param code Código da URL
     * @throws UrlNotFoundException se não encontrar
     */
    @Transactional
    public void deleteByCode(String code) {
        ShortUrlEntity entity = findByCode(code);
        repository.delete(entity);
    }

    /**
     * Gera um código único Base62 de 7 caracteres.
     * Loop até encontrar um código que não existe no banco.
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
     * Base62 = [0-9A-Za-z] = 62 caracteres possíveis.
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

package com.flylink.integration;

import com.flylink.infrastructure.persistence.repository.ShortUrlJpaRepository;
import com.flylink.infrastructure.persistence.repository.UserJpaRepository;
import com.flylink.web.dto.CreateUrlRequest;
import com.flylink.web.dto.UrlResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

class UrlShortenerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private ShortUrlJpaRepository urlRepository;

    @Autowired
    private com.flylink.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private String jwtToken;
    private com.flylink.infrastructure.persistence.entity.UserEntity testUser;

    @BeforeEach
    void setUp() {
        // ARRANGE Global: Limpa tudo para garantir isolamento total
        urlRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Cria usuário base diretamente no banco (Sem depender da rota /api/v1/auth)
        testUser = com.flylink.infrastructure.persistence.entity.UserEntity.builder()
                .name("URL User")
                .email("urluser@test.com")
                .password(passwordEncoder.encode("password123"))
                .build();
        userRepository.save(testUser);

        // 2. Gera o token JWT simulando a autenticação de forma performática
        this.jwtToken = jwtTokenProvider.generateToken(testUser.getId(), testUser.getEmail());
    }

    @AfterEach
    void tearDown() {
        urlRepository.deleteAll();
        userRepository.deleteAll();
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    @DisplayName("Deve encurtar uma URL válida com sucesso (HTTP 201)")
    void shouldCreateShortUrl() {
        // 1. ARRANGE
        CreateUrlRequest createReq = new CreateUrlRequest();
        createReq.setOriginalUrl("https://spring.io");

        HttpEntity<CreateUrlRequest> requestEntity = new HttpEntity<>(createReq, getAuthHeaders());

        // 2. ACT
        ResponseEntity<UrlResponse> response = restTemplate.exchange(
                "/api/v1/urls", HttpMethod.POST, requestEntity, UrlResponse.class);

        // 3. ASSERT
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status Code deve ser 201 CREATED");
        assertNotNull(response.getBody(), "Corpo não pode ser nulo");
        assertNotNull(response.getBody().getCode(), "Código encurtado não pode ser nulo");

        // Efeito colateral: Verifica no banco
        assertTrue(urlRepository.findByCode(response.getBody().getCode()).isPresent(),
                "URL deve ter sido salva no banco de dados com relacionamento ao usuário");
    }

    @Test
    @DisplayName("Deve buscar os detalhes de uma URL encurtada (HTTP 200)")
    void shouldRetrieveShortUrlConfig() {
        // 1. ARRANGE: Inserir URL diretamente no banco (Isolated State)
        String customCode = "busca123";
        com.flylink.infrastructure.persistence.entity.ShortUrlEntity urlEntity = com.flylink.infrastructure.persistence.entity.ShortUrlEntity
                .builder()
                .originalUrl("https://spring.io")
                .code(customCode)
                .userId(testUser.getId()) // Vincula ao user do @BeforeEach
                .build();
        urlRepository.save(urlEntity);

        HttpEntity<Void> requestEntity = new HttpEntity<>(getAuthHeaders());

        // 2. ACT
        ResponseEntity<UrlResponse> response = restTemplate.exchange(
                "/api/v1/urls/" + customCode, HttpMethod.GET, requestEntity, UrlResponse.class);

        // 3. ASSERT
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status Code deve ser 200 OK");
        assertNotNull(response.getBody(), "Corpo da resposta deve conter os dados");
        assertEquals("https://spring.io", response.getBody().getOriginalUrl(), "URL Original deve bater com o banco");
        assertEquals(customCode, response.getBody().getCode(), "Código deve bater com o banco");
    }

    @Test
    @DisplayName("Deve redirecionar para a URL original (HTTP 302)")
    void shouldRedirectToOriginalUrl() {
        // 1. ARRANGE: Inserir URL diretamente no banco
        String redirectCode = "red123";
        com.flylink.infrastructure.persistence.entity.ShortUrlEntity urlEntity = com.flylink.infrastructure.persistence.entity.ShortUrlEntity
                .builder()
                .originalUrl("https://spring.io/guides")
                .code(redirectCode)
                .userId(testUser.getId())
                .build();
        urlRepository.save(urlEntity);

        // Configuração especial para não seguir o redirecionamento automaticamente
        restTemplate.getRestTemplate()
                .setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {
                    @Override
                    protected void prepareConnection(java.net.HttpURLConnection connection, String httpMethod)
                            throws java.io.IOException {
                        super.prepareConnection(connection, httpMethod);
                        connection.setInstanceFollowRedirects(false);
                    }
                });

        // 2. ACT
        ResponseEntity<String> response = restTemplate.getForEntity("/" + redirectCode, String.class);

        // 3. ASSERT
        assertEquals(HttpStatus.FOUND, response.getStatusCode(), "Status Code de Redirecionamento deve ser 302 FOUND");
        assertNotNull(response.getHeaders().getLocation(), "Header 'Location' de destino deve existir na resposta");
        assertTrue(response.getHeaders().getLocation().toString().contains("spring.io/guides"),
                "A URL de destino deve conter a origem configurada no banco");
    }

    @Test
    @DisplayName("Deve deletar uma URL encurtada (HTTP 204)")
    void shouldDeleteShortUrl() {
        // 1. ARRANGE: Inserir URL diretamente no banco
        String deleteCode = "del123";
        com.flylink.infrastructure.persistence.entity.ShortUrlEntity urlEntity = com.flylink.infrastructure.persistence.entity.ShortUrlEntity
                .builder()
                .originalUrl("https://spring.io/delete")
                .code(deleteCode)
                .userId(testUser.getId())
                .build();
        urlRepository.save(urlEntity);

        HttpEntity<Void> requestEntity = new HttpEntity<>(getAuthHeaders());

        // 2. ACT: Chama a deleção via API
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/urls/" + deleteCode, HttpMethod.DELETE, requestEntity, Void.class);

        // 3. ASSERT
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Status code de Deleção deve ser 204 NO CONTENT");

        // Efeito Colateral: Banco não deve ter a entity
        assertFalse(urlRepository.findByCode(deleteCode).isPresent(), "A entidade de URL deve sumir do banco de dados");
    }
}

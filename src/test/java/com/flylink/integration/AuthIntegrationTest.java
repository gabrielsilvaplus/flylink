package com.flylink.integration;

import com.flylink.web.dto.AuthResponse;
import com.flylink.web.dto.LoginRequest;
import com.flylink.web.dto.RegisterRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class AuthIntegrationTest extends AbstractIntegrationTest {

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private com.flylink.infrastructure.persistence.repository.UserJpaRepository userRepository;

        @Autowired
        private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

        @BeforeEach
        void setUp() {
                // ARRANGE Global: Garante que o banco está limpo antes de cada teste iniciar
                userRepository.deleteAll();
        }

        @AfterEach
        void tearDown() {
                // Limpeza de segurança após o teste
                userRepository.deleteAll();
        }

        @Test
        @DisplayName("Deve registrar um novo usuário com sucesso (HTTP 201)")
        void shouldRegisterUserSuccessfully() {
                // 1. ARRANGE
                RegisterRequest registerReq = new RegisterRequest();
                registerReq.setName("E2E User");
                registerReq.setEmail("e2e@test.com");
                registerReq.setPassword("password123");

                // 2. ACT
                ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                                "/api/v1/auth/register", registerReq, AuthResponse.class);

                // 3. ASSERT
                assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status Code deve ser 201 CREATED");
                assertNotNull(response.getBody(), "Corpo da resposta não pode ser nulo");
                assertNotNull(response.getBody().getToken(), "Token JWT deve ser gerado e retornado");

                // ASSERT (Efeito Colateral): Verifica se realmente salvou no banco
                assertTrue(userRepository.findByEmail("e2e@test.com").isPresent(),
                                "Usuário deve existir no banco de dados");
        }

        @Test
        @DisplayName("Não deve permitir registro com e-mail duplicado (HTTP 409)")
        void shouldReturnConflictWhenRegisteringDuplicateEmail() {
                // 1. ARRANGE
                // Preparando o estado do banco: Usuário já existe
                com.flylink.infrastructure.persistence.entity.UserEntity existingUser = com.flylink.infrastructure.persistence.entity.UserEntity
                                .builder()
                                .name("Existing User")
                                .email("duplicate@test.com")
                                .password(passwordEncoder.encode("oldpassword"))
                                .build();
                userRepository.save(existingUser);

                RegisterRequest registerReq = new RegisterRequest();
                registerReq.setName("New User");
                registerReq.setEmail("duplicate@test.com"); // E-mail conflitante
                registerReq.setPassword("newpassword");

                // 2. ACT
                ResponseEntity<String> response = restTemplate.postForEntity(
                                "/api/v1/auth/register", registerReq, String.class);

                // 3. ASSERT
                assertEquals(HttpStatus.CONFLICT, response.getStatusCode(), "Status Code deve ser 409 CONFLICT");
        }

        @Test
        @DisplayName("Deve realizar login com credenciais válidas (HTTP 200)")
        void shouldLoginSuccessfully() {
                // 1. ARRANGE
                // Inserindo usuário válido diretamente no banco de dados
                com.flylink.infrastructure.persistence.entity.UserEntity validUser = com.flylink.infrastructure.persistence.entity.UserEntity
                                .builder()
                                .name("Valid User")
                                .email("login@test.com")
                                .password(passwordEncoder.encode("correctpassword"))
                                .build();
                userRepository.save(validUser);

                LoginRequest loginReq = new LoginRequest();
                loginReq.setEmail("login@test.com");
                loginReq.setPassword("correctpassword");

                // 2. ACT
                ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                                "/api/v1/auth/login", loginReq, AuthResponse.class);

                // 3. ASSERT
                assertEquals(HttpStatus.OK, response.getStatusCode(), "Status Code deve ser 200 OK");
                assertNotNull(response.getBody(), "Corpo da resposta de login não pode ser nulo");
                assertNotNull(response.getBody().getToken(), "Token JWT deve ser retornado no login");
        }

        @Test
        @DisplayName("Não deve permitir login com senha incorreta (HTTP 401)")
        void shouldReturnUnauthorizedWhenLoginWithWrongPassword() {
                // 1. ARRANGE
                com.flylink.infrastructure.persistence.entity.UserEntity validUser = com.flylink.infrastructure.persistence.entity.UserEntity
                                .builder()
                                .name("Valid User")
                                .email("wrongpass@test.com")
                                .password(passwordEncoder.encode("correctpassword"))
                                .build();
                userRepository.save(validUser);

                LoginRequest wrongLogin = new LoginRequest();
                wrongLogin.setEmail("wrongpass@test.com");
                wrongLogin.setPassword("wrongpassword");

                // 2. ACT
                ResponseEntity<String> response = restTemplate.postForEntity(
                                "/api/v1/auth/login", wrongLogin, String.class);

                // 3. ASSERT
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                                "Status Code deve ser 401 UNAUTHORIZED");
        }
}

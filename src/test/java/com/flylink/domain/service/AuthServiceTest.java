package com.flylink.domain.service;

import com.flylink.domain.exception.EmailAlreadyExistsException;
import com.flylink.infrastructure.persistence.entity.UserEntity;
import com.flylink.infrastructure.persistence.repository.UserJpaRepository;
import com.flylink.infrastructure.security.JwtTokenProvider;
import com.flylink.web.dto.AuthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Deve registrar um novo usuário com sucesso")
    void shouldRegisterUserSuccessfully() {
        // 1. ARRANGE (Preparação)
        String name = "Test User";
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword";
        String token = "jwt.token.here";

        UserEntity savedUser = UserEntity.builder()
                .id(1L)
                .name(name)
                .email(email)
                .password(encodedPassword)
                .build();

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateToken(1L, email)).thenReturn(token);

        // 2. ACT (Ação)
        AuthResponse response = authService.register(name, email, password);

        // 3. ASSERT (Verificação)
        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(name, response.getName());
        assertEquals(email, response.getEmail());

        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Deve lançar EmailAlreadyExistsException ao registrar com e-mail duplicado")
    void shouldThrowExceptionWhenRegisteringDuplicateEmail() {
        // 1. ARRANGE
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // 2 & 3. ACT & ASSERT
        assertThrows(EmailAlreadyExistsException.class,
                () -> authService.register("Name", email, "password"));

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Deve realizar login com sucesso usando credenciais corretas")
    void shouldLoginSuccessfully() {
        // 1. ARRANGE
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword";
        String token = "jwt.token.here";

        UserEntity user = UserEntity.builder()
                .id(1L)
                .name("Test User")
                .email(email)
                .password(encodedPassword)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L, email)).thenReturn(token);

        // 2. ACT
        AuthResponse response = authService.login(email, password);

        // 3. ASSERT
        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(user.getName(), response.getName());
        assertEquals(email, response.getEmail());
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando o e-mail não for encontrado")
    void shouldThrowExceptionWhenLoginEmailNotFound() {
        // 1. ARRANGE
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // 2 & 3. ACT & ASSERT
        assertThrows(BadCredentialsException.class, () -> authService.login(email, "password"));
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando a senha estiver incorreta")
    void shouldThrowExceptionWhenLoginPasswordIncorrect() {
        // 1. ARRANGE
        String email = "test@example.com";
        String password = "wrongPassword";
        String encodedPassword = "encodedPassword";

        UserEntity user = UserEntity.builder()
                .id(1L)
                .email(email)
                .password(encodedPassword)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        // 2 & 3. ACT & ASSERT
        assertThrows(BadCredentialsException.class, () -> authService.login(email, password));
    }
}

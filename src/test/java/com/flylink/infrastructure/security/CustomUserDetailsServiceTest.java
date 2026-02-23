package com.flylink.infrastructure.security;

import com.flylink.infrastructure.persistence.entity.UserEntity;
import com.flylink.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserJpaRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Deve buscar o usuário pelo nome de usuário (e-mail) com sucesso")
    void shouldLoadUserByUsername() {
        // 1. ARRANGE
        String email = "test@example.com";
        String password = "encodedPassword";

        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .password(password)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // 2. ACT
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // 3. ASSERT
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando o usuário não for encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        // 1. ARRANGE
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // 2 & 3. ACT & ASSERT
        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(email));
    }
}

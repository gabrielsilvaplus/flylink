package com.flylink.infrastructure.security;

import com.flylink.infrastructure.persistence.entity.UserEntity;
import com.flylink.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Carrega os dados do usuário a partir do banco para o Spring Security.
 * O "username" do Spring Security é o email do usuário.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserJpaRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado com o email: " + email));

        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList() // Sem roles por enquanto
        );
    }
}

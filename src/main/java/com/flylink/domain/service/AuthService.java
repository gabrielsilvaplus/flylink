package com.flylink.domain.service;

import com.flylink.domain.exception.EmailAlreadyExistsException;
import com.flylink.infrastructure.persistence.entity.UserEntity;
import com.flylink.infrastructure.persistence.repository.UserJpaRepository;
import com.flylink.infrastructure.security.JwtTokenProvider;
import com.flylink.web.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de autenticação — registro e login.
 * Toda lógica de negócio de auth concentrada aqui.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Registra um novo usuário.
     *
     * @param name     Nome do usuário
     * @param email    Email (deve ser único)
     * @param password Senha em texto plano (será hashada)
     * @return Resposta com token JWT e dados do usuário
     * @throws EmailAlreadyExistsException se o email já estiver cadastrado
     */
    @Transactional
    public AuthResponse register(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        UserEntity user = UserEntity.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();

        UserEntity savedUser = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .build();
    }

    /**
     * Autentica um usuário com email e senha.
     *
     * @param email    Email do usuário
     * @param password Senha em texto plano
     * @return Resposta com token JWT e dados do usuário
     * @throws BadCredentialsException se as credenciais forem inválidas
     */
    @Transactional(readOnly = true)
    public AuthResponse login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}

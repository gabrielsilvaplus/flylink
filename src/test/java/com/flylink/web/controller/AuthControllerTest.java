package com.flylink.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flylink.domain.service.AuthService;
import com.flylink.web.dto.AuthResponse;
import com.flylink.web.dto.LoginRequest;
import com.flylink.web.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.flylink.infrastructure.security.JwtAuthenticationFilter;
import com.flylink.infrastructure.security.CustomAuthenticationEntryPoint;
import com.flylink.infrastructure.security.CustomAccessDeniedHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Ignore Spring Security filters for unit testing controller logic
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AuthService authService;

        @MockitoBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockitoBean
        private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

        @MockitoBean
        private CustomAccessDeniedHandler customAccessDeniedHandler;

        @Test
        @DisplayName("Deve retornar 201 Created e AuthResponse no registro com sucesso")
        void shouldRegisterSuccessfully() throws Exception {
                RegisterRequest request = new RegisterRequest();
                request.setName("John Doe");
                request.setEmail("john@example.com");
                request.setPassword("password123");

                AuthResponse response = AuthResponse.builder()
                                .token("fake-jwt-token")
                                .name("John Doe")
                                .email("john@example.com")
                                .build();

                when(authService.register(anyString(), anyString(), anyString())).thenReturn(response);

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                                .andExpect(jsonPath("$.name").value("John Doe"))
                                .andExpect(jsonPath("$.email").value("john@example.com"));
        }

        @Test
        @DisplayName("Deve retornar 200 OK e AuthResponse no login com sucesso")
        void shouldLoginSuccessfully() throws Exception {
                LoginRequest request = new LoginRequest();
                request.setEmail("john@example.com");
                request.setPassword("password123");

                AuthResponse response = AuthResponse.builder()
                                .token("fake-jwt-token")
                                .name("John Doe")
                                .email("john@example.com")
                                .build();

                when(authService.login(anyString(), anyString())).thenReturn(response);

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                                .andExpect(jsonPath("$.name").value("John Doe"))
                                .andExpect(jsonPath("$.email").value("john@example.com"));
        }
}

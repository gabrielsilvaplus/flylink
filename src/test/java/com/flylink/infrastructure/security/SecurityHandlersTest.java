package com.flylink.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityHandlersTest {

    private final CustomAccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler();
    private final CustomAuthenticationEntryPoint authenticationEntryPoint = new CustomAuthenticationEntryPoint();

    @Test
    @DisplayName("Deve retornar 403 (Forbidden) em caso de Acesso Negado")
    void shouldReturn403OnAccessDenied() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException ex = new AccessDeniedException("Denied");

        accessDeniedHandler.handle(request, response, ex);

        assertEquals(403, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
    }

    @Test
    @DisplayName("Deve retornar 401 (Unauthorized) quando não autenticado")
    void shouldReturn401OnUnauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        InsufficientAuthenticationException ex = new InsufficientAuthenticationException("Unauthorized");

        authenticationEntryPoint.commence(request, response, ex);

        assertEquals(401, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
    }
}

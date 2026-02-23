package com.flylink.web.controller;

import com.flylink.domain.service.UrlShortenerService;
import com.flylink.infrastructure.persistence.entity.ShortUrlEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.flylink.infrastructure.security.JwtAuthenticationFilter;
import com.flylink.infrastructure.security.CustomAuthenticationEntryPoint;
import com.flylink.infrastructure.security.CustomAccessDeniedHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RedirectController.class)
@AutoConfigureMockMvc(addFilters = false)
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlShortenerService urlService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Test
    @DisplayName("Deve redirecionar para a URL original com sucesso")
    void shouldRedirectSuccessfully() throws Exception {
        String code = "1234567";
        String originalUrl = "https://example.com";

        ShortUrlEntity entity = ShortUrlEntity.builder()
                .code(code)
                .originalUrl(originalUrl)
                .build();

        when(urlService.findByCode(code)).thenReturn(entity);
        doNothing().when(urlService).incrementClickCount(code);

        mockMvc.perform(get("/" + code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));
    }
}

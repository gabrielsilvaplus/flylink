package com.flylink.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flylink.domain.service.UrlShortenerService;
import com.flylink.infrastructure.persistence.entity.ShortUrlEntity;
import com.flylink.web.dto.CreateUrlRequest;
import com.flylink.web.dto.UpdateUrlRequest;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UrlController.class)
@AutoConfigureMockMvc(addFilters = false)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UrlShortenerService urlService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
    }

    @Test
    @DisplayName("Deve criar a URL com sucesso")
    void shouldCreateUrl() throws Exception {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://example.com");

        ShortUrlEntity entity = ShortUrlEntity.builder()
                .code("code123")
                .originalUrl("https://example.com")
                .isActive(true)
                .build();

        when(urlService.createShortUrl(anyString(), any(), anyLong(), any(), any())).thenReturn(entity);

        mockMvc.perform(post("/api/v1/urls")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("code123"));
    }

    @Test
    @DisplayName("Deve listar todas as URLs do usuário")
    void shouldListAllUrls() throws Exception {
        ShortUrlEntity entity = ShortUrlEntity.builder().code("code123").originalUrl("https://example.com").build();
        when(urlService.findAllByUser(anyLong())).thenReturn(java.util.List.of(entity));

        mockMvc.perform(get("/api/v1/urls").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("code123"));
    }

    @Test
    @DisplayName("Deve buscar a URL pelo código")
    void shouldGetByCode() throws Exception {
        ShortUrlEntity entity = ShortUrlEntity.builder().code("code123").originalUrl("https://example.com").build();
        when(urlService.findExistingByCode(anyString(), anyLong())).thenReturn(entity);

        mockMvc.perform(get("/api/v1/urls/code123").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("code123"));
    }

    @Test
    @DisplayName("Deve atualizar a URL")
    void shouldUpdateUrl() throws Exception {
        UpdateUrlRequest request = new UpdateUrlRequest();
        request.setOriginalUrl("https://new.com");

        ShortUrlEntity entity = ShortUrlEntity.builder().code("code123").originalUrl("https://new.com").build();
        when(urlService.updateUrl(anyString(), anyString(), any(), any(), any(), anyLong())).thenReturn(entity);

        mockMvc.perform(put("/api/v1/urls/code123")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("code123"));
    }

    @Test
    @DisplayName("Deve buscar as estatísticas da URL")
    void shouldGetStats() throws Exception {
        ShortUrlEntity entity = ShortUrlEntity.builder()
                .code("code123")
                .originalUrl("https://example.com")
                .clickCount(5L)
                .build();
        when(urlService.findExistingByCode(anyString(), anyLong())).thenReturn(entity);

        mockMvc.perform(get("/api/v1/urls/code123/stats").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clickCount").value(5));
    }

    @Test
    @DisplayName("Deve alternar o status ativo/inativo da URL")
    void shouldToggleActive() throws Exception {
        ShortUrlEntity entity = ShortUrlEntity.builder().code("code123").isActive(false).build();
        when(urlService.toggleActive(anyString(), anyLong())).thenReturn(entity);

        mockMvc.perform(patch("/api/v1/urls/code123/toggle").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    @DisplayName("Deve deletar a URL com sucesso")
    void shouldDeleteUrl() throws Exception {
        doNothing().when(urlService).deleteByCode(anyString(), anyLong());

        mockMvc.perform(delete("/api/v1/urls/code123").principal(auth))
                .andExpect(status().isNoContent());
    }
}

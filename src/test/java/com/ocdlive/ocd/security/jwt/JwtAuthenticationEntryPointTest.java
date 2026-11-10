package com.ocdlive.ocd.security.jwt;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests unitaires - JwtAuthenticationEntryPoint")
class JwtAuthenticationEntryPointTest {

    private final JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint();

    @Test
    @DisplayName("commence() - Devrait écrire une réponse JSON 401 Unauthorized")
    void shouldWriteUnauthorizedJsonResponse() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/admin/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("Full authentication is required"));

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getContentAsString())
                .contains("\"status\":401")
                .contains("\"error\":\"Unauthorized\"")
                .contains("Full authentication is required")
                .contains("\"/admin/users\"");
    }
}


package com.ocdlive.ocd.security.jwt;

import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        sampleUser = User.builder()
                .id(1L)
                .email("admin@ocdlive.com")
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal() - Devrait laisser passer la requête sans authentification si aucun header Authorization")
    void shouldPassThroughWhenNoAuthorizationHeader() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    @DisplayName("doFilterInternal() - Devrait authentifier l'utilisateur quand le token Bearer est valide")
    void shouldAuthenticateUserWhenBearerTokenIsValid() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        given(jwtService.extractUsername("valid.jwt.token")).willReturn("admin@ocdlive.com");
        given(userDetailsService.loadUserByUsername("admin@ocdlive.com")).willReturn(sampleUser);
        given(jwtService.isTokenValid("valid.jwt.token", sampleUser)).willReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(sampleUser);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal() - Devrait continuer la chaîne sans authentifier si le token est malformé")
    void shouldContinueFilterChainWhenTokenThrowsException() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer invalid.jwt.token");
        given(jwtService.extractUsername("invalid.jwt.token")).willThrow(new RuntimeException("Malformed JWT"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(any());
    }
}


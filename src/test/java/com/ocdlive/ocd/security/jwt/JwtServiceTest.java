package com.ocdlive.ocd.security.jwt;

import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tests unitaires - JwtService")
class JwtServiceTest {

    private JwtService jwtService;
    private User sampleUser;

    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);

        sampleUser = User.builder()
                .id(1L)
                .email("admin@ocdlive.com")
                .firstName("Super")
                .lastName("Admin")
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("generateToken() & extractUsername() - Devrait générer un token valide et extraire le sujet")
    void shouldGenerateTokenAndExtractUsername() {
        String token = jwtService.generateToken(sampleUser);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin@ocdlive.com");
        assertThat(jwtService.isTokenValid(token, sampleUser)).isTrue();
    }

    @Test
    @DisplayName("generateToken(extraClaims) - Devrait inclure les claims supplémentaires dans le token")
    void shouldGenerateTokenWithExtraClaims() {
        String token = jwtService.generateToken(
                Map.of("userId", 1L, "role", "ROLE_ADMIN"),
                sampleUser
        );

        String roleClaim = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        assertThat(roleClaim).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("isTokenValid() - Devrait retourner false si le token appartient à un autre utilisateur")
    void shouldReturnFalseWhenTokenBelongsToDifferentUser() {
        String token = jwtService.generateToken(sampleUser);

        User otherUser = User.builder()
                .email("other@ocdlive.com")
                .role(Role.ROLE_USER)
                .build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid() - Devrait lever ExpiredJwtException si le token est expiré")
    void shouldThrowExpiredJwtExceptionWhenTokenIsExpired() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -10000L);
        String expiredToken = jwtService.generateToken(sampleUser);

        assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, sampleUser))
                .isInstanceOf(ExpiredJwtException.class);
    }
}


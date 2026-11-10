package com.ocdlive.ocd.auth.controller;

import com.ocdlive.ocd.auth.dto.AuthResponse;
import com.ocdlive.ocd.auth.dto.LoginRequest;
import com.ocdlive.ocd.auth.service.AuthService;
import com.ocdlive.ocd.user.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - AuthController")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("POST /auth/login - Devrait retourner 200 OK avec le token JWT")
    void shouldReturnOkWithAuthResponseWhenLoginSucceeds() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("admin@ocdlive.com")
                .password("admin123")
                .build();

        AuthResponse expectedResponse = AuthResponse.builder()
                .accessToken("jwt-token")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .userId(1L)
                .email("admin@ocdlive.com")
                .firstName("Super")
                .lastName("Admin")
                .role(Role.ROLE_ADMIN)
                .build();

        given(authService.login(request)).willReturn(expectedResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.login(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getBody().getEmail()).isEqualTo("admin@ocdlive.com");
        assertThat(response.getBody().getRole()).isEqualTo(Role.ROLE_ADMIN);
        verify(authService).login(request);
    }
}


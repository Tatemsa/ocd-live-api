package com.ocdlive.ocd.auth.controller;

import com.ocdlive.ocd.auth.dto.AuthResponse;
import com.ocdlive.ocd.auth.dto.LoginRequest;
import com.ocdlive.ocd.auth.dto.RegisterRequest;
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
    @DisplayName("POST /auth/register - Devrait créer un compte et retourner 201 Created")
    void shouldReturnCreatedWithAuthResponseWhenRegisterSucceeds() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Kofi")
                .lastName("Mensah")
                .email("kofi@entreprise.tg")
                .password("secret123")
                .build();

        AuthResponse expectedResponse = AuthResponse.builder()
                .accessToken("jwt-token")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .userId(2L)
                .email("kofi@entreprise.tg")
                .firstName("Kofi")
                .lastName("Mensah")
                .role(Role.ROLE_USER)
                .build();

        given(authService.register(request)).willReturn(expectedResponse);

        ResponseEntity<AuthResponse> response = authController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("kofi@entreprise.tg");
        verify(authService).register(request);
    }

    @Test
    @DisplayName("POST /auth/login - Devrait retourner 200 OK avec le token JWT")
    void shouldReturnOkWithAuthResponseWhenLoginSucceeds() {
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

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getBody().getEmail()).isEqualTo("admin@ocdlive.com");
        assertThat(response.getBody().getRole()).isEqualTo(Role.ROLE_ADMIN);
        verify(authService).login(request);
    }
}

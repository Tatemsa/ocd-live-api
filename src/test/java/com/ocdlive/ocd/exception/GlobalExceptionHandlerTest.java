package com.ocdlive.ocd.exception;

import com.ocdlive.ocd.config.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Mock
    private Translator translator;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/packs/42");
    }

    @Test
    @DisplayName("handleBaseApiException() - Devrait formater ResourceNotFoundException avec statut 404")
    void shouldHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Pack", "id", 42L);
        given(translator.toLocaleWithDefault(anyString(), anyString(), any()))
                .willReturn("Pack non trouvé(e) avec id : '42'");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseApiException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getBody().getPath()).isEqualTo("/api/packs/42");
    }

    @Test
    @DisplayName("handleBaseApiException() - Devrait formater ResourceAlreadyExistsException avec statut 409")
    void shouldHandleResourceAlreadyExistsException() {
        ResourceAlreadyExistsException ex = new ResourceAlreadyExistsException("Pack", "nom", "Pack Live");
        given(translator.toLocaleWithDefault(anyString(), anyString(), any()))
                .willReturn("Un(e) Pack avec nom 'Pack Live' existe déjà.");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseApiException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("RESOURCE_ALREADY_EXISTS");
    }

    @Test
    @DisplayName("handleBaseApiException() - Devrait formater BusinessRuleException avec statut 422")
    void shouldHandleBusinessRuleException() {
        BusinessRuleException ex = new BusinessRuleException("Règle métier violée");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseApiException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("BUSINESS_RULE_VIOLATION");
        assertThat(response.getBody().getMessage()).isEqualTo("Règle métier violée");
    }

    @Test
    @DisplayName("handleBadCredentials() - Devrait retourner 401 INVALID_CREDENTIALS")
    void shouldHandleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        given(translator.toLocaleWithDefault(anyString(), anyString()))
                .willReturn("Email ou mot de passe incorrect.");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentials(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    @DisplayName("handleAccessDenied() - Devrait retourner 403 ACCESS_DENIED")
    void shouldHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden");
        given(translator.toLocaleWithDefault(anyString(), anyString()))
                .willReturn("Vous n'avez pas les autorisations nécessaires.");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    @DisplayName("handleGeneralException() - Devrait retourner 500 INTERNAL_SERVER_ERROR")
    void shouldHandleGeneralException() {
        Exception ex = new RuntimeException("Unexpected error");
        given(translator.toLocaleWithDefault(anyString(), anyString()))
                .willReturn("Erreur interne.");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneralException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }
}


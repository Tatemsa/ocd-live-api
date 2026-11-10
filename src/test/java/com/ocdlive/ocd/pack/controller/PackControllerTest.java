package com.ocdlive.ocd.pack.controller;

import com.ocdlive.ocd.pack.dto.PackRequest;
import com.ocdlive.ocd.pack.dto.PackResponse;
import com.ocdlive.ocd.pack.service.PackService;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - PackController")
class PackControllerTest {

    @Mock
    private PackService packService;

    @InjectMocks
    private PackController packController;

    private User currentUser;
    private PackRequest sampleRequest;
    private PackResponse sampleResponse;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L)
                .email("user@ocdlive.com")
                .firstName("Jean")
                .lastName("Dupont")
                .role(Role.ROLE_USER)
                .build();

        sampleRequest = PackRequest.builder()
                .name("Pack Live Premium")
                .price(new BigDecimal("150000.00"))
                .durationMinutes(90)
                .active(true)
                .build();

        sampleResponse = PackResponse.builder()
                .id(100L)
                .name("Pack Live Premium")
                .price(new BigDecimal("150000.00"))
                .durationMinutes(90)
                .active(true)
                .createdBy("Jean Dupont")
                .build();
    }

    @Test
    @DisplayName("GET /packs - Devrait lister les packs actifs")
    void shouldGetActivePacks() {
        given(packService.getActivePacks()).willReturn(List.of(sampleResponse));

        ResponseEntity<List<PackResponse>> response = packController.getActivePacks();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("GET /packs/{id} - Devrait retourner un pack par son ID")
    void shouldGetPackById() {
        given(packService.getPackById(100L)).willReturn(sampleResponse);

        ResponseEntity<PackResponse> response = packController.getPackById(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("GET /packs/user/{userId} - Devrait lister les packs d'un utilisateur")
    void shouldGetPacksByUser() {
        given(packService.getPacksByUser(1L)).willReturn(List.of(sampleResponse));

        ResponseEntity<List<PackResponse>> response = packController.getPacksByUser(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("POST /packs - Devrait créer un pack et retourner 201 Created")
    void shouldCreatePack() {
        given(packService.createPack(sampleRequest, currentUser)).willReturn(sampleResponse);

        ResponseEntity<PackResponse> response = packController.createPack(sampleRequest, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("GET /packs/my-packs - Devrait lister les packs de l'utilisateur connecté")
    void shouldGetMyPacks() {
        given(packService.getMyPacks(currentUser)).willReturn(List.of(sampleResponse));

        ResponseEntity<List<PackResponse>> response = packController.getMyPacks(currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("PUT /packs/{id} - Devrait mettre à jour un pack")
    void shouldUpdatePack() {
        given(packService.updatePack(100L, sampleRequest, currentUser)).willReturn(sampleResponse);

        ResponseEntity<PackResponse> response = packController.updatePack(100L, sampleRequest, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("DELETE /packs/{id} - Devrait supprimer un pack et retourner 204")
    void shouldDeletePack() {
        ResponseEntity<Void> response = packController.deletePack(100L, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(packService).deletePack(100L, currentUser);
    }
}


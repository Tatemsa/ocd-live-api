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
@DisplayName("Tests unitaires - AdminPackController")
class AdminPackControllerTest {

    @Mock
    private PackService packService;

    @InjectMocks
    private AdminPackController adminPackController;

    private User adminUser;
    private PackRequest sampleRequest;
    private PackResponse sampleResponse;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .email("admin@ocdlive.com")
                .firstName("Super")
                .lastName("Admin")
                .role(Role.ROLE_ADMIN)
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
                .createdBy("Super Admin")
                .build();
    }

    @Test
    @DisplayName("POST /admin/packs - Devrait créer un pack en tant qu'admin")
    void shouldCreatePack() {
        given(packService.createPack(sampleRequest, adminUser)).willReturn(sampleResponse);

        ResponseEntity<PackResponse> response = adminPackController.createPack(sampleRequest, adminUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("GET /admin/packs - Devrait lister tous les packs")
    void shouldGetAllPacks() {
        given(packService.getAllPacks()).willReturn(List.of(sampleResponse));

        ResponseEntity<List<PackResponse>> response = adminPackController.getAllPacks();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("GET /admin/packs/{id} - Devrait retourner un pack par son ID")
    void shouldGetPackById() {
        given(packService.getPackById(100L)).willReturn(sampleResponse);

        ResponseEntity<PackResponse> response = adminPackController.getPackById(100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("PUT /admin/packs/{id} - Devrait modifier un pack existant")
    void shouldUpdatePack() {
        given(packService.updatePack(100L, sampleRequest, adminUser)).willReturn(sampleResponse);

        ResponseEntity<PackResponse> response = adminPackController.updatePack(100L, sampleRequest, adminUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("PATCH /admin/packs/{id}/status - Devrait activer ou désactiver un pack")
    void shouldToggleStatus() {
        sampleResponse.setActive(false);
        given(packService.togglePackStatus(100L, false, adminUser)).willReturn(sampleResponse);

        ResponseEntity<PackResponse> response = adminPackController.toggleStatus(100L, false, adminUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isActive()).isFalse();
    }

    @Test
    @DisplayName("DELETE /admin/packs/{id} - Devrait supprimer un pack et retourner 204")
    void shouldDeletePack() {
        ResponseEntity<Void> response = adminPackController.deletePack(100L, adminUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(packService).deletePack(100L, adminUser);
    }
}


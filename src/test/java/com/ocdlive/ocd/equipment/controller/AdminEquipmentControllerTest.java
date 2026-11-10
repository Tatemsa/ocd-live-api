package com.ocdlive.ocd.equipment.controller;

import com.ocdlive.ocd.equipment.dto.EquipmentRequest;
import com.ocdlive.ocd.equipment.dto.EquipmentResponse;
import com.ocdlive.ocd.equipment.model.EquipmentCategory;
import com.ocdlive.ocd.equipment.service.EquipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - AdminEquipmentController")
class AdminEquipmentControllerTest {

    @Mock
    private EquipmentService equipmentService;

    @InjectMocks
    private AdminEquipmentController adminEquipmentController;

    private EquipmentRequest sampleRequest;
    private EquipmentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleRequest = EquipmentRequest.builder()
                .name("Caméra Sony FX3")
                .category(EquipmentCategory.VIDEO)
                .quantity(3)
                .available(true)
                .build();

        sampleResponse = EquipmentResponse.builder()
                .id(1L)
                .name("Caméra Sony FX3")
                .category(EquipmentCategory.VIDEO)
                .quantity(3)
                .available(true)
                .build();
    }

    @Test
    @DisplayName("POST /admin/equipments - Devrait créer un équipement et retourner 201 Created")
    void shouldCreateEquipment() {
        given(equipmentService.createEquipment(sampleRequest)).willReturn(sampleResponse);

        ResponseEntity<EquipmentResponse> response = adminEquipmentController.createEquipment(sampleRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("GET /admin/equipments - Devrait lister tous les équipements")
    void shouldGetAllEquipments() {
        given(equipmentService.getAllEquipments()).willReturn(List.of(sampleResponse));

        ResponseEntity<List<EquipmentResponse>> response = adminEquipmentController.getAllEquipments();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("GET /admin/equipments/{id} - Devrait retourner un équipement par son ID")
    void shouldGetEquipmentById() {
        given(equipmentService.getEquipmentById(1L)).willReturn(sampleResponse);

        ResponseEntity<EquipmentResponse> response = adminEquipmentController.getEquipmentById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("GET /admin/equipments/category/{category} - Devrait filtrer par catégorie")
    void shouldGetByCategory() {
        given(equipmentService.getEquipmentsByCategory(EquipmentCategory.VIDEO)).willReturn(List.of(sampleResponse));

        ResponseEntity<List<EquipmentResponse>> response = adminEquipmentController.getByCategory(EquipmentCategory.VIDEO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("PUT /admin/equipments/{id} - Devrait mettre à jour un équipement")
    void shouldUpdateEquipment() {
        given(equipmentService.updateEquipment(1L, sampleRequest)).willReturn(sampleResponse);

        ResponseEntity<EquipmentResponse> response = adminEquipmentController.updateEquipment(1L, sampleRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("PATCH /admin/equipments/{id}/availability - Devrait changer la disponibilité")
    void shouldToggleAvailability() {
        sampleResponse.setAvailable(false);
        given(equipmentService.toggleAvailability(1L, false)).willReturn(sampleResponse);

        ResponseEntity<EquipmentResponse> response = adminEquipmentController.toggleAvailability(1L, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isAvailable()).isFalse();
    }

    @Test
    @DisplayName("DELETE /admin/equipments/{id} - Devrait supprimer un équipement et retourner 204")
    void shouldDeleteEquipment() {
        ResponseEntity<Void> response = adminEquipmentController.deleteEquipment(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(equipmentService).deleteEquipment(1L);
    }
}


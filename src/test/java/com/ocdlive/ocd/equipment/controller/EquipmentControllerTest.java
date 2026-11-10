package com.ocdlive.ocd.equipment.controller;

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

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - EquipmentController")
class EquipmentControllerTest {

    @Mock
    private EquipmentService equipmentService;

    @InjectMocks
    private EquipmentController equipmentController;

    private EquipmentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = EquipmentResponse.builder()
                .id(1L)
                .name("Caméra Sony FX3")
                .category(EquipmentCategory.VIDEO)
                .quantity(3)
                .available(true)
                .build();
    }

    @Test
    @DisplayName("GET /equipments - Devrait retourner la liste des équipements disponibles")
    void shouldGetAvailableEquipments() {
        given(equipmentService.getAvailableEquipments()).willReturn(List.of(sampleResponse));

        ResponseEntity<List<EquipmentResponse>> response = equipmentController.getAvailableEquipments();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("GET /equipments/{id} - Devrait retourner un équipement par son ID")
    void shouldGetEquipmentById() {
        given(equipmentService.getEquipmentById(1L)).willReturn(sampleResponse);

        ResponseEntity<EquipmentResponse> response = equipmentController.getEquipmentById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("GET /equipments/category/{category} - Devrait retourner les équipements par catégorie")
    void shouldGetByCategory() {
        given(equipmentService.getEquipmentsByCategory(EquipmentCategory.VIDEO)).willReturn(List.of(sampleResponse));

        ResponseEntity<List<EquipmentResponse>> response = equipmentController.getByCategory(EquipmentCategory.VIDEO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }
}


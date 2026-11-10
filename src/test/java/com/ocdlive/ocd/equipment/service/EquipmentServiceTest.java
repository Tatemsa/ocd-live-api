package com.ocdlive.ocd.equipment.service;

import com.ocdlive.ocd.equipment.dto.EquipmentRequest;
import com.ocdlive.ocd.equipment.dto.EquipmentResponse;
import com.ocdlive.ocd.equipment.mapper.EquipmentMapper;
import com.ocdlive.ocd.equipment.model.Equipment;
import com.ocdlive.ocd.equipment.model.EquipmentCategory;
import com.ocdlive.ocd.equipment.repository.EquipmentRepository;
import com.ocdlive.ocd.exception.ResourceAlreadyExistsException;
import com.ocdlive.ocd.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - EquipmentService")
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private EquipmentMapper equipmentMapper;

    @InjectMocks
    private EquipmentService equipmentService;

    private Equipment sampleEquipment;
    private EquipmentRequest sampleRequest;
    private EquipmentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleEquipment = Equipment.builder()
                .id(1L)
                .name("Caméra Sony FX3")
                .category(EquipmentCategory.VIDEO)
                .description("Caméra cinéma plein format")
                .quantity(3)
                .available(true)
                .build();

        sampleRequest = EquipmentRequest.builder()
                .name("Caméra Sony FX3")
                .category(EquipmentCategory.VIDEO)
                .description("Caméra cinéma plein format")
                .quantity(3)
                .available(true)
                .build();

        sampleResponse = EquipmentResponse.builder()
                .id(1L)
                .name("Caméra Sony FX3")
                .category(EquipmentCategory.VIDEO)
                .description("Caméra cinéma plein format")
                .quantity(3)
                .available(true)
                .build();
    }

    @Nested
    @DisplayName("createEquipment()")
    class CreateEquipmentTests {

        @Test
        @DisplayName("Devrait créer et retourner un équipement quand le nom est unique")
        void shouldCreateEquipmentWhenNameIsUnique() {
            given(equipmentRepository.existsByNameIgnoreCase("Caméra Sony FX3")).willReturn(false);
            given(equipmentMapper.toEntity(sampleRequest)).willReturn(sampleEquipment);
            given(equipmentRepository.save(sampleEquipment)).willReturn(sampleEquipment);
            given(equipmentMapper.toResponse(sampleEquipment)).willReturn(sampleResponse);

            EquipmentResponse result = equipmentService.createEquipment(sampleRequest);

            assertThat(result).isEqualTo(sampleResponse);
            verify(equipmentRepository).save(sampleEquipment);
        }

        @Test
        @DisplayName("Devrait lever ResourceAlreadyExistsException si le nom existe déjà")
        void shouldThrowResourceAlreadyExistsExceptionWhenNameAlreadyExists() {
            given(equipmentRepository.existsByNameIgnoreCase("Caméra Sony FX3")).willReturn(true);

            assertThatThrownBy(() -> equipmentService.createEquipment(sampleRequest))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Caméra Sony FX3");

            verify(equipmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateEquipment()")
    class UpdateEquipmentTests {

        @Test
        @DisplayName("Devrait mettre à jour un équipement avec le même nom")
        void shouldUpdateEquipmentWithSameName() {
            given(equipmentRepository.findById(1L)).willReturn(Optional.of(sampleEquipment));
            given(equipmentRepository.save(sampleEquipment)).willReturn(sampleEquipment);
            given(equipmentMapper.toResponse(sampleEquipment)).willReturn(sampleResponse);

            EquipmentResponse result = equipmentService.updateEquipment(1L, sampleRequest);

            assertThat(result).isEqualTo(sampleResponse);
            verify(equipmentMapper).updateEntityFromRequest(sampleRequest, sampleEquipment);
            verify(equipmentRepository).save(sampleEquipment);
        }

        @Test
        @DisplayName("Devrait mettre à jour un équipement avec un nouveau nom disponible")
        void shouldUpdateEquipmentWithNewAvailableName() {
            EquipmentRequest newNameRequest = EquipmentRequest.builder()
                    .name("Caméra Sony FX6")
                    .category(EquipmentCategory.VIDEO)
                    .quantity(2)
                    .available(true)
                    .build();

            given(equipmentRepository.findById(1L)).willReturn(Optional.of(sampleEquipment));
            given(equipmentRepository.existsByNameIgnoreCase("Caméra Sony FX6")).willReturn(false);
            given(equipmentRepository.save(sampleEquipment)).willReturn(sampleEquipment);
            given(equipmentMapper.toResponse(sampleEquipment)).willReturn(sampleResponse);

            EquipmentResponse result = equipmentService.updateEquipment(1L, newNameRequest);

            assertThat(result).isEqualTo(sampleResponse);
            verify(equipmentMapper).updateEntityFromRequest(newNameRequest, sampleEquipment);
        }

        @Test
        @DisplayName("Devrait lever ResourceAlreadyExistsException si le nouveau nom appartient à un autre équipement")
        void shouldThrowExceptionWhenUpdatingToExistingName() {
            EquipmentRequest conflictRequest = EquipmentRequest.builder()
                    .name("Micro Shure SM7B")
                    .category(EquipmentCategory.AUDIO)
                    .quantity(2)
                    .build();

            given(equipmentRepository.findById(1L)).willReturn(Optional.of(sampleEquipment));
            given(equipmentRepository.existsByNameIgnoreCase("Micro Shure SM7B")).willReturn(true);

            assertThatThrownBy(() -> equipmentService.updateEquipment(1L, conflictRequest))
                    .isInstanceOf(ResourceAlreadyExistsException.class);

            verify(equipmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait lever ResourceNotFoundException si l'équipement à modifier n'existe pas")
        void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentEquipment() {
            given(equipmentRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> equipmentService.updateEquipment(99L, sampleRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("toggleAvailability() & deleteEquipment()")
    class AvailabilityAndDeleteTests {

        @Test
        @DisplayName("toggleAvailability() - Devrait inverser la disponibilité de l'équipement")
        void shouldToggleAvailability() {
            given(equipmentRepository.findById(1L)).willReturn(Optional.of(sampleEquipment));
            given(equipmentRepository.save(sampleEquipment)).willReturn(sampleEquipment);
            sampleResponse.setAvailable(false);
            given(equipmentMapper.toResponse(sampleEquipment)).willReturn(sampleResponse);

            EquipmentResponse result = equipmentService.toggleAvailability(1L, false);

            assertThat(sampleEquipment.isAvailable()).isFalse();
            assertThat(result.isAvailable()).isFalse();
            verify(equipmentRepository).save(sampleEquipment);
        }

        @Test
        @DisplayName("toggleAvailability() - Devrait lever ResourceNotFoundException si l'ID est introuvable")
        void shouldThrowNotFoundWhenTogglingNonExistentEquipment() {
            given(equipmentRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> equipmentService.toggleAvailability(99L, false))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("deleteEquipment() - Devrait supprimer l'équipement s'il existe")
        void shouldDeleteEquipmentWhenExists() {
            given(equipmentRepository.existsById(1L)).willReturn(true);

            equipmentService.deleteEquipment(1L);

            verify(equipmentRepository).deleteById(1L);
        }

        @Test
        @DisplayName("deleteEquipment() - Devrait lever ResourceNotFoundException si l'ID n'existe pas")
        void shouldThrowNotFoundWhenDeletingNonExistentEquipment() {
            given(equipmentRepository.existsById(99L)).willReturn(false);

            assertThatThrownBy(() -> equipmentService.deleteEquipment(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(equipmentRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Consultation des équipements")
    class ReadEquipmentTests {

        @Test
        @DisplayName("getAllEquipments() - Devrait retourner tous les équipements")
        void shouldReturnAllEquipments() {
            given(equipmentRepository.findAll()).willReturn(List.of(sampleEquipment));
            given(equipmentMapper.toResponseList(List.of(sampleEquipment))).willReturn(List.of(sampleResponse));

            List<EquipmentResponse> result = equipmentService.getAllEquipments();

            assertThat(result).hasSize(1).containsExactly(sampleResponse);
        }

        @Test
        @DisplayName("getAvailableEquipments() - Devrait retourner uniquement les équipements disponibles")
        void shouldReturnAvailableEquipments() {
            given(equipmentRepository.findByAvailableTrue()).willReturn(List.of(sampleEquipment));
            given(equipmentMapper.toResponseList(List.of(sampleEquipment))).willReturn(List.of(sampleResponse));

            List<EquipmentResponse> result = equipmentService.getAvailableEquipments();

            assertThat(result).hasSize(1).containsExactly(sampleResponse);
        }

        @Test
        @DisplayName("getEquipmentsByCategory() - Devrait filtrer par catégorie")
        void shouldReturnEquipmentsByCategory() {
            given(equipmentRepository.findByCategory(EquipmentCategory.VIDEO)).willReturn(List.of(sampleEquipment));
            given(equipmentMapper.toResponseList(List.of(sampleEquipment))).willReturn(List.of(sampleResponse));

            List<EquipmentResponse> result = equipmentService.getEquipmentsByCategory(EquipmentCategory.VIDEO);

            assertThat(result).hasSize(1).containsExactly(sampleResponse);
        }

        @Test
        @DisplayName("getEquipmentById() - Devrait retourner l'équipement par son ID")
        void shouldReturnEquipmentById() {
            given(equipmentRepository.findById(1L)).willReturn(Optional.of(sampleEquipment));
            given(equipmentMapper.toResponse(sampleEquipment)).willReturn(sampleResponse);

            EquipmentResponse result = equipmentService.getEquipmentById(1L);

            assertThat(result).isEqualTo(sampleResponse);
        }

        @Test
        @DisplayName("getEquipmentById() - Devrait lever ResourceNotFoundException si l'ID n'existe pas")
        void shouldThrowNotFoundWhenGettingNonExistentEquipmentById() {
            given(equipmentRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> equipmentService.getEquipmentById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}


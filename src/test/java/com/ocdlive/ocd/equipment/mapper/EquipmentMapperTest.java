package com.ocdlive.ocd.equipment.mapper;

import com.ocdlive.ocd.equipment.dto.EquipmentRequest;
import com.ocdlive.ocd.equipment.dto.EquipmentResponse;
import com.ocdlive.ocd.equipment.model.Equipment;
import com.ocdlive.ocd.equipment.model.EquipmentCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests unitaires - EquipmentMapper")
class EquipmentMapperTest {

    private final EquipmentMapper equipmentMapper = Mappers.getMapper(EquipmentMapper.class);

    @Test
    @DisplayName("toEntity() - Devrait convertir un EquipmentRequest en entité Equipment")
    void shouldMapRequestToEntity() {
        EquipmentRequest request = EquipmentRequest.builder()
                .name("Micro Shure SM7B")
                .category(EquipmentCategory.AUDIO)
                .description("Microphone dynamique cardioïde")
                .quantity(4)
                .available(true)
                .build();

        Equipment entity = equipmentMapper.toEntity(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Micro Shure SM7B");
        assertThat(entity.getCategory()).isEqualTo(EquipmentCategory.AUDIO);
        assertThat(entity.getDescription()).isEqualTo("Microphone dynamique cardioïde");
        assertThat(entity.getQuantity()).isEqualTo(4);
        assertThat(entity.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("toResponse() & toResponseList() - Devrait convertir une entité Equipment en EquipmentResponse")
    void shouldMapEntityToResponse() {
        Equipment equipment = Equipment.builder()
                .id(10L)
                .name("Panneau LED Aputure")
                .category(EquipmentCategory.LIGHTING)
                .description("Éclairage studio bi-colore")
                .quantity(2)
                .available(true)
                .build();

        EquipmentResponse response = equipmentMapper.toResponse(equipment);
        List<EquipmentResponse> list = equipmentMapper.toResponseList(List.of(equipment));

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Panneau LED Aputure");
        assertThat(response.getCategory()).isEqualTo(EquipmentCategory.LIGHTING);
        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("updateEntityFromRequest() - Devrait mettre à jour les champs non nuls de l'entité")
    void shouldUpdateEntityFromRequest() {
        Equipment existing = Equipment.builder()
                .id(5L)
                .name("Ancien Nom")
                .category(EquipmentCategory.OTHER)
                .description("Ancienne description")
                .quantity(1)
                .available(true)
                .build();

        EquipmentRequest updateRequest = EquipmentRequest.builder()
                .name("Nouveau Nom")
                .category(EquipmentCategory.STREAMING)
                .description(null)
                .quantity(5)
                .available(false)
                .build();

        equipmentMapper.updateEntityFromRequest(updateRequest, existing);

        assertThat(existing.getId()).isEqualTo(5L);
        assertThat(existing.getName()).isEqualTo("Nouveau Nom");
        assertThat(existing.getCategory()).isEqualTo(EquipmentCategory.STREAMING);
        assertThat(existing.getDescription()).isEqualTo("Ancienne description"); // ignoré car null
        assertThat(existing.getQuantity()).isEqualTo(5);
        assertThat(existing.isAvailable()).isFalse();
    }
}


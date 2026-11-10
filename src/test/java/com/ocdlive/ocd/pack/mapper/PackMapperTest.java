package com.ocdlive.ocd.pack.mapper;

import com.ocdlive.ocd.equipment.mapper.EquipmentMapper;
import com.ocdlive.ocd.equipment.model.Equipment;
import com.ocdlive.ocd.equipment.model.EquipmentCategory;
import com.ocdlive.ocd.pack.dto.PackRequest;
import com.ocdlive.ocd.pack.dto.PackResponse;
import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests unitaires - PackMapper")
class PackMapperTest {

    private PackMapper packMapper;

    @BeforeEach
    void setUp() {
        packMapper = Mappers.getMapper(PackMapper.class);
        EquipmentMapper equipmentMapper = Mappers.getMapper(EquipmentMapper.class);
        ReflectionTestUtils.setField(packMapper, "equipmentMapper", equipmentMapper);
    }

    @Test
    @DisplayName("toEntity() - Devrait convertir un PackRequest en entité Pack")
    void shouldMapPackRequestToEntity() {
        PackRequest request = PackRequest.builder()
                .name("Pack Studio")
                .price(new BigDecimal("200000.00"))
                .durationMinutes(120)
                .description("Description studio")
                .active(true)
                .build();

        Pack entity = packMapper.toEntity(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Pack Studio");
        assertThat(entity.getPrice()).isEqualByComparingTo("200000.00");
        assertThat(entity.getDurationMinutes()).isEqualTo(120);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("toResponse() - Devrait convertir un Pack avec auteur et équipements en PackResponse")
    void shouldMapPackToResponseWithAuthorName() {
        User author = User.builder()
                .id(1L)
                .firstName("Super")
                .lastName("Admin")
                .build();

        Equipment eq = Equipment.builder()
                .id(10L)
                .name("Caméra Sony FX3")
                .category(EquipmentCategory.VIDEO)
                .quantity(2)
                .available(true)
                .build();

        Pack pack = Pack.builder()
                .id(100L)
                .name("Pack Live Premium")
                .price(new BigDecimal("150000.00"))
                .durationMinutes(90)
                .equipments(List.of(eq))
                .active(true)
                .createdBy(author)
                .build();

        PackResponse response = packMapper.toResponse(pack);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo("Pack Live Premium");
        assertThat(response.getCreatedBy()).isEqualTo("Super Admin");
        assertThat(response.getEquipments()).hasSize(1);
        assertThat(response.getEquipments().get(0).getName()).isEqualTo("Caméra Sony FX3");
    }

    @Test
    @DisplayName("mapAuthorName() - Devrait retourner null si l'utilisateur est null")
    void shouldReturnNullWhenAuthorIsNull() {
        assertThat(packMapper.mapAuthorName(null)).isNull();
    }
}


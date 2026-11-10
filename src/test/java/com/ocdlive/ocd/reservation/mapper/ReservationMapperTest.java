package com.ocdlive.ocd.reservation.mapper;

import com.ocdlive.ocd.equipment.mapper.EquipmentMapper;
import com.ocdlive.ocd.pack.mapper.PackMapper;
import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.reservation.dto.ReservationRequest;
import com.ocdlive.ocd.reservation.dto.ReservationResponse;
import com.ocdlive.ocd.reservation.model.Reservation;
import com.ocdlive.ocd.reservation.model.ReservationLocationType;
import com.ocdlive.ocd.reservation.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests unitaires - ReservationMapper")
class ReservationMapperTest {

    private ReservationMapper reservationMapper;

    @BeforeEach
    void setUp() {
        reservationMapper = Mappers.getMapper(ReservationMapper.class);
        PackMapper packMapper = Mappers.getMapper(PackMapper.class);
        EquipmentMapper equipmentMapper = Mappers.getMapper(EquipmentMapper.class);
        ReflectionTestUtils.setField(packMapper, "equipmentMapper", equipmentMapper);
        ReflectionTestUtils.setField(reservationMapper, "packMapper", packMapper);
    }

    @Test
    @DisplayName("toEntity() - Devrait convertir un ReservationRequest en entité Reservation")
    void shouldMapReservationRequestToEntity() {
        LocalDateTime future = LocalDateTime.now().plusDays(5);
        ReservationRequest request = ReservationRequest.builder()
                .fullNameOrCompany("Agence Nova")
                .appointmentDateTime(future)
                .phoneNumber("+237600000000")
                .email("contact@nova.com")
                .locationType(ReservationLocationType.ONLINE)
                .packId(1L)
                .activitySector("Tech")
                .activityDescription("Webinaire")
                .country("Cameroun")
                .build();

        Reservation entity = reservationMapper.toEntity(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getFullNameOrCompany()).isEqualTo("Agence Nova");
        assertThat(entity.getLocationType()).isEqualTo(ReservationLocationType.ONLINE);
        assertThat(entity.getCountry()).isEqualTo("Cameroun");
    }

    @Test
    @DisplayName("toResponse() & toResponseList() - Devrait convertir une entité Reservation avec son Pack")
    void shouldMapEntityToResponse() {
        Pack pack = Pack.builder()
                .id(10L)
                .name("Pack Live")
                .price(new BigDecimal("100000.00"))
                .durationMinutes(60)
                .active(true)
                .build();

        Reservation reservation = Reservation.builder()
                .id(500L)
                .fullNameOrCompany("Agence Nova")
                .email("contact@nova.com")
                .locationType(ReservationLocationType.LOCAL)
                .status(ReservationStatus.CONFIRMED)
                .pack(pack)
                .build();

        ReservationResponse response = reservationMapper.toResponse(reservation);
        List<ReservationResponse> list = reservationMapper.toResponseList(List.of(reservation));

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(500L);
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(response.getPack()).isNotNull();
        assertThat(response.getPack().getName()).isEqualTo("Pack Live");
        assertThat(list).hasSize(1);
    }
}


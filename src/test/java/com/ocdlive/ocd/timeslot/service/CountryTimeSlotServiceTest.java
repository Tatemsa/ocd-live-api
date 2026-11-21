package com.ocdlive.ocd.timeslot.service;

import com.ocdlive.ocd.exception.BadRequestException;
import com.ocdlive.ocd.exception.ResourceNotFoundException;
import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.pack.repository.PackRepository;
import com.ocdlive.ocd.timeslot.dto.CountryTimeSlotRequest;
import com.ocdlive.ocd.timeslot.dto.CountryTimeSlotResponse;
import com.ocdlive.ocd.timeslot.mapper.CountryTimeSlotMapper;
import com.ocdlive.ocd.timeslot.model.CountryTimeSlot;
import com.ocdlive.ocd.timeslot.repository.CountryTimeSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - CountryTimeSlotService")
class CountryTimeSlotServiceTest {

    @Mock
    private CountryTimeSlotRepository countryTimeSlotRepository;

    @Mock
    private PackRepository packRepository;

    @Mock
    private CountryTimeSlotMapper countryTimeSlotMapper;

    @InjectMocks
    private CountryTimeSlotService countryTimeSlotService;

    private CountryTimeSlot sampleSlot;
    private CountryTimeSlotRequest sampleRequest;
    private CountryTimeSlotResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleRequest = CountryTimeSlotRequest.builder()
                .country("Cameroun")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .subscriptionRequired(true)
                .active(true)
                .build();

        sampleSlot = CountryTimeSlot.builder()
                .id(1L)
                .country("Cameroun")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .subscriptionRequired(true)
                .active(true)
                .build();

        sampleResponse = CountryTimeSlotResponse.builder()
                .id(1L)
                .country("Cameroun")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .subscriptionRequired(true)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("createTimeSlot() - Devrait créer un créneau horaire valide pour un pays")
    void shouldCreateTimeSlotWhenTimeRangeIsValid() {
        given(countryTimeSlotMapper.toEntity(sampleRequest)).willReturn(sampleSlot);
        given(countryTimeSlotRepository.save(sampleSlot)).willReturn(sampleSlot);
        given(countryTimeSlotMapper.toResponse(sampleSlot)).willReturn(sampleResponse);

        CountryTimeSlotResponse result = countryTimeSlotService.createTimeSlot(sampleRequest);

        assertThat(result).isEqualTo(sampleResponse);
        verify(countryTimeSlotRepository).save(sampleSlot);
    }

    @Test
    @DisplayName("createTimeSlot() - Devrait lever BadRequestException si startTime >= endTime")
    void shouldThrowBadRequestExceptionWhenStartTimeIsNotBeforeEndTime() {
        sampleRequest.setStartTime(LocalTime.of(10, 0));
        sampleRequest.setEndTime(LocalTime.of(8, 0));

        assertThatThrownBy(() -> countryTimeSlotService.createTimeSlot(sampleRequest))
                .isInstanceOf(BadRequestException.class);

        verify(countryTimeSlotRepository, never()).save(any());
    }

    @Test
    @DisplayName("getTimeSlotsByCountry() - Devrait retourner les créneaux actifs du pays")
    void shouldReturnTimeSlotsByCountry() {
        given(countryTimeSlotRepository.findByCountryIgnoreCaseAndActiveTrue("Cameroun"))
                .willReturn(List.of(sampleSlot));
        given(countryTimeSlotMapper.toResponseList(List.of(sampleSlot)))
                .willReturn(List.of(sampleResponse));

        List<CountryTimeSlotResponse> result = countryTimeSlotService.getTimeSlotsByCountry("Cameroun");

        assertThat(result).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("deleteTimeSlot() - Devrait lever ResourceNotFoundException si l'ID n'existe pas")
    void shouldThrowNotFoundWhenDeletingNonExistentSlot() {
        given(countryTimeSlotRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> countryTimeSlotService.deleteTimeSlot(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}


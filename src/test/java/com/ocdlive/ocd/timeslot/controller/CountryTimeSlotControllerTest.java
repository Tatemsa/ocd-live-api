package com.ocdlive.ocd.timeslot.controller;

import com.ocdlive.ocd.timeslot.dto.CountryTimeSlotResponse;
import com.ocdlive.ocd.timeslot.service.CountryTimeSlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - CountryTimeSlotController")
class CountryTimeSlotControllerTest {

    @Mock
    private CountryTimeSlotService countryTimeSlotService;

    @InjectMocks
    private CountryTimeSlotController countryTimeSlotController;

    private CountryTimeSlotResponse sampleResponse;

    @BeforeEach
    void setUp() {
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
    @DisplayName("GET /timeslots - Devrait retourner les créneaux actifs")
    void shouldGetActiveTimeSlots() {
        given(countryTimeSlotService.getActiveTimeSlots()).willReturn(List.of(sampleResponse));

        ResponseEntity<List<CountryTimeSlotResponse>> response = countryTimeSlotController.getActiveTimeSlots();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("GET /timeslots/country/{country} - Devrait retourner les créneaux d'un pays")
    void shouldGetTimeSlotsByCountry() {
        given(countryTimeSlotService.getTimeSlotsByCountry("Cameroun")).willReturn(List.of(sampleResponse));

        ResponseEntity<List<CountryTimeSlotResponse>> response = countryTimeSlotController.getTimeSlotsByCountry("Cameroun");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }
}


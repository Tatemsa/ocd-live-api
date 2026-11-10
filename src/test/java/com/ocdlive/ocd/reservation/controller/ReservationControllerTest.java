package com.ocdlive.ocd.reservation.controller;

import com.ocdlive.ocd.reservation.dto.ReservationRequest;
import com.ocdlive.ocd.reservation.dto.ReservationResponse;
import com.ocdlive.ocd.reservation.model.ReservationLocationType;
import com.ocdlive.ocd.reservation.model.ReservationStatus;
import com.ocdlive.ocd.reservation.service.ReservationService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - ReservationController")
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    private User currentUser;
    private ReservationRequest sampleRequest;
    private ReservationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L)
                .email("client@nova-agency.com")
                .role(Role.ROLE_USER)
                .build();

        sampleRequest = ReservationRequest.builder()
                .fullNameOrCompany("Agence Digitale Nova")
                .appointmentDateTime(LocalDateTime.now().plusDays(7))
                .phoneNumber("+237699000111")
                .email("client@nova-agency.com")
                .locationType(ReservationLocationType.LOCAL)
                .packId(10L)
                .activitySector("E-commerce")
                .country("Cameroun")
                .build();

        sampleResponse = ReservationResponse.builder()
                .id(500L)
                .fullNameOrCompany("Agence Digitale Nova")
                .email("client@nova-agency.com")
                .status(ReservationStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("POST /reservations - Devrait créer une réservation et retourner 201 Created")
    void shouldCreateReservation() {
        given(reservationService.createReservation(sampleRequest, currentUser)).willReturn(sampleResponse);

        ResponseEntity<ReservationResponse> response = reservationController.createReservation(sampleRequest, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("GET /reservations/my-reservations - Devrait lister les réservations du client")
    void shouldGetMyReservations() {
        given(reservationService.getMyReservations(currentUser)).willReturn(List.of(sampleResponse));

        ResponseEntity<List<ReservationResponse>> response = reservationController.getMyReservations(currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("GET /reservations/my-pack-reservations - Devrait lister les réservations reçues pour les packs du créateur")
    void shouldGetReservationsForMyPacks() {
        given(reservationService.getReservationsForMyPacks(currentUser)).willReturn(List.of(sampleResponse));

        ResponseEntity<List<ReservationResponse>> response = reservationController.getReservationsForMyPacks(currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("GET /reservations/{id} - Devrait retourner une réservation par son ID")
    void shouldGetReservationById() {
        given(reservationService.getReservationById(500L, currentUser)).willReturn(sampleResponse);

        ResponseEntity<ReservationResponse> response = reservationController.getReservationById(500L, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("PATCH /reservations/{id}/status - Devrait mettre à jour le statut d'une réservation")
    void shouldUpdateStatus() {
        sampleResponse.setStatus(ReservationStatus.CONFIRMED);
        given(reservationService.updateReservationStatus(500L, ReservationStatus.CONFIRMED, currentUser))
                .willReturn(sampleResponse);

        ResponseEntity<ReservationResponse> response = reservationController.updateStatus(
                500L, ReservationStatus.CONFIRMED, currentUser
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("PATCH /reservations/{id}/cancel - Devrait annuler une réservation")
    void shouldCancelReservation() {
        sampleResponse.setStatus(ReservationStatus.CANCELLED);
        given(reservationService.cancelReservation(500L, currentUser)).willReturn(sampleResponse);

        ResponseEntity<ReservationResponse> response = reservationController.cancelReservation(500L, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }
}


package com.ocdlive.ocd.reservation.controller;

import com.ocdlive.ocd.reservation.dto.ReservationResponse;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - AdminReservationController")
class AdminReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private AdminReservationController adminReservationController;

    private User adminUser;
    private ReservationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .email("admin@ocdlive.com")
                .role(Role.ROLE_ADMIN)
                .build();

        sampleResponse = ReservationResponse.builder()
                .id(500L)
                .fullNameOrCompany("Agence Digitale Nova")
                .status(ReservationStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("GET /admin/reservations - Devrait lister toutes les réservations")
    void shouldGetAllReservations() {
        given(reservationService.getAllReservations()).willReturn(List.of(sampleResponse));

        ResponseEntity<List<ReservationResponse>> response = adminReservationController.getAllReservations();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("GET /admin/reservations/status/{status} - Devrait filtrer par statut")
    void shouldGetReservationsByStatus() {
        given(reservationService.getReservationsByStatus(ReservationStatus.PENDING)).willReturn(List.of(sampleResponse));

        ResponseEntity<List<ReservationResponse>> response = adminReservationController.getReservationsByStatus(
                ReservationStatus.PENDING
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("PATCH /admin/reservations/{id}/status - Devrait forcer la mise à jour du statut")
    void shouldUpdateReservationStatus() {
        sampleResponse.setStatus(ReservationStatus.CONFIRMED);
        given(reservationService.updateReservationStatus(500L, ReservationStatus.CONFIRMED, adminUser))
                .willReturn(sampleResponse);

        ResponseEntity<ReservationResponse> response = adminReservationController.updateReservationStatus(
                500L, ReservationStatus.CONFIRMED, adminUser
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}


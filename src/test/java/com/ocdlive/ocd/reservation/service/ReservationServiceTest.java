package com.ocdlive.ocd.reservation.service;

import com.ocdlive.ocd.exception.BusinessRuleException;
import com.ocdlive.ocd.exception.ResourceNotFoundException;
import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.pack.model.PackBillingType;
import com.ocdlive.ocd.pack.repository.PackRepository;
import com.ocdlive.ocd.reservation.dto.ReservationRequest;
import com.ocdlive.ocd.reservation.dto.ReservationResponse;
import com.ocdlive.ocd.reservation.mapper.ReservationMapper;
import com.ocdlive.ocd.reservation.model.Reservation;
import com.ocdlive.ocd.reservation.model.ReservationLocationType;
import com.ocdlive.ocd.reservation.model.ReservationStatus;
import com.ocdlive.ocd.reservation.repository.ReservationRepository;
import com.ocdlive.ocd.subscription.model.Subscription;
import com.ocdlive.ocd.subscription.model.SubscriptionStatus;
import com.ocdlive.ocd.subscription.repository.SubscriptionRepository;
import com.ocdlive.ocd.timeslot.model.CountryTimeSlot;
import com.ocdlive.ocd.timeslot.repository.CountryTimeSlotRepository;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - ReservationService")
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PackRepository packRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private CountryTimeSlotRepository countryTimeSlotRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User packOwner;
    private User clientUser;
    private User adminUser;
    private User strangerUser;
    private Pack activePack;
    private Reservation sampleReservation;
    private ReservationRequest sampleRequest;
    private ReservationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        packOwner = User.builder()
                .id(1L)
                .email("owner@ocdlive.com")
                .firstName("Owner")
                .lastName("Pack")
                .role(Role.ROLE_USER)
                .build();

        clientUser = User.builder()
                .id(2L)
                .email("client@nova-agency.com")
                .firstName("Client")
                .lastName("Nova")
                .role(Role.ROLE_USER)
                .build();

        adminUser = User.builder()
                .id(3L)
                .email("admin@ocdlive.com")
                .firstName("Super")
                .lastName("Admin")
                .role(Role.ROLE_ADMIN)
                .build();

        strangerUser = User.builder()
                .id(4L)
                .email("stranger@ocdlive.com")
                .firstName("Stranger")
                .lastName("User")
                .role(Role.ROLE_USER)
                .build();

        activePack = Pack.builder()
                .id(10L)
                .name("Pack Live Premium")
                .price(new BigDecimal("150000.00"))
                .durationMinutes(90)
                .billingType(PackBillingType.ONE_TIME)
                .active(true)
                .createdBy(packOwner)
                .build();

        LocalDateTime futureDate = LocalDateTime.now().plusDays(10).withHour(14).withMinute(0);

        sampleRequest = ReservationRequest.builder()
                .fullNameOrCompany("Agence Digitale Nova")
                .appointmentDateTime(futureDate)
                .phoneNumber("+237699000111")
                .email("client@nova-agency.com")
                .locationType(ReservationLocationType.LOCAL)
                .packId(10L)
                .activitySector("E-commerce & Mode")
                .activityDescription("Défilé interactif")
                .country("Cameroun")
                .build();

        sampleReservation = Reservation.builder()
                .id(500L)
                .fullNameOrCompany("Agence Digitale Nova")
                .appointmentDateTime(futureDate)
                .endDateTime(futureDate.plusMinutes(90))
                .phoneNumber("+237699000111")
                .email("client@nova-agency.com")
                .locationType(ReservationLocationType.LOCAL)
                .activitySector("E-commerce & Mode")
                .activityDescription("Défilé interactif")
                .country("Cameroun")
                .pack(activePack)
                .status(ReservationStatus.PENDING)
                .client(clientUser)
                .build();

        sampleResponse = ReservationResponse.builder()
                .id(500L)
                .fullNameOrCompany("Agence Digitale Nova")
                .appointmentDateTime(futureDate)
                .endDateTime(futureDate.plusMinutes(90))
                .phoneNumber("+237699000111")
                .email("client@nova-agency.com")
                .locationType(ReservationLocationType.LOCAL)
                .activitySector("E-commerce & Mode")
                .country("Cameroun")
                .status(ReservationStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("createReservation()")
    class CreateReservationTests {

        @Test
        @DisplayName("Devrait créer une réservation avec statut PENDING pour un pack unitaire")
        void shouldCreateReservationWhenPackIsActive() {
            given(packRepository.findById(10L)).willReturn(Optional.of(activePack));
            given(reservationRepository.existsOverlappingReservation(any(), any(), any())).willReturn(false);
            given(countryTimeSlotRepository.findByActiveTrue()).willReturn(Collections.emptyList());
            given(reservationMapper.toEntity(sampleRequest)).willReturn(sampleReservation);
            given(reservationRepository.save(sampleReservation)).willReturn(sampleReservation);
            given(reservationMapper.toResponse(sampleReservation)).willReturn(sampleResponse);

            ReservationResponse result = reservationService.createReservation(sampleRequest, clientUser);

            assertThat(result).isEqualTo(sampleResponse);
            assertThat(sampleReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
            assertThat(sampleReservation.getPack()).isEqualTo(activePack);
            assertThat(sampleReservation.getClient()).isEqualTo(clientUser);
            verify(reservationRepository).save(sampleReservation);
        }

        @Test
        @DisplayName("Devrait lever BusinessRuleException si le créneau horaire chevauche une réservation existante")
        void shouldThrowBusinessRuleExceptionWhenSlotIsAlreadyBooked() {
            given(packRepository.findById(10L)).willReturn(Optional.of(activePack));
            given(reservationRepository.existsOverlappingReservation(any(), any(), any())).willReturn(true);

            assertThatThrownBy(() -> reservationService.createReservation(sampleRequest, clientUser))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("déjà réservé");

            verify(reservationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait bloquer la réservation sur un créneau dédié à un autre pays (ex: 08h-10h Cameroun)")
        void shouldThrowBusinessRuleExceptionWhenBookingOnAnotherCountryDedicatedSlot() {
            LocalDateTime cameroonSlotTime = LocalDateTime.now().plusDays(5).withHour(8).withMinute(30);
            sampleRequest.setAppointmentDateTime(cameroonSlotTime);
            sampleRequest.setCountry("Togo"); // Client du Togo sur le créneau 08h-10h du Cameroun

            CountryTimeSlot cameroonSlot = CountryTimeSlot.builder()
                    .id(1L)
                    .country("Cameroun")
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(10, 0))
                    .subscriptionRequired(true)
                    .active(true)
                    .build();

            given(packRepository.findById(10L)).willReturn(Optional.of(activePack));
            given(reservationRepository.existsOverlappingReservation(any(), any(), any())).willReturn(false);
            given(countryTimeSlotRepository.findByActiveTrue()).willReturn(List.of(cameroonSlot));

            assertThatThrownBy(() -> reservationService.createReservation(sampleRequest, clientUser))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Cameroun");

            verify(reservationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait confirmer immédiatement et décrémenter le quota pour un pack à abonnement mensuel actif")
        void shouldConfirmImmediatelyAndDecrementQuotaForMonthlySubscriptionPack() {
            activePack.setBillingType(PackBillingType.MONTHLY_SUBSCRIPTION);
            activePack.setTargetCountry("Cameroun");
            activePack.setMonthlyLiveQuota(4);

            Subscription activeSub = Subscription.builder()
                    .id(55L)
                    .client(clientUser)
                    .pack(activePack)
                    .country("Cameroun")
                    .status(SubscriptionStatus.ACTIVE)
                    .startDate(LocalDate.now().minusDays(2))
                    .endDate(LocalDate.now().plusDays(28))
                    .remainingLives(3)
                    .build();

            given(packRepository.findById(10L)).willReturn(Optional.of(activePack));
            given(reservationRepository.existsOverlappingReservation(any(), any(), any())).willReturn(false);
            given(countryTimeSlotRepository.findByActiveTrue()).willReturn(Collections.emptyList());
            given(subscriptionRepository.findFirstByClientIdAndPackIdAndStatusOrderByEndDateDesc(
                    2L, 10L, SubscriptionStatus.ACTIVE
            )).willReturn(Optional.of(activeSub));
            given(reservationMapper.toEntity(sampleRequest)).willReturn(sampleReservation);
            given(reservationRepository.save(sampleReservation)).willReturn(sampleReservation);
            sampleResponse.setStatus(ReservationStatus.CONFIRMED);
            given(reservationMapper.toResponse(sampleReservation)).willReturn(sampleResponse);

            ReservationResponse result = reservationService.createReservation(sampleRequest, clientUser);

            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            assertThat(sampleReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            assertThat(activeSub.getRemainingLives()).isEqualTo(2);
            verify(subscriptionRepository).save(activeSub);
        }

        @Test
        @DisplayName("Devrait lever ResourceNotFoundException si le pack demandé n'existe pas")
        void shouldThrowResourceNotFoundExceptionWhenPackDoesNotExist() {
            given(packRepository.findById(10L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.createReservation(sampleRequest, clientUser))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(reservationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait lever BusinessRuleException si le pack sélectionné est inactif")
        void shouldThrowBusinessRuleExceptionWhenPackIsInactive() {
            activePack.setActive(false);
            given(packRepository.findById(10L)).willReturn(Optional.of(activePack));

            assertThatThrownBy(() -> reservationService.createReservation(sampleRequest, clientUser))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("pas disponible");

            verify(reservationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateReservationStatus()")
    class UpdateStatusTests {

        @Test
        @DisplayName("Devrait permettre au propriétaire du pack de confirmer une réservation")
        void shouldAllowPackOwnerToUpdateReservationStatus() {
            given(reservationRepository.findById(500L)).willReturn(Optional.of(sampleReservation));
            given(reservationRepository.save(sampleReservation)).willReturn(sampleReservation);
            sampleResponse.setStatus(ReservationStatus.CONFIRMED);
            given(reservationMapper.toResponse(sampleReservation)).willReturn(sampleResponse);

            ReservationResponse result = reservationService.updateReservationStatus(
                    500L, ReservationStatus.CONFIRMED, packOwner
            );

            assertThat(sampleReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Devrait permettre à un administrateur de mettre à jour le statut d'une réservation")
        void shouldAllowAdminToUpdateReservationStatus() {
            given(reservationRepository.findById(500L)).willReturn(Optional.of(sampleReservation));
            given(reservationRepository.save(sampleReservation)).willReturn(sampleReservation);
            sampleResponse.setStatus(ReservationStatus.COMPLETED);
            given(reservationMapper.toResponse(sampleReservation)).willReturn(sampleResponse);

            ReservationResponse result = reservationService.updateReservationStatus(
                    500L, ReservationStatus.COMPLETED, adminUser
            );

            assertThat(result.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
        }

        @Test
        @DisplayName("Devrait lever AccessDeniedException si le client ou un tiers tente de changer le statut")
        void shouldThrowAccessDeniedWhenNonManagerUpdatesStatus() {
            given(reservationRepository.findById(500L)).willReturn(Optional.of(sampleReservation));

            assertThatThrownBy(() -> reservationService.updateReservationStatus(
                    500L, ReservationStatus.CONFIRMED, clientUser
            )).isInstanceOf(AccessDeniedException.class);

            assertThatThrownBy(() -> reservationService.updateReservationStatus(
                    500L, ReservationStatus.CONFIRMED, null
            )).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("cancelReservation()")
    class CancelReservationTests {

        @Test
        @DisplayName("Devrait permettre au client d'annuler sa réservation")
        void shouldAllowClientToCancelReservation() {
            given(reservationRepository.findById(500L)).willReturn(Optional.of(sampleReservation));
            given(reservationRepository.save(sampleReservation)).willReturn(sampleReservation);
            sampleResponse.setStatus(ReservationStatus.CANCELLED);
            given(reservationMapper.toResponse(sampleReservation)).willReturn(sampleResponse);

            ReservationResponse result = reservationService.cancelReservation(500L, clientUser);

            assertThat(sampleReservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        }

        @Test
        @DisplayName("Devrait permettre à un administrateur d'annuler une réservation")
        void shouldAllowAdminToCancelReservation() {
            given(reservationRepository.findById(500L)).willReturn(Optional.of(sampleReservation));
            given(reservationRepository.save(sampleReservation)).willReturn(sampleReservation);
            sampleResponse.setStatus(ReservationStatus.CANCELLED);
            given(reservationMapper.toResponse(sampleReservation)).willReturn(sampleResponse);

            ReservationResponse result = reservationService.cancelReservation(500L, adminUser);

            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        }

        @Test
        @DisplayName("Devrait lever AccessDeniedException si un utilisateur non autorisé tente d'annuler")
        void shouldThrowAccessDeniedWhenStrangerCancelsReservation() {
            given(reservationRepository.findById(500L)).willReturn(Optional.of(sampleReservation));

            assertThatThrownBy(() -> reservationService.cancelReservation(500L, strangerUser))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("Consultation des réservations")
    class ReadReservationTests {

        @Test
        @DisplayName("getReservationById() - Devrait autoriser le client, le propriétaire du pack et l'admin")
        void shouldAllowClientPackOwnerAndAdminToGetReservationById() {
            given(reservationRepository.findById(500L)).willReturn(Optional.of(sampleReservation));
            given(reservationMapper.toResponse(sampleReservation)).willReturn(sampleResponse);

            assertThat(reservationService.getReservationById(500L, clientUser)).isEqualTo(sampleResponse);
            assertThat(reservationService.getReservationById(500L, packOwner)).isEqualTo(sampleResponse);
            assertThat(reservationService.getReservationById(500L, adminUser)).isEqualTo(sampleResponse);
        }

        @Test
        @DisplayName("getReservationById() - Devrait lever AccessDeniedException pour un utilisateur tiers")
        void shouldThrowAccessDeniedWhenStrangerGetsReservationById() {
            given(reservationRepository.findById(500L)).willReturn(Optional.of(sampleReservation));

            assertThatThrownBy(() -> reservationService.getReservationById(500L, strangerUser))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("getMyReservations() - Devrait retourner les réservations du client connecté")
        void shouldReturnMyReservations() {
            given(reservationRepository.findByClientId(2L)).willReturn(List.of(sampleReservation));
            given(reservationMapper.toResponseList(List.of(sampleReservation))).willReturn(List.of(sampleResponse));

            List<ReservationResponse> result = reservationService.getMyReservations(clientUser);

            assertThat(result).hasSize(1).containsExactly(sampleResponse);
        }

        @Test
        @DisplayName("getMyReservations() - Devrait lever AccessDeniedException si client est null")
        void shouldThrowAccessDeniedWhenGettingMyReservationsWithNullClient() {
            assertThatThrownBy(() -> reservationService.getMyReservations(null))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("getReservationsForMyPacks() - Devrait retourner les réservations pour les packs du créateur")
        void shouldReturnReservationsForMyPacks() {
            given(reservationRepository.findByPackCreatedById(1L)).willReturn(List.of(sampleReservation));
            given(reservationMapper.toResponseList(List.of(sampleReservation))).willReturn(List.of(sampleResponse));

            List<ReservationResponse> result = reservationService.getReservationsForMyPacks(packOwner);

            assertThat(result).hasSize(1).containsExactly(sampleResponse);
        }

        @Test
        @DisplayName("getReservationsForMyPacks() - Devrait lever AccessDeniedException si packOwner est null")
        void shouldThrowAccessDeniedWhenPackOwnerIsNull() {
            assertThatThrownBy(() -> reservationService.getReservationsForMyPacks(null))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("getAllReservations() & getReservationsByStatus() - Devrait retourner les réservations filtrées")
        void shouldReturnAllAndFilteredReservations() {
            given(reservationRepository.findAll()).willReturn(List.of(sampleReservation));
            given(reservationRepository.findByStatus(ReservationStatus.PENDING)).willReturn(List.of(sampleReservation));
            given(reservationMapper.toResponseList(List.of(sampleReservation))).willReturn(List.of(sampleResponse));

            assertThat(reservationService.getAllReservations()).hasSize(1);
            assertThat(reservationService.getReservationsByStatus(ReservationStatus.PENDING)).hasSize(1);
        }
    }
}

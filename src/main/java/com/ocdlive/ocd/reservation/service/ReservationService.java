package com.ocdlive.ocd.reservation.service;

import com.ocdlive.ocd.exception.BusinessRuleException;
import com.ocdlive.ocd.exception.ResourceNotFoundException;
import com.ocdlive.ocd.notification.service.EmailNotificationService;
import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.pack.model.PackBillingType;
import com.ocdlive.ocd.pack.repository.PackRepository;
import com.ocdlive.ocd.reservation.dto.ReservationRequest;
import com.ocdlive.ocd.reservation.dto.ReservationResponse;
import com.ocdlive.ocd.reservation.mapper.ReservationMapper;
import com.ocdlive.ocd.reservation.model.Reservation;
import com.ocdlive.ocd.reservation.model.ReservationStatus;
import com.ocdlive.ocd.reservation.repository.ReservationRepository;
import com.ocdlive.ocd.subscription.model.Subscription;
import com.ocdlive.ocd.subscription.model.SubscriptionStatus;
import com.ocdlive.ocd.subscription.repository.SubscriptionRepository;
import com.ocdlive.ocd.timeslot.model.CountryTimeSlot;
import com.ocdlive.ocd.timeslot.repository.CountryTimeSlotRepository;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PackRepository packRepository;
    private final ReservationMapper reservationMapper;
    private final CountryTimeSlotRepository countryTimeSlotRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailNotificationService emailNotificationService;

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, User client) {
        Pack pack = packRepository.findById(request.getPackId())
                .orElseThrow(() -> new ResourceNotFoundException("Pack", "id", request.getPackId()));

        if (!pack.isActive()) {
            throw new BusinessRuleException("Le pack sélectionné n'est actuellement pas disponible à la réservation.", "PACK_NOT_ACTIVE", "error.pack.not_active");
        }

        // 1. Vérifie si le pack est restreint à un pays particulier
        if (pack.getTargetCountry() != null && !pack.getTargetCountry().isBlank()
                && !pack.getTargetCountry().equalsIgnoreCase(request.getCountry())) {
            throw new BusinessRuleException(
                    String.format("Le pack '%s' est exclusivement réservé aux clients du pays : %s.", pack.getName(), pack.getTargetCountry()),
                    "PACK_COUNTRY_RESTRICTED"
            );
        }

        int durationMinutes = pack.getDurationMinutes() != null ? pack.getDurationMinutes() : 60;
        LocalDateTime startDateTime = request.getAppointmentDateTime();
        LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);

        // 2. Vérifie l'absence de chevauchement d'horaire avec une autre réservation active
        boolean slotTaken = reservationRepository.existsOverlappingReservation(
                startDateTime,
                endDateTime,
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
        );
        if (slotTaken) {
            throw new BusinessRuleException(
                    "Ce créneau horaire est déjà réservé par un autre live. Veuillez choisir un autre horaire.",
                    "TIME_SLOT_ALREADY_BOOKED"
            );
        }

        // 3. Vérifie les règles de créneaux horaires dédiés par pays (ex: 08h-10h réservé au Cameroun)
        validateCountryDedicatedTimeSlots(startDateTime, endDateTime, request.getCountry(), pack);

        // 4. Gestion des packs à abonnement mensuel vs packs unitaires
        ReservationStatus initialStatus = ReservationStatus.PENDING;
        if (pack.getBillingType() == PackBillingType.MONTHLY_SUBSCRIPTION) {
            consumeMonthlySubscriptionQuota(client, pack);
            // L'abonnement mensuel étant déjà payé et actif, la réservation est immédiatement confirmée
            initialStatus = ReservationStatus.CONFIRMED;
        }

        Reservation reservation = reservationMapper.toEntity(request);
        reservation.setEndDateTime(endDateTime);
        reservation.setPack(pack);
        reservation.setStatus(initialStatus);
        reservation.setClient(client);

        Reservation saved = reservationRepository.save(reservation);
        if (initialStatus == ReservationStatus.CONFIRMED && emailNotificationService != null) {
            emailNotificationService.sendReservationPaymentConfirmationEmails(null, saved);
        }
        return reservationMapper.toResponse(saved);
    }

    private void validateCountryDedicatedTimeSlots(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String clientCountry,
            Pack pack
    ) {
        if (countryTimeSlotRepository == null) {
            return;
        }

        List<CountryTimeSlot> activeSlots = countryTimeSlotRepository.findByActiveTrue();
        if (activeSlots == null || activeSlots.isEmpty()) {
            return;
        }

        DayOfWeek appointmentDay = startDateTime.getDayOfWeek();
        LocalTime reqStart = startDateTime.toLocalTime();
        LocalTime reqEnd = endDateTime.toLocalTime();

        for (CountryTimeSlot slot : activeSlots) {
            boolean sameDayOrEveryDay = slot.getDayOfWeek() == null || slot.getDayOfWeek() == appointmentDay;
            boolean timeOverlaps = reqStart.isBefore(slot.getEndTime()) && reqEnd.isAfter(slot.getStartTime());

            if (sameDayOrEveryDay && timeOverlaps) {
                if (!slot.getCountry().equalsIgnoreCase(clientCountry)) {
                    throw new BusinessRuleException(
                            String.format(
                                    "Le créneau de %s à %s est exclusivement réservé aux lives des clients du pays : %s.",
                                    slot.getStartTime(), slot.getEndTime(), slot.getCountry()
                            ),
                            "COUNTRY_SLOT_RESTRICTED"
                    );
                }

                if (slot.isSubscriptionRequired() && pack.getBillingType() != PackBillingType.MONTHLY_SUBSCRIPTION) {
                    throw new BusinessRuleException(
                            String.format(
                                    "Le créneau dédié (%s - %s) pour %s nécessite un pack à abonnement mensuel.",
                                    slot.getStartTime(), slot.getEndTime(), slot.getCountry()
                            ),
                            "SUBSCRIPTION_PACK_REQUIRED"
                    );
                }

                if (slot.getPack() != null && !slot.getPack().getId().equals(pack.getId())) {
                    throw new BusinessRuleException(
                            String.format(
                                    "Ce créneau dédié nécessite la réservation du pack spécifique : %s.",
                                    slot.getPack().getName()
                            ),
                            "SPECIFIC_PACK_REQUIRED"
                    );
                }
            }
        }
    }

    private void consumeMonthlySubscriptionQuota(User client, Pack pack) {
        if (client == null) {
            throw new AccessDeniedException("Vous devez être connecté avec un abonnement mensuel actif pour réserver ce pack.");
        }

        Subscription subscription = subscriptionRepository
                .findFirstByClientIdAndPackIdAndStatusOrderByEndDateDesc(
                        client.getId(),
                        pack.getId(),
                        SubscriptionStatus.ACTIVE
                )
                .orElseThrow(() -> new BusinessRuleException(
                        "Vous ne disposez d'aucun abonnement mensuel actif pour ce pack.",
                        "NO_ACTIVE_SUBSCRIPTION"
                ));

        if (subscription.getEndDate() != null && subscription.getEndDate().isBefore(LocalDate.now())) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            throw new BusinessRuleException(
                    "Votre abonnement mensuel pour ce pack a expiré. Veuillez le renouveler.",
                    "SUBSCRIPTION_EXPIRED"
            );
        }

        if (subscription.getRemainingLives() == null || subscription.getRemainingLives() <= 0) {
            throw new BusinessRuleException(
                    "Vous avez épuisé votre quota de lives pour ce mois-ci.",
                    "SUBSCRIPTION_QUOTA_EXCEEDED"
            );
        }

        subscription.setRemainingLives(subscription.getRemainingLives() - 1);
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public ReservationResponse updateReservationStatus(Long id, ReservationStatus newStatus, User currentUser) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation", "id", id));

        checkManagerPermission(reservation, currentUser);

        reservation.setStatus(newStatus);
        return reservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse cancelReservation(Long id, User currentUser) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation", "id", id));

        boolean isClient = (currentUser != null && reservation.getClient() != null && reservation.getClient().getId().equals(currentUser.getId()))
                || (currentUser != null && reservation.getEmail().equalsIgnoreCase(currentUser.getEmail()));
        boolean isAdmin = currentUser != null && currentUser.getRole() == Role.ROLE_ADMIN;

        if (!isClient && !isAdmin) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à annuler cette réservation.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id, User currentUser) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation", "id", id));

        if (currentUser != null && currentUser.getRole() != Role.ROLE_ADMIN) {
            boolean isClient = (reservation.getClient() != null && reservation.getClient().getId().equals(currentUser.getId()))
                    || reservation.getEmail().equalsIgnoreCase(currentUser.getEmail());
            boolean isPackOwner = reservation.getPack().getCreatedBy() != null
                    && reservation.getPack().getCreatedBy().getId().equals(currentUser.getId());

            if (!isClient && !isPackOwner) {
                throw new AccessDeniedException("Accès non autorisé à cette réservation.");
            }
        }

        return reservationMapper.toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(User client) {
        if (client == null) {
            throw new AccessDeniedException("Vous devez être authentifié pour consulter vos réservations.");
        }
        return reservationMapper.toResponseList(reservationRepository.findByClientId(client.getId()));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsForMyPacks(User packOwner) {
        if (packOwner == null) {
            throw new AccessDeniedException("Vous devez être authentifié.");
        }
        return reservationMapper.toResponseList(reservationRepository.findByPackCreatedById(packOwner.getId()));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationMapper.toResponseList(reservationRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByStatus(ReservationStatus status) {
        return reservationMapper.toResponseList(reservationRepository.findByStatus(status));
    }

    private void checkManagerPermission(Reservation reservation, User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Utilisateur non authentifié.");
        }
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        boolean isPackOwner = reservation.getPack().getCreatedBy() != null
                && reservation.getPack().getCreatedBy().getId().equals(currentUser.getId());

        if (!isAdmin && !isPackOwner) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier le statut de cette réservation.");
        }
    }
}

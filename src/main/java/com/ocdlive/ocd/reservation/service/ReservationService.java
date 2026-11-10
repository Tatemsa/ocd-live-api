package com.ocdlive.ocd.reservation.service;

import com.ocdlive.ocd.exception.BusinessRuleException;
import com.ocdlive.ocd.exception.ResourceNotFoundException;
import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.pack.repository.PackRepository;
import com.ocdlive.ocd.reservation.dto.ReservationRequest;
import com.ocdlive.ocd.reservation.dto.ReservationResponse;
import com.ocdlive.ocd.reservation.mapper.ReservationMapper;
import com.ocdlive.ocd.reservation.model.Reservation;
import com.ocdlive.ocd.reservation.model.ReservationStatus;
import com.ocdlive.ocd.reservation.repository.ReservationRepository;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PackRepository packRepository;
    private final ReservationMapper reservationMapper;

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, User client) {
        Pack pack = packRepository.findById(request.getPackId())
                .orElseThrow(() -> new ResourceNotFoundException("Pack", "id", request.getPackId()));

        if (!pack.isActive()) {
            throw new BusinessRuleException("Le pack sélectionné n'est actuellement pas disponible à la réservation.", "PACK_NOT_ACTIVE", "error.pack.not_active");
        }

        Reservation reservation = reservationMapper.toEntity(request);
        reservation.setPack(pack);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setClient(client);

        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toResponse(saved);
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

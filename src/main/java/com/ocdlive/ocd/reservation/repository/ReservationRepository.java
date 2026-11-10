package com.ocdlive.ocd.reservation.repository;

import com.ocdlive.ocd.reservation.model.Reservation;
import com.ocdlive.ocd.reservation.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPackId(Long packId);

    List<Reservation> findByEmailIgnoreCase(String email);

    List<Reservation> findByClientId(Long clientId);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByPackCreatedById(Long packOwnerUserId);

    List<Reservation> findByAppointmentDateTimeBetween(LocalDateTime start, LocalDateTime end);
}


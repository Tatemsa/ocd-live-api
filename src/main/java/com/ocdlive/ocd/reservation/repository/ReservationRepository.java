package com.ocdlive.ocd.reservation.repository;

import com.ocdlive.ocd.reservation.model.Reservation;
import com.ocdlive.ocd.reservation.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPackId(Long packId);

    List<Reservation> findByEmailIgnoreCase(String email);

    List<Reservation> findByClientId(Long clientId);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByPackCreatedById(Long packOwnerUserId);

    List<Reservation> findByAppointmentDateTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.status IN :activeStatuses
              AND r.appointmentDateTime < :endDateTime
              AND r.endDateTime > :startDateTime
            """)
    boolean existsOverlappingReservation(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("activeStatuses") Collection<ReservationStatus> activeStatuses
    );
}

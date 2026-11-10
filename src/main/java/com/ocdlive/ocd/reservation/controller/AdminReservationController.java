package com.ocdlive.ocd.reservation.controller;

import com.ocdlive.ocd.reservation.dto.ReservationResponse;
import com.ocdlive.ocd.reservation.model.ReservationStatus;
import com.ocdlive.ocd.reservation.service.ReservationService;
import com.ocdlive.ocd.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Administration - Réservations", description = "Endpoints réservés aux administrateurs pour la supervision de toutes les réservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @Operation(summary = "Lister toutes les réservations du système")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Filtrer les réservations par statut (PENDING, CONFIRMED, CANCELLED, COMPLETED)")
    public ResponseEntity<List<ReservationResponse>> getReservationsByStatus(@PathVariable ReservationStatus status) {
        return ResponseEntity.ok(reservationService.getReservationsByStatus(status));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Forcer la mise à jour du statut d'une réservation")
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam ReservationStatus status,
            @AuthenticationPrincipal User adminUser
    ) {
        return ResponseEntity.ok(reservationService.updateReservationStatus(id, status, adminUser));
    }
}


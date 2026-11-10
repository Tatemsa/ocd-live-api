package com.ocdlive.ocd.reservation.controller;

import com.ocdlive.ocd.reservation.dto.ReservationRequest;
import com.ocdlive.ocd.reservation.dto.ReservationResponse;
import com.ocdlive.ocd.reservation.model.ReservationStatus;
import com.ocdlive.ocd.reservation.service.ReservationService;
import com.ocdlive.ocd.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Réservations", description = "Endpoints de réservation de packs pour les sessions live")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Effectuer une réservation de pack", description = "Permet de réserver un pack pour un live (en ligne ou en présentiel). Accessible avec ou sans compte connecté.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Réservation enregistrée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou pack indisponible")
    })
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal User client
    ) {
        ReservationResponse response = reservationService.createReservation(request, client);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-reservations")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Lister mes réservations passées", description = "Retourne l'historique des réservations effectuées par le client connecté")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(@AuthenticationPrincipal User client) {
        return ResponseEntity.ok(reservationService.getMyReservations(client));
    }

    @GetMapping("/my-pack-reservations")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Lister les réservations reçues pour mes packs", description = "Permet au créateur d'un pack de voir toutes les réservations reçues")
    public ResponseEntity<List<ReservationResponse>> getReservationsForMyPacks(@AuthenticationPrincipal User packOwner) {
        return ResponseEntity.ok(reservationService.getReservationsForMyPacks(packOwner));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Consulter le détail d'une réservation par son ID")
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(reservationService.getReservationById(id, currentUser));
    }

    @PatchMapping("/{id}/status")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Mettre à jour le statut d'une réservation (Confirmer / Terminer)", description = "Réservé au créateur du pack réservé ou à l'administrateur")
    public ResponseEntity<ReservationResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ReservationStatus status,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(reservationService.updateReservationStatus(id, status, currentUser));
    }

    @PatchMapping("/{id}/cancel")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Annuler une réservation", description = "Permet au client ou à l'administrateur d'annuler une réservation")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(reservationService.cancelReservation(id, currentUser));
    }
}


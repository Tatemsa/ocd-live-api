package com.ocdlive.ocd.reservation.dto;

import com.ocdlive.ocd.pack.dto.PackResponse;
import com.ocdlive.ocd.reservation.model.Reservation;
import com.ocdlive.ocd.reservation.model.ReservationLocationType;
import com.ocdlive.ocd.reservation.model.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Détails complets d'une réservation")
public class ReservationResponse {

    @Schema(description = "Identifiant unique de la réservation", example = "1")
    private Long id;

    @Schema(description = "Nom complet de la personne, marque ou entreprise", example = "Agence Digitale Nova")
    private String fullNameOrCompany;

    @Schema(description = "Date et heure du rendez-vous / live", example = "2026-12-15T14:30:00")
    private LocalDateTime appointmentDateTime;

    @Schema(description = "Numéro de téléphone", example = "+237699000111")
    private String phoneNumber;

    @Schema(description = "Adresse email", example = "contact@nova-agency.com")
    private String email;

    @Schema(description = "Type de réservation (ONLINE ou LOCAL)", example = "LOCAL")
    private ReservationLocationType locationType;

    @Schema(description = "Secteur d'activité", example = "E-commerce & Mode")
    private String activitySector;

    @Schema(description = "Description de l'activité du live")
    private String activityDescription;

    @Schema(description = "Pays d'origine", example = "Cameroun")
    private String country;

    @Schema(description = "Statut actuel de la réservation (PENDING, CONFIRMED, CANCELLED, COMPLETED)", example = "PENDING")
    private ReservationStatus status;

    @Schema(description = "Détails du pack réservé")
    private PackResponse pack;

    @Schema(description = "Date de soumission de la réservation")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière modification")
    private LocalDateTime updatedAt;
}


package com.ocdlive.ocd.subscription.dto;

import com.ocdlive.ocd.pack.dto.PackResponse;
import com.ocdlive.ocd.subscription.model.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Détails d'un abonnement mensuel à un pack")
public class SubscriptionResponse {

    @Schema(description = "Identifiant unique de l'abonnement", example = "1")
    private Long id;

    @Schema(description = "Identifiant du client abonné", example = "2")
    private Long clientId;

    @Schema(description = "Email du client abonné", example = "client@nova-agency.com")
    private String clientEmail;

    @Schema(description = "Pays de rattachement de l'abonnement", example = "Cameroun")
    private String country;

    @Schema(description = "Statut de l'abonnement (PENDING_PAYMENT, ACTIVE, EXPIRED, CANCELLED)", example = "ACTIVE")
    private SubscriptionStatus status;

    @Schema(description = "Date de début de validité", example = "2026-11-11")
    private LocalDate startDate;

    @Schema(description = "Date de fin de validité mensuelle", example = "2026-12-11")
    private LocalDate endDate;

    @Schema(description = "Nombre de sessions live restantes sur la période mensuelle", example = "4")
    private Integer remainingLives;

    @Schema(description = "Détails du pack souscrit")
    private PackResponse pack;

    @Schema(description = "Date de création")
    private LocalDateTime createdAt;

    @Schema(description = "Date de mise à jour")
    private LocalDateTime updatedAt;
}


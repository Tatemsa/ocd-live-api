package com.ocdlive.ocd.timeslot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Détails d'un créneau horaire dédié à un pays")
public class CountryTimeSlotResponse {

    @Schema(description = "Identifiant unique du créneau", example = "1")
    private Long id;

    @Schema(description = "Pays réservataire du créneau", example = "Cameroun")
    private String country;

    @Schema(description = "Jour de la semaine (null si tous les jours)", example = "MONDAY")
    private DayOfWeek dayOfWeek;

    @Schema(description = "Heure de début", example = "08:00:00")
    private LocalTime startTime;

    @Schema(description = "Heure de fin", example = "10:00:00")
    private LocalTime endTime;

    @Schema(description = "Indique si un abonnement mensuel est requis", example = "true")
    private boolean subscriptionRequired;

    @Schema(description = "Identifiant du pack associé (si applicable)", example = "1")
    private Long packId;

    @Schema(description = "Nom du pack associé (si applicable)", example = "Pack Mensuel Cameroun")
    private String packName;

    @Schema(description = "Indique si le créneau est actif", example = "true")
    private boolean active;

    @Schema(description = "Date de création")
    private LocalDateTime createdAt;

    @Schema(description = "Date de modification")
    private LocalDateTime updatedAt;
}


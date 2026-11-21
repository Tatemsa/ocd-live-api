package com.ocdlive.ocd.timeslot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Données pour créer ou modifier un créneau horaire dédié à un pays")
public class CountryTimeSlotRequest {

    @NotBlank(message = "Le pays est obligatoire")
    @Schema(description = "Pays réservataire du créneau", example = "Cameroun")
    private String country;

    @Schema(description = "Jour de la semaine (null si le créneau s'applique tous les jours)", example = "MONDAY")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "L'heure de début est obligatoire")
    @Schema(description = "Heure de début du créneau", example = "08:00:00")
    private LocalTime startTime;

    @NotNull(message = "L'heure de fin est obligatoire")
    @Schema(description = "Heure de fin du créneau", example = "10:00:00")
    private LocalTime endTime;

    @Builder.Default
    @Schema(description = "Nécessite un pack à abonnement mensuel", example = "true")
    private Boolean subscriptionRequired = true;

    @Schema(description = "Identifiant du pack spécifique associé (optionnel)", example = "1")
    private Long packId;

    @Builder.Default
    @Schema(description = "Statut actif du créneau", example = "true")
    private Boolean active = true;
}


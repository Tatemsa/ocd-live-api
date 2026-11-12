package com.ocdlive.ocd.pack.dto;

import com.ocdlive.ocd.pack.model.PackBillingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Données pour la création ou la modification d'un pack de live")
public class PackRequest {

    @NotBlank(message = "Le nom du pack est obligatoire")
    @Schema(description = "Nom du pack", example = "Pack Live Premium")
    private String name;

    @NotNull(message = "Le prix du pack est obligatoire")
    @Positive(message = "Le prix doit être strictement positif")
    @Schema(description = "Prix du pack en devise locale (ex: FCFA, EUR...)", example = "150000.00")
    private BigDecimal price;

    @NotNull(message = "La durée du rendez-vous est obligatoire")
    @Min(value = 1, message = "La durée doit être d'au moins 1 minute")
    @Schema(description = "Durée estimée du rendez-vous en minutes", example = "90")
    private Integer durationMinutes;

    @Schema(description = "Description détaillée du pack et de ses avantages", example = "Pack complet incluant diffusion multi-caméras et régie mobile")
    private String description;

    @Builder.Default
    @Schema(description = "Type de facturation : ONE_TIME (unitaire) ou MONTHLY_SUBSCRIPTION (abonnement mensuel)", example = "ONE_TIME")
    private PackBillingType billingType = PackBillingType.ONE_TIME;

    @Schema(description = "Pays cible si le pack est dédié à un pays spécifique (ex: Cameroun)", example = "Cameroun")
    private String targetCountry;

    @Min(value = 1, message = "Le quota mensuel de lives doit être d'au moins 1")
    @Schema(description = "Nombre de sessions live incluses par mois (pour un abonnement mensuel)", example = "4")
    private Integer monthlyLiveQuota;

    @Schema(description = "Identifiants des équipements à associer à ce pack", example = "[1, 2, 4]")
    private List<Long> equipmentIds;

    @Builder.Default
    @Schema(description = "Statut d'activation du pack", example = "true")
    private Boolean active = true;
}

package com.ocdlive.ocd.pack.dto;

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

    @Schema(description = "Description détaillée du pack", example = "Pack complet incluant diffusion multi-caméras et régie mobile")
    private String description;

    @Schema(description = "Identifiants des équipements à associer à ce pack", example = "[1, 2, 4]")
    private List<Long> equipmentIds;

    @Builder.Default
    @Schema(description = "Statut d'activation du pack", example = "true")
    private Boolean active = true;
}

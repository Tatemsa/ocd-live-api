package com.ocdlive.ocd.pack.dto;

import com.ocdlive.ocd.equipment.dto.EquipmentResponse;
import com.ocdlive.ocd.pack.model.Pack;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Détails d'un pack de rendez-vous de live")
public class PackResponse {

    @Schema(description = "Identifiant unique du pack", example = "1")
    private Long id;

    @Schema(description = "Nom du pack", example = "Pack Live Premium")
    private String name;

    @Schema(description = "Prix du pack", example = "150000.00")
    private BigDecimal price;

    @Schema(description = "Durée du rendez-vous en minutes", example = "90")
    private Integer durationMinutes;

    @Schema(description = "Description du pack")
    private String description;

    @Schema(description = "Liste détaillée des équipements inclus")
    private List<EquipmentResponse> equipments;

    @Schema(description = "Indique si le pack est disponible à la réservation", example = "true")
    private boolean active;

    @Schema(description = "Nom de l'administrateur créateur", example = "Super Admin")
    private String createdBy;

    @Schema(description = "Date de création")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière modification")
    private LocalDateTime updatedAt;
}

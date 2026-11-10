package com.ocdlive.ocd.equipment.dto;

import com.ocdlive.ocd.equipment.model.Equipment;
import com.ocdlive.ocd.equipment.model.EquipmentCategory;
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
@Schema(description = "Détails d'un équipement")
public class EquipmentResponse {

    @Schema(description = "Identifiant unique de l'équipement", example = "1")
    private Long id;

    @Schema(description = "Nom de l'équipement", example = "Caméra Sony FX3")
    private String name;

    @Schema(description = "Catégorie", example = "VIDEO")
    private EquipmentCategory category;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Quantité en stock", example = "3")
    private Integer quantity;

    @Schema(description = "Disponible à l'attribution", example = "true")
    private boolean available;

    @Schema(description = "Date d'ajout")
    private LocalDateTime createdAt;

    @Schema(description = "Date de mise à jour")
    private LocalDateTime updatedAt;
}


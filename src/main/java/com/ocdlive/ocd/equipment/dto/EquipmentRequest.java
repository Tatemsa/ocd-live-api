package com.ocdlive.ocd.equipment.dto;

import com.ocdlive.ocd.equipment.model.EquipmentCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Données pour la création ou modification d'un équipement")
public class EquipmentRequest {

    @NotBlank(message = "Le nom de l'équipement est obligatoire")
    @Schema(description = "Nom de l'équipement", example = "Caméra Sony FX3")
    private String name;

    @NotNull(message = "La catégorie est obligatoire")
    @Schema(description = "Catégorie de l'équipement (VIDEO, AUDIO, LIGHTING, STREAMING, ACCESSORY, OTHER)", example = "VIDEO")
    private EquipmentCategory category;

    @Schema(description = "Description technique ou détails", example = "Caméra cinéma plein format 4K 120fps")
    private String description;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité minimale est 1")
    @Schema(description = "Nombre d'unités disponibles en stock", example = "3")
    private Integer quantity;

    @Builder.Default
    @Schema(description = "Disponibilité de l'équipement", example = "true")
    private Boolean available = true;
}


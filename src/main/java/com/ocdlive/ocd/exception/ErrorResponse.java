package com.ocdlive.ocd.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Format standardisé des erreurs retournées par l'API")
public class ErrorResponse {

    @Schema(description = "Horodatage de l'erreur au format ISO-8601", example = "2026-11-10T08:50:00Z")
    @Builder.Default
    private Instant timestamp = Instant.now();

    @Schema(description = "Code de statut HTTP", example = "404")
    private int status;

    @Schema(description = "Libellé de l'erreur HTTP", example = "Not Found")
    private String error;

    @Schema(description = "Code d'erreur métier destiné au frontend", example = "RESOURCE_NOT_FOUND")
    private String errorCode;

    @Schema(description = "Message explicatif de l'erreur", example = "Pack non trouvé avec l'identifiant : 42")
    private String message;

    @Schema(description = "Chemin URI de la requête", example = "/api/packs/42")
    private String path;

    @Schema(description = "Détails des erreurs de validation par champ (uniquement si validation échouée)")
    private Map<String, String> validationErrors;
}


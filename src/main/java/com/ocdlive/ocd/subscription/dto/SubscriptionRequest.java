package com.ocdlive.ocd.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Données nécessaires pour souscrire à un pack à abonnement mensuel")
public class SubscriptionRequest {

    @NotNull(message = "L'identifiant du pack est obligatoire")
    @Schema(description = "Identifiant du pack mensuel choisi", example = "1")
    private Long packId;

    @NotBlank(message = "Le pays est obligatoire")
    @Schema(description = "Pays du client pour l'abonnement", example = "Cameroun")
    private String country;
}


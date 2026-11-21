package com.ocdlive.ocd.payment.dto;

import com.ocdlive.ocd.payment.model.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Données pour initier le paiement d'une réservation ou d'un abonnement mensuel")
public class PaymentInitiateRequest {

    @Schema(description = "Identifiant de la réservation à payer (exclusif avec subscriptionId)", example = "1")
    private Long reservationId;

    @Schema(description = "Identifiant de l'abonnement mensuel à payer (exclusif avec reservationId)", example = "1")
    private Long subscriptionId;

    @NotNull(message = "Le moyen de paiement est obligatoire")
    @Schema(description = "Moyen de paiement (MOBILE_MONEY, CARD, BANK_TRANSFER)", example = "MOBILE_MONEY")
    private PaymentMethod method;

    @Builder.Default
    @Schema(description = "Devise du paiement", example = "XOF")
    private String currency = "XOF";

    @Schema(description = "Numéro Mobile Money ou email du payeur", example = "+22890000000")
    private String payerContact;
}


package com.ocdlive.ocd.payment.dto;

import com.ocdlive.ocd.payment.model.PaymentMethod;
import com.ocdlive.ocd.payment.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Détails d'une transaction de paiement")
public class PaymentResponse {

    @Schema(description = "Identifiant unique du paiement", example = "1")
    private Long id;

    @Schema(description = "Référence unique de la transaction", example = "OCD-PAY-8F9A2B1C")
    private String transactionReference;

    @Schema(description = "Montant de la transaction", example = "150000.00")
    private BigDecimal amount;

    @Schema(description = "Devise", example = "XOF")
    private String currency;

    @Schema(description = "Moyen de paiement", example = "MOBILE_MONEY")
    private PaymentMethod method;

    @Schema(description = "Statut du paiement (PENDING, SUCCESS, FAILED, REFUNDED)", example = "SUCCESS")
    private PaymentStatus status;

    @Schema(description = "Contact du payeur", example = "+22890000000")
    private String payerContact;

    @Schema(description = "Identifiant de la réservation associée (si paiement de réservation)", example = "10")
    private Long reservationId;

    @Schema(description = "Identifiant de l'abonnement associé (si paiement d'abonnement)", example = "5")
    private Long subscriptionId;

    @Schema(description = "Date de validation du paiement")
    private LocalDateTime paidAt;

    @Schema(description = "Date de création")
    private LocalDateTime createdAt;
}


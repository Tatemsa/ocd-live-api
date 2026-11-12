package com.ocdlive.ocd.payment.dto;

import com.ocdlive.ocd.payment.model.PaymentStatus;
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
@Schema(description = "Notification de confirmation ou d'échec de paiement (Webhook passerelle)")
public class PaymentWebhookRequest {

    @NotBlank(message = "La référence de transaction est obligatoire")
    @Schema(description = "Référence unique de la transaction", example = "OCD-PAY-12345678")
    private String transactionReference;

    @NotNull(message = "Le statut du paiement est obligatoire")
    @Schema(description = "Nouveau statut du paiement (SUCCESS, FAILED, REFUNDED)", example = "SUCCESS")
    private PaymentStatus status;
}


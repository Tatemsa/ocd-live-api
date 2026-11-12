package com.ocdlive.ocd.payment.controller;

import com.ocdlive.ocd.payment.dto.PaymentInitiateRequest;
import com.ocdlive.ocd.payment.dto.PaymentResponse;
import com.ocdlive.ocd.payment.dto.PaymentWebhookRequest;
import com.ocdlive.ocd.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Paiements", description = "Initiation et confirmation par paiement des réservations et abonnements mensuels")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    @Operation(summary = "Initier un paiement", description = "Crée une transaction de paiement pour une réservation ou un abonnement mensuel")
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentInitiateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiatePayment(request));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Webhook de confirmation de paiement", description = "Confirme automatiquement la réservation ou active l'abonnement mensuel lorsque le paiement réussit")
    public ResponseEntity<PaymentResponse> handleWebhook(@Valid @RequestBody PaymentWebhookRequest request) {
        return ResponseEntity.ok(paymentService.processWebhook(request));
    }

    @GetMapping("/reference/{reference}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Consulter un paiement par sa référence de transaction")
    public ResponseEntity<PaymentResponse> getByReference(@PathVariable String reference) {
        return ResponseEntity.ok(paymentService.getPaymentByReference(reference));
    }

    @GetMapping("/reservation/{reservationId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Lister les paiements d'une réservation")
    public ResponseEntity<List<PaymentResponse>> getByReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentService.getPaymentsByReservation(reservationId));
    }

    @GetMapping("/subscription/{subscriptionId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Lister les paiements d'un abonnement")
    public ResponseEntity<List<PaymentResponse>> getBySubscription(@PathVariable Long subscriptionId) {
        return ResponseEntity.ok(paymentService.getPaymentsBySubscription(subscriptionId));
    }
}


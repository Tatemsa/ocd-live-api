package com.ocdlive.ocd.payment.controller;

import com.ocdlive.ocd.payment.dto.PaymentInitiateRequest;
import com.ocdlive.ocd.payment.dto.PaymentResponse;
import com.ocdlive.ocd.payment.dto.PaymentWebhookRequest;
import com.ocdlive.ocd.payment.model.PaymentMethod;
import com.ocdlive.ocd.payment.model.PaymentStatus;
import com.ocdlive.ocd.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - PaymentController")
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    @DisplayName("POST /payments/initiate - Devrait initier un paiement et retourner 201 Created")
    void shouldInitiatePayment() {
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .reservationId(50L)
                .method(PaymentMethod.MOBILE_MONEY)
                .currency("XOF")
                .build();

        PaymentResponse expectedResponse = PaymentResponse.builder()
                .id(1L)
                .transactionReference("OCD-PAY-12345678")
                .amount(new BigDecimal("150000.00"))
                .status(PaymentStatus.PENDING)
                .reservationId(50L)
                .build();

        given(paymentService.initiatePayment(request)).willReturn(expectedResponse);

        ResponseEntity<PaymentResponse> response = paymentController.initiatePayment(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("POST /payments/webhook - Devrait traiter le webhook de paiement et retourner 200 OK")
    void shouldHandleWebhook() {
        PaymentWebhookRequest webhookRequest = PaymentWebhookRequest.builder()
                .transactionReference("OCD-PAY-12345678")
                .status(PaymentStatus.SUCCESS)
                .build();

        PaymentResponse expectedResponse = PaymentResponse.builder()
                .id(1L)
                .transactionReference("OCD-PAY-12345678")
                .status(PaymentStatus.SUCCESS)
                .reservationId(50L)
                .build();

        given(paymentService.processWebhook(webhookRequest)).willReturn(expectedResponse);

        ResponseEntity<PaymentResponse> response = paymentController.handleWebhook(webhookRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }
}


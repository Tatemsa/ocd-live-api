package com.ocdlive.ocd.payment.service;

import com.ocdlive.ocd.exception.BadRequestException;
import com.ocdlive.ocd.exception.BusinessRuleException;
import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.payment.dto.PaymentInitiateRequest;
import com.ocdlive.ocd.payment.dto.PaymentResponse;
import com.ocdlive.ocd.payment.dto.PaymentWebhookRequest;
import com.ocdlive.ocd.payment.mapper.PaymentMapper;
import com.ocdlive.ocd.payment.model.Payment;
import com.ocdlive.ocd.payment.model.PaymentMethod;
import com.ocdlive.ocd.payment.model.PaymentStatus;
import com.ocdlive.ocd.payment.repository.PaymentRepository;
import com.ocdlive.ocd.reservation.model.Reservation;
import com.ocdlive.ocd.reservation.model.ReservationStatus;
import com.ocdlive.ocd.reservation.repository.ReservationRepository;
import com.ocdlive.ocd.subscription.model.Subscription;
import com.ocdlive.ocd.subscription.model.SubscriptionStatus;
import com.ocdlive.ocd.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - PaymentService")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private com.ocdlive.ocd.notification.service.EmailNotificationService emailNotificationService;

    @InjectMocks
    private PaymentService paymentService;

    private Pack samplePack;
    private Reservation sampleReservation;
    private Subscription sampleSubscription;

    @BeforeEach
    void setUp() {
        samplePack = Pack.builder()
                .id(10L)
                .name("Pack Live")
                .price(new BigDecimal("150000.00"))
                .monthlyLiveQuota(4)
                .build();

        sampleReservation = Reservation.builder()
                .id(50L)
                .pack(samplePack)
                .status(ReservationStatus.PENDING)
                .build();

        sampleSubscription = Subscription.builder()
                .id(60L)
                .pack(samplePack)
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .remainingLives(4)
                .build();
    }

    @Test
    @DisplayName("initiatePayment() - Devrait initier un paiement PENDING pour une réservation")
    void shouldInitiatePaymentForReservation() {
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .reservationId(50L)
                .method(PaymentMethod.MOBILE_MONEY)
                .currency("XOF")
                .payerContact("+22890000000")
                .build();

        Payment savedPayment = Payment.builder()
                .id(1L)
                .transactionReference("OCD-PAY-12345678")
                .amount(new BigDecimal("150000.00"))
                .status(PaymentStatus.PENDING)
                .reservation(sampleReservation)
                .build();

        PaymentResponse expectedResponse = PaymentResponse.builder()
                .id(1L)
                .transactionReference("OCD-PAY-12345678")
                .amount(new BigDecimal("150000.00"))
                .status(PaymentStatus.PENDING)
                .reservationId(50L)
                .build();

        given(reservationRepository.findById(50L)).willReturn(Optional.of(sampleReservation));
        given(paymentRepository.save(any(Payment.class))).willReturn(savedPayment);
        given(paymentMapper.toResponse(savedPayment)).willReturn(expectedResponse);

        PaymentResponse result = paymentService.initiatePayment(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getAmount()).isEqualByComparingTo("150000.00");
    }

    @Test
    @DisplayName("initiatePayment() - Devrait lever BadRequestException si ni reservationId ni subscriptionId n'est fourni")
    void shouldThrowBadRequestWhenNeitherReservationNorSubscriptionProvided() {
        PaymentInitiateRequest invalidRequest = PaymentInitiateRequest.builder()
                .method(PaymentMethod.CARD)
                .build();

        assertThatThrownBy(() -> paymentService.initiatePayment(invalidRequest))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("processWebhook() - Devrait confirmer automatiquement la réservation quand le paiement réussit")
    void shouldAutomaticallyConfirmReservationWhenPaymentSucceeds() {
        Payment pendingPayment = Payment.builder()
                .id(1L)
                .transactionReference("OCD-PAY-12345678")
                .status(PaymentStatus.PENDING)
                .reservation(sampleReservation)
                .build();

        PaymentWebhookRequest webhook = PaymentWebhookRequest.builder()
                .transactionReference("OCD-PAY-12345678")
                .status(PaymentStatus.SUCCESS)
                .build();

        PaymentResponse expectedResponse = PaymentResponse.builder()
                .id(1L)
                .transactionReference("OCD-PAY-12345678")
                .status(PaymentStatus.SUCCESS)
                .reservationId(50L)
                .build();

        given(paymentRepository.findByTransactionReference("OCD-PAY-12345678"))
                .willReturn(Optional.of(pendingPayment));
        given(paymentRepository.save(pendingPayment)).willReturn(pendingPayment);
        given(paymentMapper.toResponse(pendingPayment)).willReturn(expectedResponse);

        PaymentResponse result = paymentService.processWebhook(webhook);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(sampleReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(pendingPayment.getPaidAt()).isNotNull();
        verify(reservationRepository).save(sampleReservation);
        verify(emailNotificationService).sendReservationPaymentConfirmationEmails(pendingPayment, sampleReservation);
    }

    @Test
    @DisplayName("processWebhook() - Devrait activer automatiquement l'abonnement mensuel quand le paiement réussit")
    void shouldAutomaticallyActivateSubscriptionWhenPaymentSucceeds() {
        Payment pendingPayment = Payment.builder()
                .id(2L)
                .transactionReference("OCD-PAY-SUB999")
                .status(PaymentStatus.PENDING)
                .subscription(sampleSubscription)
                .build();

        PaymentWebhookRequest webhook = PaymentWebhookRequest.builder()
                .transactionReference("OCD-PAY-SUB999")
                .status(PaymentStatus.SUCCESS)
                .build();

        PaymentResponse expectedResponse = PaymentResponse.builder()
                .id(2L)
                .transactionReference("OCD-PAY-SUB999")
                .status(PaymentStatus.SUCCESS)
                .subscriptionId(60L)
                .build();

        given(paymentRepository.findByTransactionReference("OCD-PAY-SUB999"))
                .willReturn(Optional.of(pendingPayment));
        given(paymentRepository.save(pendingPayment)).willReturn(pendingPayment);
        given(paymentMapper.toResponse(pendingPayment)).willReturn(expectedResponse);

        PaymentResponse result = paymentService.processWebhook(webhook);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(sampleSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(sampleSubscription.getStartDate()).isNotNull();
        assertThat(sampleSubscription.getEndDate()).isEqualTo(sampleSubscription.getStartDate().plusMonths(1));
        verify(subscriptionRepository).save(sampleSubscription);
        verify(emailNotificationService).sendSubscriptionPaymentConfirmationEmails(pendingPayment, sampleSubscription);
    }

    @Test
    @DisplayName("processWebhook() - Devrait lever BusinessRuleException si le paiement a déjà été validé")
    void shouldThrowBusinessRuleExceptionWhenPaymentAlreadyProcessed() {
        Payment alreadyPaid = Payment.builder()
                .id(1L)
                .transactionReference("OCD-PAY-DONE")
                .status(PaymentStatus.SUCCESS)
                .build();

        given(paymentRepository.findByTransactionReference("OCD-PAY-DONE"))
                .willReturn(Optional.of(alreadyPaid));

        PaymentWebhookRequest webhook = PaymentWebhookRequest.builder()
                .transactionReference("OCD-PAY-DONE")
                .status(PaymentStatus.SUCCESS)
                .build();

        assertThatThrownBy(() -> paymentService.processWebhook(webhook))
                .isInstanceOf(BusinessRuleException.class);
    }
}


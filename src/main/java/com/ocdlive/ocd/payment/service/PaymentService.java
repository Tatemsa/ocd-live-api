package com.ocdlive.ocd.payment.service;

import com.ocdlive.ocd.exception.BadRequestException;
import com.ocdlive.ocd.exception.BusinessRuleException;
import com.ocdlive.ocd.exception.ResourceNotFoundException;
import com.ocdlive.ocd.notification.service.EmailNotificationService;
import com.ocdlive.ocd.payment.dto.PaymentInitiateRequest;
import com.ocdlive.ocd.payment.dto.PaymentResponse;
import com.ocdlive.ocd.payment.dto.PaymentWebhookRequest;
import com.ocdlive.ocd.payment.mapper.PaymentMapper;
import com.ocdlive.ocd.payment.model.Payment;
import com.ocdlive.ocd.payment.model.PaymentStatus;
import com.ocdlive.ocd.payment.repository.PaymentRepository;
import com.ocdlive.ocd.reservation.model.Reservation;
import com.ocdlive.ocd.reservation.model.ReservationStatus;
import com.ocdlive.ocd.reservation.repository.ReservationRepository;
import com.ocdlive.ocd.subscription.model.Subscription;
import com.ocdlive.ocd.subscription.model.SubscriptionStatus;
import com.ocdlive.ocd.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentMapper paymentMapper;
    private final EmailNotificationService emailNotificationService;

    @Transactional
    public PaymentResponse initiatePayment(PaymentInitiateRequest request) {
        boolean hasReservation = request.getReservationId() != null;
        boolean hasSubscription = request.getSubscriptionId() != null;

        if (hasReservation == hasSubscription) {
            throw new BadRequestException(
                    "Vous devez fournir soit un reservationId soit un subscriptionId (mais pas les deux).",
                    "INVALID_PAYMENT_TARGET"
            );
        }

        Reservation reservation = null;
        Subscription subscription = null;
        BigDecimal amount;

        if (hasReservation) {
            reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Réservation", "id", request.getReservationId()));

            if (reservation.getStatus() == ReservationStatus.CANCELLED) {
                throw new BusinessRuleException("Impossible de payer une réservation annulée.", "RESERVATION_CANCELLED");
            }
            if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                throw new BusinessRuleException("Cette réservation est déjà confirmée et payée.", "RESERVATION_ALREADY_CONFIRMED");
            }
            amount = reservation.getPack().getPrice();
        } else {
            subscription = subscriptionRepository.findById(request.getSubscriptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Abonnement", "id", request.getSubscriptionId()));

            if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                throw new BusinessRuleException("Cet abonnement est déjà actif.", "SUBSCRIPTION_ALREADY_ACTIVE");
            }
            amount = subscription.getPack().getPrice();
        }

        String txRef = "OCD-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
                .transactionReference(txRef)
                .amount(amount)
                .currency(request.getCurrency() != null ? request.getCurrency() : "XOF")
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .payerContact(request.getPayerContact())
                .reservation(reservation)
                .subscription(subscription)
                .build();

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse processWebhook(PaymentWebhookRequest webhookRequest) {
        Payment payment = paymentRepository.findByTransactionReference(webhookRequest.getTransactionReference())
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", "référence", webhookRequest.getTransactionReference()));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new BusinessRuleException("Ce paiement a déjà été validé.", "PAYMENT_ALREADY_PROCESSED");
        }

        payment.setStatus(webhookRequest.getStatus());

        if (webhookRequest.getStatus() == PaymentStatus.SUCCESS) {
            payment.setPaidAt(LocalDateTime.now());

            // Confirmation automatique de la réservation associée + envoi des emails au client et à l'admin
            if (payment.getReservation() != null) {
                Reservation reservation = payment.getReservation();
                reservation.setStatus(ReservationStatus.CONFIRMED);
                reservationRepository.save(reservation);
                if (emailNotificationService != null) {
                    emailNotificationService.sendReservationPaymentConfirmationEmails(payment, reservation);
                }
            }

            // Activation automatique de l'abonnement mensuel associé + envoi des emails au client et à l'admin
            if (payment.getSubscription() != null) {
                Subscription subscription = payment.getSubscription();
                LocalDate today = LocalDate.now();
                int quota = subscription.getPack().getMonthlyLiveQuota() != null
                        ? subscription.getPack().getMonthlyLiveQuota()
                        : 4;
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                subscription.setStartDate(today);
                subscription.setEndDate(today.plusMonths(1));
                subscription.setRemainingLives(quota);
                subscriptionRepository.save(subscription);
                if (emailNotificationService != null) {
                    emailNotificationService.sendSubscriptionPaymentConfirmationEmails(payment, subscription);
                }
            }
        }

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String transactionReference) {
        Payment payment = paymentRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", "référence", transactionReference));
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByReservation(Long reservationId) {
        return paymentMapper.toResponseList(paymentRepository.findByReservationId(reservationId));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsBySubscription(Long subscriptionId) {
        return paymentMapper.toResponseList(paymentRepository.findBySubscriptionId(subscriptionId));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentMapper.toResponseList(paymentRepository.findAll());
    }
}


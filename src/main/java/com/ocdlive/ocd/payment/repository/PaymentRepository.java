package com.ocdlive.ocd.payment.repository;

import com.ocdlive.ocd.payment.model.Payment;
import com.ocdlive.ocd.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionReference(String transactionReference);

    List<Payment> findByReservationId(Long reservationId);

    List<Payment> findBySubscriptionId(Long subscriptionId);

    List<Payment> findByStatus(PaymentStatus status);
}


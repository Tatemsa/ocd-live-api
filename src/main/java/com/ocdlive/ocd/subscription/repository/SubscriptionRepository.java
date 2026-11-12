package com.ocdlive.ocd.subscription.repository;

import com.ocdlive.ocd.subscription.model.Subscription;
import com.ocdlive.ocd.subscription.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByClientId(Long clientId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    Optional<Subscription> findFirstByClientIdAndPackIdAndStatusOrderByEndDateDesc(
            Long clientId,
            Long packId,
            SubscriptionStatus status
    );
}


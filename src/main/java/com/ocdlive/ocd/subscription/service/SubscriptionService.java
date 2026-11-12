package com.ocdlive.ocd.subscription.service;

import com.ocdlive.ocd.exception.BusinessRuleException;
import com.ocdlive.ocd.exception.ResourceNotFoundException;
import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.pack.model.PackBillingType;
import com.ocdlive.ocd.pack.repository.PackRepository;
import com.ocdlive.ocd.subscription.dto.SubscriptionRequest;
import com.ocdlive.ocd.subscription.dto.SubscriptionResponse;
import com.ocdlive.ocd.subscription.mapper.SubscriptionMapper;
import com.ocdlive.ocd.subscription.model.Subscription;
import com.ocdlive.ocd.subscription.model.SubscriptionStatus;
import com.ocdlive.ocd.subscription.repository.SubscriptionRepository;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PackRepository packRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional
    public SubscriptionResponse subscribe(SubscriptionRequest request, User client) {
        if (client == null) {
            throw new AccessDeniedException("Vous devez être authentifié pour souscrire à un abonnement mensuel.");
        }

        Pack pack = packRepository.findById(request.getPackId())
                .orElseThrow(() -> new ResourceNotFoundException("Pack", "id", request.getPackId()));

        if (!pack.isActive()) {
            throw new BusinessRuleException("Ce pack n'est actuellement pas disponible.", "PACK_NOT_ACTIVE");
        }

        if (pack.getBillingType() != PackBillingType.MONTHLY_SUBSCRIPTION) {
            throw new BusinessRuleException("Ce pack n'est pas un pack à abonnement mensuel.", "NOT_SUBSCRIPTION_PACK");
        }

        if (pack.getTargetCountry() != null && !pack.getTargetCountry().isBlank()
                && !pack.getTargetCountry().equalsIgnoreCase(request.getCountry())) {
            throw new BusinessRuleException(
                    "Ce pack d'abonnement est exclusivement réservé aux clients du pays : " + pack.getTargetCountry(),
                    "COUNTRY_MISMATCH"
            );
        }

        int initialQuota = pack.getMonthlyLiveQuota() != null ? pack.getMonthlyLiveQuota() : 4;

        Subscription subscription = Subscription.builder()
                .client(client)
                .pack(pack)
                .country(request.getCountry())
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .remainingLives(initialQuota)
                .build();

        return subscriptionMapper.toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional
    public SubscriptionResponse activateSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement", "id", subscriptionId));

        LocalDate now = LocalDate.now();
        int quota = subscription.getPack().getMonthlyLiveQuota() != null
                ? subscription.getPack().getMonthlyLiveQuota()
                : 4;

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(now);
        subscription.setEndDate(now.plusMonths(1));
        subscription.setRemainingLives(quota);

        return subscriptionMapper.toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(Long id, User currentUser) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement", "id", id));

        checkOwnerOrAdmin(subscription, currentUser);

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        return subscriptionMapper.toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getMySubscriptions(User client) {
        if (client == null) {
            throw new AccessDeniedException("Vous devez être authentifié.");
        }
        return subscriptionMapper.toResponseList(subscriptionRepository.findByClientId(client.getId()));
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(Long id, User currentUser) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement", "id", id));

        checkOwnerOrAdmin(subscription, currentUser);
        return subscriptionMapper.toResponse(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionMapper.toResponseList(subscriptionRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByStatus(SubscriptionStatus status) {
        return subscriptionMapper.toResponseList(subscriptionRepository.findByStatus(status));
    }

    private void checkOwnerOrAdmin(Subscription subscription, User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Utilisateur non authentifié.");
        }
        boolean isOwner = subscription.getClient() != null && subscription.getClient().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Accès non autorisé à cet abonnement.");
        }
    }
}


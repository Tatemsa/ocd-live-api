package com.ocdlive.ocd.subscription.service;

import com.ocdlive.ocd.exception.BusinessRuleException;
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
@DisplayName("Tests unitaires - SubscriptionService")
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PackRepository packRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User clientUser;
    private Pack monthlyPack;
    private Subscription sampleSubscription;
    private SubscriptionResponse sampleResponse;

    @BeforeEach
    void setUp() {
        clientUser = User.builder()
                .id(1L)
                .email("client@cameroun.cm")
                .role(Role.ROLE_USER)
                .build();

        monthlyPack = Pack.builder()
                .id(10L)
                .name("Pack Mensuel Cameroun")
                .price(new BigDecimal("300000.00"))
                .durationMinutes(60)
                .billingType(PackBillingType.MONTHLY_SUBSCRIPTION)
                .targetCountry("Cameroun")
                .monthlyLiveQuota(4)
                .active(true)
                .build();

        sampleSubscription = Subscription.builder()
                .id(100L)
                .client(clientUser)
                .pack(monthlyPack)
                .country("Cameroun")
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .remainingLives(4)
                .build();

        sampleResponse = SubscriptionResponse.builder()
                .id(100L)
                .clientId(1L)
                .country("Cameroun")
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .remainingLives(4)
                .build();
    }

    @Test
    @DisplayName("subscribe() - Devrait créer un abonnement en attente de paiement pour un pack mensuel")
    void shouldCreatePendingSubscriptionForMonthlyPack() {
        SubscriptionRequest request = SubscriptionRequest.builder()
                .packId(10L)
                .country("Cameroun")
                .build();

        given(packRepository.findById(10L)).willReturn(Optional.of(monthlyPack));
        given(subscriptionRepository.save(any(Subscription.class))).willReturn(sampleSubscription);
        given(subscriptionMapper.toResponse(sampleSubscription)).willReturn(sampleResponse);

        SubscriptionResponse result = subscriptionService.subscribe(request, clientUser);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.PENDING_PAYMENT);
        assertThat(result.getRemainingLives()).isEqualTo(4);
    }

    @Test
    @DisplayName("subscribe() - Devrait lever BusinessRuleException si le pack est unitaire (ONE_TIME)")
    void shouldThrowBusinessRuleExceptionWhenSubscribingToOneTimePack() {
        monthlyPack.setBillingType(PackBillingType.ONE_TIME);
        SubscriptionRequest request = SubscriptionRequest.builder()
                .packId(10L)
                .country("Cameroun")
                .build();

        given(packRepository.findById(10L)).willReturn(Optional.of(monthlyPack));

        assertThatThrownBy(() -> subscriptionService.subscribe(request, clientUser))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("abonnement mensuel");
    }

    @Test
    @DisplayName("activateSubscription() - Devrait activer l'abonnement pour 1 mois et initialiser le quota")
    void shouldActivateSubscriptionForOneMonth() {
        given(subscriptionRepository.findById(100L)).willReturn(Optional.of(sampleSubscription));
        given(subscriptionRepository.save(sampleSubscription)).willReturn(sampleSubscription);
        sampleResponse.setStatus(SubscriptionStatus.ACTIVE);
        given(subscriptionMapper.toResponse(sampleSubscription)).willReturn(sampleResponse);

        SubscriptionResponse result = subscriptionService.activateSubscription(100L);

        assertThat(sampleSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(sampleSubscription.getStartDate()).isNotNull();
        assertThat(sampleSubscription.getEndDate()).isEqualTo(sampleSubscription.getStartDate().plusMonths(1));
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        verify(subscriptionRepository).save(sampleSubscription);
    }
}


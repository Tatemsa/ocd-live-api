package com.ocdlive.ocd.subscription.controller;

import com.ocdlive.ocd.subscription.dto.SubscriptionRequest;
import com.ocdlive.ocd.subscription.dto.SubscriptionResponse;
import com.ocdlive.ocd.subscription.model.SubscriptionStatus;
import com.ocdlive.ocd.subscription.service.SubscriptionService;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - SubscriptionController")
class SubscriptionControllerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionController subscriptionController;

    private User currentUser;
    private SubscriptionResponse sampleResponse;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L)
                .email("client@cameroun.cm")
                .role(Role.ROLE_USER)
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
    @DisplayName("POST /subscriptions - Devrait créer un abonnement et retourner 201 Created")
    void shouldSubscribe() {
        SubscriptionRequest request = SubscriptionRequest.builder()
                .packId(10L)
                .country("Cameroun")
                .build();

        given(subscriptionService.subscribe(request, currentUser)).willReturn(sampleResponse);

        ResponseEntity<SubscriptionResponse> response = subscriptionController.subscribe(request, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("GET /subscriptions/my-subscriptions - Devrait lister les abonnements du client")
    void shouldGetMySubscriptions() {
        given(subscriptionService.getMySubscriptions(currentUser)).willReturn(List.of(sampleResponse));

        ResponseEntity<List<SubscriptionResponse>> response = subscriptionController.getMySubscriptions(currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }
}


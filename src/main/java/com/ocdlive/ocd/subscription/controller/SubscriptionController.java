package com.ocdlive.ocd.subscription.controller;

import com.ocdlive.ocd.subscription.dto.SubscriptionRequest;
import com.ocdlive.ocd.subscription.dto.SubscriptionResponse;
import com.ocdlive.ocd.subscription.service.SubscriptionService;
import com.ocdlive.ocd.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Abonnements Mensuels", description = "Souscription et suivi des packs à abonnement mensuel")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "Souscrire à un pack mensuel", description = "Crée un abonnement mensuel en attente de paiement")
    public ResponseEntity<SubscriptionResponse> subscribe(
            @Valid @RequestBody SubscriptionRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.subscribe(request, currentUser));
    }

    @GetMapping("/my-subscriptions")
    @Operation(summary = "Lister mes abonnements mensuels")
    public ResponseEntity<List<SubscriptionResponse>> getMySubscriptions(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(subscriptionService.getMySubscriptions(currentUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un abonnement par son ID")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id, currentUser));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Résilier un abonnement mensuel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(id, currentUser));
    }
}


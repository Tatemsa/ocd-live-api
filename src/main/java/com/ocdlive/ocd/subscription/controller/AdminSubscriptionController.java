package com.ocdlive.ocd.subscription.controller;

import com.ocdlive.ocd.subscription.dto.SubscriptionResponse;
import com.ocdlive.ocd.subscription.model.SubscriptionStatus;
import com.ocdlive.ocd.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Administration - Abonnements", description = "Supervision et activation des abonnements mensuels")
public class AdminSubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    @Operation(summary = "Lister tous les abonnements")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Filtrer les abonnements par statut")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptionsByStatus(@PathVariable SubscriptionStatus status) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByStatus(status));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activer manuellement un abonnement mensuel")
    public ResponseEntity<SubscriptionResponse> activateSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.activateSubscription(id));
    }
}


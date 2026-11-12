package com.ocdlive.ocd.pack.controller;

import com.ocdlive.ocd.pack.dto.PackRequest;
import com.ocdlive.ocd.pack.dto.PackResponse;
import com.ocdlive.ocd.pack.service.PackService;
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
@RequestMapping("/packs")
@RequiredArgsConstructor
@Tag(name = "Packs", description = "Endpoints de gestion et consultation des packs de live")
public class PackController {

    private final PackService packService;

    @GetMapping
    @Operation(summary = "Lister les packs actifs", description = "Retourne la liste des packs disponibles à la réservation")
    public ResponseEntity<List<PackResponse>> getActivePacks() {
        return ResponseEntity.ok(packService.getActivePacks());
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "Lister les packs à abonnement mensuel", description = "Retourne les packs particuliers nécessitant un abonnement mensuel")
    public ResponseEntity<List<PackResponse>> getSubscriptionPacks() {
        return ResponseEntity.ok(packService.getSubscriptionPacks());
    }

    @GetMapping("/country/{country}")
    @Operation(summary = "Lister les packs dédiés à un pays", description = "Retourne les packs actifs ciblant un pays spécifique (ex: Cameroun)")
    public ResponseEntity<List<PackResponse>> getPacksByCountry(@PathVariable String country) {
        return ResponseEntity.ok(packService.getPacksByCountry(country));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter les détails d'un pack disponible")
    public ResponseEntity<PackResponse> getPackById(@PathVariable Long id) {
        return ResponseEntity.ok(packService.getPackById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Lister les packs créés par un utilisateur")
    public ResponseEntity<List<PackResponse>> getPacksByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(packService.getPacksByUser(userId));
    }

    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Créer un pack", description = "Permet à l'utilisateur authentifié de créer son pack")
    public ResponseEntity<PackResponse> createPack(
            @Valid @RequestBody PackRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        PackResponse created = packService.createPack(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/my-packs")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Lister mes packs", description = "Retourne les packs créés par l'utilisateur connecté")
    public ResponseEntity<List<PackResponse>> getMyPacks(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(packService.getMyPacks(currentUser));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Modifier mon pack", description = "Permet au créateur de modifier son pack")
    public ResponseEntity<PackResponse> updatePack(
            @PathVariable Long id,
            @Valid @RequestBody PackRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(packService.updatePack(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Supprimer mon pack", description = "Permet au créateur de supprimer son pack")
    public ResponseEntity<Void> deletePack(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        packService.deletePack(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}

package com.ocdlive.ocd.pack.controller;

import com.ocdlive.ocd.pack.dto.PackRequest;
import com.ocdlive.ocd.pack.dto.PackResponse;
import com.ocdlive.ocd.pack.service.PackService;
import com.ocdlive.ocd.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/packs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Administration - Packs", description = "Endpoints réservés aux administrateurs pour la gestion des packs de live")
public class AdminPackController {

    private final PackService packService;

    @PostMapping
    @Operation(summary = "Créer un nouveau pack", description = "Permet à l'administrateur de créer un pack avec ses tarifs, durée et équipements")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pack créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou nom déjà existant"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis")
    })
    public ResponseEntity<PackResponse> createPack(
            @Valid @RequestBody PackRequest request,
            @AuthenticationPrincipal User adminUser
    ) {
        PackResponse created = packService.createPack(request, adminUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Lister tous les packs", description = "Retourne la totalité des packs (actifs et inactifs)")
    public ResponseEntity<List<PackResponse>> getAllPacks() {
        return ResponseEntity.ok(packService.getAllPacks());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un pack par son ID")
    public ResponseEntity<PackResponse> getPackById(@PathVariable Long id) {
        return ResponseEntity.ok(packService.getPackById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un pack existant")
    public ResponseEntity<PackResponse> updatePack(
            @PathVariable Long id,
            @Valid @RequestBody PackRequest request,
            @AuthenticationPrincipal User adminUser
    ) {
        return ResponseEntity.ok(packService.updatePack(id, request, adminUser));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activer ou désactiver un pack")
    public ResponseEntity<PackResponse> toggleStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            @AuthenticationPrincipal User adminUser
    ) {
        return ResponseEntity.ok(packService.togglePackStatus(id, active, adminUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un pack")
    public ResponseEntity<Void> deletePack(
            @PathVariable Long id,
            @AuthenticationPrincipal User adminUser
    ) {
        packService.deletePack(id, adminUser);
        return ResponseEntity.noContent().build();
    }
}

package com.ocdlive.ocd.equipment.controller;

import com.ocdlive.ocd.equipment.dto.EquipmentRequest;
import com.ocdlive.ocd.equipment.dto.EquipmentResponse;
import com.ocdlive.ocd.equipment.model.EquipmentCategory;
import com.ocdlive.ocd.equipment.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/equipments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Administration - Équipements", description = "Endpoints réservés aux administrateurs pour la gestion du parc d'équipements")
public class AdminEquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    @Operation(summary = "Créer un équipement", description = "Ajoute un nouvel équipement au catalogue")
    public ResponseEntity<EquipmentResponse> createEquipment(@Valid @RequestBody EquipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.createEquipment(request));
    }

    @GetMapping
    @Operation(summary = "Lister tous les équipements")
    public ResponseEntity<List<EquipmentResponse>> getAllEquipments() {
        return ResponseEntity.ok(equipmentService.getAllEquipments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un équipement par son ID")
    public ResponseEntity<EquipmentResponse> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Filtrer les équipements par catégorie")
    public ResponseEntity<List<EquipmentResponse>> getByCategory(@PathVariable EquipmentCategory category) {
        return ResponseEntity.ok(equipmentService.getEquipmentsByCategory(category));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un équipement")
    public ResponseEntity<EquipmentResponse> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentRequest request
    ) {
        return ResponseEntity.ok(equipmentService.updateEquipment(id, request));
    }

    @PatchMapping("/{id}/availability")
    @Operation(summary = "Changer la disponibilité d'un équipement")
    public ResponseEntity<EquipmentResponse> toggleAvailability(
            @PathVariable Long id,
            @RequestParam boolean available
    ) {
        return ResponseEntity.ok(equipmentService.toggleAvailability(id, available));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un équipement")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
}


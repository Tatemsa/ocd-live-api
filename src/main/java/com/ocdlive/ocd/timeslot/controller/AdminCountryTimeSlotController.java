package com.ocdlive.ocd.timeslot.controller;

import com.ocdlive.ocd.timeslot.dto.CountryTimeSlotRequest;
import com.ocdlive.ocd.timeslot.dto.CountryTimeSlotResponse;
import com.ocdlive.ocd.timeslot.service.CountryTimeSlotService;
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
@RequestMapping("/admin/timeslots")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Administration - Créneaux Pays", description = "Gestion des créneaux horaires dédiés par pays")
public class AdminCountryTimeSlotController {

    private final CountryTimeSlotService countryTimeSlotService;

    @PostMapping
    @Operation(summary = "Créer un créneau horaire dédié à un pays")
    public ResponseEntity<CountryTimeSlotResponse> createTimeSlot(@Valid @RequestBody CountryTimeSlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(countryTimeSlotService.createTimeSlot(request));
    }

    @GetMapping
    @Operation(summary = "Lister tous les créneaux horaires (actifs et inactifs)")
    public ResponseEntity<List<CountryTimeSlotResponse>> getAllTimeSlots() {
        return ResponseEntity.ok(countryTimeSlotService.getAllTimeSlots());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un créneau horaire")
    public ResponseEntity<CountryTimeSlotResponse> updateTimeSlot(
            @PathVariable Long id,
            @Valid @RequestBody CountryTimeSlotRequest request
    ) {
        return ResponseEntity.ok(countryTimeSlotService.updateTimeSlot(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un créneau horaire")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable Long id) {
        countryTimeSlotService.deleteTimeSlot(id);
        return ResponseEntity.noContent().build();
    }
}


package com.ocdlive.ocd.timeslot.controller;

import com.ocdlive.ocd.timeslot.dto.CountryTimeSlotResponse;
import com.ocdlive.ocd.timeslot.service.CountryTimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/timeslots")
@RequiredArgsConstructor
@Tag(name = "Créneaux Pays", description = "Consultation des créneaux horaires réservés par pays (ex: 08h-10h Cameroun)")
public class CountryTimeSlotController {

    private final CountryTimeSlotService countryTimeSlotService;

    @GetMapping
    @Operation(summary = "Lister les créneaux horaires actifs dédiés par pays")
    public ResponseEntity<List<CountryTimeSlotResponse>> getActiveTimeSlots() {
        return ResponseEntity.ok(countryTimeSlotService.getActiveTimeSlots());
    }

    @GetMapping("/country/{country}")
    @Operation(summary = "Lister les créneaux dédiés à un pays spécifique")
    public ResponseEntity<List<CountryTimeSlotResponse>> getTimeSlotsByCountry(@PathVariable String country) {
        return ResponseEntity.ok(countryTimeSlotService.getTimeSlotsByCountry(country));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un créneau horaire par son ID")
    public ResponseEntity<CountryTimeSlotResponse> getTimeSlotById(@PathVariable Long id) {
        return ResponseEntity.ok(countryTimeSlotService.getTimeSlotById(id));
    }
}


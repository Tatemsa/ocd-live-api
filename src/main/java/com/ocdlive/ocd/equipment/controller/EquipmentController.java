package com.ocdlive.ocd.equipment.controller;

import com.ocdlive.ocd.equipment.dto.EquipmentResponse;
import com.ocdlive.ocd.equipment.model.EquipmentCategory;
import com.ocdlive.ocd.equipment.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/equipments")
@RequiredArgsConstructor
@Tag(name = "Équipements", description = "Endpoints de consultation du catalogue d'équipements")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    @Operation(summary = "Lister les équipements disponibles")
    public ResponseEntity<List<EquipmentResponse>> getAvailableEquipments() {
        return ResponseEntity.ok(equipmentService.getAvailableEquipments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un équipement par son ID")
    public ResponseEntity<EquipmentResponse> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Lister les équipements disponibles par catégorie")
    public ResponseEntity<List<EquipmentResponse>> getByCategory(@PathVariable EquipmentCategory category) {
        return ResponseEntity.ok(equipmentService.getEquipmentsByCategory(category));
    }
}


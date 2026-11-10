package com.ocdlive.ocd.equipment.service;

import com.ocdlive.ocd.equipment.dto.EquipmentRequest;
import com.ocdlive.ocd.equipment.dto.EquipmentResponse;
import com.ocdlive.ocd.equipment.mapper.EquipmentMapper;
import com.ocdlive.ocd.equipment.model.Equipment;
import com.ocdlive.ocd.equipment.model.EquipmentCategory;
import com.ocdlive.ocd.equipment.repository.EquipmentRepository;
import com.ocdlive.ocd.exception.ResourceAlreadyExistsException;
import com.ocdlive.ocd.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentMapper equipmentMapper;

    @Transactional
    public EquipmentResponse createEquipment(EquipmentRequest request) {
        if (equipmentRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ResourceAlreadyExistsException("Équipement", "nom", request.getName());
        }

        Equipment equipment = equipmentMapper.toEntity(request);
        return equipmentMapper.toResponse(equipmentRepository.save(equipment));
    }

    @Transactional
    public EquipmentResponse updateEquipment(Long id, EquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Équipement", "id", id));

        if (!equipment.getName().equalsIgnoreCase(request.getName()) && equipmentRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ResourceAlreadyExistsException("Équipement", "nom", request.getName());
        }

        equipmentMapper.updateEntityFromRequest(request, equipment);
        return equipmentMapper.toResponse(equipmentRepository.save(equipment));
    }

    @Transactional
    public EquipmentResponse toggleAvailability(Long id, boolean available) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Équipement", "id", id));

        equipment.setAvailable(available);
        return equipmentMapper.toResponse(equipmentRepository.save(equipment));
    }

    @Transactional
    public void deleteEquipment(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Équipement", "id", id);
        }
        equipmentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getAllEquipments() {
        return equipmentMapper.toResponseList(equipmentRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getAvailableEquipments() {
        return equipmentMapper.toResponseList(equipmentRepository.findByAvailableTrue());
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getEquipmentsByCategory(EquipmentCategory category) {
        return equipmentMapper.toResponseList(equipmentRepository.findByCategory(category));
    }

    @Transactional(readOnly = true)
    public EquipmentResponse getEquipmentById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Équipement", "id", id));
        return equipmentMapper.toResponse(equipment);
    }
}

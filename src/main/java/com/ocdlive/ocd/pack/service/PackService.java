package com.ocdlive.ocd.pack.service;

import com.ocdlive.ocd.equipment.model.Equipment;
import com.ocdlive.ocd.equipment.repository.EquipmentRepository;
import com.ocdlive.ocd.exception.BadRequestException;
import com.ocdlive.ocd.exception.ResourceAlreadyExistsException;
import com.ocdlive.ocd.exception.ResourceNotFoundException;
import com.ocdlive.ocd.pack.dto.PackRequest;
import com.ocdlive.ocd.pack.dto.PackResponse;
import com.ocdlive.ocd.pack.mapper.PackMapper;
import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.pack.model.PackBillingType;
import com.ocdlive.ocd.pack.repository.PackRepository;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PackService {

    private final PackRepository packRepository;
    private final EquipmentRepository equipmentRepository;
    private final PackMapper packMapper;

    @Transactional
    public PackResponse createPack(PackRequest request, User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Vous devez être authentifié pour créer un pack.");
        }

        if (packRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ResourceAlreadyExistsException("Pack", "nom", request.getName());
        }

        Pack pack = packMapper.toEntity(request);
        if (pack.getBillingType() == null) {
            pack.setBillingType(request.getBillingType() != null ? request.getBillingType() : PackBillingType.ONE_TIME);
        }
        if (pack.getBillingType() == PackBillingType.MONTHLY_SUBSCRIPTION && pack.getMonthlyLiveQuota() == null) {
            pack.setMonthlyLiveQuota(request.getMonthlyLiveQuota() != null ? request.getMonthlyLiveQuota() : 4);
        }
        pack.setCreatedBy(currentUser);
        pack.setEquipments(resolveEquipments(request.getEquipmentIds()));

        Pack saved = packRepository.save(pack);
        return packMapper.toResponse(saved);
    }

    @Transactional
    public PackResponse updatePack(Long id, PackRequest request, User currentUser) {
        Pack pack = packRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pack", "id", id));

        checkOwnershipOrAdmin(pack, currentUser);

        if (!pack.getName().equalsIgnoreCase(request.getName()) && packRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ResourceAlreadyExistsException("Pack", "nom", request.getName());
        }

        packMapper.updateEntityFromRequest(request, pack);

        if (request.getEquipmentIds() != null) {
            pack.setEquipments(resolveEquipments(request.getEquipmentIds()));
        }

        Pack updated = packRepository.save(pack);
        return packMapper.toResponse(updated);
    }

    @Transactional
    public PackResponse togglePackStatus(Long id, boolean active, User currentUser) {
        Pack pack = packRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pack", "id", id));

        checkOwnershipOrAdmin(pack, currentUser);

        pack.setActive(active);
        return packMapper.toResponse(packRepository.save(pack));
    }

    @Transactional
    public void deletePack(Long id, User currentUser) {
        Pack pack = packRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pack", "id", id));

        checkOwnershipOrAdmin(pack, currentUser);

        packRepository.delete(pack);
    }

    @Transactional(readOnly = true)
    public List<PackResponse> getMyPacks(User currentUser) {
        return packMapper.toResponseList(packRepository.findByCreatedById(currentUser.getId()));
    }

    @Transactional(readOnly = true)
    public List<PackResponse> getPacksByUser(Long userId) {
        return packMapper.toResponseList(packRepository.findByCreatedByIdAndActiveTrue(userId));
    }

    @Transactional(readOnly = true)
    public List<PackResponse> getActivePacks() {
        return packMapper.toResponseList(packRepository.findByActiveTrue());
    }

    @Transactional(readOnly = true)
    public List<PackResponse> getSubscriptionPacks() {
        return packMapper.toResponseList(packRepository.findByBillingTypeAndActiveTrue(PackBillingType.MONTHLY_SUBSCRIPTION));
    }

    @Transactional(readOnly = true)
    public List<PackResponse> getPacksByCountry(String country) {
        return packMapper.toResponseList(packRepository.findByTargetCountryIgnoreCaseAndActiveTrue(country));
    }

    @Transactional(readOnly = true)
    public List<PackResponse> getAllPacks() {
        return packMapper.toResponseList(packRepository.findAll());
    }

    @Transactional(readOnly = true)
    public PackResponse getPackById(Long id) {
        Pack pack = packRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pack", "id", id));
        return packMapper.toResponse(pack);
    }

    private void checkOwnershipOrAdmin(Pack pack, User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Utilisateur non authentifié.");
        }
        boolean isOwner = pack.getCreatedBy() != null && pack.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier ce pack.");
        }
    }

    private List<Equipment> resolveEquipments(List<Long> equipmentIds) {
        if (equipmentIds == null || equipmentIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Equipment> equipments = equipmentRepository.findAllById(equipmentIds);
        if (equipments.size() != equipmentIds.size()) {
            throw new BadRequestException("Certains identifiants d'équipements fournis sont introuvables.", "EQUIPMENT_NOT_FOUND", "error.pack.equipments_not_found");
        }
        return equipments;
    }
}

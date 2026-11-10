package com.ocdlive.ocd.equipment.repository;

import com.ocdlive.ocd.equipment.model.Equipment;
import com.ocdlive.ocd.equipment.model.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Equipment> findByAvailableTrue();

    List<Equipment> findByCategory(EquipmentCategory category);
}


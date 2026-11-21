package com.ocdlive.ocd.pack.repository;

import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.pack.model.PackBillingType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackRepository extends JpaRepository<Pack, Long> {

    @EntityGraph(attributePaths = {"equipments", "createdBy"})
    List<Pack> findByActiveTrue();

    boolean existsByNameIgnoreCase(String name);

    @EntityGraph(attributePaths = {"equipments", "createdBy"})
    List<Pack> findByCreatedById(Long userId);

    @EntityGraph(attributePaths = {"equipments", "createdBy"})
    List<Pack> findByCreatedByIdAndActiveTrue(Long userId);

    Optional<Pack> findByIdAndCreatedById(Long id, Long userId);

    @EntityGraph(attributePaths = {"equipments", "createdBy"})
    List<Pack> findByBillingTypeAndActiveTrue(PackBillingType billingType);

    @EntityGraph(attributePaths = {"equipments", "createdBy"})
    List<Pack> findByTargetCountryIgnoreCaseAndActiveTrue(String targetCountry);
}

package com.ocdlive.ocd.pack.repository;

import com.ocdlive.ocd.pack.model.Pack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackRepository extends JpaRepository<Pack, Long> {

    List<Pack> findByActiveTrue();

    boolean existsByNameIgnoreCase(String name);

    List<Pack> findByCreatedById(Long userId);

    List<Pack> findByCreatedByIdAndActiveTrue(Long userId);

    Optional<Pack> findByIdAndCreatedById(Long id, Long userId);
}

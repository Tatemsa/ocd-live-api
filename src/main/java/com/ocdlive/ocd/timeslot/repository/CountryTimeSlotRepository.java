package com.ocdlive.ocd.timeslot.repository;

import com.ocdlive.ocd.timeslot.model.CountryTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryTimeSlotRepository extends JpaRepository<CountryTimeSlot, Long> {

    List<CountryTimeSlot> findByActiveTrue();

    List<CountryTimeSlot> findByCountryIgnoreCaseAndActiveTrue(String country);
}


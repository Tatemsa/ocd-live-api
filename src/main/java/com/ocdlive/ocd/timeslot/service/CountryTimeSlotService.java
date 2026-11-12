package com.ocdlive.ocd.timeslot.service;

import com.ocdlive.ocd.exception.BadRequestException;
import com.ocdlive.ocd.exception.ResourceNotFoundException;
import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.pack.repository.PackRepository;
import com.ocdlive.ocd.timeslot.dto.CountryTimeSlotRequest;
import com.ocdlive.ocd.timeslot.dto.CountryTimeSlotResponse;
import com.ocdlive.ocd.timeslot.mapper.CountryTimeSlotMapper;
import com.ocdlive.ocd.timeslot.model.CountryTimeSlot;
import com.ocdlive.ocd.timeslot.repository.CountryTimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryTimeSlotService {

    private final CountryTimeSlotRepository countryTimeSlotRepository;
    private final PackRepository packRepository;
    private final CountryTimeSlotMapper countryTimeSlotMapper;

    @Transactional
    public CountryTimeSlotResponse createTimeSlot(CountryTimeSlotRequest request) {
        validateTimeRange(request);

        CountryTimeSlot slot = countryTimeSlotMapper.toEntity(request);
        if (request.getPackId() != null) {
            Pack pack = packRepository.findById(request.getPackId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pack", "id", request.getPackId()));
            slot.setPack(pack);
        }

        return countryTimeSlotMapper.toResponse(countryTimeSlotRepository.save(slot));
    }

    @Transactional
    public CountryTimeSlotResponse updateTimeSlot(Long id, CountryTimeSlotRequest request) {
        validateTimeRange(request);

        CountryTimeSlot slot = countryTimeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Créneau horaire", "id", id));

        countryTimeSlotMapper.updateEntityFromRequest(request, slot);

        if (request.getPackId() != null) {
            Pack pack = packRepository.findById(request.getPackId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pack", "id", request.getPackId()));
            slot.setPack(pack);
        } else {
            slot.setPack(null);
        }

        return countryTimeSlotMapper.toResponse(countryTimeSlotRepository.save(slot));
    }

    @Transactional
    public void deleteTimeSlot(Long id) {
        if (!countryTimeSlotRepository.existsById(id)) {
            throw new ResourceNotFoundException("Créneau horaire", "id", id);
        }
        countryTimeSlotRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CountryTimeSlotResponse> getActiveTimeSlots() {
        return countryTimeSlotMapper.toResponseList(countryTimeSlotRepository.findByActiveTrue());
    }

    @Transactional(readOnly = true)
    public List<CountryTimeSlotResponse> getTimeSlotsByCountry(String country) {
        return countryTimeSlotMapper.toResponseList(countryTimeSlotRepository.findByCountryIgnoreCaseAndActiveTrue(country));
    }

    @Transactional(readOnly = true)
    public List<CountryTimeSlotResponse> getAllTimeSlots() {
        return countryTimeSlotMapper.toResponseList(countryTimeSlotRepository.findAll());
    }

    @Transactional(readOnly = true)
    public CountryTimeSlotResponse getTimeSlotById(Long id) {
        CountryTimeSlot slot = countryTimeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Créneau horaire", "id", id));
        return countryTimeSlotMapper.toResponse(slot);
    }

    private void validateTimeRange(CountryTimeSlotRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null
                || !request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("L'heure de début du créneau doit être strictement antérieure à l'heure de fin.", "INVALID_TIME_RANGE");
        }
    }
}


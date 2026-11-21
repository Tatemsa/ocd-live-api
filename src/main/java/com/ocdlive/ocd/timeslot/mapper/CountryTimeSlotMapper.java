package com.ocdlive.ocd.timeslot.mapper;

import com.ocdlive.ocd.timeslot.dto.CountryTimeSlotRequest;
import com.ocdlive.ocd.timeslot.dto.CountryTimeSlotResponse;
import com.ocdlive.ocd.timeslot.model.CountryTimeSlot;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CountryTimeSlotMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pack", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CountryTimeSlot toEntity(CountryTimeSlotRequest request);

    @Mapping(target = "packId", source = "pack.id")
    @Mapping(target = "packName", source = "pack.name")
    CountryTimeSlotResponse toResponse(CountryTimeSlot entity);

    List<CountryTimeSlotResponse> toResponseList(List<CountryTimeSlot> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pack", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CountryTimeSlotRequest request, @MappingTarget CountryTimeSlot entity);
}


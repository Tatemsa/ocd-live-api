package com.ocdlive.ocd.pack.mapper;

import com.ocdlive.ocd.equipment.mapper.EquipmentMapper;
import com.ocdlive.ocd.pack.dto.PackRequest;
import com.ocdlive.ocd.pack.dto.PackResponse;
import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.user.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {EquipmentMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PackMapper {

    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "mapAuthorName")
    PackResponse toResponse(Pack pack);

    List<PackResponse> toResponseList(List<Pack> packs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipments", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Pack toEntity(PackRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipments", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(PackRequest request, @MappingTarget Pack pack);

    @Named("mapAuthorName")
    default String mapAuthorName(User user) {
        if (user == null) {
            return null;
        }
        return user.getFirstName() + " " + user.getLastName();
    }
}


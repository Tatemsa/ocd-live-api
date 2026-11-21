package com.ocdlive.ocd.subscription.mapper;

import com.ocdlive.ocd.pack.mapper.PackMapper;
import com.ocdlive.ocd.subscription.dto.SubscriptionResponse;
import com.ocdlive.ocd.subscription.model.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {PackMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SubscriptionMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientEmail", source = "client.email")
    SubscriptionResponse toResponse(Subscription subscription);

    List<SubscriptionResponse> toResponseList(List<Subscription> subscriptions);
}


package com.ocdlive.ocd.payment.mapper;

import com.ocdlive.ocd.payment.dto.PaymentResponse;
import com.ocdlive.ocd.payment.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "reservationId", source = "reservation.id")
    @Mapping(target = "subscriptionId", source = "subscription.id")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);
}


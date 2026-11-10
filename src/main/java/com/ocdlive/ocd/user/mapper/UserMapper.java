package com.ocdlive.ocd.user.mapper;

import com.ocdlive.ocd.user.dto.UserResponse;
import com.ocdlive.ocd.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}


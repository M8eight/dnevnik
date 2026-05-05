package com.rusobr.user.infrastructure.mapper;

import com.rusobr.user.domain.model.User;
import com.rusobr.user.web.dto.user.UserCreateRequest;
import com.rusobr.user.web.dto.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toUser(UserCreateRequest createUserDtoRequest, String keycloakId);
    UserCreateRequest toRequestUserDto(User user);
    UserResponse toCreateUserResponse(User user);
    UserResponse toUserResponse(User user);
}

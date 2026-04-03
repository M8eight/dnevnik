package com.rusobr.user.infrastructure.mapper;

import com.rusobr.user.domain.model.User;
import com.rusobr.user.web.dto.keycloak.CreateUserRequest;
import com.rusobr.user.web.dto.keycloak.CreateUserResponse;
import com.rusobr.user.web.dto.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toUser(CreateUserRequest createUserDtoRequest);
    CreateUserRequest toRequestUserDto(User user);

    CreateUserResponse toCreateUserResponse(User user);
    UserResponse toUserResponse(User user);
}

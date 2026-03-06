package com.rusobr.user.infrastructure.mapper;

import com.rusobr.user.domain.model.User;
import com.rusobr.user.web.dto.keycloack.KeycloackUserRequest;
import com.rusobr.user.web.dto.keycloack.KeycloackUserResponse;
import com.rusobr.user.web.dto.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toUser(KeycloackUserRequest createUserDtoRequest);
    KeycloackUserRequest toRequestUserDto(User user);

    KeycloackUserResponse toKeycloackUserResponse(User user);
    UserResponse toUserResponse(User user);
}

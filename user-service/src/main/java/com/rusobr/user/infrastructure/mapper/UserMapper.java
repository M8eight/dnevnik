package com.rusobr.user.infrastructure.mapper;

import com.rusobr.user.domain.model.User;
import com.rusobr.user.web.dto.user.CreateUserDtoRequest;
import com.rusobr.user.web.dto.user.CreateUserDtoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toUser(CreateUserDtoRequest createUserDtoRequest);
    CreateUserDtoRequest toRequestUserDto(User user);

    CreateUserDtoResponse toCreateUserDto(User user);
}

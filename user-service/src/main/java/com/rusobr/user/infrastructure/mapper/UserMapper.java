package com.rusobr.user.infrastructure.mapper;

import com.rusobr.user.domain.model.User;
import com.rusobr.user.web.dto.RequestUserDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toUser(RequestUserDto requestUserDto);
    RequestUserDto toRequestUserDto(User user);
}

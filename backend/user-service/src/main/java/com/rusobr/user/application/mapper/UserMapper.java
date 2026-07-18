package com.rusobr.user.application.mapper;

import com.rusobr.common.enums.UserRole;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.persistence.repository.projection.UserProjection;
import com.rusobr.user.web.dto.feign.UserFeignResponse;
import com.rusobr.user.web.dto.user.UserDataDto;
import com.rusobr.user.web.dto.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toUser(UserDataDto createUserDtoRequest, Set<UserRole> roles);
    UserResponse toCreateUserResponse(User user);
    UserResponse toUserResponse(User user);
    UserFeignResponse toUserFeignResponse(UserProjection userProjection);
}

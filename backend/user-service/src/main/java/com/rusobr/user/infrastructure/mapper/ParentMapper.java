package com.rusobr.user.infrastructure.mapper;

import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.parent.ParentResponse;
import com.rusobr.user.web.dto.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ParentMapper {
    @Mapping(target = "user", source = "user")
    @Mapping(target = "id", ignore = true)
    Parent toEntity(User user, ParentDetails parentDetails);

    @Mapping(target = "children", source = "parent.children")
    ParentResponse toResponse(Parent parent);

    @Mapping(target = "id", source = "student.user.id")
    @Mapping(target = "firstName", source = "student.user.firstName")
    @Mapping(target = "lastName", source = "student.user.lastName")
    @Mapping(target = "username", source = "student.user.username")
    @Mapping(target = "keycloakId", source = "student.user.keycloakId")
    @Mapping(target = "roles", source = "student.user.roles")
    UserResponse toUserResponse(Student student);

    ParentDetails toParentDetails(Parent parent);
}

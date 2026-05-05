package com.rusobr.user.infrastructure.mapper;

import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.web.dto.parent.ParentDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ParentMapper {
    @Mapping(target = "user", source = "user")
    @Mapping(target = "id", ignore = true)
    Parent toEntity(User user, ParentDetails parentDetails);
}

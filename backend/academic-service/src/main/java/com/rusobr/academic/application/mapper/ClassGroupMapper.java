package com.rusobr.academic.application.mapper;

import com.rusobr.academic.domain.model.ClassGroup;
import com.rusobr.academic.infrastructure.persistence.projection.ClassGroupWithCountProjection;
import com.rusobr.academic.web.dto.classGroup.ClassGroupRequest;
import com.rusobr.academic.web.dto.classGroup.ClassGroupResponse;
import com.rusobr.academic.web.dto.classGroup.ClassGroupWithCountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClassGroupMapper {

    ClassGroupWithCountResponse toClassGroupResponse(ClassGroupWithCountProjection classGroup);

    ClassGroup toEntity(ClassGroupRequest request);

    ClassGroupResponse toClassGroupResponse(ClassGroup request);

}

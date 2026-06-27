package com.rusobr.academic.application.mapper;

import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.projection.TeachingAssignmentDetailsProjection;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentDetailsDto;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeachingAssignmentMapper {

    TeachingAssignmentDetailsDto toTeachingAssignmentDetailsDto(TeachingAssignmentDetailsProjection projection);
    TeachingAssignmentResponse toTeachingAssignmentRawResponse(TeachingAssignment teachingAssignment);

}

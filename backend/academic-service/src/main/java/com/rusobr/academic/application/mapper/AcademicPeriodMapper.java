package com.rusobr.academic.application.mapper;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.infrastructure.persistence.projection.AcademicPeriodProjection;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodRequest;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AcademicPeriodMapper {
    @Mapping(source = "closed", target = "isClosed")
    AcademicPeriodResponse toDto(AcademicPeriod academicPeriod);
    AcademicPeriod toEntity(AcademicPeriodRequest academicPeriodRequest);
    AcademicPeriodResponse toResponse(AcademicPeriodProjection academicPeriodProjection);
}

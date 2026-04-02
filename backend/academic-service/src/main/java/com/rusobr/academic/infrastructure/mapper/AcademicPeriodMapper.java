package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AcademicPeriodMapper {
    AcademicPeriodResponse toDto(AcademicPeriod academicPeriod);

}

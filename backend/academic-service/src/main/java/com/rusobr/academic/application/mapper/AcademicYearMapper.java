package com.rusobr.academic.application.mapper;

import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.web.dto.academicYear.AcademicYearRequest;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AcademicYearMapper {
    AcademicYearResponse toResponse(AcademicYear academicYear);
    AcademicYear toEntity(AcademicYearRequest academicYearRequest);
}

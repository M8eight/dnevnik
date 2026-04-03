package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SchoolClassMapper {
    SchoolClassResponse toSchoolClassResponse(SchoolClass schoolClass);
}

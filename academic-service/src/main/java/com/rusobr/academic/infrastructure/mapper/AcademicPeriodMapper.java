package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AcademicPeriodMapper {
    AcademicPeriodDto toDto(AcademicPeriod academicPeriod);

}

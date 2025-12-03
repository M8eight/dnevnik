package com.rusobr.service.infrastructure.mappers;

import com.rusobr.service.domain.model.Grade;
import com.rusobr.service.web.dto.grade.GradeRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GradeMapper {
    GradeRequestDto toGradeRequestDto(Grade grade);
    Grade toGrade(GradeRequestDto gradeRequestDto);
}

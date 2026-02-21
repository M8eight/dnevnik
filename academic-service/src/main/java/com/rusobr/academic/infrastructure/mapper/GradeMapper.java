package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.web.dto.grade.GradeRequestDto;
import com.rusobr.academic.web.dto.grade.GradeResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GradeMapper {
    GradeResponseDto toGradeResponseDto(Grade grade);
    Grade toGrade(GradeRequestDto gradeRequestDto);
    void updateEntityFromDto(GradeRequestDto dto, @MappingTarget Grade grade);
}

package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.web.dto.grade.GradeJournalData;
import com.rusobr.academic.web.dto.grade.GradeJournalResponse;
import com.rusobr.academic.web.dto.grade.GradeRequestDto;
import com.rusobr.academic.web.dto.grade.GradeResponseDto;
import com.rusobr.academic.web.dto.userService.UserResponse;
import org.mapstruct.*;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GradeMapper {
    @Mapping(target = "date", source = "date")
    @Mapping(target = "gradeId", source = "grade.id")
    @Mapping(target = "gradeType", source = "grade.type")
    GradeResponseDto toGradeResponseDto(Grade grade, LocalDate date);
    Grade toGrade(GradeRequestDto gradeRequestDto);
    void updateEntityFromDto(GradeRequestDto dto, @MappingTarget Grade grade);
    GradeJournalResponse toGradeJournalResponse(List<UserResponse> users, GradeJournalData grade);
}

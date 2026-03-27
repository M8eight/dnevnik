package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.web.dto.grade.GradeResponseDto;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequestDto;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponseDto;
import com.rusobr.academic.web.dto.grade.GradeJournalData;
import com.rusobr.academic.web.dto.grade.GradeJournalResponse;
import com.rusobr.academic.web.dto.userService.UserResponse;
import org.mapstruct.*;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GradeMapper {
    @Mapping(target = "date", source = "date")
    @Mapping(target = "gradeId", source = "grade.id")
    @Mapping(target = "gradeType", source = "grade.type")
    CreateGradeResponseDto toCreateGradeResponseDto(Grade grade, LocalDate date);

    @Mapping(target = "gradeId", source = "grade.id")
    @Mapping(target = "gradeType", source = "grade.type")
    GradeResponseDto toGradeResponseDto(Grade grade);

    Grade toGrade(CreateGradeRequestDto gradeRequestDto);
    void updateEntityFromDto(CreateGradeRequestDto dto, @MappingTarget Grade grade);

    GradeJournalResponse toGradeJournalResponse(List<UserResponse> users, GradeJournalData grade);
}

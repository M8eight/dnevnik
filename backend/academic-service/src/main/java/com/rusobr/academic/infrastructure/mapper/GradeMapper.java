package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.web.dto.grade.GetGradeDataDto;
import com.rusobr.academic.web.dto.grade.GradeJournalResponse;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.userService.UserResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GradeMapper {
    @Mapping(target = "gradeId", source = "grade.id")
    @Mapping(target = "gradeType", source = "grade.type")
    @Mapping(target = "lessonInstance", source = "lessonInstanceDto")
    CreateGradeResponse toCreateGradeResponseDto(Grade grade, LessonInstanceDto lessonInstanceDto);

    @Mapping(target = "gradeId", source = "grade.id")
    @Mapping(target = "gradeType", source = "grade.type")
    GradeResponse toGradeResponseDto(Grade grade);

    @Mapping(target = "type", source = "gradeType")
    @Mapping(target = "lessonInstance", ignore = true)
    Grade toGrade(CreateGradeRequest gradeRequestDto);

    void updateEntityFromDto(CreateGradeRequest dto, @MappingTarget Grade grade);

    GradeJournalResponse toGradeJournalResponse(List<UserResponse> users, GetGradeDataDto grade);
}

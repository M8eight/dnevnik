package com.rusobr.academic.application.mapper;

import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.infrastructure.persistence.projection.GradeDetailProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeJournalItemProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeWithSubjectNameProjection;
import com.rusobr.academic.infrastructure.persistence.projection.StudentAverageProjection;
import com.rusobr.academic.web.dto.grade.*;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GradeMapper {

    @Mapping(target = "gradeId", source = "grade.id")
    @Mapping(target = "gradeType", source = "grade.type")
    @Mapping(target = "lessonInstance", source = "lessonInstanceDto")
    CreateGradeResponse toCreateGradeResponseDto(Grade grade, LessonInstanceDto lessonInstanceDto);

    @Mapping(target = "id", source = "grade.id")
    @Mapping(target = "type", source = "grade.type")
    GradeResponse toGradeResponseDto(Grade grade);

    @Mapping(target = "type", source = "gradeType")
    @Mapping(target = "lessonInstance", ignore = true)
    Grade toGrade(CreateGradeRequest gradeRequestDto);

    void updateEntityFromDto(CreateGradeRequest dto, @MappingTarget Grade grade);

    GradeJournalResponse toGradeJournalResponse(List<UserFeignResponse> users, GetGradeDataDto grade);

    GradeJournalItemDto toItemProjection(GradeJournalItemProjection itemProjection);

    GradeWithSubjectNameResponse toWithSubjectNameResponse(GradeWithSubjectNameProjection projection);

    StudentAverageDto toStudentAverageDto(StudentAverageProjection projection);

    @Mapping(target = "id", source = "gradeProjection.id")
    @Mapping(target = "type", source = "gradeProjection.gradeType")
    GradeDetailResponse toGradeDetailResponse(GradeDetailProjection gradeProjection, UserFeignResponse teacher);

}

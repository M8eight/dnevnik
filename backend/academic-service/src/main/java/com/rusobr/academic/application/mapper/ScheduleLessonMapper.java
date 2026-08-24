package com.rusobr.academic.application.mapper;

import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.projection.ScheduleLessonProjection;
import com.rusobr.academic.infrastructure.persistence.projection.SchoolLessonProjection;
import com.rusobr.academic.web.dto.scheduleLesson.studentDiary.ScheduleLessonDiaryDto;
import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.academic.web.dto.scheduleLesson.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScheduleLessonMapper {

    @Mapping(target = "id", source = "scheduleLesson.id")
    @Mapping(target = "teacher", source = "teacher")
    @Mapping(target = "subject", source = "scheduleLesson.teachingAssignment.subject")
    @Mapping(target = "classGroup", source = "scheduleLesson.teachingAssignment.classGroup")
    ScheduleLessonDto toDto(ScheduleLesson scheduleLesson, UserFeignResponse teacher);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teachingAssignment", source = "teachingAssignment")
    ScheduleLesson toEntity(ScheduleLessonRequest scheduleLessonRequest, TeachingAssignment teachingAssignment);

    ScheduleLessonResponse toScheduleLessonResponse(ScheduleLessonProjection projection);

    SchoolLessonResponse toSchoolLessonResponse(SchoolLessonProjection projection);

    @Mapping(target = "dayOfWeek", source = "lessonInstance.scheduleLesson.dayOfWeek")
    @Mapping(target = "subject", source = "lessonInstance.scheduleLesson.teachingAssignment.subject")
    @Mapping(target = "lessonInstance", source = "lessonInstance")
    @Mapping(target = "schoolClass", source = "lessonInstance.scheduleLesson.teachingAssignment.schoolClass")
    TeacherScheduleItem toTeacherScheduleItem(LessonInstance lessonInstance);

    @Mapping(target = "subject", source = "scheduleLesson.teachingAssignment.subject")
    ScheduleLessonDiaryDto toScheduleLessonDiaryDto(ScheduleLesson scheduleLesson);

}

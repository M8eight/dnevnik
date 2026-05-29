package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.lessonInstance.DiaryLessonInstanceDto;
import com.rusobr.academic.web.dto.scheduleLesson.DiaryScheduleDto;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonDto;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScheduleLessonMapper {
    @Mapping(target = "id", source = "scheduleLesson.id")
    @Mapping(target = "teacher", source = "teacher")
    @Mapping(target = "subject", source = "scheduleLesson.teachingAssignment.subject")
    ScheduleLessonDto toDto(ScheduleLesson scheduleLesson, UserFeignResponse teacher);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teachingAssignment", source = "teachingAssignment")
    ScheduleLesson toEntity(ScheduleLessonRequest scheduleLessonRequest, TeachingAssignment teachingAssignment);

    @Mapping(target = "id", source = "scheduleLesson.id")
    @Mapping(target = "instance", source = "diaryLessonInstance")
    @Mapping(target = "subject", source = "scheduleLesson.teachingAssignment.subject")
    DiaryScheduleDto toDiaryScheduleDto(ScheduleLesson scheduleLesson, DiaryLessonInstanceDto diaryLessonInstance);
}

package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.Attendance;
import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.web.dto.attendances.journal.AttendanceSimpleResponse;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.scheduleLesson.DiaryLessonResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface LessonInstanceMapper {

    @Mapping(target = "lessonDate", source = "lessonDate")
    @Mapping(target = "subjectName", source = "scheduleLesson.teachingAssignment.subject.name")
    @Mapping(target = "dayOfWeek", source = "scheduleLesson.dayOfWeek")
    @Mapping(target = "lessonNumber", source = "scheduleLesson.lessonNumber")
    @Mapping(target = "classRoom", source = "scheduleLesson.classRoom")
    @Mapping(target = "grades", expression = "java(filterGrades(entity.getGrades(), studentId))")
    @Mapping(target = "attendance", expression = "java(filterAttendance(entity.getAttendances(), studentId))")
    @Mapping(target = "homeworks", source = "homeworks")
    DiaryLessonResponse toDiaryLessonResponse(LessonInstance entity, @Context Long studentId);

    List<DiaryLessonResponse> toDiaryLessonResponseList(List<LessonInstance> lessonInstanceList,
                                                        @Context Long studentId);

    default List<GradeResponse> filterGrades(List<Grade> grades, @Context Long studentId) {
        if (grades == null) return List.of();
        return grades.stream()
                .filter(g -> g.getStudentId().equals(studentId))
                .map(g -> new GradeResponse(g.getId(), g.getStudentId(), g.getValue(), g.getType()))
                .toList();
    }

    default AttendanceSimpleResponse filterAttendance(List<Attendance> attendances, @Context Long studentId) {
        if (attendances == null) return null;
        return attendances.stream()
                .filter(a -> a.getStudentId().equals(studentId))
                .findFirst()
                .map(a -> new AttendanceSimpleResponse(a.getId(), a.getStatus(), a.getStudentId()))
                .orElse(null);
    }

    LessonInstanceDto toLessonInstanceDto(LessonInstance lessonInstance);


}

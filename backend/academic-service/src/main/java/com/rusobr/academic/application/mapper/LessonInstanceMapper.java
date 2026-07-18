package com.rusobr.academic.application.mapper;

import com.rusobr.academic.domain.model.Homework;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.persistence.projection.AttendanceStudentProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeJournalProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeStudentProjection;
import com.rusobr.academic.infrastructure.persistence.projection.LessonInstanceProjection;
import com.rusobr.academic.web.dto.homework.HomeworkDiaryResponse;
import com.rusobr.academic.web.dto.lessonInstance.DiaryLessonInstanceDto;
import com.rusobr.academic.web.dto.lessonInstance.GradeJournalDto;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.AttendanceStudentDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.GradeStudentDto;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface LessonInstanceMapper {

    LessonInstanceDto toLessonInstanceDto(LessonInstance lessonInstance);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "scheduleId", source = "scheduleLesson.id")
    @Mapping(target = "homework", source = "homeworks", qualifiedByName = "firstHomework")
    DiaryLessonInstanceDto toDiaryLessonInstance(LessonInstance lessonInstance);

    @Named("firstHomework")
    default HomeworkDiaryResponse firstHomework(Set<Homework> homeworks) {
        return homeworks == null || homeworks.isEmpty()
                ? null
                : toHomeworkDto(homeworks.iterator().next());
    }

    HomeworkDiaryResponse toHomeworkDto(Homework homework);

    @Mapping(target = "date", source = "lessonDate")
    GradeJournalDto toGradeJournalProjection(GradeJournalProjection projection);

    LessonInstanceDto toLessonInstanceDto(LessonInstanceProjection projection);

    GradeStudentDto toGradeStudentDto(GradeStudentProjection projection);

    AttendanceStudentDto toAttendanceStudentDto(AttendanceStudentProjection projection);

}

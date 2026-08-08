package com.rusobr.academic.application.mapper;

import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.persistence.projection.AttendanceStudentProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeJournalProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeStudentProjection;
import com.rusobr.academic.infrastructure.persistence.projection.LessonInstanceProjection;
import com.rusobr.academic.web.dto.lessonInstance.GradeJournalDto;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.StudentJournalDto;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LessonInstanceMapper {

    LessonInstanceDto toLessonInstanceDto(LessonInstance lessonInstance);

    StudentJournalDto.GradeLessonTeacherDto toGradeStudentDto(GradeStudentProjection projection);

    @Mapping(target = "date", source = "lessonDate")
    GradeJournalDto toGradeJournalProjection(GradeJournalProjection projection);

    LessonInstanceDto toLessonInstanceDto(LessonInstanceProjection projection);

    StudentJournalDto.AttendanceLessonTeacherDto toAttendanceStudentDto(AttendanceStudentProjection projection);

}

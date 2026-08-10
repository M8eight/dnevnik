package com.rusobr.academic.mapper;

import com.rusobr.academic.application.mapper.PeriodGradeMapper;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.PeriodGrade;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeStudentResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class PeriodGradeMapperTest {

    private final PeriodGradeMapper mapper = Mappers.getMapper(PeriodGradeMapper.class);

    @Test
    void shouldMapAllFieldsToPeriodGradeResponse() {
        AcademicPeriod period = AcademicPeriod.builder()
                .id(7L)
                .name("I полугодие")
                .build();
        PeriodGrade periodGrade = PeriodGrade.builder()
                .id(1L)
                .value(4)
                .description("Хорошо")
                .academicPeriod(period)
                .studentId(5L)
                .build();

        PeriodGradeResponse result = mapper.toPeriodGradeResponse(periodGrade);

        assertThat(result).isEqualTo(new PeriodGradeResponse(1L, 4, "Хорошо", 5L, 7L));
    }

    @Test
    void shouldReturnNullWhenSourceIsNull() {
        assertThat(mapper.toPeriodGradeResponse(null)).isNull();
    }

    @Test
    void shouldReturnNullAcademicPeriodIdWhenNestedIsNull() {
        PeriodGrade periodGrade = PeriodGrade.builder()
                .id(1L)
                .value(4)
                .studentId(5L)
                .build();

        PeriodGradeResponse result = mapper.toPeriodGradeResponse(periodGrade);

        assertThat(result.academicPeriodId()).isNull();
    }

    @Test
    void shouldMapAllFieldsToStudentResponse() {
        Subject subject = Subject.builder().id(3L).name("Алгебра").build();
        TeachingAssignment assignment = TeachingAssignment.builder()
                .id(9L)
                .teacherId(10L)
                .subject(subject)
                .build();
        AcademicPeriod period = AcademicPeriod.builder()
                .id(7L)
                .name("I полугодие")
                .build();
        PeriodGrade periodGrade = PeriodGrade.builder()
                .id(1L)
                .value(4)
                .description("Хорошо")
                .academicPeriod(period)
                .teachingAssignment(assignment)
                .studentId(5L)
                .build();

        PeriodGradeStudentResponse result = mapper.toPeriodGradeStudentResponse(periodGrade);

        assertThat(result).isEqualTo(new PeriodGradeStudentResponse(1L, 4, "Хорошо", "Алгебра", 7L));
    }

    @Test
    void shouldReturnNullWhenStudentSourceIsNull() {
        assertThat(mapper.toPeriodGradeStudentResponse(null)).isNull();
    }

    @Test
    void shouldReturnNullSubjectNameWhenTeachingAssignmentIsNull() {
        AcademicPeriod period = AcademicPeriod.builder()
                .id(7L)
                .name("I полугодие")
                .build();
        PeriodGrade periodGrade = PeriodGrade.builder()
                .id(1L)
                .value(4)
                .academicPeriod(period)
                .studentId(5L)
                .build();

        PeriodGradeStudentResponse result = mapper.toPeriodGradeStudentResponse(periodGrade);

        assertThat(result.subjectName()).isNull();
    }
}

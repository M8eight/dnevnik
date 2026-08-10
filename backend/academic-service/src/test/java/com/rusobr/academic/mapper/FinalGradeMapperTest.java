package com.rusobr.academic.mapper;

import com.rusobr.academic.application.mapper.FinalGradeMapper;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.domain.model.FinalGrade;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeCreateResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeRequest;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class FinalGradeMapperTest {

    private final FinalGradeMapper mapper = Mappers.getMapper(FinalGradeMapper.class);

    private static final LocalDate START_DATE = LocalDate.of(2025, 9, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 5, 31);

    private AcademicYear academicYear() {
        return AcademicYear.builder()
                .id(1L)
                .name("2025-2026")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build();
    }

    @Test
    void shouldMapRequestToEntity() {
        FinalGradeRequest request = new FinalGradeRequest(5L, 1L, 4, "Хорошо", 3L);

        FinalGrade result = mapper.toFinalGrade(request);

        assertThat(result.getId()).isNull();
        assertThat(result.getStudentId()).isEqualTo(5L);
        assertThat(result.getValue()).isEqualTo(4);
        assertThat(result.getDescription()).isEqualTo("Хорошо");
        assertThat(result.getAcademicYear()).isNull();
        assertThat(result.getTeachingAssignment()).isNull();
    }

    @Test
    void shouldReturnNullWhenRequestIsNull() {
        assertThat(mapper.toFinalGrade(null)).isNull();
    }

    @Test
    void shouldMapNullDescriptionAsNull() {
        FinalGradeRequest request = new FinalGradeRequest(5L, 1L, 4, null, 3L);

        FinalGrade result = mapper.toFinalGrade(request);

        assertThat(result.getDescription()).isNull();
    }

    @Test
    void shouldMapAllFieldsToCreateResponse() {
        FinalGrade finalGrade = FinalGrade.builder()
                .id(1L)
                .studentId(5L)
                .academicYear(academicYear())
                .value(4)
                .description("Хорошо")
                .build();

        FinalGradeCreateResponse result = mapper.toFinalGradeCreateResponse(finalGrade);

        assertThat(result).isEqualTo(new FinalGradeCreateResponse(
                1L,
                5L,
                new AcademicYearResponse(1L, "2025-2026", null, START_DATE, END_DATE, false),
                4,
                "Хорошо"));
    }

    @Test
    void shouldReturnNullWhenCreateSourceIsNull() {
        assertThat(mapper.toFinalGradeCreateResponse(null)).isNull();
    }

    @Test
    void shouldMapSubjectNameFromTeachingAssignment() {
        Subject subject = Subject.builder().id(7L).name("Алгебра").build();
        TeachingAssignment assignment = TeachingAssignment.builder()
                .id(3L)
                .teacherId(10L)
                .subject(subject)
                .build();
        FinalGrade finalGrade = FinalGrade.builder()
                .id(1L)
                .studentId(5L)
                .academicYear(academicYear())
                .value(4)
                .description("Хорошо")
                .teachingAssignment(assignment)
                .build();

        FinalGradeResponse result = mapper.toFinalGradeResponse(finalGrade);

        assertThat(result.subjectName()).isEqualTo("Алгебра");
    }

    @Test
    void shouldReturnNullWhenResponseSourceIsNull() {
        assertThat(mapper.toFinalGradeResponse(null)).isNull();
    }

    @Test
    void shouldReturnNullSubjectNameWhenTeachingAssignmentIsNull() {
        FinalGrade finalGrade = FinalGrade.builder()
                .id(1L)
                .studentId(5L)
                .academicYear(academicYear())
                .value(4)
                .build();

        FinalGradeResponse result = mapper.toFinalGradeResponse(finalGrade);

        assertThat(result.subjectName()).isNull();
    }

    @Test
    void shouldReturnNullSubjectNameWhenSubjectIsNull() {
        TeachingAssignment assignment = TeachingAssignment.builder()
                .id(3L)
                .teacherId(10L)
                .build();
        FinalGrade finalGrade = FinalGrade.builder()
                .id(1L)
                .studentId(5L)
                .academicYear(academicYear())
                .value(4)
                .teachingAssignment(assignment)
                .build();

        FinalGradeResponse result = mapper.toFinalGradeResponse(finalGrade);

        assertThat(result.subjectName()).isNull();
    }
}

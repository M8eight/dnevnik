package com.rusobr.academic.mapper;

import com.rusobr.academic.application.mapper.TeachingAssignmentMapper;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.projection.TeachingAssignmentDetailsProjection;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentDetailsDto;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TeachingAssignmentMapperTest {

    private final TeachingAssignmentMapper mapper = Mappers.getMapper(TeachingAssignmentMapper.class);

    @Test
    void shouldMapAllFieldsFromProjection() {
        TeachingAssignmentDetailsProjection projection = mock(TeachingAssignmentDetailsProjection.class);
        when(projection.getTeachingAssignmentId()).thenReturn(1L);
        when(projection.getSchoolClassId()).thenReturn(5L);
        when(projection.getSchoolClassName()).thenReturn("5А");
        when(projection.getSubjectId()).thenReturn(7L);
        when(projection.getSubjectName()).thenReturn("Алгебра");

        TeachingAssignmentDetailsDto result = mapper.toTeachingAssignmentDetailsDto(projection);

        assertThat(result).isEqualTo(new TeachingAssignmentDetailsDto(1L, 5L, "5А", 7L, "Алгебра"));
    }

    @Test
    void shouldReturnNullWhenProjectionIsNull() {
        assertThat(mapper.toTeachingAssignmentDetailsDto(null)).isNull();
    }

    @Test
    void shouldMapAllFieldsToRawResponse() {
        Subject subject = Subject.builder().id(7L).name("Алгебра").build();
        AcademicYear year = AcademicYear.builder()
                .id(1L)
                .name("2025-2026")
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2026, 5, 31))
                .build();
        SchoolClass schoolClass = SchoolClass.builder()
                .id(5L)
                .name("5А")
                .academicYear(year)
                .classTeacherId(10L)
                .build();
        TeachingAssignment assignment = TeachingAssignment.builder()
                .id(1L)
                .teacherId(10L)
                .schoolClass(schoolClass)
                .subject(subject)
                .build();

        TeachingAssignmentResponse result = mapper.toTeachingAssignmentRawResponse(assignment);

        assertThat(result).isEqualTo(new TeachingAssignmentResponse(
                1L,
                new SubjectResponseDto(7L, "Алгебра"),
                new SchoolClassResponse(
                        5L,
                        "5А",
                        new AcademicYearResponse(1L, "2025-2026", null, LocalDate.of(2025, 9, 1), LocalDate.of(2026, 5, 31), false),
                        10L)));
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        assertThat(mapper.toTeachingAssignmentRawResponse(null)).isNull();
    }

    @Test
    void shouldReturnNullSubjectWhenNestedIsNull() {
        TeachingAssignment assignment = TeachingAssignment.builder()
                .id(1L)
                .teacherId(10L)
                .build();

        TeachingAssignmentResponse result = mapper.toTeachingAssignmentRawResponse(assignment);

        assertThat(result.subject()).isNull();
    }

    @Test
    void shouldReturnNullSchoolClassWhenNestedIsNull() {
        Subject subject = Subject.builder().id(7L).name("Алгебра").build();
        TeachingAssignment assignment = TeachingAssignment.builder()
                .id(1L)
                .teacherId(10L)
                .subject(subject)
                .build();

        TeachingAssignmentResponse result = mapper.toTeachingAssignmentRawResponse(assignment);

        assertThat(result.schoolClass()).isNull();
    }
}

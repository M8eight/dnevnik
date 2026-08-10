package com.rusobr.academic.mapper;

import com.rusobr.academic.application.mapper.LessonInstanceMapper;
import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.persistence.projection.AttendanceStudentProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeJournalProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeStudentProjection;
import com.rusobr.academic.infrastructure.persistence.projection.LessonInstanceProjection;
import com.rusobr.academic.web.dto.lessonInstance.GradeJournalDto;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.StudentJournalDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LessonInstanceMapperTest {

    private final LessonInstanceMapper mapper = Mappers.getMapper(LessonInstanceMapper.class);

    private static final LocalDate LESSON_DATE = LocalDate.of(2026, 2, 10);

    @Test
    void shouldMapAllFieldsToDto() {
        LessonInstance lessonInstance = LessonInstance.builder()
                .id(9L)
                .lessonDate(LESSON_DATE)
                .build();

        LessonInstanceDto result = mapper.toLessonInstanceDto(lessonInstance);

        assertThat(result).isEqualTo(new LessonInstanceDto(9L, LESSON_DATE));
    }

    @Test
    void shouldReturnNullWhenEntitySourceIsNull() {
        assertThat(mapper.toLessonInstanceDto((LessonInstance) null)).isNull();
    }

    @ParameterizedTest
    @EnumSource(GradeType.class)
    void shouldMapGradeStudentProjection(GradeType type) {
        GradeStudentProjection projection = mock(GradeStudentProjection.class);
        when(projection.getGradeId()).thenReturn(1L);
        when(projection.getValue()).thenReturn(4);
        when(projection.getWeight()).thenReturn(2);
        when(projection.getGradeType()).thenReturn(type);
        when(projection.getStudentId()).thenReturn(5L);
        when(projection.getLessonInstanceId()).thenReturn(9L);

        StudentJournalDto.GradeLessonTeacherDto result = mapper.toGradeStudentDto(projection);

        assertThat(result).isEqualTo(new StudentJournalDto.GradeLessonTeacherDto(1L, 4, 2, type, 5L, 9L));
    }

    @Test
    void shouldReturnNullWhenGradeStudentProjectionIsNull() {
        assertThat(mapper.toGradeStudentDto(null)).isNull();
    }

    @Test
    void shouldMapAllFieldsToGradeJournalDto() {
        GradeJournalProjection projection = mock(GradeJournalProjection.class);
        when(projection.getSubjectName()).thenReturn("Алгебра");
        when(projection.getGradeId()).thenReturn(1L);
        when(projection.getValue()).thenReturn(4);
        when(projection.getWeight()).thenReturn(2);
        when(projection.getGradeType()).thenReturn(GradeType.CONTROL);
        when(projection.getLessonDate()).thenReturn(LESSON_DATE);

        GradeJournalDto result = mapper.toGradeJournalProjection(projection);

        assertThat(result).isEqualTo(new GradeJournalDto("Алгебра", 1L, 4, 2, GradeType.CONTROL, LESSON_DATE));
    }

    @Test
    void shouldReturnNullWhenGradeJournalProjectionIsNull() {
        assertThat(mapper.toGradeJournalProjection(null)).isNull();
    }

    @Test
    void shouldMapAllFieldsFromProjection() {
        LessonInstanceProjection projection = mock(LessonInstanceProjection.class);
        when(projection.getId()).thenReturn(9L);
        when(projection.getLessonDate()).thenReturn(LESSON_DATE);

        LessonInstanceDto result = mapper.toLessonInstanceDto(projection);

        assertThat(result).isEqualTo(new LessonInstanceDto(9L, LESSON_DATE));
    }

    @Test
    void shouldReturnNullWhenProjectionSourceIsNull() {
        assertThat(mapper.toLessonInstanceDto((LessonInstanceProjection) null)).isNull();
    }

    @ParameterizedTest
    @EnumSource(AttendanceStatus.class)
    void shouldMapAttendanceStudentProjection(AttendanceStatus status) {
        AttendanceStudentProjection projection = mock(AttendanceStudentProjection.class);
        when(projection.getAttendanceId()).thenReturn(2L);
        when(projection.getStatus()).thenReturn(status);
        when(projection.getStudentId()).thenReturn(5L);
        when(projection.getLessonInstanceId()).thenReturn(9L);

        StudentJournalDto.AttendanceLessonTeacherDto result = mapper.toAttendanceStudentDto(projection);

        assertThat(result).isEqualTo(new StudentJournalDto.AttendanceLessonTeacherDto(2L, status, 5L, 9L));
    }

    @Test
    void shouldReturnNullWhenAttendanceProjectionIsNull() {
        assertThat(mapper.toAttendanceStudentDto(null)).isNull();
    }
}

package com.rusobr.academic.mapper;

import com.rusobr.academic.application.mapper.AttendanceMapper;
import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.model.Attendance;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.web.dto.attendances.AttendanceRequest;
import com.rusobr.academic.web.dto.attendances.AttendanceResponse;
import com.rusobr.academic.web.dto.attendances.journal.AttendanceSimpleResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class AttendanceMapperTest {

    private final AttendanceMapper mapper = Mappers.getMapper(AttendanceMapper.class);

    private static final LocalDate LESSON_DATE = LocalDate.of(2026, 2, 10);

    @Test
    void shouldMapAllFieldsToAttendanceResponse() {
        LessonInstance lesson = LessonInstance.builder()
                .id(9L)
                .lessonDate(LESSON_DATE)
                .build();
        Attendance attendance = Attendance.builder()
                .id(1L)
                .studentId(5L)
                .status(AttendanceStatus.ABSENT)
                .lessonInstance(lesson)
                .build();

        AttendanceResponse result = mapper.toAttendanceResponse(attendance);

        assertThat(result).isEqualTo(new AttendanceResponse(
                1L,
                5L,
                AttendanceStatus.ABSENT,
                new LessonInstanceDto(9L, LESSON_DATE)));
    }

    @Test
    void shouldReturnNullWhenSourceIsNull() {
        assertThat(mapper.toAttendanceResponse(null)).isNull();
    }

    @Test
    void shouldReturnNullLessonInstanceWhenNestedIsNull() {
        Attendance attendance = Attendance.builder()
                .id(1L)
                .studentId(5L)
                .status(AttendanceStatus.ABSENT)
                .build();

        AttendanceResponse result = mapper.toAttendanceResponse(attendance);

        assertThat(result.lessonInstance()).isNull();
    }

    @ParameterizedTest
    @EnumSource(AttendanceStatus.class)
    void shouldMapStatusToSimpleResponse(AttendanceStatus status) {
        Attendance attendance = Attendance.builder()
                .id(1L)
                .studentId(5L)
                .status(status)
                .build();

        AttendanceSimpleResponse result = mapper.toAttendanceSimpleResponse(attendance);

        assertThat(result).isEqualTo(new AttendanceSimpleResponse(1L, status, 5L));
    }

    @Test
    void shouldReturnNullWhenSimpleSourceIsNull() {
        assertThat(mapper.toAttendanceSimpleResponse(null)).isNull();
    }

    @Test
    void shouldMapRequestAndLessonInstanceToEntity() {
        AttendanceRequest request = new AttendanceRequest(5L, AttendanceStatus.LATE, 9L);
        LessonInstance lesson = LessonInstance.builder()
                .id(9L)
                .lessonDate(LESSON_DATE)
                .build();

        Attendance result = mapper.toAttendance(request, lesson);

        assertThat(result.getId()).isNull();
        assertThat(result.getStudentId()).isEqualTo(5L);
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.LATE);
        assertThat(result.getLessonInstance()).isSameAs(lesson);
    }

    @Test
    void shouldReturnNullWhenRequestAndLessonInstanceAreNull() {
        assertThat(mapper.toAttendance(null, null)).isNull();
    }

    @Test
    void shouldMapNullRequestFieldsAsNullWhenLessonInstanceProvided() {
        LessonInstance lesson = LessonInstance.builder()
                .id(9L)
                .lessonDate(LESSON_DATE)
                .build();

        Attendance result = mapper.toAttendance(null, lesson);

        assertThat(result.getStudentId()).isNull();
        assertThat(result.getStatus()).isNull();
        assertThat(result.getLessonInstance()).isSameAs(lesson);
    }
}

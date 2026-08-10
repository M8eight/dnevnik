package com.rusobr.academic.mapper;

import com.rusobr.academic.application.mapper.ScheduleLessonMapper;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.projection.ScheduleLessonProjection;
import com.rusobr.academic.infrastructure.persistence.projection.SchoolLessonProjection;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonDto;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonRequest;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonResponse;
import com.rusobr.academic.web.dto.scheduleLesson.SchoolLessonResponse;
import com.rusobr.academic.web.dto.scheduleLesson.TeacherScheduleItem;
import com.rusobr.academic.web.dto.scheduleLesson.studentDiary.ScheduleLessonDiaryDto;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import com.rusobr.common.dto.UserFeignResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScheduleLessonMapperTest {

    private final ScheduleLessonMapper mapper = Mappers.getMapper(ScheduleLessonMapper.class);

    private static final LocalDate VALID_FROM = LocalDate.of(2026, 2, 1);
    private static final LocalDate VALID_TO = LocalDate.of(2026, 2, 28);
    private static final LocalDate LESSON_DATE = LocalDate.of(2026, 2, 10);

    private TeachingAssignment assignmentWithSubjectAndClass() {
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
        return TeachingAssignment.builder()
                .id(2L)
                .teacherId(10L)
                .schoolClass(schoolClass)
                .subject(subject)
                .build();
    }

    private ScheduleLesson scheduleLesson() {
        return ScheduleLesson.builder()
                .id(3L)
                .teachingAssignment(assignmentWithSubjectAndClass())
                .dayOfWeek(DayOfWeek.MONDAY)
                .lessonNumber(1)
                .classRoom("101")
                .validFrom(VALID_FROM)
                .validTo(VALID_TO)
                .build();
    }

    @Test
    void shouldMapAllFieldsToDto() {
        UserFeignResponse teacher = UserFeignResponse.builder()
                .id(10L)
                .firstName("Иван")
                .lastName("Петров")
                .build();

        ScheduleLessonDto result = mapper.toDto(scheduleLesson(), teacher);

        assertThat(result).isEqualTo(new ScheduleLessonDto(
                3L,
                DayOfWeek.MONDAY,
                1,
                "101",
                VALID_FROM,
                VALID_TO,
                new SubjectResponseDto(7L, "Алгебра"),
                teacher));
    }

    @Test
    void shouldReturnNullWhenBothSourcesAreNull() {
        assertThat(mapper.toDto(null, null)).isNull();
    }

    @Test
    void shouldReturnNullTeacherWhenTeacherIsNull() {
        ScheduleLessonDto result = mapper.toDto(scheduleLesson(), null);

        assertThat(result.teacher()).isNull();
        assertThat(result.subject()).isEqualTo(new SubjectResponseDto(7L, "Алгебра"));
    }

    @Test
    void shouldMapRequestAndAssignmentToEntity() {
        ScheduleLessonRequest request = new ScheduleLessonRequest(
                5L, 7L, 10L, DayOfWeek.TUESDAY, 2, "102", VALID_FROM);
        TeachingAssignment assignment = TeachingAssignment.builder().id(2L).build();

        ScheduleLesson result = mapper.toEntity(request, assignment);

        assertThat(result.getId()).isNull();
        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(result.getLessonNumber()).isEqualTo(2);
        assertThat(result.getClassRoom()).isEqualTo("102");
        assertThat(result.getValidFrom()).isEqualTo(VALID_FROM);
        assertThat(result.getValidTo()).isNull();
        assertThat(result.getTeachingAssignment()).isSameAs(assignment);
    }

    @Test
    void shouldReturnNullWhenRequestAndAssignmentAreNull() {
        assertThat(mapper.toEntity(null, null)).isNull();
    }

    @Test
    void shouldMapAllFieldsFromScheduleProjection() {
        ScheduleLessonProjection projection = mock(ScheduleLessonProjection.class);
        when(projection.getId()).thenReturn(3L);
        when(projection.getLessonNumber()).thenReturn(1);
        when(projection.getSubjectName()).thenReturn("Алгебра");
        when(projection.getClassRoom()).thenReturn("101");

        ScheduleLessonResponse result = mapper.toScheduleLessonResponse(projection);

        assertThat(result).isEqualTo(new ScheduleLessonResponse(3L, 1, "Алгебра", "101"));
    }

    @Test
    void shouldReturnNullWhenScheduleProjectionIsNull() {
        assertThat(mapper.toScheduleLessonResponse(null)).isNull();
    }

    @Test
    void shouldMapAllFieldsFromSchoolProjection() {
        SchoolLessonProjection projection = mock(SchoolLessonProjection.class);
        when(projection.getId()).thenReturn(3L);
        when(projection.getLessonNumber()).thenReturn(1);
        when(projection.getSubjectName()).thenReturn("Алгебра");
        when(projection.getClassRoom()).thenReturn("101");
        when(projection.getDayOfWeek()).thenReturn(DayOfWeek.WEDNESDAY);

        SchoolLessonResponse result = mapper.toSchoolLessonResponse(projection);

        assertThat(result).isEqualTo(new SchoolLessonResponse(3L, 1, "Алгебра", "101", DayOfWeek.WEDNESDAY));
    }

    @Test
    void shouldReturnNullWhenSchoolProjectionIsNull() {
        assertThat(mapper.toSchoolLessonResponse(null)).isNull();
    }

    @Test
    void shouldMapAllNestedFieldsToTeacherScheduleItem() {
        LessonInstance lessonInstance = LessonInstance.builder()
                .id(9L)
                .scheduleLesson(scheduleLesson())
                .lessonDate(LESSON_DATE)
                .build();

        TeacherScheduleItem result = mapper.toTeacherScheduleItem(lessonInstance);

        assertThat(result.lessonInstance()).isEqualTo(new LessonInstanceDto(9L, LESSON_DATE));
        assertThat(result.subject()).isEqualTo(new SubjectResponseDto(7L, "Алгебра"));
        assertThat(result.scheduleLesson()).isEqualTo(new ScheduleLessonResponse(3L, 1, null, "101"));
        assertThat(result.schoolClass()).isEqualTo(new SchoolClassResponse(
                5L,
                "5А",
                new AcademicYearResponse(1L, "2025-2026", null, LocalDate.of(2025, 9, 1), LocalDate.of(2026, 5, 31), false),
                10L));
        assertThat(result.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void shouldReturnNullWhenLessonInstanceIsNull() {
        assertThat(mapper.toTeacherScheduleItem(null)).isNull();
    }

    @Test
    void shouldMapAllFieldsToDiaryDto() {
        ScheduleLessonDiaryDto result = mapper.toScheduleLessonDiaryDto(scheduleLesson());

        assertThat(result).isEqualTo(new ScheduleLessonDiaryDto(
                3L,
                DayOfWeek.MONDAY,
                1,
                new SubjectResponseDto(7L, "Алгебра"),
                "101"));
    }

    @Test
    void shouldReturnNullWhenDiarySourceIsNull() {
        assertThat(mapper.toScheduleLessonDiaryDto(null)).isNull();
    }
}

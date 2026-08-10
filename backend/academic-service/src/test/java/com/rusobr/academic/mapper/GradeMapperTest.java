package com.rusobr.academic.mapper;

import com.rusobr.academic.application.mapper.GradeMapper;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.infrastructure.persistence.projection.GradeDetailProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeJournalItemProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeWithSubjectNameProjection;
import com.rusobr.academic.infrastructure.persistence.projection.StudentAverageProjection;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import com.rusobr.academic.web.dto.grade.DateScheduleAssignDto;
import com.rusobr.academic.web.dto.grade.GetGradeDataDto;
import com.rusobr.academic.web.dto.grade.GradeDetailResponse;
import com.rusobr.academic.web.dto.grade.GradeJournalItemDto;
import com.rusobr.academic.web.dto.grade.GradeJournalResponse;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.grade.GradeWithSubjectNameResponse;
import com.rusobr.academic.web.dto.grade.StudentAverageDto;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.common.dto.UserFeignResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GradeMapperTest {

    private final GradeMapper mapper = Mappers.getMapper(GradeMapper.class);

    private static final LocalDate LESSON_DATE = LocalDate.of(2026, 2, 10);

    private Grade grade() {
        return Grade.builder()
                .id(1L)
                .studentId(5L)
                .value(4)
                .weight(2)
                .type(GradeType.CONTROL)
                .build();
    }

    @Test
    void shouldMapAllFieldsToCreateGradeResponse() {
        LessonInstanceDto lessonInstance = new LessonInstanceDto(9L, LESSON_DATE);

        CreateGradeResponse result = mapper.toCreateGradeResponseDto(grade(), lessonInstance);

        assertThat(result).isEqualTo(new CreateGradeResponse(1L, 5L, lessonInstance, 4, 2, GradeType.CONTROL));
    }

    @Test
    void shouldReturnNullWhenGradeAndLessonInstanceAreNull() {
        assertThat(mapper.toCreateGradeResponseDto(null, null)).isNull();
    }

    @Test
    void shouldReturnNullLessonInstanceWhenDtoIsNull() {
        CreateGradeResponse result = mapper.toCreateGradeResponseDto(grade(), null);

        assertThat(result.lessonInstance()).isNull();
        assertThat(result.gradeId()).isEqualTo(1L);
    }

    @Test
    void shouldMapAllFieldsToGradeResponse() {
        GradeResponse result = mapper.toGradeResponseDto(grade());

        assertThat(result).isEqualTo(new GradeResponse(1L, 5L, 4, 2, GradeType.CONTROL));
    }

    @ParameterizedTest
    @EnumSource(GradeType.class)
    void shouldMapGradeType(GradeType type) {
        Grade source = Grade.builder()
                .id(1L)
                .studentId(5L)
                .value(4)
                .weight(2)
                .type(type)
                .build();

        GradeResponse result = mapper.toGradeResponseDto(source);

        assertThat(result.type()).isEqualTo(type);
    }

    @Test
    void shouldReturnNullWhenGradeResponseSourceIsNull() {
        assertThat(mapper.toGradeResponseDto(null)).isNull();
    }

    @Test
    void shouldMapNullValueAndWeightToZero() {
        Grade source = Grade.builder()
                .id(1L)
                .studentId(5L)
                .value(null)
                .weight(null)
                .type(GradeType.CONTROL)
                .build();

        GradeResponse result = mapper.toGradeResponseDto(source);

        assertThat(result.value()).isZero();
        assertThat(result.weight()).isZero();
    }

    @Test
    void shouldMapRequestToGradeEntity() {
        CreateGradeRequest request = new CreateGradeRequest(5L, 9L, 4, 2, GradeType.HOMEWORK);

        Grade result = mapper.toGrade(request);

        assertThat(result.getId()).isNull();
        assertThat(result.getStudentId()).isEqualTo(5L);
        assertThat(result.getLessonInstance()).isNull();
        assertThat(result.getValue()).isEqualTo(4);
        assertThat(result.getWeight()).isEqualTo(2);
        assertThat(result.getType()).isEqualTo(GradeType.HOMEWORK);
    }

    @Test
    void shouldReturnNullEntityWhenCreateRequestIsNull() {
        assertThat(mapper.toGrade(null)).isNull();
    }

    @Test
    void shouldUpdateMappedFieldsAndKeepType() {
        Grade entity = grade();
        CreateGradeRequest request = new CreateGradeRequest(6L, 9L, 5, 1, GradeType.TEST);

        mapper.updateEntityFromDto(request, entity);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getStudentId()).isEqualTo(6L);
        assertThat(entity.getValue()).isEqualTo(5);
        assertThat(entity.getWeight()).isEqualTo(1);
        assertThat(entity.getType()).isEqualTo(GradeType.CONTROL);
    }

    @Test
    void shouldDoNothingWhenUpdatingWithNullDto() {
        Grade entity = grade();

        mapper.updateEntityFromDto(null, entity);

        assertThat(entity).usingRecursiveComparison().isEqualTo(grade());
    }

    @Test
    void shouldOverwriteValueWithNullWhenDtoValueIsNull() {
        Grade entity = grade();
        CreateGradeRequest request = new CreateGradeRequest(6L, 9L, null, null, GradeType.TEST);

        mapper.updateEntityFromDto(request, entity);

        assertThat(entity.getValue()).isNull();
        assertThat(entity.getWeight()).isNull();
    }

    @Test
    void shouldMapAllFieldsToGradeJournalResponse() {
        List<UserFeignResponse> users = List.of(UserFeignResponse.builder()
                .id(5L)
                .firstName("Иван")
                .lastName("Петров")
                .build());
        List<DateScheduleAssignDto> dates = List.of(new DateScheduleAssignDto(LESSON_DATE, 3L));
        List<GradeJournalItemDto> grades = List.of(new GradeJournalItemDto(
                5L, 1L, 4, GradeType.CONTROL, LESSON_DATE));
        AcademicPeriodResponse period = new AcademicPeriodResponse(
                7L,
                "I полугодие",
                new AcademicYearResponse(1L, "2025-2026", null, LocalDate.of(2025, 9, 1), LocalDate.of(2026, 5, 31), false),
                true,
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 12, 31));
        GetGradeDataDto gradeData = new GetGradeDataDto(dates, grades, period);

        GradeJournalResponse result = mapper.toGradeJournalResponse(users, gradeData);

        assertThat(result).isEqualTo(new GradeJournalResponse(users, dates, grades, period));
    }

    @Test
    void shouldReturnNullWhenUsersAndGradeDataAreNull() {
        assertThat(mapper.toGradeJournalResponse(null, null)).isNull();
    }

    @Test
    void shouldMapEmptyListsAsEmptyLists() {
        GetGradeDataDto gradeData = new GetGradeDataDto(List.of(), List.of(), null);

        GradeJournalResponse result = mapper.toGradeJournalResponse(List.of(), gradeData);

        assertThat(result.users()).isEmpty();
        assertThat(result.dates()).isEmpty();
        assertThat(result.grades()).isEmpty();
    }

    @Test
    void shouldMapNullListsAsNull() {
        GetGradeDataDto gradeData = new GetGradeDataDto(null, null, null);

        GradeJournalResponse result = mapper.toGradeJournalResponse(null, gradeData);

        assertThat(result.users()).isNull();
        assertThat(result.dates()).isNull();
        assertThat(result.grades()).isNull();
    }

    @Test
    void shouldMapProjectionToJournalItem() {
        GradeJournalItemProjection projection = mock(GradeJournalItemProjection.class);
        when(projection.getStudentId()).thenReturn(5L);
        when(projection.getGradeId()).thenReturn(1L);
        when(projection.getValue()).thenReturn(4);
        when(projection.getType()).thenReturn(GradeType.CONTROL);
        when(projection.getLessonDate()).thenReturn(LESSON_DATE);

        GradeJournalItemDto result = mapper.toItemProjection(projection);

        assertThat(result.studentId()).isEqualTo(5L);
        assertThat(result.gradeId()).isEqualTo(1L);
        assertThat(result.value()).isEqualTo(4);
        assertThat(result.type()).isEqualTo(GradeType.CONTROL);
        // Проекция предоставляет getLessonDate(), целевое поле date ожидает дату урока
        assertThat(result.date()).isEqualTo(LESSON_DATE);
    }

    @Test
    void shouldReturnNullWhenJournalItemProjectionIsNull() {
        assertThat(mapper.toItemProjection(null)).isNull();
    }

    @Test
    void shouldMapAllFieldsToWithSubjectNameResponse() {
        GradeWithSubjectNameProjection projection = mock(GradeWithSubjectNameProjection.class);
        when(projection.getId()).thenReturn(1L);
        when(projection.getValue()).thenReturn(4);
        when(projection.getGradeType()).thenReturn(GradeType.CONTROL);
        when(projection.getSubjectName()).thenReturn("Алгебра");

        GradeWithSubjectNameResponse result = mapper.toWithSubjectNameResponse(projection);

        assertThat(result).isEqualTo(new GradeWithSubjectNameResponse(1L, 4, GradeType.CONTROL, "Алгебра"));
    }

    @Test
    void shouldReturnNullWhenWithSubjectNameProjectionIsNull() {
        assertThat(mapper.toWithSubjectNameResponse(null)).isNull();
    }

    @Test
    void shouldMapAllFieldsToStudentAverageDto() {
        StudentAverageProjection projection = mock(StudentAverageProjection.class);
        when(projection.getStudentId()).thenReturn(5L);
        when(projection.getAverage()).thenReturn(4.25);

        StudentAverageDto result = mapper.toStudentAverageDto(projection);

        assertThat(result).isEqualTo(new StudentAverageDto(5L, 4.25));
    }

    @Test
    void shouldReturnNullWhenStudentAverageProjectionIsNull() {
        assertThat(mapper.toStudentAverageDto(null)).isNull();
    }

    @Test
    void shouldMapAllFieldsToGradeDetailResponse() {
        GradeDetailProjection projection = mock(GradeDetailProjection.class);
        Instant date = Instant.parse("2026-02-10T09:00:00Z");
        when(projection.getId()).thenReturn(1L);
        when(projection.getDate()).thenReturn(date);
        when(projection.getGradeType()).thenReturn(GradeType.TEST);
        when(projection.getValue()).thenReturn(4);
        when(projection.getWeight()).thenReturn(2);
        UserFeignResponse teacher = UserFeignResponse.builder()
                .id(10L)
                .firstName("Иван")
                .lastName("Петров")
                .build();

        GradeDetailResponse result = mapper.toGradeDetailResponse(projection, teacher);

        assertThat(result).isEqualTo(new GradeDetailResponse(1L, date, GradeType.TEST, 4, 2, teacher));
    }

    @Test
    void shouldReturnNullWhenGradeDetailSourcesAreNull() {
        assertThat(mapper.toGradeDetailResponse(null, null)).isNull();
    }

    @Test
    void shouldReturnNullTeacherWhenTeacherIsNull() {
        GradeDetailProjection projection = mock(GradeDetailProjection.class);
        when(projection.getId()).thenReturn(1L);

        GradeDetailResponse result = mapper.toGradeDetailResponse(projection, null);

        assertThat(result.teacher()).isNull();
        assertThat(result.id()).isEqualTo(1L);
    }
}

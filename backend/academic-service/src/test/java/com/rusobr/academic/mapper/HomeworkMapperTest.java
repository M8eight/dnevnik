package com.rusobr.academic.mapper;

import com.rusobr.academic.application.mapper.HomeworkMapper;
import com.rusobr.academic.domain.model.Homework;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.persistence.projection.HomeworkWithSubjectProjection;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;
import com.rusobr.academic.web.dto.homework.HomeworkSimpleResponse;
import com.rusobr.academic.web.dto.homework.HomeworkWithSubjectResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HomeworkMapperTest {

    private final HomeworkMapper mapper = Mappers.getMapper(HomeworkMapper.class);

    private static final LocalDate LESSON_DATE = LocalDate.of(2026, 2, 10);
    private static final String HOMEWORK_TEXT = "Решить задачи 1-5";

    @Test
    void shouldMapAllFieldsToHomeworkResponse() {
        LessonInstance lesson = LessonInstance.builder()
                .id(9L)
                .lessonDate(LESSON_DATE)
                .build();
        Homework homework = Homework.builder()
                .id(1L)
                .text(HOMEWORK_TEXT)
                .lessonInstance(lesson)
                .build();

        HomeworkResponse result = mapper.toHomeworkResponse(homework);

        assertThat(result).isEqualTo(new HomeworkResponse(
                1L,
                HOMEWORK_TEXT,
                new LessonInstanceDto(9L, LESSON_DATE)));
    }

    @Test
    void shouldReturnNullWhenSourceIsNull() {
        assertThat(mapper.toHomeworkResponse(null)).isNull();
    }

    @Test
    void shouldReturnNullLessonInstanceWhenNestedIsNull() {
        Homework homework = Homework.builder()
                .id(1L)
                .text(HOMEWORK_TEXT)
                .build();

        HomeworkResponse result = mapper.toHomeworkResponse(homework);

        assertThat(result.lessonInstance()).isNull();
    }

    @Test
    void shouldMapAllFieldsToSimpleResponse() {
        Homework homework = Homework.builder()
                .id(1L)
                .text(HOMEWORK_TEXT)
                .build();

        HomeworkSimpleResponse result = mapper.toHomeworkSimpleResponse(homework);

        assertThat(result).isEqualTo(new HomeworkSimpleResponse(1L, HOMEWORK_TEXT));
    }

    @Test
    void shouldReturnNullWhenSimpleSourceIsNull() {
        assertThat(mapper.toHomeworkSimpleResponse(null)).isNull();
    }

    @Test
    void shouldMapAllFieldsFromProjection() {
        HomeworkWithSubjectProjection projection = mock(HomeworkWithSubjectProjection.class);
        when(projection.getId()).thenReturn(1L);
        when(projection.getText()).thenReturn(HOMEWORK_TEXT);
        when(projection.getSubjectName()).thenReturn("Алгебра");

        HomeworkWithSubjectResponse result = mapper.toWithSubjectResponse(projection);

        assertThat(result).isEqualTo(new HomeworkWithSubjectResponse(1L, HOMEWORK_TEXT, "Алгебра"));
    }

    @Test
    void shouldReturnNullWhenProjectionIsNull() {
        assertThat(mapper.toWithSubjectResponse(null)).isNull();
    }

    @Test
    void shouldMapNullSubjectNameAsNull() {
        HomeworkWithSubjectProjection projection = mock(HomeworkWithSubjectProjection.class);
        when(projection.getId()).thenReturn(1L);
        when(projection.getText()).thenReturn(HOMEWORK_TEXT);

        HomeworkWithSubjectResponse result = mapper.toWithSubjectResponse(projection);

        assertThat(result.subjectName()).isNull();
    }
}

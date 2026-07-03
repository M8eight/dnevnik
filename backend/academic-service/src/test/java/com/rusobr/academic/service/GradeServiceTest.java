package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.GradeMapper;
import com.rusobr.academic.application.mapper.LessonInstanceMapper;
import com.rusobr.academic.application.service.AcademicPeriodService;
import com.rusobr.academic.application.service.GradeService;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock private GradeRepository gradeRepository;
    @Mock private GradeMapper gradeMapper;
    @Mock private AcademicPeriodRepository academicPeriodRepository;
    @Mock private LessonInstanceRepository lessonInstanceRepository;
    @Mock private LessonInstanceMapper lessonInstanceMapper;
    @Mock private AcademicPeriodService academicPeriodService;

    @InjectMocks private GradeService service;

    private static final Long GRADE_ID = 1L;
    private static final Long STUDENT_ID = 10L;
    private static final Long PERIOD_ID = 5L;
    private static final Long LESSON_ID = 100L;
    private static final LocalDate DATE = LocalDate.of(2026, 10, 20);

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("успешно возвращает GradeResponse")
        void success() {
            Grade grade = Grade.builder().id(GRADE_ID).build();
            GradeResponse response = new GradeResponse(GRADE_ID, STUDENT_ID, 5, 1, GradeType.CONTROL);

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(grade));
            when(gradeMapper.toGradeResponseDto(grade)).thenReturn(response);

            GradeResponse result = service.getById(GRADE_ID);

            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("оценка не найдена — бросает NotFoundException")
        void notFound() {
            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(GRADE_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAverageByPeriod")
    class GetAverageByPeriod {

        @Test
        @DisplayName("успешно возвращает средний балл с округлением до 2 знаков")
        void success() {
            AcademicPeriod period = AcademicPeriod.builder()
                    .startDate(DATE.minusMonths(1))
                    .endDate(DATE.plusMonths(1))
                    .build();

            // Имитируем среднее число, которое требует округления (например, 4.3333)
            when(academicPeriodService.getById(PERIOD_ID)).thenReturn(period);
            when(gradeRepository.getAverageGrade(STUDENT_ID, period.getStartDate(), period.getEndDate()))
                    .thenReturn(4.3333333);

            Double result = service.getAverageByPeriod(STUDENT_ID, PERIOD_ID);

            assertThat(result).isEqualTo(4.33);
        }

        @Test
        @DisplayName("если оценок нет — возвращает null")
        void returnsNull_whenNoGrades() {
            AcademicPeriod period = AcademicPeriod.builder().build();
            when(academicPeriodService.getById(PERIOD_ID)).thenReturn(period);
            when(gradeRepository.getAverageGrade(anyLong(), any(), any())).thenReturn(0.0);

            Double result = service.getAverageByPeriod(STUDENT_ID, PERIOD_ID);

            assertThat(result).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("успешно создает оценку, если период открыт")
        void success() {
            CreateGradeRequest request = new CreateGradeRequest(STUDENT_ID, LESSON_ID, 1L, 5, 2, GradeType.TEST);
            LessonInstance lesson = LessonInstance.builder().id(LESSON_ID).lessonDate(DATE).build();
            AcademicPeriod period = AcademicPeriod.builder().closed(false).build();
            Grade grade = new Grade();
            LessonInstanceDto lessonDto = new LessonInstanceDto(LESSON_ID, DATE);
            CreateGradeResponse expectedResponse = mock(CreateGradeResponse.class);

            when(lessonInstanceRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
            when(academicPeriodService.getByDate(DATE)).thenReturn(period);
            when(gradeMapper.toGrade(request)).thenReturn(grade);
            when(gradeRepository.save(grade)).thenReturn(grade);
            when(lessonInstanceMapper.toLessonInstanceDto(lesson)).thenReturn(lessonDto);
            when(gradeMapper.toCreateGradeResponseDto(grade, lessonDto)).thenReturn(expectedResponse);

            CreateGradeResponse result = service.create(request);

            assertThat(result).isEqualTo(expectedResponse);
            assertThat(grade.getLessonInstance()).isEqualTo(lesson);
        }

        @Test
        @DisplayName("период закрыт — бросает ConflictException")
        void periodClosed_throwsException() {
            CreateGradeRequest request = new CreateGradeRequest(STUDENT_ID, LESSON_ID, 1L, 5, 2, GradeType.TEST);
            LessonInstance lesson = LessonInstance.builder().lessonDate(DATE).build();
            AcademicPeriod period = AcademicPeriod.builder().closed(true).build();

            when(lessonInstanceRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
            when(academicPeriodService.getByDate(DATE)).thenReturn(period);

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("is already closed");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("успешно удаляет оценку")
        void success() {
            LessonInstance lesson = LessonInstance.builder().lessonDate(DATE).build();
            Grade grade = Grade.builder().id(GRADE_ID).lessonInstance(lesson).build();
            AcademicPeriod period = AcademicPeriod.builder().closed(false).build();

            when(gradeRepository.findWithLessonInstanceById(GRADE_ID)).thenReturn(Optional.of(grade));
            when(academicPeriodService.getByDate(DATE)).thenReturn(period);

            service.delete(GRADE_ID);

            verify(gradeRepository).delete(grade);
        }

        @Test
        @DisplayName("удаление запрещено, если период уже закрыт")
        void periodClosed_throwsException() {
            LessonInstance lesson = LessonInstance.builder().lessonDate(DATE).build();
            Grade grade = Grade.builder().lessonInstance(lesson).build();
            AcademicPeriod period = AcademicPeriod.builder().closed(true).build();

            when(gradeRepository.findWithLessonInstanceById(GRADE_ID)).thenReturn(Optional.of(grade));
            when(academicPeriodService.getByDate(DATE)).thenReturn(period);

            assertThatThrownBy(() -> service.delete(GRADE_ID))
                    .isInstanceOf(ConflictException.class);

            verify(gradeRepository, never()).delete(any());
        }
    }
}
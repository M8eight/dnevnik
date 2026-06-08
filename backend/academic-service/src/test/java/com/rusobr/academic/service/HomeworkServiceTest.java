package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.HomeworkMapper;
import com.rusobr.academic.application.service.HomeworkService;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Homework;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.HomeworkRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.web.dto.homework.HomeworkRequest;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;
import com.rusobr.academic.web.dto.homework.HomeworkWithSubjectResponse;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeworkServiceTest {

    @Mock private HomeworkRepository homeworkRepository;
    @Mock private LessonInstanceRepository lessonInstanceRepository;
    @Mock private HomeworkMapper homeworkMapper;
    @Mock private AcademicPeriodRepository academicPeriodRepository;

    @InjectMocks private HomeworkService service;

    private static final Long HW_ID = 1L;
    private static final Long LESSON_ID = 100L;
    private static final LocalDate DATE = LocalDate.of(2026, 11, 15);

    @Nested
    @DisplayName("getByDate")
    class GetByDate {
        @Test
        @DisplayName("успешно возвращает список ДЗ с названием предмета")
        void success() {
            Long studentId = 10L;

            // Мокаем интерфейс проекции, который ожидает репозиторий и маппер
            com.rusobr.academic.infrastructure.persistence.projection.HomeworkWithSubjectProjection projection =
                    mock(com.rusobr.academic.infrastructure.persistence.projection.HomeworkWithSubjectProjection.class);

            HomeworkWithSubjectResponse response = new HomeworkWithSubjectResponse(HW_ID, "Стр. 10 упр. 5", "Алгебра");

            // Обучаем моки работать с проекцией вместо сущности
            when(homeworkRepository.findHomeworksByDate(DATE, studentId)).thenReturn(List.of(projection));
            when(homeworkMapper.toWithSubjectResponse(projection)).thenReturn(response);

            List<HomeworkWithSubjectResponse> result = service.getByDate(DATE, studentId);

            assertThat(result).hasSize(1).contains(response);
            verify(homeworkRepository).findHomeworksByDate(DATE, studentId);
            verify(homeworkMapper).toWithSubjectResponse(projection);
        }
    }

    @Nested
    @DisplayName("getByAssignment")
    class GetByAssignment {
        @Test
        @DisplayName("успешно возвращает страницу с ДЗ")
        void success() {
            Long assignmentId = 5L;
            Pageable pageable = PageRequest.of(0, 10);
            Homework homework = new Homework();
            HomeworkResponse response = mock(HomeworkResponse.class);
            Page<Homework> page = new PageImpl<>(List.of(homework));

            when(homeworkRepository.findHomeworksByTeachingAssignmentId(assignmentId, pageable)).thenReturn(page);
            when(homeworkMapper.toHomeworkResponse(homework)).thenReturn(response);

            Page<HomeworkResponse> result = service.getByAssignment(assignmentId, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(homeworkMapper).toHomeworkResponse(homework);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {
        private final HomeworkRequest request = new HomeworkRequest("Учить теорему", LESSON_ID);

        @Test
        @DisplayName("успешно создает ДЗ, если период открыт")
        void success() {
            LessonInstance lesson = LessonInstance.builder().id(LESSON_ID).lessonDate(DATE).build();
            AcademicPeriod period = AcademicPeriod.builder().closed(false).build();
            Homework savedHw = Homework.builder().id(HW_ID).text(request.text()).build();
            HomeworkResponse expectedResponse = mock(HomeworkResponse.class);

            when(lessonInstanceRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
            when(academicPeriodRepository.findByDate(DATE)).thenReturn(Optional.of(period));
            when(homeworkRepository.save(any(Homework.class))).thenReturn(savedHw);
            when(homeworkMapper.toHomeworkResponse(savedHw)).thenReturn(expectedResponse);

            HomeworkResponse result = service.create(request);

            assertThat(result).isEqualTo(expectedResponse);
            verify(homeworkRepository).save(argThat(hw -> hw.getText().equals(request.text())));
        }

        @Test
        @DisplayName("период закрыт — бросает ConflictException")
        void periodClosed() {
            LessonInstance lesson = LessonInstance.builder().lessonDate(DATE).build();
            AcademicPeriod closedPeriod = AcademicPeriod.builder().closed(true).build();

            when(lessonInstanceRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
            when(academicPeriodRepository.findByDate(DATE)).thenReturn(Optional.of(closedPeriod));

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Academic Period is closed");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("успешно удаляет ДЗ, если период открыт")
        void success() {
            LessonInstance lesson = LessonInstance.builder().lessonDate(DATE).build();
            Homework homework = Homework.builder().id(HW_ID).lessonInstance(lesson).build();
            AcademicPeriod openPeriod = AcademicPeriod.builder().closed(false).build();

            when(homeworkRepository.findWithLessonInstanceById(HW_ID)).thenReturn(Optional.of(homework));
            when(academicPeriodRepository.findByDate(DATE)).thenReturn(Optional.of(openPeriod));

            service.delete(HW_ID);

            verify(homeworkRepository).delete(homework);
        }

        @Test
        @DisplayName("ДЗ не найдено — бросает NotFoundException")
        void notFound() {
            when(homeworkRepository.findWithLessonInstanceById(HW_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(HW_ID))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("удаление запрещено — период закрыт")
        void periodClosed() {
            LessonInstance lesson = LessonInstance.builder().lessonDate(DATE).build();
            Homework homework = Homework.builder().lessonInstance(lesson).build();
            AcademicPeriod closedPeriod = AcademicPeriod.builder().closed(true).build();

            when(homeworkRepository.findWithLessonInstanceById(HW_ID)).thenReturn(Optional.of(homework));
            when(academicPeriodRepository.findByDate(DATE)).thenReturn(Optional.of(closedPeriod));

            assertThatThrownBy(() -> service.delete(HW_ID))
                    .isInstanceOf(ConflictException.class);

            verify(homeworkRepository, never()).delete(any());
        }


    }
}
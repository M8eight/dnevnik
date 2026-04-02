package com.rusobr.academic;

import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.AcademicPeriodMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.infrastructure.service.GradeDataService;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.dto.grade.DateScheduleAssignDto;
import com.rusobr.academic.web.dto.grade.GetGradeDataDto;
import com.rusobr.academic.web.dto.grade.GradeJournalItemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GradeDataServiceTest {

    @InjectMocks
    private GradeDataService gradeDataService;

    @Mock
    private ScheduleLessonRepository scheduleLessonRepository;
    @Mock
    private AcademicPeriodRepository academicPeriodRepository;
    @Mock
    private AcademicPeriodMapper academicPeriodMapper;
    @Mock
    private GradeRepository gradeRepository;

    @Test
    @DisplayName("Вернуть данные о расписании при правильных данных")
    void getGradeData_ShouldReturnCorrectDatesAndGrades_WhenValidRequest() {
        Long teachingAssignmentId = 1L;
        LocalDate date = LocalDate.now();

        Optional<AcademicPeriod> resAcademicPeriod = Optional.of(
                AcademicPeriod.builder()
                        .startDate(LocalDate.of(2025, 9, 1))
                        .endDate(LocalDate.of(2025, 10, 26))
                        .build()
        );

        // Теперь мокируем List<ScheduleLesson> вместо List<DayOfWeek>
        ScheduleLesson mondayLesson = new ScheduleLesson();
        mondayLesson.setId(10L);
        mondayLesson.setDayOfWeek(DayOfWeek.MONDAY);

        ScheduleLesson wednesdayLesson = new ScheduleLesson();
        wednesdayLesson.setId(11L);
        wednesdayLesson.setDayOfWeek(DayOfWeek.WEDNESDAY);

        List<ScheduleLesson> resScheduleLessons = List.of(mondayLesson, wednesdayLesson);

        List<GradeJournalItemDto> grades = List.of(
                new GradeJournalItemDto(1L, 1L, 5, GradeType.TEST, LocalDate.of(2025, 9, 7))
        );

        when(academicPeriodRepository.findByDate(any())).thenReturn(resAcademicPeriod);
        when(scheduleLessonRepository.findByTeachingAssignmentId(teachingAssignmentId))
                .thenReturn(resScheduleLessons);
        when(gradeRepository.getClassGrades(teachingAssignmentId)).thenReturn(grades);
        when(academicPeriodMapper.toDto(any(AcademicPeriod.class))).thenReturn(
                new AcademicPeriodResponse(1L,
                        "Первая четверть",
                        "2025-2026",
                        false,
                        LocalDate.of(2025, 9, 1),
                        LocalDate.of(2025, 10, 26))
        );

        GetGradeDataDto res = gradeDataService.getGradeData(teachingAssignmentId, date);

        verify(academicPeriodRepository).findByDate(date);
        verify(scheduleLessonRepository).findByTeachingAssignmentId(teachingAssignmentId);
        verify(gradeRepository).getClassGrades(teachingAssignmentId);

        // Оценки попали в dto
        assertEquals(grades.size(), res.grades().size());

        // Первая дата — понедельник 1 сентября 2025
        assertEquals(DayOfWeek.MONDAY, res.dates().get(0).date().getDayOfWeek());
        assertEquals(LocalDate.of(2025, 9, 1), res.dates().get(0).date());

        // Последняя дата — среда 22 октября 2025
        DateScheduleAssignDto last = res.dates().get(res.dates().size() - 1);
        assertEquals(LocalDate.of(2025, 10, 22), last.date());
    }

    @Test
    @DisplayName("Вернуть NotFoundException если не найден период по дате")
    void shouldThrowNotFoundException_WhenDateIsInvalid() {
        Long teachingAssignmentId = 1L;
        LocalDate date = LocalDate.now();

        when(academicPeriodRepository.findByDate(any())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> gradeDataService.getGradeData(teachingAssignmentId, date));

        assertEquals("Current academic period not found", ex.getMessage());
    }

    @Test
    @DisplayName("Должен вернуть NotFoundException если список уроков пустой")
    void shouldThrowNotFoundException_WhenScheduleLessonsIsEmpty() {
        Long teachingAssignmentId = 1L;
        LocalDate date = LocalDate.now();

        Optional<AcademicPeriod> resAcademicPeriod = Optional.of(
                AcademicPeriod.builder()
                        .schoolYear("2025-2026")
                        .startDate(LocalDate.of(2025, 9, 1))
                        .endDate(LocalDate.of(2025, 10, 26))
                        .isClosed(false)
                        .name("Первая четверть")
                        .build()
        );

        when(academicPeriodRepository.findByDate(any())).thenReturn(resAcademicPeriod);
        // Возвращаем пустой список ScheduleLesson
        when(scheduleLessonRepository.findByTeachingAssignmentId(any())).thenReturn(List.of());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> gradeDataService.getGradeData(teachingAssignmentId, date));

        // Сообщение изменилось в новой реализации
        assertEquals("Not found schedule lessons", ex.getMessage());
    }
}

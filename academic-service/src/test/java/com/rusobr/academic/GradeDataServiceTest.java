package com.rusobr.academic;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.AcademicPeriodMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.infrastructure.service.GradeDataService;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodDto;
import com.rusobr.academic.web.dto.grade.GradeJournalData;
import com.rusobr.academic.web.dto.grade.TeacherGradeDto;
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
        //Период из базы
        Optional<AcademicPeriod> resAcademicPeriod = Optional.of(
                AcademicPeriod.builder()
                        .startDate(LocalDate.of(2025, 9, 1))
                        .endDate(LocalDate.of(2025, 10, 26))
                        .build()
        );
        //По каким дням идут уроки из бд
        List<DayOfWeek> resDaysOfWeek = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        //Оценки из бд
        List<TeacherGradeDto> grades = List.of(new TeacherGradeDto(1L, 1L, 5, "TEST", LocalDate.of(2025, 9, 7)));


        when(academicPeriodRepository.findByDate(any())).thenReturn(resAcademicPeriod);
        when(scheduleLessonRepository.findDaysOfWeeksByTeachingAssignmentId(teachingAssignmentId))
                .thenReturn(resDaysOfWeek);
        when(gradeRepository.getClassGrades(teachingAssignmentId)).thenReturn(grades);
        when(academicPeriodMapper.toDto(any(AcademicPeriod.class))).thenReturn(
                new AcademicPeriodDto(1L,
                        "Первая четверть",
                        "2025-2026",
                        false,
                        LocalDate.of(2025, 9, 1), LocalDate.of(2025, 10, 26))
        );


        GradeJournalData res = gradeDataService.getGradeData(teachingAssignmentId, date);

        //Вызывался ли
        verify(academicPeriodRepository).findByDate(date);
        verify(scheduleLessonRepository).findDaysOfWeeksByTeachingAssignmentId(teachingAssignmentId);
        verify(gradeRepository).getClassGrades(teachingAssignmentId);

        //Попали ли оценки в dto
        assertEquals(res.grades().size(), grades.size());
        assertEquals(DayOfWeek.MONDAY, res.dates().get(0).getDayOfWeek());
        //Проверка границ дат
        assertEquals(LocalDate.of(2025, 9, 1), res.dates().get(0));
        assertEquals(LocalDate.of(2025, 10, 22), res.dates().get(res.dates().size() - 1));
    }

    @Test
    @DisplayName("Вернуть NotFoundException если не найден период по дате")
    void shouldThrowNotFoundException_WhenDateIsInvalid() {
        Long teachingAssignmentId = 1L;
        LocalDate date = LocalDate.now();

        when(academicPeriodRepository.findByDate(any())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> gradeDataService.getGradeData(teachingAssignmentId, date));

        assertEquals("Current academic period not found", ex.getMessage());
    }

    @Test
    @DisplayName("Должен вернуть NotFoundException если список дней пустой")
    void shouldThrowNotFoundException_WhenDaysOfWeekIsEmpty() {
        Long teachingAssignmentId = 1L;
        LocalDate date = LocalDate.now();
        //Период из базы
        Optional<AcademicPeriod> resAcademicPeriod = Optional.of(
                AcademicPeriod.builder()
                        .schoolYear("2025-2026")
                        .startDate(LocalDate.of(2025, 9, 1))
                        .endDate(LocalDate.of(2025, 10, 26))
                        .isClosed(false)
                        .name("Первая четверть").build()
        );
        List<DayOfWeek> resDaysOfWeek = List.of();

        when(academicPeriodRepository.findByDate(any())).thenReturn(resAcademicPeriod);
        when(scheduleLessonRepository.findDaysOfWeeksByTeachingAssignmentId(any())).thenReturn(resDaysOfWeek);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> gradeDataService.getGradeData(teachingAssignmentId, date));

        assertEquals("Not found day of weeks", ex.getMessage());

    }

}

package com.rusobr.academic.service;

import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.infrastructure.service.ScheduleService;
import com.rusobr.academic.web.dto.lessonInstance.LessonWeekItemDto;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonResponse;
import com.rusobr.academic.web.dto.scheduleLesson.SchoolLessonResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {

    @Mock LessonInstanceRepository lessonInstanceRepository;
    @Mock ScheduleLessonRepository scheduleLessonRepository;

    @InjectMocks ScheduleService service;

    @Nested
    @DisplayName("getScheduleByDate")
    class GetScheduleByDate {

        @Test
        @DisplayName("делегирует вызов репозиторию и возвращает результат")
        void delegatesToRepository() {
            LocalDate date = LocalDate.of(2026, 4, 14);
            ScheduleLessonResponse response = new ScheduleLessonResponse(1L, 2, "Физика", "202");

            when(scheduleLessonRepository.getScheduleByDate(1L, DayOfWeek.TUESDAY, date)).thenReturn(List.of(response));

            List<ScheduleLessonResponse> result = service.getScheduleByDate(1L, DayOfWeek.TUESDAY, date);

            assertThat(result).containsExactly(response);
            verify(scheduleLessonRepository).getScheduleByDate(1L, DayOfWeek.TUESDAY, date);
        }

        @Test
        @DisplayName("нет уроков на дату — возвращает пустой список")
        void noLessons_returnsEmpty() {
            LocalDate date = LocalDate.of(2026, 4, 14);

            when(scheduleLessonRepository.getScheduleByDate(1L, DayOfWeek.TUESDAY, date)).thenReturn(List.of());

            List<ScheduleLessonResponse> result = service.getScheduleByDate(1L, DayOfWeek.TUESDAY, date);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getWeekSchedule")
    class GetWeekSchedule {

        @Test
        @DisplayName("группирует уроки по дням недели в LinkedHashMap")
        void groupsByDayOfWeek() {
            SchoolLessonResponse mon1 = new SchoolLessonResponse(1L, 1, "Математика", "101", DayOfWeek.MONDAY);
            SchoolLessonResponse mon2 = new SchoolLessonResponse(2L, 2, "Физика", "202", DayOfWeek.MONDAY);
            SchoolLessonResponse wed1 = new SchoolLessonResponse(3L, 1, "Химия", "303", DayOfWeek.WEDNESDAY);

            when(scheduleLessonRepository.findAllByStudentId(1L)).thenReturn(List.of(mon1, mon2, wed1));

            Map<DayOfWeek, List<SchoolLessonResponse>> result = service.getWeekSchedule(1L);

            assertThat(result).hasSize(2);
            assertThat(result.get(DayOfWeek.MONDAY)).containsExactly(mon1, mon2);
            assertThat(result.get(DayOfWeek.WEDNESDAY)).containsExactly(wed1);
        }

        @Test
        @DisplayName("уроки отсортированы по dayOfWeek, затем по lessonNumber")
        void sortsByDayOfWeekThenLessonNumber() {
            SchoolLessonResponse wed2 = new SchoolLessonResponse(1L, 2, "Химия", "303", DayOfWeek.WEDNESDAY);
            SchoolLessonResponse mon1 = new SchoolLessonResponse(2L, 1, "Математика", "101", DayOfWeek.MONDAY);
            SchoolLessonResponse wed1 = new SchoolLessonResponse(3L, 1, "Физика", "202", DayOfWeek.WEDNESDAY);

            // репозиторий возвращает в произвольном порядке
            when(scheduleLessonRepository.findAllByStudentId(1L)).thenReturn(List.of(wed2, mon1, wed1));

            Map<DayOfWeek, List<SchoolLessonResponse>> result = service.getWeekSchedule(1L);

            // ключи идут в порядке вставки (MONDAY раньше WEDNESDAY после сортировки)
            assertThat(result.keySet()).containsExactly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
            // внутри среды — сначала lessonNumber=1, потом 2
            assertThat(result.get(DayOfWeek.WEDNESDAY)).containsExactly(wed1, wed2);
        }

        @Test
        @DisplayName("нет уроков — возвращает пустую Map")
        void noLessons_returnsEmptyMap() {
            when(scheduleLessonRepository.findAllByStudentId(1L)).thenReturn(List.of());

            Map<DayOfWeek, List<SchoolLessonResponse>> result = service.getWeekSchedule(1L);

            assertThat(result).isEmpty();
        }
    }
}
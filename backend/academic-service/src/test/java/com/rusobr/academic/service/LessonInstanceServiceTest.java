package com.rusobr.academic.service;

import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.mapper.LessonInstanceMapper;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.service.LessonInstanceService;
import com.rusobr.academic.web.dto.scheduleLesson.DiaryLessonResponse;
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
public class LessonInstanceServiceTest {

    @Mock LessonInstanceRepository lessonInstanceRepository;
    @Mock LessonInstanceMapper lessonInstanceMapper;

    @InjectMocks LessonInstanceService service;

    @Nested
    @DisplayName("getDiaryLessonsByStudentIdAndDateRange")
    class GetDiaryLessonsByStudentIdAndDateRange {

        @Test
        @DisplayName("группирует уроки по дате в TreeMap")
        void groupsByDateInTreeMap() {
            LocalDate monday = LocalDate.of(2026, 4, 13);
            LocalDate tuesday = LocalDate.of(2026, 4, 14);

            LessonInstance li1 = LessonInstance.builder().build();
            LessonInstance li2 = LessonInstance.builder().build();

            DiaryLessonResponse lesson1 = new DiaryLessonResponse(monday, "Математика", DayOfWeek.MONDAY, 1, "101", List.of(), List.of(), null);
            DiaryLessonResponse lesson2 = new DiaryLessonResponse(tuesday, "Физика", DayOfWeek.TUESDAY, 2, "202", List.of(), List.of(), null);

            when(lessonInstanceRepository.findDiaryLessonsByStudentIdAndDateRange(1L, monday, tuesday))
                    .thenReturn(List.of(li1, li2));
            when(lessonInstanceMapper.toDiaryLessonResponseList(List.of(li1, li2), 1L))
                    .thenReturn(List.of(lesson1, lesson2));

            Map<LocalDate, List<DiaryLessonResponse>> result =
                    service.getDiaryLessonsByStudentIdAndDateRange(1L, monday, tuesday);

            assertThat(result).hasSize(2);
            assertThat(result.get(monday)).containsExactly(lesson1);
            assertThat(result.get(tuesday)).containsExactly(lesson2);
        }

        @Test
        @DisplayName("несколько уроков в один день — группируются под одним ключом")
        void multipleLessonsOnSameDate_groupedTogether() {
            LocalDate monday = LocalDate.of(2026, 4, 13);

            LessonInstance li1 = LessonInstance.builder().build();
            LessonInstance li2 = LessonInstance.builder().build();

            DiaryLessonResponse lesson1 = new DiaryLessonResponse(monday, "Математика", DayOfWeek.MONDAY, 1, "101", List.of(), List.of(), null);
            DiaryLessonResponse lesson2 = new DiaryLessonResponse(monday, "Физика", DayOfWeek.MONDAY, 2, "202", List.of(), List.of(), null);

            when(lessonInstanceRepository.findDiaryLessonsByStudentIdAndDateRange(1L, monday, monday))
                    .thenReturn(List.of(li1, li2));
            when(lessonInstanceMapper.toDiaryLessonResponseList(List.of(li1, li2), 1L))
                    .thenReturn(List.of(lesson1, lesson2));

            Map<LocalDate, List<DiaryLessonResponse>> result =
                    service.getDiaryLessonsByStudentIdAndDateRange(1L, monday, monday);

            assertThat(result).hasSize(1);
            assertThat(result.get(monday)).containsExactly(lesson1, lesson2);
        }

        @Test
        @DisplayName("результат — TreeMap, ключи отсортированы по дате")
        void resultIsTreeMap_keysAreSorted() {
            LocalDate wednesday = LocalDate.of(2026, 4, 15);
            LocalDate monday = LocalDate.of(2026, 4, 13);

            LessonInstance li1 = LessonInstance.builder().build();
            LessonInstance li2 = LessonInstance.builder().build();

            DiaryLessonResponse lessonWed = new DiaryLessonResponse(wednesday, "Химия", DayOfWeek.WEDNESDAY, 1, "303", List.of(), List.of(), null);
            DiaryLessonResponse lessonMon = new DiaryLessonResponse(monday, "История", DayOfWeek.MONDAY, 3, "104", List.of(), List.of(), null);

            when(lessonInstanceRepository.findDiaryLessonsByStudentIdAndDateRange(1L, monday, wednesday))
                    .thenReturn(List.of(li1, li2));
            // маппер возвращает в "перемешанном" порядке — среда раньше понедельника
            when(lessonInstanceMapper.toDiaryLessonResponseList(List.of(li1, li2), 1L))
                    .thenReturn(List.of(lessonWed, lessonMon));

            Map<LocalDate, List<DiaryLessonResponse>> result =
                    service.getDiaryLessonsByStudentIdAndDateRange(1L, monday, wednesday);

            assertThat(result.keySet()).containsExactly(monday, wednesday);
        }

        @Test
        @DisplayName("нет уроков в диапазоне — возвращает пустую Map")
        void noLessons_returnsEmptyMap() {
            LocalDate start = LocalDate.of(2026, 4, 13);
            LocalDate end = LocalDate.of(2026, 4, 19);

            when(lessonInstanceRepository.findDiaryLessonsByStudentIdAndDateRange(1L, start, end))
                    .thenReturn(List.of());
            when(lessonInstanceMapper.toDiaryLessonResponseList(List.of(), 1L))
                    .thenReturn(List.of());

            Map<LocalDate, List<DiaryLessonResponse>> result =
                    service.getDiaryLessonsByStudentIdAndDateRange(1L, start, end);

            assertThat(result).isEmpty();
        }
    }
}
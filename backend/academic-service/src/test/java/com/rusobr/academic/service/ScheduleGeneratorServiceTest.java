package com.rusobr.academic.service;

import com.rusobr.academic.application.service.ScheduleGeneratorService;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.common.exception.NotFoundException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleGeneratorServiceTest {

    @Mock private AcademicPeriodRepository academicPeriodRepository;
    @Mock private LessonInstanceRepository lessonInstanceRepository;
    @Mock private ScheduleLessonRepository scheduleLessonRepository;

    @InjectMocks private ScheduleGeneratorService scheduleGeneratorService;

    private static final LocalDate PERIOD_START = LocalDate.of(2026, 6, 1);
    private static final LocalDate PERIOD_END = LocalDate.of(2026, 6, 30);
    private static final LocalDate VALID_TO = LocalDate.of(2026, 6, 22);

    private AcademicPeriod period(LocalDate start, LocalDate end) {
        return AcademicPeriod.builder()
                .startDate(start)
                .endDate(end)
                .build();
    }

    private ScheduleLesson lesson(DayOfWeek dayOfWeek, LocalDate validFrom, LocalDate validTo) {
        return ScheduleLesson.builder()
                .dayOfWeek(dayOfWeek)
                .validFrom(validFrom)
                .validTo(validTo)
                .build();
    }

    @Nested
    @DisplayName("generateInstanceForUpcomingPeriod")
    class GenerateInstanceForUpcomingPeriod {

        @Test
        @DisplayName("генерирует уроки следующего периода, когда период заканчивается в ближайшие 14 дней")
        void success_generatesForNextPeriod() {
            AcademicPeriod current = period(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30));
            AcademicPeriod next = period(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30));

            when(academicPeriodRepository.findByEndDateBetween(any(), any())).thenReturn(current);
            when(academicPeriodRepository.findFirstByStartDateAfter(current.getEndDate())).thenReturn(Optional.of(next));

            scheduleGeneratorService.generateInstanceForUpcomingPeriod();

            verify(academicPeriodRepository).findFirstByStartDateAfter(current.getEndDate());
            verify(scheduleLessonRepository).findAllBetween(next.getStartDate(), next.getEndDate());
        }

        @Test
        @DisplayName("ничего не генерирует, если следующего периода нет")
        void noNextPeriod_doesNothing() {
            AcademicPeriod current = period(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30));

            when(academicPeriodRepository.findByEndDateBetween(any(), any())).thenReturn(current);
            when(academicPeriodRepository.findFirstByStartDateAfter(current.getEndDate())).thenReturn(Optional.empty());

            scheduleGeneratorService.generateInstanceForUpcomingPeriod();

            verify(scheduleLessonRepository, never()).findAllBetween(any(), any());
        }

        @Test
        @DisplayName("ничего не делает, если ни один период не заканчивается в ближайшие 14 дней")
        void noPeriodEndingInWindow_doesNothing() {
            when(academicPeriodRepository.findByEndDateBetween(any(), any())).thenReturn(null);

            scheduleGeneratorService.generateInstanceForUpcomingPeriod();

            verify(academicPeriodRepository, never()).findFirstByStartDateAfter(any());
            verify(scheduleLessonRepository, never()).findAllBetween(any(), any());
        }
    }

    @Nested
    @DisplayName("generateForPeriod")
    class GenerateForPeriod {

        @Test
        @DisplayName("генерирует уроки для всех занятий расписания внутри периода")
        void success_generatesForAllLessons() {
            AcademicPeriod period = period(PERIOD_START, PERIOD_END);
            ScheduleLesson sl1 = lesson(DayOfWeek.MONDAY, PERIOD_START, null);
            ScheduleLesson sl2 = lesson(DayOfWeek.TUESDAY, LocalDate.of(2026, 6, 2), null);

            when(scheduleLessonRepository.findAllBetween(PERIOD_START, PERIOD_END)).thenReturn(List.of(sl1, sl2));
            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(any(), any())).thenReturn(false);

            scheduleGeneratorService.generateForPeriod(period);

            verify(lessonInstanceRepository, times(5)).save(argThat(li -> li.getScheduleLesson() == sl1));
            verify(lessonInstanceRepository, times(5)).save(argThat(li -> li.getScheduleLesson() == sl2));
            // sl1 (пн): 1, 8, 15, 22, 29 июня; sl2 (вт): 2, 9, 16, 23, 30 июня
            verify(lessonInstanceRepository, times(10)).save(any(LessonInstance.class));
        }

        @Test
        @DisplayName("продолжает обработку остальных занятий, если для одного из них генерация упала")
        void continuesWhenOneLessonFails() {
            AcademicPeriod period = period(PERIOD_START, PERIOD_END);
            ScheduleLesson sl1 = lesson(DayOfWeek.MONDAY, PERIOD_START, null);
            ScheduleLesson sl2 = lesson(DayOfWeek.TUESDAY, LocalDate.of(2026, 6, 2), null);

            when(scheduleLessonRepository.findAllBetween(PERIOD_START, PERIOD_END)).thenReturn(List.of(sl1, sl2));
            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(eq(sl1), any()))
                    .thenThrow(new RuntimeException("Generation failed"));
            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(eq(sl2), any())).thenReturn(false);

            scheduleGeneratorService.generateForPeriod(period);

            verify(lessonInstanceRepository, never()).save(argThat(li -> li.getScheduleLesson() == sl1));
            verify(lessonInstanceRepository, times(5)).save(argThat(li -> li.getScheduleLesson() == sl2));
        }
    }

    @Nested
    @DisplayName("generateInstanceForPeriod")
    class GenerateInstanceForPeriod {

        @Test
        @DisplayName("генерирует уроки от validFrom до min(validTo, конец периода)")
        void success_generatesInstances() {
            AcademicPeriod period = period(PERIOD_START, PERIOD_END);
            ScheduleLesson sl = lesson(DayOfWeek.MONDAY, PERIOD_START, VALID_TO);
            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(any(), any())).thenReturn(false);

            scheduleGeneratorService.generateInstanceForPeriod(sl, period);

            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 1))));
            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 22))));
            verify(lessonInstanceRepository, never()).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 29))));
            verify(lessonInstanceRepository, times(4)).save(any(LessonInstance.class));
        }

        @Test
        @DisplayName("начинает генерацию с начала периода, если validFrom раньше него")
        void validFromBeforePeriodStart_startsFromPeriodStart() {
            AcademicPeriod period = period(PERIOD_START, PERIOD_END);
            ScheduleLesson sl = lesson(DayOfWeek.MONDAY, LocalDate.of(2026, 5, 1), null);
            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(any(), any())).thenReturn(false);

            scheduleGeneratorService.generateInstanceForPeriod(sl, period);

            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 1))));
            verify(lessonInstanceRepository, never()).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 5, 25))));
            verify(lessonInstanceRepository, times(5)).save(any(LessonInstance.class));
        }

        @Test
        @DisplayName("не создает уроки, если период заканчивается раньше первого подходящего дня недели")
        void noMatchingDayInPeriod_doesNothing() {
            AcademicPeriod period = period(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 4));
            ScheduleLesson sl = lesson(DayOfWeek.FRIDAY, LocalDate.of(2026, 5, 1), null);

            scheduleGeneratorService.generateInstanceForPeriod(sl, period);

            verify(lessonInstanceRepository, never()).save(any(LessonInstance.class));
        }
    }

    @Nested
    @DisplayName("generateInstanceForLesson")
    class GenerateInstanceForLesson {

        @Test
        @DisplayName("генерирует уроки от validFrom до validTo внутри академического периода")
        void success_generatesInstances() {
            AcademicPeriod period = period(PERIOD_START, PERIOD_END);
            ScheduleLesson sl = lesson(DayOfWeek.MONDAY, PERIOD_START, VALID_TO);

            when(academicPeriodRepository.findByDate(PERIOD_START)).thenReturn(Optional.of(period));
            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(any(), any())).thenReturn(false);

            scheduleGeneratorService.generateInstanceForLesson(sl);

            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 1))));
            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 8))));
            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 15))));
            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 22))));
            verify(lessonInstanceRepository, never()).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 29))));
            verify(lessonInstanceRepository, times(4)).save(any(LessonInstance.class));
        }

        @Test
        @DisplayName("при validTo = null использует конец академического периода как границу")
        void validToNull_usesPeriodEnd() {
            AcademicPeriod period = period(PERIOD_START, LocalDate.of(2026, 6, 15));
            ScheduleLesson sl = lesson(DayOfWeek.MONDAY, PERIOD_START, null);

            when(academicPeriodRepository.findByDate(PERIOD_START)).thenReturn(Optional.of(period));
            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(any(), any())).thenReturn(false);

            scheduleGeneratorService.generateInstanceForLesson(sl);

            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 15))));
            verify(lessonInstanceRepository, never()).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 22))));
            verify(lessonInstanceRepository, times(3)).save(any(LessonInstance.class));
        }

        @Test
        @DisplayName("если validTo после конца периода, генерация обрезается по концу периода")
        void validToAfterPeriodEnd_clampsToPeriodEnd() {
            AcademicPeriod period = period(PERIOD_START, PERIOD_END);
            ScheduleLesson sl = lesson(DayOfWeek.MONDAY, PERIOD_START, LocalDate.of(2026, 7, 15));

            when(academicPeriodRepository.findByDate(PERIOD_START)).thenReturn(Optional.of(period));
            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(any(), any())).thenReturn(false);

            scheduleGeneratorService.generateInstanceForLesson(sl);

            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 29))));
            verify(lessonInstanceRepository, never()).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 7, 6))));
            verify(lessonInstanceRepository, times(5)).save(any(LessonInstance.class));
        }

        @Test
        @DisplayName("бросает NotFoundException, если для даты validFrom нет академического периода")
        void noAcademicPeriod_throwsNotFoundException() {
            ScheduleLesson sl = lesson(DayOfWeek.MONDAY, PERIOD_START, null);
            when(academicPeriodRepository.findByDate(PERIOD_START)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduleGeneratorService.generateInstanceForLesson(sl))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Academic period by date");

            verify(lessonInstanceRepository, never()).save(any(LessonInstance.class));
        }
    }

    @Nested
    @DisplayName("generateInstanceBetween")
    class GenerateInstanceBetween {

        @Test
        @DisplayName("создает уроки каждую неделю в нужный день недели")
        void success_generatesWeeklyInstances() {
            ScheduleLesson sl = lesson(DayOfWeek.MONDAY, null, null);
            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(any(), any())).thenReturn(false);

            scheduleGeneratorService.generateInstanceBetween(sl, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 22));

            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 1))));
            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 8))));
            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 15))));
            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 22))));
            verify(lessonInstanceRepository, times(4)).save(any(LessonInstance.class));
        }

        @Test
        @DisplayName("не создает уроки, которые уже существуют на эту дату")
        void skipsExistingInstances() {
            ScheduleLesson sl = lesson(DayOfWeek.MONDAY, null, null);
            LocalDate date1 = LocalDate.of(2026, 6, 1);
            LocalDate date2 = LocalDate.of(2026, 6, 8);
            LocalDate date3 = LocalDate.of(2026, 6, 15);

            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(eq(sl), eq(date1))).thenReturn(true);
            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(eq(sl), eq(date2))).thenReturn(false);
            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(eq(sl), eq(date3))).thenReturn(false);

            scheduleGeneratorService.generateInstanceBetween(sl, date1, date3);

            verify(lessonInstanceRepository, never()).save(argThat(li -> li.getLessonDate().equals(date1)));
            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(date2)));
            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(date3)));
            verify(lessonInstanceRepository, times(2)).save(any(LessonInstance.class));
        }

        @Test
        @DisplayName("начинает с ближайшего подходящего дня недели, если период начинается не с него")
        void startsFromNextMatchingDayOfWeek() {
            ScheduleLesson sl = lesson(DayOfWeek.FRIDAY, null, null);
            when(lessonInstanceRepository.existsByScheduleLessonAndLessonDate(any(), any())).thenReturn(false);

            scheduleGeneratorService.generateInstanceBetween(sl, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 12));

            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 5))));
            verify(lessonInstanceRepository).save(argThat(li -> li.getLessonDate().equals(LocalDate.of(2026, 6, 12))));
            verify(lessonInstanceRepository, times(2)).save(any(LessonInstance.class));
        }

        @Test
        @DisplayName("ничего не генерирует, если в периоде нет подходящего дня недели")
        void noMatchingDayInPeriod_doesNothing() {
            ScheduleLesson sl = lesson(DayOfWeek.FRIDAY, null, null);

            scheduleGeneratorService.generateInstanceBetween(sl, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 4));

            verify(lessonInstanceRepository, never()).save(any(LessonInstance.class));
            verify(lessonInstanceRepository, never()).existsByScheduleLessonAndLessonDate(any(), any());
        }
    }
}

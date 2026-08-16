package com.rusobr.academic.application.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.max;
import static org.apache.commons.lang3.ObjectUtils.min;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleGeneratorService {

    private final AcademicPeriodRepository academicPeriodRepository;
    private final LessonInstanceRepository lessonInstanceRepository;

    private static final int ADVANCE_DAYS = 14;
    private final ScheduleLessonRepository scheduleLessonRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void generateInstanceForUpcomingPeriod() {
        LocalDate today = LocalDate.now();
        LocalDate advanceDate = today.plusDays(ADVANCE_DAYS);

        AcademicPeriod academicPeriod = academicPeriodRepository.findByEndDateBetween(today, advanceDate);
        if (academicPeriod == null) {
            log.info("No academic period ending within {} days", ADVANCE_DAYS);
            return;
        }

        academicPeriodRepository.findFirstByStartDateAfter(academicPeriod.getEndDate()).ifPresentOrElse(
                this::generateForPeriod,
                () -> log.info("No upcoming academic periods found")
        );
    }

    public void generateForPeriod(AcademicPeriod academicPeriod) {
        List<ScheduleLesson> scheduleLessons = scheduleLessonRepository.findAllBetween(academicPeriod.getStartDate(), academicPeriod.getEndDate());
        for (ScheduleLesson sl : scheduleLessons) {
            try {
                generateInstanceForPeriod(sl, academicPeriod);
            } catch (Exception e) {
                log.error("Failed to generate instances for scheduleLesson id={}", sl.getId(), e);
            }
        }
    }

    public void generateInstanceForPeriod(ScheduleLesson scheduleLesson, AcademicPeriod targetPeriod) {
        LocalDate from = max(scheduleLesson.getValidFrom(), targetPeriod.getStartDate());
        LocalDate to = min(scheduleLesson.getValidTo(), targetPeriod.getEndDate());
        generateInstanceBetween(scheduleLesson, from, to);
    }

    public void generateInstanceForLesson(ScheduleLesson scheduleLesson) {
        LocalDate from = scheduleLesson.getValidFrom();

        AcademicPeriod academicPeriod = academicPeriodRepository.findByDate(from)
                .orElseThrow(() -> new NotFoundException("Academic period by date %s not found".formatted(from),
                        AcademicExceptionCode.ACADEMIC_PERIOD_NOT_FOUND));
        LocalDate to = min(scheduleLesson.getValidTo(), academicPeriod.getEndDate());


        generateInstanceBetween(scheduleLesson, from, to);
    }

    public void generateInstanceBetween(ScheduleLesson scheduleLesson, LocalDate from, LocalDate to) {
        DayOfWeek dayOfWeek = scheduleLesson.getDayOfWeek();

        // начинаем с первого подходящего дня недели в периоде
        LocalDate current = from.with(TemporalAdjusters.nextOrSame(dayOfWeek));

        while (!current.isAfter(to)) {
            if (!lessonInstanceRepository.existsByScheduleLessonAndLessonDate(scheduleLesson, current)) {
                LessonInstance li = LessonInstance.builder()
                        .scheduleLesson(scheduleLesson)
                        .lessonDate(current)
                        .build();

                lessonInstanceRepository.save(li);
            }
            current = current.plusWeeks(1);
        }
        log.info("Generate lesson instance for schedule between: id={}, from={}, to={}", scheduleLesson.getId(), from, to);
    }
}

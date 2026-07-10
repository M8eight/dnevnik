package com.rusobr.academic.application.service.bff;

import com.rusobr.academic.application.service.GradeService;
import com.rusobr.academic.application.service.HomeworkService;
import com.rusobr.academic.application.service.ScheduleService;
import com.rusobr.academic.web.dto.bff.student.HomeAggregation;
import com.rusobr.academic.web.dto.grade.GradeWithSubjectNameResponse;
import com.rusobr.academic.web.dto.homework.HomeworkWithSubjectResponse;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonResponse;
import com.rusobr.academic.web.dto.scheduleLesson.SchoolLessonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentBffService {

    private final HomeworkService homeworkService;
    private final ScheduleService scheduleService;
    private final GradeService gradeService;
    private final Executor bffExecutor;

    public HomeAggregation getHomeAggregation(LocalDate date, Long userId) {

        CompletableFuture<List<HomeworkWithSubjectResponse>> homeworkFuture =
                CompletableFuture.supplyAsync(() -> homeworkService.getByDate(date, userId), bffExecutor)
                        .exceptionally(ex -> {
                            log.error(getErrorMessage("homework", userId), ex);
                            return List.of();
                        });

        CompletableFuture<Map<DayOfWeek, List<SchoolLessonResponse>>> scheduleWeekFuture =
                CompletableFuture.supplyAsync(() -> scheduleService.getWeekSchedule(userId), bffExecutor)
                        .exceptionally(ex -> {
                            log.error(getErrorMessage("schedule week", userId), ex);
                            return Map.of();
                        });

        CompletableFuture<List<ScheduleLessonResponse>> scheduleFuture =
                CompletableFuture.supplyAsync(() -> scheduleService.getByDate(userId, date), bffExecutor)
                        .exceptionally(ex -> {
                            log.error(getErrorMessage("schedule day", userId), ex);
                            return List.of();
                        });

        CompletableFuture<List<GradeWithSubjectNameResponse>> gradesFuture =
                CompletableFuture.supplyAsync(() -> gradeService.findAllByDate(userId, date), bffExecutor)
                        .exceptionally(ex -> {
                            log.error(getErrorMessage("grades", userId), ex);
                            return List.of();
                        });

        CompletableFuture<Double> averageFuture =
                CompletableFuture.supplyAsync(() -> gradeService.getAverageByPeriod(userId, date), bffExecutor)
                        .exceptionally(ex -> {
                            log.error(getErrorMessage("average by period", userId), ex);
                            return 0.0;
                        });

        CompletableFuture.allOf(homeworkFuture, scheduleWeekFuture, scheduleFuture, gradesFuture, averageFuture).join();

        return new HomeAggregation(homeworkFuture.join(), scheduleWeekFuture.join(), scheduleFuture.join(),
                gradesFuture.join(), averageFuture.join());

    }

    private String getErrorMessage(String text, Long userId) {
        return "StudentBff Error: Failed to fetch %s for user %s".formatted(text, userId);
    }

}

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
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class StudentBffService {

    private final HomeworkService homeworkService;
    private final ScheduleService scheduleService;
    private final GradeService gradeService;

    public HomeAggregation getHomeAggregation(LocalDate date, Long userId) {

        CompletableFuture<List<HomeworkWithSubjectResponse>> homeworkFuture =
                CompletableFuture.supplyAsync(() -> homeworkService.getByDate(date, userId));

        CompletableFuture<Map<DayOfWeek, List<SchoolLessonResponse>>> scheduleWeekFuture =
                CompletableFuture.supplyAsync(() -> scheduleService.getWeekSchedule(userId));

        CompletableFuture<List<ScheduleLessonResponse>> scheduleFuture =
                CompletableFuture.supplyAsync(() -> scheduleService.getByDate(userId, date));

        CompletableFuture<List<GradeWithSubjectNameResponse>> gradesFuture =
                CompletableFuture.supplyAsync(() -> gradeService.findAllByDate(userId, date));

        CompletableFuture<Double> averageFuture =
                CompletableFuture.supplyAsync(() -> gradeService.getAverageByPeriod(userId, date));

        CompletableFuture.allOf(homeworkFuture, scheduleWeekFuture, scheduleFuture, gradesFuture, averageFuture).join();

        return new HomeAggregation(homeworkFuture.join(), scheduleWeekFuture.join(), scheduleFuture.join(),
                gradesFuture.join(), averageFuture.join());

    }

}

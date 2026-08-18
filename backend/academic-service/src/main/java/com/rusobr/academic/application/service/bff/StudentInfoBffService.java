package com.rusobr.academic.application.service.bff;

import com.rusobr.academic.application.service.AttendanceService;
import com.rusobr.academic.application.service.GradeService;
import com.rusobr.academic.application.service.SchoolClassService;
import com.rusobr.academic.web.dto.bff.student.AttendanceStudentStatus;
import com.rusobr.academic.web.dto.bff.student.StudentInfoDto;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentInfoBffService {

    private final GradeService gradeService;
    private final Executor bffExecutor;
    private final AttendanceService attendanceService;
    private final SchoolClassService schoolClassService;

    public StudentInfoDto getInfoAggregation(Long userId) {
        LocalDate date = LocalDate.now();

        CompletableFuture<Double> averageFuture =
                CompletableFuture.supplyAsync(() -> gradeService.getAverageByPeriod(userId, date), bffExecutor)
                        .exceptionally(ex -> {
                            log.error(getErrorMessage("average by period", userId), ex);
                            return 0.0;
                        });

        CompletableFuture<AttendanceStudentStatus> studentAttendanceStatus =
                CompletableFuture.supplyAsync(() -> attendanceService.presencePercent(userId, date), bffExecutor)
                        .exceptionally(ex -> {
                            log.error(getErrorMessage("student attendance status", userId), ex);
                            return new AttendanceStudentStatus(0.0, 0, 0, 0);
                        });

        CompletableFuture<SchoolClassResponse> studentSchoolClass =
                CompletableFuture.supplyAsync(() -> schoolClassService.findByStudent(userId), bffExecutor)
                        .exceptionally(ex -> {
                            log.error(getErrorMessage("student SchoolClass", userId), ex);
                            return new SchoolClassResponse(null, null, null, null);
                        });

        CompletableFuture.allOf(averageFuture, studentAttendanceStatus, studentSchoolClass).join();

        return new StudentInfoDto(averageFuture.join(), studentAttendanceStatus.join(), studentSchoolClass.join());

    }

    private String getErrorMessage(String text, Long userId) {
        return "StudentInfoBff Error: Failed to fetch %s for user %s".formatted(text, userId);
    }
}

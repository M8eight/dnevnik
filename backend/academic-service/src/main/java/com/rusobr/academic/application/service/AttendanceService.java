package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.AttendanceMapper;
import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Attendance;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.persistence.repository.AttendanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.web.dto.attendances.AttendanceRequest;
import com.rusobr.academic.web.dto.attendances.AttendanceResponse;
import com.rusobr.academic.web.dto.bff.student.AttendanceStudentStatus;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.exception.ConflictException;
import com.rusobr.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final LessonInstanceService lessonInstanceService;
    private final AcademicPeriodService academicPeriodService;
    private final LessonInstanceRepository lessonInstanceRepository;

    @Cacheable(value = "attendanceStudentStatus", key = "#studentId + '#' + #date")
    public AttendanceStudentStatus presencePercent(Long studentId, LocalDate date) {
        AcademicPeriod academicPeriod = academicPeriodService.getByDate(date);
        LocalDate startDate = academicPeriod.getStartDate();
        LocalDate endDate = academicPeriod.getEndDate();
        List<Attendance> attendances = attendanceRepository.getAllAttendanceByStudentAndPeriod(studentId, startDate, endDate);

        int attendancesCount = attendances.size();
        int lessonsCount = lessonInstanceRepository.countLessonsByPeriod(studentId, startDate, endDate);

        int lateCount = (int) attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        int absenceCount = (int) attendances.stream().filter(a -> a.getStatus() != AttendanceStatus.LATE).count();

        double presencePercent = BigDecimal.valueOf(
                (double) (lessonsCount - attendancesCount) / lessonsCount * 100)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();

        return new AttendanceStudentStatus(presencePercent, lateCount, absenceCount, lessonsCount);
    }

    @CacheEvict(value = {"journalByAssignment", "journalByStudentId", "schedulesByStudentId", "attendanceStudentStatus"}, allEntries = true)
    @Transactional
    public AttendanceResponse create(AttendanceRequest attendanceRequest) {
        LessonInstance lessonInstance = lessonInstanceService.getById(attendanceRequest.lessonInstanceId());

        AcademicPeriod academicPeriod = academicPeriodService.getByDate(lessonInstance.getLessonDate());
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period with id: %d is closed".formatted(academicPeriod.getId()), AcademicExceptionCode.ACADEMIC_PERIOD_CLOSED_CONFLICT);
        }

        //Выполняем upsert, если нашли то map, если нет создаем новый экземпляр
        Attendance attendance = attendanceRepository
                .findByStudentIdAndLessonInstanceId(attendanceRequest.studentId(), attendanceRequest.lessonInstanceId())
                .map(existing -> {
                    existing.setStatus(attendanceRequest.status());
                    return existing;
                })
                .orElseGet(() -> attendanceMapper.toAttendance(attendanceRequest, lessonInstance)
                );

        AttendanceResponse response = attendanceMapper.toAttendanceResponse(attendanceRepository.save(attendance));
        log.info("Attendance created: request={}", attendanceRequest);
        return response;
    }

    @CacheEvict(value = {"journalByAssignment", "journalByStudentId", "schedulesByStudentId", "attendanceStudentStatus"}, allEntries = true)
    @Transactional
    public void delete(Long id) {

        Attendance attendance = attendanceRepository.findWithLessonInstanceById(id)
                .orElseThrow(() -> new NotFoundException("Attendance with id: %d".formatted(id), AcademicExceptionCode.ATTENDANCE_NOT_FOUND));

        AcademicPeriod academicPeriod = academicPeriodService.getByDate(attendance.getLessonInstance().getLessonDate());
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period is closed", AcademicExceptionCode.ACADEMIC_PERIOD_CLOSED_CONFLICT);
        }

        attendanceRepository.deleteById(id);
        log.info("Attendance deleted: id={}", id);
    }

}

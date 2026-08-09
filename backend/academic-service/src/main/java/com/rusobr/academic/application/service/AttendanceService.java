package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.AttendanceMapper;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Attendance;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.persistence.repository.AttendanceRepository;
import com.rusobr.academic.web.dto.attendances.AttendanceRequest;
import com.rusobr.academic.web.dto.attendances.AttendanceResponse;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.exception.ConflictException;
import com.rusobr.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final LessonInstanceService lessonInstanceService;
    private final AcademicPeriodService academicPeriodService;

    @CacheEvict(value = {"journalByAssignment", "journalByStudentId", "schedulesByStudentId"}, allEntries = true)
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

    @CacheEvict(value = {"journalByAssignment", "journalByStudentId", "schedulesByStudentId"}, allEntries = true)
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

package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.AttendanceMapper;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Attendance;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.persistence.repository.AttendanceRepository;
import com.rusobr.academic.web.dto.attendances.AttendanceRequest;
import com.rusobr.academic.web.dto.attendances.AttendanceResponse;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final LessonInstanceService lessonInstanceService;
    private final AcademicPeriodService academicPeriodService;

    @Transactional
    public AttendanceResponse create(AttendanceRequest attendanceRequest) {
        LessonInstance lessonInstance = lessonInstanceService.getById(attendanceRequest.lessonInstanceId());

        AcademicPeriod academicPeriod = academicPeriodService.getByDate(lessonInstance.getLessonDate());
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period with id: %d is closed".formatted(academicPeriod.getId()), ExceptionCode.ACADEMIC_PERIOD_CLOSED_CONFLICT);
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

        return attendanceMapper.toAttendanceResponse(attendanceRepository.save(attendance));
    }

    @Transactional
    public void delete(Long id) {
        attendanceRepository.deleteById(id);
    }

}

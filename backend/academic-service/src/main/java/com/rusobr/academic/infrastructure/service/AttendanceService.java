package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Attendance;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.AttendanceMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.AttendanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.web.dto.attendances.AttendanceRequest;
import com.rusobr.academic.web.dto.attendances.AttendanceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final LessonInstanceRepository lessonInstanceRepository;
    private final AcademicPeriodRepository academicPeriodRepository;

    @Transactional
    public AttendanceResponse createAttendance(AttendanceRequest attendanceRequest) {

        LessonInstance lessonInstance = lessonInstanceRepository.findById(attendanceRequest.lessonInstanceId())
                .orElseThrow(() -> new NotFoundException("Lesson Instance Not Found" + attendanceRequest.lessonInstanceId()));

        AcademicPeriod academicPeriod = academicPeriodRepository.findByDate(lessonInstance.getLessonDate())
                .orElseThrow(() -> new NotFoundException("Academic Period Not Found"));
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic Period is closed");
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
    public void deleteAttendance(Long id) {
        attendanceRepository.deleteById(id);
    }

}

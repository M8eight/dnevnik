package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.PeriodGrade;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.feignClient.UserClient;
import com.rusobr.academic.infrastructure.mapper.PeriodGradeMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.PeriodGradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeRequest;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.StudentPeriodGradeProjection;
import com.rusobr.academic.web.dto.grade.periodGrade.StudentPeriodGradeResponse;
import com.rusobr.academic.web.dto.userService.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PeriodGradeService {

    private final PeriodGradeRepository periodGradeRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final PeriodGradeMapper periodGradeMapper;
    private final UserClient userClient;

    public List<StudentPeriodGradeResponse> findBySchoolClassId(Long teachingAssignmentId, Long academicPeriodId) {
        List<StudentPeriodGradeProjection> studentPeriodGrades = periodGradeRepository.findPeriodGradesByTeachingAssignment(teachingAssignmentId, academicPeriodId);

        List<Long> studentIds = studentPeriodGrades.stream().map(StudentPeriodGradeProjection::studentId).toList();

        Map<Long, UserResponse> students = userClient.getBatchUsers(studentIds).stream()
                .collect(Collectors.toMap(UserResponse::id, g -> g));

        return studentPeriodGrades.stream().map(g ->
                        periodGradeMapper.toStudentPeriodGradeResponse(g, students.get(g.studentId())))
                .toList();
    }

    @Transactional
    public PeriodGradeResponse createGrade(PeriodGradeRequest dto) {
        AcademicPeriod period = academicPeriodRepository.findByDate(dto.date())
                .orElseThrow(() -> new NotFoundException("Academic period not found by date"));
        if (period.isClosed()) {
            throw new ConflictException("Period is already closed");
        }

        TeachingAssignment teachingAssignment = teachingAssignmentRepository.findById(dto.teachingAssignmentId())
                .orElseThrow(() -> new NotFoundException("Teaching assignment not found"));

        PeriodGrade periodGrade = PeriodGrade.builder()
                .value(dto.value())
                .description(dto.description())
                .academicPeriod(period)
                .studentId(dto.studentId())
                .teachingAssignment(teachingAssignment)
                .build();

        return periodGradeMapper.toPeriodGradeResponse(periodGradeRepository.save(periodGrade));

    }

    @Transactional
    public void deletePeriodGrade(Long periodGradeId) {
        PeriodGrade periodGrade = periodGradeRepository.findWithAcademicPeriodById(periodGradeId)
                .orElseThrow(() -> new NotFoundException("Period grade not found"));

        if (periodGrade.getAcademicPeriod().isClosed()) {
            throw new ConflictException("Period is already closed");
        }

        periodGradeRepository.delete(periodGrade);
    }

}

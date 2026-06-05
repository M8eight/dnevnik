package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.PeriodGrade;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.feignClient.UserClient;
import com.rusobr.academic.infrastructure.mapper.PeriodGradeMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.PeriodGradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.grade.StudentAverageProjection;
import com.rusobr.academic.web.dto.grade.periodGrade.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PeriodGradeService {

    private final PeriodGradeRepository periodGradeRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final PeriodGradeMapper periodGradeMapper;
    private final UserClient userClient;
    private final TeachingAssignmentService teachingAssignmentService;
    private final GradeRepository gradeRepository;

    @Transactional(readOnly = true)
    public Map<String, List<PeriodGradeStudentResponse>> getByStudentId(Long studentId, String schoolYear) {
        List<PeriodGradeStudentResponse> periodGrades = periodGradeRepository.findPeriodGradeByStudentId(studentId, schoolYear)
                .stream().map(periodGradeMapper::toPeriodGradeStudentResponse)
                .toList();

        return periodGrades.stream().collect(
                Collectors.groupingBy(
                        PeriodGradeStudentResponse::subjectName,
                        TreeMap::new,
                        Collectors.toList()
                )
        );
    }

    @Transactional(readOnly = true)
    public List<PeriodGradeTeacherResponse> getByAssignmentWithAverage(Long teachingAssignmentId, Long currentAcademicPeriodId, String schoolYear) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(currentAcademicPeriodId)
                .orElseThrow(() -> new NotFoundException("Academic period not found academicPeriodId: " + currentAcademicPeriodId));
        List<Long> studentIds = teachingAssignmentService.getStudentIdsByTeachingAssignmentId(teachingAssignmentId);
        List<UserFeignResponse> students = userClient.getBatchUsers(studentIds);

        List<PeriodGradeResponse> periodGrades = periodGradeRepository
                .findPeriodGradesByTeachingAssignmentId(teachingAssignmentId, schoolYear);
        Map<Long, List<PeriodGradeResponse>> periodGradesMap = periodGrades.stream().collect(Collectors.groupingBy(
                PeriodGradeResponse::studentId,
                HashMap::new,
                Collectors.toList()
        ));

        List<StudentAverageProjection> averageGrades = gradeRepository.findAverageStudentsByTeachingAssignment(
                teachingAssignmentId,
                academicPeriod.getStartDate(),
                academicPeriod.getEndDate());
        Map<Long, Double> averageGradesMap = averageGrades.stream().collect(Collectors.toMap(
                        StudentAverageProjection::getStudentId,
                        StudentAverageProjection::getAverage
        ));

        return students.stream().map(user ->
                new PeriodGradeTeacherResponse(user, periodGradesMap.get(user.id()), averageGradesMap.get(user.id()))
        ).toList();
    }

    @Transactional
    public PeriodGradeResponse create(PeriodGradeRequest dto) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(dto.academicPeriodId())
                .orElseThrow(() -> new NotFoundException("Academic period not found academicPeriodId: " + dto.academicPeriodId()));
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Period is already closed");
        }

        TeachingAssignment teachingAssignment = teachingAssignmentRepository.findById(dto.teachingAssignmentId())
                .orElseThrow(() -> new NotFoundException("Teaching assignment not found"));

        PeriodGrade periodGrade = PeriodGrade.builder()
                .value(dto.value())
                .description(dto.description())
                .academicPeriod(academicPeriod)
                .studentId(dto.studentId())
                .teachingAssignment(teachingAssignment)
                .build();

        return periodGradeMapper.toPeriodGradeResponse(periodGradeRepository.save(periodGrade));

    }

    @Transactional
    public void delete(Long periodGradeId) {
        PeriodGrade periodGrade = periodGradeRepository.findWithAcademicPeriodById(periodGradeId)
                .orElseThrow(() -> new NotFoundException("Period grade not found"));

        if (periodGrade.getAcademicPeriod().isClosed()) {
            throw new ConflictException("Period is already closed");
        }

        periodGradeRepository.delete(periodGrade);
    }

}

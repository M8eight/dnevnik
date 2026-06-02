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
import com.rusobr.academic.web.dto.grade.StudentAverageResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeRequest;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeStudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Map<String, List<PeriodGradeStudentResponse>> findBySchoolClassId(Long studentId) {
        List<PeriodGradeStudentResponse> periodGrades = periodGradeRepository.findPeriodGradeByStudentId(studentId)
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

    @Transactional
    public List<StudentAverageResponse> getStudentPeriodGradesWithAverage(Long teachingAssignmentId, Long academicPeriodId) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(academicPeriodId)
                .orElseThrow(() -> new NotFoundException("Academic period not found academicPeriodId: " + academicPeriodId));
        List<Long> studentIds = teachingAssignmentService.getStudentIdsByTeachingAssignmentId(teachingAssignmentId);
        List<UserFeignResponse> students = userClient.getBatchUsers(studentIds);

        List<PeriodGradeResponse> periodGrades = periodGradeRepository
                .findPeriodGradesByTeachingAssignmentId(teachingAssignmentId, academicPeriodId);
        Map<Long, PeriodGradeResponse> periodGradesMap = periodGrades.stream().collect(Collectors.toMap(
                PeriodGradeResponse::studentId,
                o -> o
        ));

        List<StudentAverageProjection> averageGrades = gradeRepository
                .findAverageStudentsByTeachingAssignment(teachingAssignmentId,
                        academicPeriod.getStartDate(),
                        academicPeriod.getEndDate());

        Map<Long, Double> averageGradesMap = averageGrades.stream().collect(Collectors.toMap(
                        StudentAverageProjection::getStudentId,
                        StudentAverageProjection::getAverage
        ));

        return students.stream().map(user ->
                new StudentAverageResponse(user, periodGradesMap.get(user.id()), averageGradesMap.get(user.id()))
        ).toList();
    }

    @Transactional
    public PeriodGradeResponse createGrade(PeriodGradeRequest dto) {
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
    public void deletePeriodGrade(Long periodGradeId) {
        PeriodGrade periodGrade = periodGradeRepository.findWithAcademicPeriodById(periodGradeId)
                .orElseThrow(() -> new NotFoundException("Period grade not found"));

        if (periodGrade.getAcademicPeriod().isClosed()) {
            throw new ConflictException("Period is already closed");
        }

        periodGradeRepository.delete(periodGrade);
    }

}

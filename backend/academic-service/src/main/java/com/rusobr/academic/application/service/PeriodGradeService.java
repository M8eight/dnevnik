package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.GradeMapper;
import com.rusobr.academic.application.mapper.PeriodGradeMapper;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.PeriodGrade;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.PeriodGradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.grade.StudentAverageDto;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeRequest;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeStudentResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeTeacherResponse;
import com.rusobr.common.exception.ConflictException;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final PeriodGradeMapper periodGradeMapper;
    private final UserClient userClient;
    private final TeachingAssignmentService teachingAssignmentService;
    private final GradeRepository gradeRepository;
    private final GradeMapper gradeMapper;
    private final AcademicPeriodService academicPeriodService;

    private record PeriodGradeDbData(
            List<Long> studentIds,
            Map<Long, List<PeriodGradeResponse>> periodGradesMap,
            Map<Long, Double> averageGradesMap
    ) {}

    @Lazy
    @Autowired
    private PeriodGradeService self;

    @Transactional(readOnly = true)
    public Map<String, List<PeriodGradeStudentResponse>> getByStudentId(Long studentId, Long academicYearId) {
        List<PeriodGradeStudentResponse> periodGrades = periodGradeRepository
                .findPeriodGradeByStudentId(studentId, academicYearId)
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

    public List<PeriodGradeTeacherResponse> getByAssignmentWithAverage(Long teachingAssignmentId, Long currentAcademicPeriodId, Long academicYearId) {
        PeriodGradeDbData data = self.getByAssignmentWithAverageTransactional(teachingAssignmentId, currentAcademicPeriodId, academicYearId);

        List<UserFeignResponse> students = userClient.getBatchUsers(data.studentIds()).found();

        return students.stream()
                .map(user -> new PeriodGradeTeacherResponse(
                        user,
                        data.periodGradesMap().get(user.id()),
                        data.averageGradesMap().get(user.id())
                )).toList();
    }

    @Transactional(readOnly = true)
    PeriodGradeDbData getByAssignmentWithAverageTransactional(Long teachingAssignmentId, Long currentAcademicPeriodId, Long academicYearId) {
        AcademicPeriod academicPeriod = academicPeriodService.getById(currentAcademicPeriodId);
        List<Long> studentIds = teachingAssignmentService.getStudentIdsByTeachingAssignmentId(teachingAssignmentId);

        List<PeriodGradeResponse> periodGrades = periodGradeRepository
                .findPeriodGradesByTeachingAssignmentId(teachingAssignmentId, academicYearId)
                .stream().map(periodGradeMapper::toPeriodGradeResponse).toList();
        Map<Long, List<PeriodGradeResponse>> periodGradesMap = periodGrades.stream().collect(Collectors.groupingBy(
                PeriodGradeResponse::studentId, HashMap::new, Collectors.toList()));

        List<StudentAverageDto> averageGrades = gradeRepository.findAverageStudentsByTeachingAssignment(
                        teachingAssignmentId, academicPeriod.getStartDate(), academicPeriod.getEndDate())
                .stream().map(gradeMapper::toStudentAverageDto).toList();
        Map<Long, Double> averageGradesMap = averageGrades.stream().collect(Collectors.toMap(
                StudentAverageDto::studentId, StudentAverageDto::average));

        return new PeriodGradeDbData(studentIds, periodGradesMap, averageGradesMap);
    }

    @Transactional
    public PeriodGradeResponse create(PeriodGradeRequest dto) {
        AcademicPeriod academicPeriod = academicPeriodService.getById(dto.academicPeriodId());
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period with id: %d is closed".formatted(academicPeriod.getId()),
                    AcademicExceptionCode.ACADEMIC_PERIOD_CLOSED_CONFLICT);
        }

        TeachingAssignment teachingAssignment = teachingAssignmentRepository.findById(dto.teachingAssignmentId())
                .orElseThrow(() -> new NotFoundException("Teaching assignment with id: %d not found".formatted(dto.teachingAssignmentId()),
                        AcademicExceptionCode.TEACHING_ASSIGNMENT_NOT_FOUND));

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
                .orElseThrow(() -> new NotFoundException("Period grade with id: %d not found".formatted(periodGradeId),
                        AcademicExceptionCode.PERIOD_GRADE_NOT_FOUND));

        if (periodGrade.getAcademicPeriod().isClosed()) {
            throw new ConflictException("Academic period with id: %d is closed".formatted(periodGrade.getAcademicPeriod().getId()),
                    AcademicExceptionCode.ACADEMIC_PERIOD_CLOSED_CONFLICT);
        }

        periodGradeRepository.delete(periodGrade);
    }

}

package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.FinalGradeMapper;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.domain.model.FinalGrade;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicYearRepository;
import com.rusobr.academic.infrastructure.persistence.repository.FinalGradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.grade.finalGrade.*;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.dto.BatchUserResponse;
import com.rusobr.common.exception.ConflictException;
import com.rusobr.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinalGradeService {

    private final FinalGradeRepository finalGradeRepository;
    private final FinalGradeMapper finalGradeMapper;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final TeachingAssignmentService teachingAssignmentService;
    private final UserClient userClient;
    private final AcademicYearRepository academicYearRepository;

    record DbData(List<FinalGrade> finalGrades, List<Long> studentIds) {}

    @Lazy
    @Autowired
    private FinalGradeService self;

    @Cacheable(value = "finalGradesByStudent", key = "#studentId + '#' + #academicYearId")
    @Transactional(readOnly = true)
    public Map<String, FinalGradeResponse> getByStudentId(Long studentId, Long academicYearId) {
        List<FinalGrade> finalGrades = finalGradeRepository.findFinalGradesByStudentId(studentId, academicYearId);
        List<FinalGradeResponse> mappedFinalGrades = finalGrades.stream().map(finalGradeMapper::toFinalGradeResponse).toList();
        return mappedFinalGrades.stream()
                .collect(Collectors.toMap(
                        FinalGradeResponse::subjectName,
                        v -> v
                ));
    }

    @Cacheable(value = "finalGradesByAssignment", key = "#teachingAssignmentId + '#' + #academicYearId", unless = "#result.degradedUsers")
    public FinalGradeTeacherResponse getByAssignmentId(Long teachingAssignmentId, Long academicYearId) {
        DbData data = self.getByAssignmentIdTransactional(teachingAssignmentId, academicYearId);
        List<FinalGradeResponse> mappedFinalGrades = data.finalGrades().stream().map(finalGradeMapper::toFinalGradeResponse).toList();
        Map<Long, List<FinalGradeResponse>> finalGradesMap = mappedFinalGrades.stream().collect(Collectors.groupingBy(
                FinalGradeResponse::studentId,
                HashMap::new,
                Collectors.toList()
        ));

        BatchUserResponse students = userClient.getBatchStudents(data.studentIds());

        List<StudentFinalGradesResponse> result = students.found().stream().map(user ->
                new StudentFinalGradesResponse(user, finalGradesMap.getOrDefault(user.id(), List.of()))).toList();

        return new FinalGradeTeacherResponse(result, students.degraded());
    }

    @Transactional
    DbData getByAssignmentIdTransactional(Long teachingAssignmentId, Long academicYearId) {
        List<FinalGrade> finalGrades = finalGradeRepository.findFinalGradesByTeachingAssignmentId(teachingAssignmentId, academicYearId);
        List<Long> studentIds = teachingAssignmentService.getStudentIdsByTeachingAssignmentId(teachingAssignmentId);
        return new DbData(finalGrades, studentIds);
    }

    @CacheEvict(value = {"finalGradesByStudent", "finalGradesByAssignment", "journalPeriodFinalGradeByStudentId"}, allEntries = true)
    @Transactional
    public FinalGradeCreateResponse create(FinalGradeRequest finalGradeRequest) {

        List<AcademicPeriod> academicPeriods = academicPeriodRepository.getAcademicPeriodsByAcademicYearId(finalGradeRequest.academicYearId());
        if (academicPeriods.isEmpty()) {
            throw new ConflictException("Academic periods by id: %d not found".formatted(finalGradeRequest.academicYearId()), AcademicExceptionCode.ACADEMIC_PERIODS_NOT_FOUND);
        }
        academicPeriods.forEach(ap -> {
            if (!ap.isClosed()) {
                throw new ConflictException("Academic period with id: %d is not closed".formatted(ap.getId()),
                        AcademicExceptionCode.FINAL_GRADE_YEAR_NOT_CLOSED_CONFLICT);
            }
        });

        TeachingAssignment teachingAssignment = teachingAssignmentRepository.findById(finalGradeRequest.teachingAssignmentId())
                .orElseThrow(() -> new NotFoundException("Teaching assignment with id: %d".formatted(
                        finalGradeRequest.teachingAssignmentId()), AcademicExceptionCode.TEACHING_ASSIGNMENT_NOT_FOUND));
        AcademicYear academicYear = academicYearRepository.findById(finalGradeRequest.academicYearId())
                .orElseThrow(() -> new NotFoundException("Academic year with id: %d".formatted(
                        finalGradeRequest.academicYearId()), AcademicExceptionCode.ACADEMIC_YEAR_NOT_FOUND));
        FinalGrade finalGrade = finalGradeMapper.toFinalGrade(finalGradeRequest);
        finalGrade.setTeachingAssignment(teachingAssignment);
        finalGrade.setAcademicYear(academicYear);
        FinalGradeCreateResponse response = finalGradeMapper
                .toFinalGradeCreateResponse(finalGradeRepository.save(finalGrade));
        log.info("Create final grade: request={}", finalGradeRequest);
        return response;
    }

    @CacheEvict(value = {"finalGradesByStudent", "finalGradesByAssignment", "journalPeriodFinalGradeByStudentId"}, allEntries = true)
    @Transactional
    public void delete(Long id) {
        FinalGrade finalGrade = finalGradeRepository.findWithAcademicYearById(id)
                .orElseThrow(() -> new NotFoundException("Final grade with id: %d".formatted(id),
                        AcademicExceptionCode.FINAL_GRADE_NOT_FOUND));

        List<AcademicPeriod> academicPeriods = academicPeriodRepository
                .getAcademicPeriodsByAcademicYearId(finalGrade.getAcademicYear().getId());
        if (academicPeriods.isEmpty()) {
            throw new ConflictException("Academic periods by academicYearId: %d not found"
                    .formatted(finalGrade.getAcademicYear().getId()), AcademicExceptionCode.ACADEMIC_PERIODS_NOT_FOUND);
        }
        academicPeriods.forEach(ap -> {
            if (!ap.isClosed()) {
                throw new ConflictException("Academic period is not closed", AcademicExceptionCode.FINAL_GRADE_YEAR_NOT_CLOSED_CONFLICT);
            }
        });

        finalGradeRepository.deleteById(id);
        log.info("Delete final grade: gradeId={}", id);
    }

}

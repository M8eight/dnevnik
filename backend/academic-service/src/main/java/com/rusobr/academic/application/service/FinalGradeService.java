package com.rusobr.academic.application.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.FinalGrade;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.application.mapper.FinalGradeMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.FinalGradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeCreateResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeRequest;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeTeacherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinalGradeService {

    private final FinalGradeRepository finalGradeRepository;
    private final FinalGradeMapper finalGradeMapper;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final TeachingAssignmentService teachingAssignmentService;
    private final UserClient userClient;

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

    @Transactional(readOnly = true)
    public List<FinalGradeTeacherResponse> getByAssignmentId(Long teachingAssignmentId, Long academicYearId) {
        List<FinalGrade> finalGrades = finalGradeRepository.findFinalGradesByTeachingAssignmentId(teachingAssignmentId, academicYearId);
        List<FinalGradeResponse> mappedFinalGrades = finalGrades.stream().map(finalGradeMapper::toFinalGradeResponse).toList();
        Map<Long, List<FinalGradeResponse>> finalGradesMap = mappedFinalGrades.stream().collect(Collectors.groupingBy(
                FinalGradeResponse::studentId,
                HashMap::new,
                Collectors.toList()
        ));

        List<Long> studentIds = teachingAssignmentService.getStudentIdsByTeachingAssignmentId(teachingAssignmentId);
        List<UserFeignResponse> students = userClient.getBatchUsers(studentIds).found();

        return students.stream().map(user ->
                new FinalGradeTeacherResponse(user, finalGradesMap.get(user.id()))).toList();
    }

    @Transactional
    public FinalGradeCreateResponse create(FinalGradeRequest finalGradeRequest) {

        List<AcademicPeriod> academicPeriods = academicPeriodRepository.findAcademicPeriodsByAcademicYearId(finalGradeRequest.academicYearId());
        if (academicPeriods.isEmpty()) {
            throw new ConflictException("Academic periods not found by academic year id " + finalGradeRequest.academicYearId());
        }
        academicPeriods.forEach(ap -> {
            if (!ap.isClosed()) {
                throw new ConflictException("Academic period is not closed");
            }
        });

        TeachingAssignment teachingAssignment = teachingAssignmentRepository.findById(finalGradeRequest.teachingAssignmentId())
                .orElseThrow(() -> new NotFoundException("Teaching assignment not found with id " +
                        finalGradeRequest.teachingAssignmentId()));
        FinalGrade finalGrade = finalGradeMapper.toFinalGrade(finalGradeRequest);
        finalGrade.setTeachingAssignment(teachingAssignment);
        return finalGradeMapper.toFinalGradeCreateResponse(finalGradeRepository.save(finalGrade));
    }

    @Transactional
    public void delete(Long id) {
        FinalGrade finalGrade = finalGradeRepository.findWithAcademicYearById(id)
                .orElseThrow(() -> new NotFoundException("Final grade not found with id " + id));

        List<AcademicPeriod> academicPeriods = academicPeriodRepository.findAcademicPeriodsByAcademicYearId(finalGrade.getAcademicYear().getId());
        if (academicPeriods.isEmpty()) {
            throw new ConflictException("Academic period not found with school year " + finalGrade.getAcademicYear().getId());
        }
        academicPeriods.forEach(ap -> {
            if (!ap.isClosed()) {
                throw new ConflictException("Academic period is not closed");
            }
        });

        finalGradeRepository.deleteById(id);
    }

}

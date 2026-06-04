package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.FinalGrade;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.feignClient.UserClient;
import com.rusobr.academic.infrastructure.mapper.FinalGradeMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.FinalGradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeCreateResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeRequest;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeTeacherResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
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
    public Map<String, FinalGradeResponse> getByStudentId(Long studentId, String schoolYear) {
        List<FinalGrade> finalGrades = finalGradeRepository.findFinalGradesByStudentId(studentId, schoolYear);
        List<FinalGradeResponse> mappedFinalGrades = finalGrades.stream().map(finalGradeMapper::toFinalGradeResponse).toList();
        return mappedFinalGrades.stream()
                .collect(Collectors.toMap(
                        FinalGradeResponse::subjectName,
                        v -> v
                ));
    }

    @Transactional(readOnly = true)
    public List<FinalGradeTeacherResponse> getByTeachingAssignmentId(Long teachingAssignmentId, String schoolYear) {
        List<FinalGrade> finalGrades = finalGradeRepository.findFinalGradesByTeachingAssignmentId(teachingAssignmentId, schoolYear);
        List<FinalGradeResponse> mappedFinalGrades = finalGrades.stream().map(finalGradeMapper::toFinalGradeResponse).toList();
        Map<Long, List<FinalGradeResponse>> finalGradesMap = mappedFinalGrades.stream().collect(Collectors.groupingBy(
                FinalGradeResponse::studentId,
                HashMap::new,
                Collectors.toList()
        ));

        List<Long> studentIds = teachingAssignmentService.getStudentIdsByTeachingAssignmentId(teachingAssignmentId);
        List<UserFeignResponse> students = userClient.getBatchUsers(studentIds);

        return students.stream().map(user ->
                new FinalGradeTeacherResponse(user, finalGradesMap.get(user.id()))).toList();
    }

    @Transactional
    public FinalGradeCreateResponse create(FinalGradeRequest finalGradeRequest) {
        List<AcademicPeriod> academicPeriods = academicPeriodRepository.findAcademicPeriodsBySchoolYear(finalGradeRequest.schoolYear());
        if (academicPeriods.isEmpty()) {
            throw new ConflictException("Academic period not found with school year " + finalGradeRequest.schoolYear());
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
        FinalGrade finalGrade = finalGradeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Final grade not found with id " + id));

        List<AcademicPeriod> academicPeriods = academicPeriodRepository.findAcademicPeriodsBySchoolYear(finalGrade.getSchoolYear());
        if (academicPeriods.isEmpty()) {
            throw new ConflictException("Academic period not found with school year " + finalGrade.getSchoolYear());
        }
        academicPeriods.forEach(ap -> {
            if (!ap.isClosed()) {
                throw new ConflictException("Academic period is not closed");
            }
        });

        finalGradeRepository.deleteById(id);
    }

}

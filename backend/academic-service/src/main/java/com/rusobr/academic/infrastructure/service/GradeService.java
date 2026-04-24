package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.GradeMapper;
import com.rusobr.academic.infrastructure.mapper.LessonInstanceMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.grade.GradeWithSubjectNameResponse;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeService {

    private final GradeRepository gradeRepository;
    private final GradeMapper gradeMapper;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final LessonInstanceRepository lessonInstanceRepository;
    private final LessonInstanceMapper lessonInstanceMapper;

    public GradeResponse getGradeById(Long gradeId) {
        Grade grade = gradeRepository.findById(gradeId).orElseThrow(() -> new NotFoundException("Grade not found gradeId: " + gradeId));
        return gradeMapper.toGradeResponseDto(grade);
    }

    public Double getAverageGrade(Long studentId, Long academicPeriodId) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(academicPeriodId)
                .orElseThrow(() -> new NotFoundException("Academic period not found academicPeriodId: " + academicPeriodId));
        Double avg = gradeRepository.getAverageGrade(studentId, academicPeriod.getStartDate(), academicPeriod.getEndDate());
        if (avg == null) return null;

        return BigDecimal.valueOf(avg)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public List<GradeWithSubjectNameResponse> findAllGradesByDate(Long studentId, LocalDate date) {
        return gradeRepository.findAllGradesByDate(studentId, date);
    }

    @Transactional
    public CreateGradeResponse createGrade(CreateGradeRequest gradeDto) {

        LessonInstance lessonInstance = lessonInstanceRepository.findById(gradeDto.lessonInstanceId())
                .orElseThrow(() -> new NotFoundException("Lesson instance not found"));
        AcademicPeriod academicPeriod = academicPeriodRepository.findByDate(lessonInstance.getLessonDate())
                .orElseThrow(() -> new NotFoundException("Academic period not found"));
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period is already closed");
        }

        Grade grade = gradeMapper.toGrade(gradeDto);
        grade.setLessonInstance(lessonInstance);

        return gradeMapper.toCreateGradeResponseDto(
                gradeRepository.save(grade), lessonInstanceMapper.toLessonInstanceDto(lessonInstance));

    }

    @Transactional
    public void deleteGrade(Long id) {

        Grade grade = gradeRepository.findWithLessonInstanceById(id)
                .orElseThrow(() -> new NotFoundException("Grade not found gradeId: " + id));

        AcademicPeriod academicPeriod = academicPeriodRepository.findByDate(grade.getLessonInstance().getLessonDate())
                .orElseThrow(() -> new NotFoundException("Academic period not found"));
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period is closed");
        }

        gradeRepository.delete(grade);

    }

}

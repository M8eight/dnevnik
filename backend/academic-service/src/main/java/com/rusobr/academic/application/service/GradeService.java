package com.rusobr.academic.application.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import com.rusobr.academic.application.mapper.GradeMapper;
import com.rusobr.academic.application.mapper.LessonInstanceMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.grade.GradeWithSubjectNameResponse;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final GradeMapper gradeMapper;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final LessonInstanceRepository lessonInstanceRepository;
    private final LessonInstanceMapper lessonInstanceMapper;

    @Transactional(readOnly = true)
    public GradeResponse getById(Long gradeId) {
        Grade grade = gradeRepository.findById(gradeId).orElseThrow(() -> new NotFoundException("Grade not found gradeId: " + gradeId));
        return gradeMapper.toGradeResponseDto(grade);
    }

    @Transactional(readOnly = true)
    public Double getAverageByPeriod(Long studentId, Long academicPeriodId) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(academicPeriodId)
                .orElseThrow(() -> new NotFoundException("Academic period not found academicPeriodId: " + academicPeriodId));
        Double avg = gradeRepository.getAverageGrade(studentId, academicPeriod.getStartDate(), academicPeriod.getEndDate());
        if (avg == null) return 0.0;

        return BigDecimal.valueOf(avg)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Transactional(readOnly = true)
    public List<GradeWithSubjectNameResponse> findAllByDate(Long studentId, LocalDate date) {
        return gradeRepository.findAllByDateAndStudentId(studentId, date).stream().map(gradeMapper::toWithSubjectNameResponse).toList();
    }

    @Transactional
    public CreateGradeResponse create(CreateGradeRequest gradeDto) {
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
    public void delete(Long id) {
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

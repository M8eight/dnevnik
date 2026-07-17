package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.GradeMapper;
import com.rusobr.academic.application.mapper.LessonInstanceMapper;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.projection.GradeDetailProjection;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.grade.GradeDetailResponse;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.grade.GradeWithSubjectNameResponse;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.ExceptionCode;
import com.rusobr.academic.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final GradeMapper gradeMapper;
    private final LessonInstanceRepository lessonInstanceRepository;
    private final LessonInstanceMapper lessonInstanceMapper;
    private final AcademicPeriodService academicPeriodService;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final UserClient userClient;
    private final TransactionTemplate readOnlyTransactionTemplate;

    @Transactional(readOnly = true)
    public GradeResponse getById(Long id) {
        Grade grade = gradeRepository.findById(id).orElseThrow(() -> new NotFoundException("Grade with id: %d not found".formatted(id),
                ExceptionCode.GRADE_NOT_FOUND));
        return gradeMapper.toGradeResponseDto(grade);
    }

    @Transactional(readOnly = true)
    public Double getAverageByPeriod(Long studentId, LocalDate date) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findByDate(date)
                .orElse(null);
        if (academicPeriod == null) return 0.0;
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

    public GradeDetailResponse getDetail(Long id) {
        GradeDetailProjection gradeProjection = Objects.requireNonNull(readOnlyTransactionTemplate.execute(status ->
             gradeRepository.findDetailById(id)
                    .orElseThrow(() -> new NotFoundException("Grade with id: %d not found".formatted(id), ExceptionCode.GRADE_NOT_FOUND))
        ));
        UserFeignResponse teacher = userClient.getTeacherSimpleById(gradeProjection.getTeacherId());
        return gradeMapper.toGradeDetailResponse(gradeProjection, teacher);
    }

    @Transactional
    public CreateGradeResponse create(CreateGradeRequest gradeDto) {
        LessonInstance lessonInstance = lessonInstanceRepository.findById(gradeDto.lessonInstanceId())
                .orElseThrow(() -> new NotFoundException("Lesson instance with id: %d".formatted(gradeDto.lessonInstanceId()),
                        ExceptionCode.LESSON_INSTANCE_NOT_FOUND));
        AcademicPeriod academicPeriod = academicPeriodService.getByDate(lessonInstance.getLessonDate());
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period with id: %d is already closed".formatted(academicPeriod.getId()),
                    ExceptionCode.ACADEMIC_PERIOD_CLOSED_CONFLICT);
        }

        Grade grade = gradeMapper.toGrade(gradeDto);
        grade.setLessonInstance(lessonInstance);

        return gradeMapper.toCreateGradeResponseDto(
                gradeRepository.save(grade), lessonInstanceMapper.toLessonInstanceDto(lessonInstance));
    }

    @Transactional
    public void delete(Long id) {
        Grade grade = gradeRepository.findWithLessonInstanceById(id)
                .orElseThrow(() -> new NotFoundException("Grade with id: %d".formatted(id), ExceptionCode.GRADE_NOT_FOUND));

        AcademicPeriod academicPeriod = academicPeriodService.getByDate(grade.getLessonInstance().getLessonDate());
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period is closed", ExceptionCode.ACADEMIC_PERIOD_CLOSED_CONFLICT);
        }

        gradeRepository.delete(grade);
    }

}

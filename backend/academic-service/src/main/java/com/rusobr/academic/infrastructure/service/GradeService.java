package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.GradeMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.grade.GradeWithSubjectNameResponse;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeRequest;
import com.rusobr.academic.web.dto.grade.createGrade.CreateGradeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ScheduleLessonRepository scheduleLessonRepository;

    public GradeResponse getGradeById(Long gradeId) {
        Grade grade = gradeRepository.findById(gradeId).orElseThrow(() -> new NotFoundException("Grade not found gradeId: " + gradeId));
        return gradeMapper.toGradeResponseDto(grade);
    }

    public Double getAverageGrade(Long studentId, Long academicPeriodId) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(academicPeriodId)
                .orElseThrow(() -> new NotFoundException("Academic period not found academicPeriodId: " + academicPeriodId));
        log.info(academicPeriod.toString());
        return gradeRepository.getAverageGrade(studentId, academicPeriod.getStartDate(), academicPeriod.getEndDate());
    }

    public List<GradeWithSubjectNameResponse> findAllGradesByDate(Long studentId, LocalDate date) {
        return gradeRepository.findAllGradesByDate(studentId, date);
    }

    @Transactional
    public CreateGradeResponse createGrade(CreateGradeRequest gradeDto) {

        log.info("createGrade({})", gradeDto);

        AcademicPeriod academicPeriod = academicPeriodRepository.findByDate(gradeDto.date()).orElseThrow(() -> new NotFoundException("Academic period not found"));
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period is closed");
        }

        LessonInstance lessonInstance = lessonInstanceRepository.findByDateAndScheduleLessonId(gradeDto.date(), gradeDto.scheduleLessonId()).orElseGet(() -> {
            ScheduleLesson schedule = scheduleLessonRepository.findById(gradeDto.scheduleLessonId()).orElseThrow(() -> new NotFoundException("Schedule lesson not found"));

            return lessonInstanceRepository.save(LessonInstance.builder().date(gradeDto.date()).scheduleLesson(schedule).build());
        });

        Grade gradeEntity = Grade.builder().studentId(gradeDto.studentId()).value(gradeDto.value()).type(gradeDto.gradeType()).lessonInstance(lessonInstance).build();


        return gradeMapper.toCreateGradeResponseDto(gradeRepository.save(gradeEntity), lessonInstance.getDate());

    }

    @Transactional
    public void deleteGrade(Long id) {

        Grade grade = gradeRepository.findWithLessonInstanceById(id)
                .orElseThrow(() -> new NotFoundException("Grade not found gradeId: " + id));

        AcademicPeriod academicPeriod = academicPeriodRepository.findByDate(grade.getLessonInstance().getDate())
                .orElseThrow(() -> new NotFoundException("Academic period not found"));
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period is closed");
        }

        gradeRepository.delete(grade);
        log.info("deleteGrade({})", id);
    }

}

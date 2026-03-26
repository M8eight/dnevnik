package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.infrastructure.exception.Conflict;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.GradeMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.web.dto.grade.GradeRequestDto;
import com.rusobr.academic.web.dto.grade.GradeResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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

//    public Page<GradeResponseDto> getGrades(Pageable pageable) {
//        return gradeRepository.findAll(pageable).map(gradeMapper::toGradeResponseDto);
//    }

//    public GradeResponseDto getGrade(Long id) {
//        Grade grade = gradeRepository.findById(id).orElseThrow(() -> new NotFoundException("Grade not found " + id));
//        return gradeMapper.toGradeResponseDto(grade);
//    }

    @Transactional
    public GradeResponseDto createGrade(GradeRequestDto gradeDto) {

        log.info("createGrade({})", gradeDto);

        AcademicPeriod academicPeriod = academicPeriodRepository.findByDate(gradeDto.date())
                .orElseThrow(() -> new NotFoundException("Academic period not found"));
        if (academicPeriod.isClosed()) {
            throw new Conflict("Academic period is closed");
        }

        LessonInstance lessonInstance = lessonInstanceRepository.findByDateAndScheduleLessonId(gradeDto.date(), gradeDto.scheduleLessonId()).orElseGet(() -> {
            ScheduleLesson schedule = scheduleLessonRepository.findById(gradeDto.scheduleLessonId()).orElseThrow(() -> new NotFoundException("Schedule lesson not found"));

            return lessonInstanceRepository.save(LessonInstance.builder().date(gradeDto.date())
                    .scheduleLesson(schedule)
                    .build());
        });

        Grade gradeEntity = Grade.builder()
                .studentId(gradeDto.studentId())
                .value(gradeDto.value())
                .type(gradeDto.gradeType().name())
                .lessonInstance(lessonInstance)
                .build();


        return gradeMapper.toGradeResponseDto(gradeRepository.save(gradeEntity), lessonInstance.getDate());

    }
//
//    @Transactional
//    public GradeResponseDto updateGrade(Long id, GradeRequestDto dto) {
//        Grade grade = gradeRepository.findById(id).orElseThrow(() -> new NotFoundException("Grade not found " + id));
//        gradeMapper.updateEntityFromDto(dto, grade);
//        return gradeMapper.toGradeResponseDto(gradeRepository.save(grade));
//    }
//
//    @Transactional
//    public void deleteGrade(Long id) {
//        if (!gradeRepository.existsById(id)) {
//            throw new NotFoundException("Grade not found " + id);
//        }
//        gradeRepository.deleteById(id);
//    }

}

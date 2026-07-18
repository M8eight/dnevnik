package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.HomeworkMapper;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.Homework;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.persistence.repository.HomeworkRepository;
import com.rusobr.academic.web.dto.homework.HomeworkRequest;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;
import com.rusobr.academic.web.dto.homework.HomeworkWithSubjectResponse;
import com.rusobr.common.exception.ConflictException;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeworkService {

    private final HomeworkRepository homeworkRepository;
    private final HomeworkMapper homeworkMapper;
    private final AcademicPeriodService academicPeriodService;
    private final LessonInstanceService lessonInstanceService;

    @Transactional(readOnly = true)
    public List<HomeworkWithSubjectResponse> getByDate(LocalDate date, Long studentId) {
        return homeworkRepository.findHomeworksByDate(date, studentId).stream().map(homeworkMapper::toWithSubjectResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<HomeworkResponse> getByAssignment(Long teachingAssignmentId, Pageable pageable) {
        Page<Homework> homeworkPage = homeworkRepository.findHomeworksByTeachingAssignmentId(teachingAssignmentId, pageable);
        return homeworkPage.map(homeworkMapper::toHomeworkResponse);
    }

    @Transactional
    public HomeworkResponse create(HomeworkRequest homeworkRequest) {
        LessonInstance lessonInstance = lessonInstanceService.getById(homeworkRequest.lessonInstanceId());

        AcademicPeriod academicPeriod = academicPeriodService.getByDate(lessonInstance.getLessonDate());
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period with id: %d is closed".formatted(academicPeriod.getId()),
                    AcademicExceptionCode.ACADEMIC_PERIOD_CLOSED_CONFLICT);
        }

        Homework homework = Homework.builder()
                .lessonInstance(lessonInstance)
                .text(homeworkRequest.text())
                .build();

        return homeworkMapper.toHomeworkResponse(homeworkRepository.save(homework));
    }

    @Transactional
    public void delete(Long id) {
        Homework homework = homeworkRepository.findWithLessonInstanceById(id)
                .orElseThrow(() -> new NotFoundException("Homework with id: %d not found".formatted(id), AcademicExceptionCode.HOMEWORK_NOT_FOUND));

        AcademicPeriod academicPeriod = academicPeriodService.getByDate(homework.getLessonInstance().getLessonDate());
        if (academicPeriod.isClosed()) {
            throw new ConflictException("Academic period with id: %d is closed".formatted(academicPeriod.getId()),
                    AcademicExceptionCode.ACADEMIC_PERIOD_CLOSED_CONFLICT);
        }

        homeworkRepository.delete(homework);
    }

}

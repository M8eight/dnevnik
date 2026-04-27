package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.Homework;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.HomeworkMapper;
import com.rusobr.academic.infrastructure.persistence.repository.HomeworkRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.web.dto.homework.HomeworkHomePageResponse;
import com.rusobr.academic.web.dto.homework.HomeworkRequest;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;
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
    private final LessonInstanceRepository lessonInstanceRepository;
    private final HomeworkMapper homeworkMapper;

    @Transactional(readOnly = true)
    public List<HomeworkHomePageResponse> getHomeworksByDate(LocalDate date, Long studentId) {
        return homeworkRepository.findHomeworksByDate(date, studentId);
    }

    @Transactional(readOnly = true)
    public Page<HomeworkResponse> getHomeworksByTeachingAssignment(Long teachingAssignmentId, Pageable pageable) {
        Page<Homework> homeworkPage = homeworkRepository.findHomeworksByTeachingAssignmentId(teachingAssignmentId, pageable);
        return homeworkPage.map(homeworkMapper::toHomeworkResponse);
    }

    @Transactional
    public HomeworkResponse createHomework(HomeworkRequest homeworkRequest) {
        LessonInstance lessonInstance = lessonInstanceRepository.findById(homeworkRequest.lessonInstanceId())
                .orElseThrow(() -> new NotFoundException("Lesson Instance Not Found"));

        Homework homework = Homework.builder()
                .lessonInstance(lessonInstance)
                .text(homeworkRequest.text())
                .build();

        return homeworkMapper.toHomeworkResponse(homeworkRepository.save(homework));
    }

}

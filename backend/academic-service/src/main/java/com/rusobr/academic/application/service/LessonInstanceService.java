package com.rusobr.academic.application.service;

import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.web.exception.ExceptionCode;
import com.rusobr.academic.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LessonInstanceService {

    private final LessonInstanceRepository lessonInstanceRepository;

    public LessonInstance getById(Long id) {
        return lessonInstanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lesson instance with id " + id
                        + " not found", ExceptionCode.LESSON_INSTANCE_NOT_FOUND));
    }

}

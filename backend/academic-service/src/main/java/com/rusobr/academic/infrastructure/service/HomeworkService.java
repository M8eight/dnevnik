package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.infrastructure.persistence.repository.HomeworkRepository;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeworkService {
    private final HomeworkRepository homeworkRepository;

    @Transactional(readOnly = true)
    public List<HomeworkResponse> getHomeworksByDate(LocalDate date, Long studentId) {
        return homeworkRepository.findHomeworksByDate(date, studentId);
    }

}

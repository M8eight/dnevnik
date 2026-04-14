package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.mapper.LessonInstanceMapper;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.web.dto.scheduleLesson.DiaryLessonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonInstanceService {

    private final LessonInstanceRepository lessonInstanceRepository;
    private final LessonInstanceMapper lessonInstanceMapper;

    public Map<LocalDate, List<DiaryLessonResponse>> getDiaryLessonsByStudentIdAndDateRange(Long studentId,
                                                                                            LocalDate startDate,
                                                                                            LocalDate endDate) {
        List<LessonInstance> lis = lessonInstanceRepository.findDiaryLessonsByStudentIdAndDateRange(studentId, startDate, endDate);

        List<DiaryLessonResponse> schedule = lessonInstanceMapper.toDiaryLessonResponseList(lis, studentId);

        return schedule.stream()
                .collect(Collectors.groupingBy(
                        DiaryLessonResponse::lessonDate,
                        TreeMap::new,
                        Collectors.toList()
                ));
    }
}

package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonResponse;
import com.rusobr.academic.web.dto.scheduleLesson.SchoolLessonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleLessonRepository scheduleLessonRepository;

    public List<ScheduleLessonResponse> getScheduleByDate(Long studentId, DayOfWeek dayOfWeek, LocalDate date) {
        return scheduleLessonRepository.getScheduleByDate(studentId, dayOfWeek, date);
    }

    public Map<DayOfWeek, List<SchoolLessonResponse>> getWeekSchedule(Long studentId) {
        List<SchoolLessonResponse> sortedRes = scheduleLessonRepository.findAllByStudentId(studentId).stream()
                .sorted(Comparator.comparing(SchoolLessonResponse::dayOfWeek)
                        .thenComparing(SchoolLessonResponse::lessonNumber))
                .toList();

        return sortedRes.stream()
                .collect(
                        Collectors.groupingBy(
                                SchoolLessonResponse::dayOfWeek,
                                LinkedHashMap::new,
                                Collectors.toList()
                        )
                );
    }

    //todo расписание для учителя какие уроки у нее

}

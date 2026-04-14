package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.web.dto.lessonInstance.LessonWeekItemDto;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonResponse;
import com.rusobr.academic.web.dto.scheduleLesson.SchoolLessonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final LessonInstanceRepository lessonInstanceRepository;
    private final ScheduleLessonRepository scheduleLessonRepository;

    public List<LessonWeekItemDto> getSchedule(Long classId, Long studentId, LocalDate startDate, LocalDate endDate) {
        log.info("getSchedule for studentId:{}  classId:{} startDate:{} endDate:{}", studentId, classId, startDate, endDate);
        return lessonInstanceRepository.getSchedule(classId, studentId, startDate, endDate);
    }

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

package com.rusobr.academic.domain.service;

import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.web.dto.lessonInstance.LessonWeekDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final LessonInstanceRepository lessonInstanceRepository;

    public List<LessonWeekDto> getSchedule(Long classId, Long studentId, LocalDate startDate, LocalDate endDate) {
        log.info("getSchedule for studentId:{}  classId:{} startDate:{} endDate:{}", studentId, classId, startDate, endDate);
        return lessonInstanceRepository.getSchedule(classId, studentId, startDate, endDate);
    }

}

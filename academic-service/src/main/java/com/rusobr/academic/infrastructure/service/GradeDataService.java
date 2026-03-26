package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.AcademicPeriodMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.web.dto.grade.DateScheduleAssignDto;
import com.rusobr.academic.web.dto.grade.GradeJournalData;
import com.rusobr.academic.web.dto.grade.TeacherGradeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeDataService {

    private final ScheduleLessonRepository scheduleLessonRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final AcademicPeriodMapper academicPeriodMapper;
    private final GradeRepository gradeRepository;

    @Transactional(readOnly = true)
    public GradeJournalData getGradeData(Long teachingAssignmentId, LocalDate date) {

        //Получаем период из бд по дате
        AcademicPeriod academicPeriod = academicPeriodRepository.findByDate(date)
                .orElseThrow(() -> new NotFoundException("Current academic period not found"));
        log.info("academicPeriod: {}", academicPeriod);


        List<ScheduleLesson> scheduleLessons = scheduleLessonRepository.findByTeachingAssignmentId(teachingAssignmentId);
        //Получаем список дней когда проводится предмет по teachingAssignment
        if (scheduleLessons.isEmpty()) {
            throw new NotFoundException("Not found schedule lessons");
        }

        Map<DayOfWeek, Long> dayToLessonId = scheduleLessons.stream().collect(
                Collectors.toMap(
                        ScheduleLesson::getDayOfWeek,
                        ScheduleLesson::getId
                ));

        //Получаем список дат и привязанных к ним scheduleId а также получаем циклически дни до конца периода
        List<DateScheduleAssignDto> dates = academicPeriod.getStartDate()
                .datesUntil(academicPeriod.getEndDate().plusDays(1))
                .filter(dateV -> dayToLessonId.containsKey(dateV.getDayOfWeek()))
                .map(dateV -> new DateScheduleAssignDto(dateV, dayToLessonId.get(dateV.getDayOfWeek())))
                .toList();

        //Получаем список оценок с присвоенными к ним studentId
        List<TeacherGradeDto> grades = gradeRepository.getClassGrades(teachingAssignmentId);

        return new GradeJournalData(dates, grades, academicPeriodMapper.toDto(academicPeriod), scheduleLessons);
    }
}

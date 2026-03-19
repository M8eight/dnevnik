package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.AcademicPeriodMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.web.dto.grade.GradeJournalData;
import com.rusobr.academic.web.dto.grade.TeacherGradeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeDataService {

    private final ScheduleLessonRepository scheduleLessonRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final AcademicPeriodMapper academicPeriodMapper;
    private final GradeRepository gradeRepository;

    //DB слой с транзакцией
    @Transactional(readOnly = true)
    public GradeJournalData getGradeData(Long teachingAssignmentId, LocalDate date) {

        //Получаем верхнюю строку дат (дат по которым проводится предмет)
        AcademicPeriod academicPeriod = academicPeriodRepository.findByDate(date)
                .orElseThrow(() -> new NotFoundException("Current academic period not found"));
        log.info("academicPeriod: {}", academicPeriod);

        List<DayOfWeek> dayOfWeeks = scheduleLessonRepository.findDaysOfWeeksByTeachingAssignmentId(teachingAssignmentId);
        if (dayOfWeeks.isEmpty()) {
            throw new NotFoundException("Not found day of weeks");
        }

        List<LocalDate> dates = academicPeriod.getStartDate()
                .datesUntil(academicPeriod.getEndDate().plusDays(1))
                .filter(dateV -> dayOfWeeks.contains(dateV.getDayOfWeek()))
                .toList();

        List<TeacherGradeDto> grades = gradeRepository.getClassGrades(teachingAssignmentId);

        return new GradeJournalData(dates, grades, academicPeriodMapper.toDto(academicPeriod));
    }
}

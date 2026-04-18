package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.AcademicPeriodMapper;
import com.rusobr.academic.infrastructure.mapper.LessonInstanceMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.web.dto.lessonInstance.DatesGradesDto;
import com.rusobr.academic.web.dto.lessonInstance.GradeJournalProjection;
import com.rusobr.academic.web.dto.lessonInstance.GradeLessonDto;
import com.rusobr.academic.web.dto.lessonInstance.GradesLessonsResponse;
import com.rusobr.academic.web.dto.scheduleLesson.DiaryLessonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
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
    private final AcademicPeriodRepository academicPeriodRepository;
    private final AcademicPeriodMapper academicPeriodMapper;

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

    @Transactional(readOnly = true)
    public GradesLessonsResponse getGradesLessonsByStudentId(Long studentId, Long academicPeriodId) {
        //Получаем Academic period
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(academicPeriodId)
                .orElseThrow(() -> new NotFoundException("Academic period not found: " + academicPeriodId));


        //Получаем плоский список оценок по предметам
        List<GradeJournalProjection> gradeJournal = lessonInstanceRepository.findGradesLessonsByStudentId(studentId,
                academicPeriod.getStartDate(), academicPeriod.getEndDate());

        //Получаем даты для верхней строки таблицы (даты четверти)
        List<LocalDate> dates = lessonInstanceRepository.findLessonDatesByStudentId(studentId,
                academicPeriod.getStartDate(), academicPeriod.getEndDate());

        //Преобразуем в map, где название предмета key а список оценок value
        Map<String, List<GradeLessonDto>> mappedGradesBySubject =
                gradeJournal.stream()
                        //Группируем по названию предмета
                        .collect(Collectors.groupingBy(
                                GradeJournalProjection::subjectName,
                                LinkedHashMap::new,
                                Collectors.mapping(
                                        //value список оценок
                                        p -> new GradeLessonDto(
                                                p.gradeId(), p.value(), p.weight(), p.gradeType(), p.date()
                                        ),
                                        Collectors.toList()
                                )
                        ));

        //Превращаем map в dto
        List<DatesGradesDto> subjects = mappedGradesBySubject.entrySet().stream()
                .map(e -> new DatesGradesDto(e.getKey(), e.getValue()))
                .toList();

        return new GradesLessonsResponse(academicPeriodMapper.toDto(academicPeriod), dates, subjects);
    }
}

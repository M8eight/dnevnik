package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.feignClient.UserClient;
import com.rusobr.academic.infrastructure.mapper.AcademicPeriodMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.lessonInstance.*;
import com.rusobr.academic.web.dto.lessonInstance.teacher.AttendanceStudentProjection;
import com.rusobr.academic.web.dto.lessonInstance.teacher.GradeStudentProjection;
import com.rusobr.academic.web.dto.lessonInstance.teacher.StudentJournalDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.TeacherJournalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonInstanceService {

    private final LessonInstanceRepository lessonInstanceRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final AcademicPeriodMapper academicPeriodMapper;
    private final SchoolClassRepository schoolClassRepository;
    private final UserClient userClient;

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

    public TeacherJournalResponse getGradesAttendancesByTeachingAssignment(Long teachingAssignmentId,
                                                                           Long academicPeriodId) {
        //Получаем Academic period
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(academicPeriodId)
                .orElseThrow(() -> new NotFoundException("Academic period not found: " + academicPeriodId));

        //Получаем экземпляры lessonInstance для верхней строки таблицы
        List<LessonInstanceDto> lessonInstances = lessonInstanceRepository.findLessonInstanceByTeachingAssignmentId(teachingAssignmentId,
                academicPeriod.getStartDate(), academicPeriod.getEndDate());

        //Получаем список учеников с именами
        List<Long> studentsIds = schoolClassRepository.findStudentsIdsByTeachingAssignment(teachingAssignmentId);
        List<UserFeignResponse> studentNames = userClient.getBatchUsers(studentsIds);

        //Получаем оценки и посещаемость из базы данных
        List<GradeStudentProjection> grades = lessonInstanceRepository.findGradesByTeachingAssignment(teachingAssignmentId,
                academicPeriod.getStartDate(), academicPeriod.getEndDate());

        List<AttendanceStudentProjection> attendances = lessonInstanceRepository.findAttendancesByTeachingAssignment(
                teachingAssignmentId,
                academicPeriod.getStartDate(), academicPeriod.getEndDate());

        //Группируем оценки и посещаемость по id ученика
        var gradeJournal = grades.stream()
                .collect(Collectors.groupingBy(GradeStudentProjection::studentId, LinkedHashMap::new, Collectors.mapping(
                        p -> new StudentJournalDto.GradeLessonTeacherDto(
                                p.gradeId(), p.value(), p.weight(), p.gradeType(), p.lessonInstanceId()
                        ),
                        Collectors.toList()
                )));

        var attendanceJournal = attendances.stream()
                .collect(Collectors.groupingBy(AttendanceStudentProjection::studentId, LinkedHashMap::new, Collectors.mapping(
                        p -> new StudentJournalDto.AttendanceLessonTeacherDto(
                                p.attendanceId(), p.attendanceStatus(), p.lessonInstanceId()
                        ),
                        Collectors.toList()
                )));

        //Собираем все в один список
        Set<Long> allStudentIds = new LinkedHashSet<>();
        allStudentIds.addAll(gradeJournal.keySet());
        allStudentIds.addAll(attendanceJournal.keySet());

        //Преобразуем в dto с studentId и списком оценок и посещаемости
        List<StudentJournalDto> studentJournal = allStudentIds.stream()
                .map(studentId -> new StudentJournalDto(
                        studentId,
                        gradeJournal.getOrDefault(studentId, List.of()),
                        attendanceJournal.getOrDefault(studentId, List.of())
                ))
                .toList();

        return new TeacherJournalResponse(academicPeriodMapper.toDto(academicPeriod),
                studentNames, lessonInstances, studentJournal);
    }

    public List<LessonInstanceDto> getLessonInstanceByTeachingAssignmentId(Long teachingAssignmentId, Long academicPeriodId) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(academicPeriodId)
                .orElseThrow(() -> new NotFoundException("Academic period not found: " + academicPeriodId));

        return lessonInstanceRepository.findLessonInstanceByTeachingAssignmentId(teachingAssignmentId,
                academicPeriod.getStartDate(), academicPeriod.getEndDate());
    }

    public void generateForLesson(ScheduleLesson scheduleLesson) {
        LocalDate from = scheduleLesson.getValidFrom();
        LocalDate to = from.plusWeeks(2);

        if (scheduleLesson.getValidTo() != null && scheduleLesson.getValidTo().isBefore(to)) {
            to = scheduleLesson.getValidTo();
        }

        generateBetween(scheduleLesson, from, to);
    }

    public void generateBetween(ScheduleLesson scheduleLesson, LocalDate from, LocalDate to) {
        DayOfWeek dayOfWeek = scheduleLesson.getDayOfWeek();

        // начинаем с первого подходящего дня недели в периоде
        LocalDate current = from.with(TemporalAdjusters.nextOrSame(
                DayOfWeek.valueOf(dayOfWeek.name())
        ));

        while (!current.isAfter(to)) {
            if (!lessonInstanceRepository.existsByScheduleLessonAndLessonDate(scheduleLesson, current)) {
                LessonInstance li = LessonInstance.builder()
                        .scheduleLesson(scheduleLesson)
                        .lessonDate(current)
                        .build();

                lessonInstanceRepository.save(li);
            }
            current = current.plusWeeks(1);
        }
    }

}

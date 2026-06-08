package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.LessonInstanceMapper;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.web.exception.NotFoundException;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.application.mapper.AcademicPeriodMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.lessonInstance.*;
import com.rusobr.academic.web.dto.lessonInstance.teacher.AttendanceStudentDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.GradeStudentDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.StudentJournalDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.TeacherJournalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JournalService {

    private final LessonInstanceRepository lessonInstanceRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final AcademicPeriodMapper academicPeriodMapper;
    private final SchoolClassRepository schoolClassRepository;
    private final UserClient userClient;
    private final LessonInstanceMapper lessonInstanceMapper;

    @Transactional(readOnly = true)
    public GradesLessonsResponse getGradesLessonsByStudentId(Long studentId, Long academicPeriodId) {
        //Получаем Academic period
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(academicPeriodId)
                .orElseThrow(() -> new NotFoundException("Academic period not found: " + academicPeriodId));

        //Получаем плоский список оценок по предметам
        List<GradeJournalDto> gradeJournal = lessonInstanceRepository.findGradesLessonsByStudentId(studentId,
                academicPeriod.getStartDate(), academicPeriod.getEndDate())
                .stream().map(lessonInstanceMapper::toGradeJournalProjection).toList();

        //Получаем даты для верхней строки таблицы (даты четверти)
        List<LocalDate> dates = lessonInstanceRepository.findLessonDatesByStudentId(studentId,
                academicPeriod.getStartDate(), academicPeriod.getEndDate());

        //Преобразуем в map, где название предмета key а список оценок value
        Map<String, List<GradeLessonDto>> mappedGradesBySubject =
                gradeJournal.stream()
                        //Группируем по названию предмета
                        .collect(Collectors.groupingBy(
                                GradeJournalDto::subjectName,
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

        return new GradesLessonsResponse(academicPeriodMapper.toResponse(academicPeriod), dates, subjects);
    }

    public TeacherJournalResponse getJournalByAssignment(Long teachingAssignmentId,
                                                         Long academicPeriodId) {
        //Получаем Academic period
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(academicPeriodId)
                .orElseThrow(() -> new NotFoundException("Academic period not found: " + academicPeriodId));

        //Получаем экземпляры lessonInstance для верхней строки таблицы
        List<LessonInstanceDto> lessonInstances = lessonInstanceRepository.findLessonInstanceByTeachingAssignmentId(teachingAssignmentId,
                academicPeriod.getStartDate(), academicPeriod.getEndDate())
                .stream().map(lessonInstanceMapper::toLessonInstanceDto).toList();

        //Получаем список учеников с именами
        List<Long> studentsIds = schoolClassRepository.findStudentsIdsByTeachingAssignment(teachingAssignmentId);
        List<UserFeignResponse> studentNames = userClient.getBatchUsers(studentsIds);

        //Получаем оценки и посещаемость из базы данных
        List<GradeStudentDto> grades = lessonInstanceRepository.findGradesByTeachingAssignment(teachingAssignmentId,
                academicPeriod.getStartDate(), academicPeriod.getEndDate())
                .stream().map(lessonInstanceMapper::toGradeStudentDto).toList();

        List<AttendanceStudentDto> attendances = lessonInstanceRepository.findAttendancesByTeachingAssignment(
                teachingAssignmentId,
                academicPeriod.getStartDate(), academicPeriod.getEndDate())
                .stream().map(lessonInstanceMapper::toAttendanceStudentDto).toList();

        //Группируем оценки и посещаемость по id ученика
        var gradeJournal = grades.stream()
                .collect(Collectors.groupingBy(GradeStudentDto::studentId, LinkedHashMap::new, Collectors.mapping(
                        p -> new StudentJournalDto.GradeLessonTeacherDto(
                                p.gradeId(), p.value(), p.weight(), p.gradeType(), p.lessonInstanceId()
                        ),
                Collectors.toList())));

        var attendanceJournal = attendances.stream()
                .collect(Collectors.groupingBy(AttendanceStudentDto::studentId, LinkedHashMap::new, Collectors.mapping(
                        p -> new StudentJournalDto.AttendanceLessonTeacherDto(
                                p.attendanceId(), p.status(), p.lessonInstanceId()
                        ),
                Collectors.toList())));

        //Собираем все в один список
        Set<Long> allStudentIds = new LinkedHashSet<>();
        allStudentIds.addAll(gradeJournal.keySet());
        allStudentIds.addAll(attendanceJournal.keySet());

        //Преобразуем в dto с studentId и списком оценок и посещаемости
        List <StudentJournalDto > studentJournal = allStudentIds.stream().map(studentId -> {
                    var studentGrades = gradeJournal.getOrDefault(studentId, List.of());
                    //считаем среднее взвешенное и округляем до 2-х знаков
                    double gradeTop = studentGrades.stream().mapToDouble(g -> g.value() * g.weight()).sum();
                    double gradeBottom = studentGrades.stream().mapToDouble(StudentJournalDto.GradeLessonTeacherDto::weight).sum();
                    double average = (gradeBottom > 0)
                            ? BigDecimal.valueOf(gradeTop / gradeBottom)
                              .setScale(2, RoundingMode.HALF_UP)
                              .doubleValue()
                            : 0.0;

                    return new StudentJournalDto(
                            studentId, gradeJournal.getOrDefault(studentId, List.of()),
                            average, attendanceJournal.getOrDefault(studentId, List.of())
                    );
        }).toList();

        return new TeacherJournalResponse(academicPeriodMapper.toResponse(academicPeriod), studentNames, lessonInstances, studentJournal);
    }

    public List<LessonInstanceDto> getInstancesByAssignment(Long teachingAssignmentId, Long academicPeriodId) {
        AcademicPeriod academicPeriod = academicPeriodRepository.findById(academicPeriodId)
                .orElseThrow(() -> new NotFoundException("Academic period not found: " + academicPeriodId));

        return lessonInstanceRepository.findLessonInstanceByTeachingAssignmentId(teachingAssignmentId,
                academicPeriod.getStartDate(), academicPeriod.getEndDate())
                .stream().map(lessonInstanceMapper::toLessonInstanceDto).toList();
    }

    public void generateInstanceForLesson(ScheduleLesson scheduleLesson) {
        LocalDate from = scheduleLesson.getValidFrom();
        LocalDate to = from.plusWeeks(2);

        if (scheduleLesson.getValidTo() != null && scheduleLesson.getValidTo().isBefore(to)) {
            to = scheduleLesson.getValidTo();
        }

        generateInstanceBetween(scheduleLesson, from, to);
    }

    public void generateInstanceBetween(ScheduleLesson scheduleLesson, LocalDate from, LocalDate to) {
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

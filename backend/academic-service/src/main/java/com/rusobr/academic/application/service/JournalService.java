package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.AcademicPeriodMapper;
import com.rusobr.academic.application.mapper.LessonInstanceMapper;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.web.dto.feign.BatchUserResponse;
import com.rusobr.academic.web.dto.lessonInstance.*;
import com.rusobr.academic.web.dto.lessonInstance.teacher.AttendanceStudentDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.GradeStudentDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.StudentJournalDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.TeacherJournalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

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
    private final AcademicPeriodMapper academicPeriodMapper;
    private final SchoolClassRepository schoolClassRepository;
    private final UserClient userClient;
    private final LessonInstanceMapper lessonInstanceMapper;
    private final AcademicPeriodService academicPeriodService;
    private final TransactionTemplate readOnlyTransactionTemplate;

    private record JournalDbData(
            AcademicPeriod academicPeriod,
            List<LessonInstanceDto> lessonInstances,
            List<Long> studentsIds,
            List<GradeStudentDto> grades,
            List<AttendanceStudentDto> attendances
    ) {}

    @Transactional(readOnly = true)
    public GradesLessonsResponse getGradesLessonsByStudentId(Long studentId, Long academicPeriodId) {
        //Получаем Academic period
        AcademicPeriod academicPeriod = academicPeriodService.getById(academicPeriodId);

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

    public TeacherJournalResponse getJournalByAssignment(Long teachingAssignmentId, Long academicPeriodId) {
        JournalDbData data = Objects.requireNonNull(readOnlyTransactionTemplate.execute(status ->
                fetchJournalData(teachingAssignmentId, academicPeriodId)));

        BatchUserResponse students = userClient.getBatchUsers(data.studentsIds());

        List<StudentJournalDto> studentJournal = buildStudentJournal(data.grades(), data.attendances());

        return new TeacherJournalResponse(
                academicPeriodMapper.toResponse(data.academicPeriod()),
                students,
                data.lessonInstances(),
                studentJournal
        );
    }

    private JournalDbData fetchJournalData(Long teachingAssignmentId, Long academicPeriodId) {
        AcademicPeriod academicPeriod = academicPeriodService.getById(academicPeriodId);

        List<LessonInstanceDto> lessonInstances = lessonInstanceRepository
                .findLessonInstanceByTeachingAssignmentId(teachingAssignmentId, academicPeriod.getStartDate(), academicPeriod.getEndDate())
                .stream().map(lessonInstanceMapper::toLessonInstanceDto).toList();

        List<Long> studentsIds = schoolClassRepository.findStudentsIdsByTeachingAssignment(teachingAssignmentId);

        List<GradeStudentDto> grades = lessonInstanceRepository
                .findGradesByTeachingAssignment(teachingAssignmentId, academicPeriod.getStartDate(), academicPeriod.getEndDate())
                .stream().map(lessonInstanceMapper::toGradeStudentDto).toList();

        List<AttendanceStudentDto> attendances = lessonInstanceRepository
                .findAttendancesByTeachingAssignment(teachingAssignmentId, academicPeriod.getStartDate(), academicPeriod.getEndDate())
                .stream().map(lessonInstanceMapper::toAttendanceStudentDto).toList();

        return new JournalDbData(academicPeriod, lessonInstances, studentsIds, grades, attendances);
    }

    private List<StudentJournalDto> buildStudentJournal(List<GradeStudentDto> grades, List<AttendanceStudentDto> attendances) {
        var gradeJournal = grades.stream()
                .collect(Collectors.groupingBy(GradeStudentDto::studentId, LinkedHashMap::new, Collectors.mapping(
                        p -> new StudentJournalDto.GradeLessonTeacherDto(p.gradeId(), p.value(), p.weight(), p.gradeType(), p.lessonInstanceId()),
                        Collectors.toList())));

        var attendanceJournal = attendances.stream()
                .collect(Collectors.groupingBy(AttendanceStudentDto::studentId, LinkedHashMap::new, Collectors.mapping(
                        p -> new StudentJournalDto.AttendanceLessonTeacherDto(p.attendanceId(), p.status(), p.lessonInstanceId()),
                        Collectors.toList())));

        Set<Long> allStudentIds = new LinkedHashSet<>();
        allStudentIds.addAll(gradeJournal.keySet());
        allStudentIds.addAll(attendanceJournal.keySet());

        return allStudentIds.stream()
                .map(studentId -> {
                    var studentGrades = gradeJournal.getOrDefault(studentId, List.of());
                    return new StudentJournalDto(
                            studentId,
                            studentGrades,
                            calculateWeightedAverage(studentGrades),
                            attendanceJournal.getOrDefault(studentId, List.of())
                    );
                }).toList();
    }

    private double calculateWeightedAverage(List<StudentJournalDto.GradeLessonTeacherDto> grades) {
        double top = grades.stream().mapToDouble(g -> g.value() * g.weight()).sum();
        double bottom = grades.stream().mapToDouble(StudentJournalDto.GradeLessonTeacherDto::weight).sum();
        return bottom > 0
                ? BigDecimal.valueOf(top / bottom).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;
    }

    public List<LessonInstanceDto> getInstancesByAssignment(Long teachingAssignmentId, Long academicPeriodId) {
        AcademicPeriod academicPeriod = academicPeriodService.getById(academicPeriodId);

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

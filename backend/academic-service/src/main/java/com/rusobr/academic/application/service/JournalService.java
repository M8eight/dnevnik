package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.AcademicPeriodMapper;
import com.rusobr.academic.application.mapper.LessonInstanceMapper;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.ClassGroupStudents;
import com.rusobr.academic.domain.model.ClassStudent;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.dto.grade.PeriodFinalGradeResponse;
import com.rusobr.academic.web.dto.grade.WeightedGrade;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeStudentResponse;
import com.rusobr.academic.web.dto.lessonInstance.*;
import com.rusobr.academic.web.dto.lessonInstance.teacher.StudentJournalDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.TeacherJournalResponse;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.dto.BatchUserResponse;
import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JournalService {

    private final LessonInstanceRepository lessonInstanceRepository;
    private final AcademicPeriodMapper academicPeriodMapper;
    private final UserClient userClient;
    private final LessonInstanceMapper lessonInstanceMapper;
    private final TransactionTemplate readOnlyTransactionTemplate;
    private final PeriodGradeService periodGradeService;
    private final FinalGradeService finalGradeService;
    private final AcademicPeriodService academicPeriodService;
    private final TeachingAssignmentRepository teachingAssignmentRepository;


    @Cacheable(value = "journalByStudentId", key = "#studentId + '#' + #academicPeriodId")
    @Transactional(readOnly = true)
    public GradesLessonsResponse getGradesByStudentId(Long studentId, Long academicPeriodId) {
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
                .map(e -> new DatesGradesDto(e.getKey(), e.getValue(), calculateWeightedAverage(e.getValue())))
                .toList();

        return new GradesLessonsResponse(academicPeriodMapper.toResponse(academicPeriod), dates, subjects);
    }

    @Cacheable(value = "journalPeriodFinalGradeByStudentId", key = "#studentId + '#' + #academicYearId")
    public List<PeriodFinalGradeResponse> getPeriodFinalGrades(Long studentId, Long academicYearId) {
        Map<String, List<PeriodGradeStudentResponse>> periodGrades = periodGradeService.getByStudentId(studentId, academicYearId);
        Map<String, FinalGradeResponse> finalGrades = finalGradeService.getByStudentId(studentId, academicYearId);
        return periodGrades.keySet().stream().map(
                subject ->
                    new PeriodFinalGradeResponse(
                            subject,
                            periodGrades.get(subject),
                            finalGrades.get(subject)
                    )).toList();
    }

    @Cacheable(value = "journalByAssignment", key = "#teachingAssignmentId + '#' + #academicPeriodId", unless = "#result.degradedStudents")
    public TeacherJournalResponse getJournalByAssignment(Long teachingAssignmentId, Long academicPeriodId) {
        JournalDbData data = Objects.requireNonNull(readOnlyTransactionTemplate.execute(status ->
                fetchJournalData(teachingAssignmentId, academicPeriodId)));

        BatchUserResponse students = userClient.getBatchStudents(data.studentsIds());

        var mappedStudents = students.found().stream().collect(Collectors.toMap(
                UserFeignResponse::id,
                user -> user
        ));

        var mappedGrades = data.grades.stream().collect(Collectors.groupingBy(
                StudentJournalDto.GradeLessonTeacherDto::studentId,
                LinkedHashMap::new,
                Collectors.groupingBy(
                        StudentJournalDto.GradeLessonTeacherDto::lessonInstanceId,
                        Collectors.toList()
                )
        ));

        var mappedAttendances = data.attendances.stream().collect(Collectors.groupingBy(
                StudentJournalDto.AttendanceLessonTeacherDto::studentId,
                LinkedHashMap::new,
                Collectors.toMap(
                        StudentJournalDto.AttendanceLessonTeacherDto::lessonInstanceId,
                        a -> a
                )
        ));

        var journal = data.studentsIds().stream().map(
                studentId -> {
                    var gradesOrDefault = mappedGrades.getOrDefault(studentId, Map.of());
                    List<StudentJournalDto.GradeLessonTeacherDto> allGrades = gradesOrDefault.values().stream().flatMap(List::stream).toList();
                    return new StudentJournalDto(
                            mappedStudents.get(studentId),
                            gradesOrDefault,
                            mappedAttendances.getOrDefault(studentId, Map.of()),
                            calculateWeightedAverage(allGrades)
                            );
                }
        ).toList();

        return new TeacherJournalResponse(
                data.academicPeriod(),
                data.lessonInstances(),
                journal,
                students.degraded()
        );
    }

    private record JournalDbData(
            AcademicPeriodResponse academicPeriod,
            List<LessonInstanceDto> lessonInstances,
            List<Long> studentsIds,
            List<StudentJournalDto.GradeLessonTeacherDto> grades,
            List<StudentJournalDto.AttendanceLessonTeacherDto> attendances
    ) {}

    private JournalDbData fetchJournalData(Long teachingAssignmentId, Long academicPeriodId) {
        AcademicPeriod academicPeriod = academicPeriodService.getById(academicPeriodId);

        List<LessonInstanceDto> lessonInstances = lessonInstanceRepository
                .findLessonInstanceByTeachingAssignmentId(teachingAssignmentId, academicPeriod.getStartDate(), academicPeriod.getEndDate())
                .stream().map(lessonInstanceMapper::toLessonInstanceDto).toList();

        TeachingAssignment ta = teachingAssignmentRepository.findWithGroup(teachingAssignmentId)
                .orElseThrow(() -> new NotFoundException("TeachingAssignment with id=%s not found",
                        AcademicExceptionCode.TEACHING_ASSIGNMENT_NOT_FOUND));
        List<Long> studentsIds;
        if (ta.getClassGroup() == null) {
            studentsIds = ta.getSchoolClass().getStudents()
                    .stream().map(ClassStudent::getStudentId).toList();
        } else {
            studentsIds = ta.getClassGroup().getClassGroupStudents()
                    .stream().map(ClassGroupStudents::getStudentId).toList();
        }

        List<StudentJournalDto.GradeLessonTeacherDto> grades = lessonInstanceRepository
                .findGradesByTeachingAssignment(teachingAssignmentId, academicPeriod.getStartDate(), academicPeriod.getEndDate())
                .stream().map(lessonInstanceMapper::toGradeStudentDto).toList();

        List<StudentJournalDto.AttendanceLessonTeacherDto> attendances = lessonInstanceRepository
                .findAttendancesByTeachingAssignment(teachingAssignmentId, academicPeriod.getStartDate(), academicPeriod.getEndDate())
                .stream().map(lessonInstanceMapper::toAttendanceStudentDto).toList();

        return new JournalDbData(academicPeriodMapper.toResponse(academicPeriod), lessonInstances, studentsIds, grades, attendances);
    }

    public static double calculateWeightedAverage(List<? extends WeightedGrade> grades) {
        double top = grades.stream().mapToDouble(g -> g.value() * g.weight()).sum();
        double bottom = grades.stream().mapToDouble(WeightedGrade::weight).sum();
        return bottom > 0
                ? BigDecimal.valueOf(top / bottom).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;
    }

    @Cacheable(value = "lessonInstancesByAssignment", key = "#teachingAssignmentId + '#' + #academicPeriodId")
    public List<LessonInstanceDto> getInstancesByAssignment(Long teachingAssignmentId, Long academicPeriodId) {
        AcademicPeriod academicPeriod = academicPeriodService.getById(academicPeriodId);

        return lessonInstanceRepository.findLessonInstanceByTeachingAssignmentId(teachingAssignmentId,
                academicPeriod.getStartDate(), academicPeriod.getEndDate())
                .stream().map(lessonInstanceMapper::toLessonInstanceDto).toList();
    }


}

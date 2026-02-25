package com.rusobr.academic.infrastructure.initializer;

import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.*;
import com.rusobr.academic.infrastructure.persistence.repository.*;
import com.rusobr.academic.web.dto.lessonInstance.LessonWeekDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ExampleDataInitializer implements CommandLineRunner {

    private final GradeConstantRepository gradeConstantRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final ScheduleLessonRepository scheduleLessonRepository;
    private final LessonInstanceRepository lessonInstanceRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    public void run(String... args) {

        createGradeConstants();

        // ====== ПРЕДМЕТЫ ======
        Subject algebra = subjectRepository.save(
                Subject.builder().name("Алгебра").build());

        Subject russian = subjectRepository.save(
                Subject.builder().name("Русский язык").build());

        Subject physics = subjectRepository.save(
                Subject.builder().name("Физика").build());

        Subject informatics = subjectRepository.save(
                Subject.builder().name("Информатика").build());

        // ====== КЛАСС ======
        SchoolClass class8b = schoolClassRepository.save(
                SchoolClass.builder()
                        .name("8Б")
                        .year("2025")
                        .classTeacherId(1L)
                        .build()
        );

        // ====== УЧЕНИК (один в классе) ======
        Long studentId = 1L;

        // ====== НАЗНАЧЕНИЯ ======
        TeachingAssignment algebraAssignment = teachingAssignmentRepository.save(
                TeachingAssignment.builder()
                        .teacherId(1L)
                        .schoolClass(class8b)
                        .subject(algebra)
                        .build()
        );

        TeachingAssignment russianAssignment = teachingAssignmentRepository.save(
                TeachingAssignment.builder()
                        .teacherId(2L)
                        .schoolClass(class8b)
                        .subject(russian)
                        .build()
        );

        TeachingAssignment physicsAssignment = teachingAssignmentRepository.save(
                TeachingAssignment.builder()
                        .teacherId(3L)
                        .schoolClass(class8b)
                        .subject(physics)
                        .build()
        );

        TeachingAssignment informaticsAssignment = teachingAssignmentRepository.save(
                TeachingAssignment.builder()
                        .teacherId(4L)
                        .schoolClass(class8b)
                        .subject(informatics)
                        .build()
        );

        // ====== РАСПИСАНИЕ НА НЕДЕЛЮ ======
        ScheduleLesson mondayAlgebra = scheduleLessonRepository.save(
                ScheduleLesson.builder()
                        .classRoom("28")
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .lessonNumber(1)
                        .teachingAssignment(algebraAssignment)
                        .build()
        );

        ScheduleLesson tuesdayRussian = scheduleLessonRepository.save(
                ScheduleLesson.builder()
                        .classRoom("30")
                        .dayOfWeek(DayOfWeek.TUESDAY)
                        .lessonNumber(2)
                        .teachingAssignment(russianAssignment)
                        .build()
        );

        ScheduleLesson wednesdayPhysics = scheduleLessonRepository.save(
                ScheduleLesson.builder()
                        .classRoom("12")
                        .dayOfWeek(DayOfWeek.WEDNESDAY)
                        .lessonNumber(3)
                        .teachingAssignment(physicsAssignment)
                        .build()
        );

        ScheduleLesson thursdayInformatics = scheduleLessonRepository.save(
                ScheduleLesson.builder()
                        .classRoom("15")
                        .dayOfWeek(DayOfWeek.THURSDAY)
                        .lessonNumber(4)
                        .teachingAssignment(informaticsAssignment)
                        .build()
        );

        ScheduleLesson fridayAlgebra = scheduleLessonRepository.save(
                ScheduleLesson.builder()
                        .classRoom("28")
                        .dayOfWeek(DayOfWeek.FRIDAY)
                        .lessonNumber(2)
                        .teachingAssignment(algebraAssignment)
                        .build()
        );

        // ====== КОНКРЕТНАЯ НЕДЕЛЯ ======
        LocalDate monday = LocalDate.of(2025, 3, 3);
        LocalDate sun = LocalDate.of(2025, 3, 10);

        createLessonWithData(monday, mondayAlgebra, studentId, 5, GradeType.HOMEWORK, AttendanceStatus.PRESENT);
        createLessonWithData(monday.plusDays(1), tuesdayRussian, studentId, 4, GradeType.TEST, AttendanceStatus.LATE);
        createLessonWithData(monday.plusDays(2), wednesdayPhysics, studentId, 3, GradeType.CONTROL, AttendanceStatus.PRESENT);
        createLessonWithData(monday.plusDays(3), thursdayInformatics, studentId, 5, GradeType.HOMEWORK, AttendanceStatus.PRESENT);
        createLessonWithData(monday.plusDays(4), fridayAlgebra, studentId, 4, GradeType.TEST, AttendanceStatus.EXCUSED);

        List<LessonWeekDto> schedule = lessonInstanceRepository.getSchedule(class8b.getId(), studentId, monday, sun);
        log.info("Schedule lesson instances: {}", schedule);
        log.info(Arrays.toString(schedule.toArray()));
    }

    private void createLessonWithData(LocalDate date,
                                      ScheduleLesson scheduleLesson,
                                      Long studentId,
                                      int gradeValue,
                                      GradeType gradeType,
                                      AttendanceStatus attendanceStatus) {

        LessonInstance lessonInstance = lessonInstanceRepository.save(
                LessonInstance.builder()
                        .date(date)
                        .scheduleLesson(scheduleLesson)
                        .build()
        );

        attendanceRepository.save(
                Attendance.builder()
                        .studentId(studentId)
                        .lessonInstance(lessonInstance)
                        .status(attendanceStatus)
                        .build()
        );

        gradeRepository.save(
                Grade.builder()
                        .value(gradeValue)
                        .type(gradeType.name())
                        .studentId(studentId)
                        .lessonInstance(lessonInstance)
                        .build()
        );
    }

    private void createGradeConstants() {
        gradeConstantRepository.save(
                GradeConstant.builder().name("5").description("Отлично").value(5).build());

        gradeConstantRepository.save(
                GradeConstant.builder().name("4").description("Хорошо").value(4).build());

        gradeConstantRepository.save(
                GradeConstant.builder().name("3").description("Удовлетворительно").value(3).build());

        gradeConstantRepository.save(
                GradeConstant.builder().name("2").description("Неудовлетворительно").value(2).build());

        gradeConstantRepository.save(
                GradeConstant.builder().name("1").description("Очень плохо").value(1).build());
    }
}
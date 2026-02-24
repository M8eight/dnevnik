package com.rusobr.academic.infrastructure.initializer;

import com.rusobr.academic.domain.model.*;
import com.rusobr.academic.infrastructure.persistence.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Transactional
public class ExampleDataInitializer implements CommandLineRunner {

    private final GradeConstantRepository gradeConstantRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final ScheduleLessonRepository scheduleLessonRepository;
    private final LessonInstanceRepository lessonInstanceRepository;

    @Override
    public void run(String... args) {
        // 1. Константы оценок
        createGradeConstants();

        // 2. Предметы
        Subject algebra = subjectRepository.save(
                Subject.builder().name("Алгебра").build());

        Subject russian = subjectRepository.save(
                Subject.builder().name("Русский язык").build());

        Subject art = subjectRepository.save(
                Subject.builder().name("ИЗО").build());

        // 3. Класс
        SchoolClass class8b = schoolClassRepository.save(
                SchoolClass.builder()
                        .name("8Б")
                        .year("2025")
                        .classTeacherId(1L)
                        .build()
        );

        // 4. Назначения преподавателя
        TeachingAssignment algebraAssignment = teachingAssignmentRepository.save(
                TeachingAssignment.builder()
                        .teacherId(1L)
                        .schoolClass(class8b)
                        .subject(algebra)
                        .build()
        );

        TeachingAssignment russianAssignment = teachingAssignmentRepository.save(
                TeachingAssignment.builder()
                        .teacherId(1L)
                        .schoolClass(class8b)
                        .subject(russian)
                        .build()
        );

        // 5. Расписание
        ScheduleLesson lesson1 = scheduleLessonRepository.save(
                ScheduleLesson.builder()
                        .classRoom("28")
                        .dayOfWeek(DayOfWeek.WEDNESDAY)
                        .lessonNumber(1)
                        .teachingAssignment(algebraAssignment)
                        .build()
        );

        ScheduleLesson lesson2 = scheduleLessonRepository.save(
                ScheduleLesson.builder()
                        .classRoom("28")
                        .dayOfWeek(DayOfWeek.WEDNESDAY)
                        .lessonNumber(2)
                        .teachingAssignment(algebraAssignment)
                        .build()
        );

        ScheduleLesson lesson3 = scheduleLessonRepository.save(
                ScheduleLesson.builder()
                        .classRoom("28")
                        .dayOfWeek(DayOfWeek.WEDNESDAY)
                        .lessonNumber(3)
                        .teachingAssignment(russianAssignment)
                        .build()
        );

        // 6. Конкретное проведение урока
        lessonInstanceRepository.save(
                LessonInstance.builder()
                        .date(LocalDate.of(2025, 2, 28))
                        .scheduleLesson(lesson1)
                        .build()
        );
    }

    private void createGradeConstants() {
        gradeConstantRepository.save(
                GradeConstant.builder().name("5")
                        .description("Пятерка, высшая оценка")
                        .value(5)
                        .build());

        gradeConstantRepository.save(
                GradeConstant.builder().name("4")
                        .description("Четверка, оценка хорошо")
                        .value(4)
                        .build());

        gradeConstantRepository.save(
                GradeConstant.builder().name("3")
                        .description("Тройка, оценка удовлетворительно")
                        .value(3)
                        .build());

        gradeConstantRepository.save(
                GradeConstant.builder().name("2")
                        .description("Двойка, оценка неудовлетворительно")
                        .value(2)
                        .build());

        gradeConstantRepository.save(
                GradeConstant.builder().name("1")
                        .description("Кол, это печально")
                        .value(1)
                        .build());
    }
}
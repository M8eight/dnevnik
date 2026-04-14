package com.rusobr.academic.infrastructure.initializer;

import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.*;
import com.rusobr.academic.infrastructure.persistence.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DebugDataInitializer implements CommandLineRunner {

    private static final LocalDate ANCHOR_DATE = LocalDate.of(2026, 4, 6);

    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final ScheduleLessonRepository scheduleLessonRepository;
    private final LessonInstanceRepository lessonInstanceRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final HomeworkRepository homeworkRepository;

    private final Random rng = new Random(42);

    @Override
    public void run(String... args) {
        if (schoolClassRepository.count() > 0) {
            log.info("Данные уже инициализированы. Пропуск.");
            return;
        }

        log.info("=== Полная инициализация академических данных (8А класс) ===");

        Map<String, Subject> subjects = new LinkedHashMap<>();
        List.of("Алгебра", "Геометрия", "Русский язык", "Литература", "Физика",
                        "Химия", "Биология", "История", "Информатика", "Английский язык", "Физкультура")
                .forEach(name -> subjects.put(name, subjectRepository.save(Subject.builder().name(name).build())));

        List<Long> studentIds = java.util.stream.LongStream.rangeClosed(1, 12).boxed().toList();
        SchoolClass c8a = SchoolClass.builder()
                .name("8А")
                .year("2025")
                .classTeacherId(17L)
                .students(new HashSet<>())
                .build();

        studentIds.forEach(sid -> {
            c8a.getStudents().add(ClassStudent.builder()
                    .schoolClass(c8a)
                    .studentId(sid)
                    .build());
        });
        schoolClassRepository.save(c8a);

        Map<String, TeachingAssignment> ta = new HashMap<>();
        ta.put("Алг", saveTA(17L, c8a, subjects.get("Алгебра")));
        ta.put("Гео", saveTA(17L, c8a, subjects.get("Геометрия")));
        ta.put("Рус", saveTA(18L, c8a, subjects.get("Русский язык")));
        ta.put("Лит", saveTA(18L, c8a, subjects.get("Литература")));
        ta.put("Физ", saveTA(19L, c8a, subjects.get("Физика")));
        ta.put("Хим", saveTA(20L, c8a, subjects.get("Химия")));
        ta.put("Био", saveTA(21L, c8a, subjects.get("Биология")));
        ta.put("Ист", saveTA(22L, c8a, subjects.get("История")));
        ta.put("Инф", saveTA(23L, c8a, subjects.get("Информатика")));
        ta.put("Анг", saveTA(24L, c8a, subjects.get("Английский язык")));
        ta.put("Спорт", saveTA(25L, c8a, subjects.get("Физкультура")));

        Map<DayOfWeek, List<Slot>> weeklyPlan = new LinkedHashMap<>();

        weeklyPlan.put(DayOfWeek.MONDAY, List.of(
                new Slot(1, "201", ta.get("Рус")), new Slot(2, "201", ta.get("Лит")),
                new Slot(3, "305", ta.get("Алг")), new Slot(4, "305", ta.get("Гео")),
                new Slot(5, "Зал", ta.get("Спорт"))
        ));

        weeklyPlan.put(DayOfWeek.TUESDAY, List.of(
                new Slot(1, "402", ta.get("Физ")), new Slot(2, "402", ta.get("Хим")),
                new Slot(3, "104", ta.get("Анг")), new Slot(4, "205", ta.get("Ист")),
                new Slot(5, "301", ta.get("Био"))
        ));

        weeklyPlan.put(DayOfWeek.WEDNESDAY, List.of(
                new Slot(1, "305", ta.get("Алг")), new Slot(2, "305", ta.get("Алг")),
                new Slot(3, "201", ta.get("Рус")), new Slot(4, "ПК-1", ta.get("Инф")),
                new Slot(5, "ПК-1", ta.get("Инф"))
        ));

        weeklyPlan.put(DayOfWeek.THURSDAY, List.of(
                new Slot(1, "104", ta.get("Анг")), new Slot(2, "402", ta.get("Физ")),
                new Slot(3, "205", ta.get("Ист")), new Slot(4, "305", ta.get("Гео")),
                new Slot(5, "201", ta.get("Лит"))
        ));

        weeklyPlan.put(DayOfWeek.FRIDAY, List.of(
                new Slot(1, "301", ta.get("Био")), new Slot(2, "402", ta.get("Хим")),
                new Slot(3, "305", ta.get("Алг")), new Slot(4, "201", ta.get("Рус")),
                new Slot(5, "Зал", ta.get("Спорт"))
        ));

        Map<DayOfWeek, List<ScheduleLesson>> savedSchedule = new HashMap<>();
        weeklyPlan.forEach((day, slots) -> savedSchedule.put(day, schedule(day, slots)));

        for (int i = 0; i < 4; i++) {
            LocalDate mondayOfCurrentWeek = ANCHOR_DATE.plusWeeks(i);
            savedSchedule.forEach((day, lessons) -> {
                LocalDate dateOfLesson = mondayOfCurrentWeek.plusDays(day.getValue() - 1);
                buildDay(dateOfLesson, lessons, c8a);
            });
        }

        savePeriods();

        log.info("=== Инициализация завершена успешно ===");
    }

    private TeachingAssignment saveTA(Long tid, SchoolClass sc, Subject sub) {
        return teachingAssignmentRepository.save(TeachingAssignment.builder()
                .teacherId(tid).schoolClass(sc).subject(sub).build());
    }

    private List<ScheduleLesson> schedule(DayOfWeek day, List<Slot> slots) {
        return slots.stream().map(s -> scheduleLessonRepository.save(ScheduleLesson.builder()
                .dayOfWeek(day)
                .lessonNumber(s.number())
                .classRoom(s.room())
                .teachingAssignment(s.ta())
                .validFrom(ANCHOR_DATE.minusMonths(1))
                .build())).toList();
    }

    @Transactional
    void buildDay(LocalDate date, List<ScheduleLesson> slots, SchoolClass schoolClass) {
        for (ScheduleLesson slot : slots) {
            LessonInstance li = lessonInstanceRepository.save(
                    LessonInstance.builder().lessonDate(date).scheduleLesson(slot).build()
            );

            homeworkRepository.save(Homework.builder()
                    .text("Изучить тему и выполнить упражнения в тетради")
                    .lessonInstance(li).build());

            for (ClassStudent cs : schoolClass.getStudents()) {
                Long sid = cs.getStudentId();
                if (rng.nextInt(100) < 30) {
                    gradeRepository.save(Grade.builder()
                            .lessonInstance(li).studentId(sid)
                            .value(generateRealisticGrade()).weight(1).type(GradeType.HOMEWORK).build());
                }
                if (rng.nextInt(100) < 5) {
                    attendanceRepository.save(Attendance.builder()
                            .lessonInstance(li).studentId(sid)
                            .status(rng.nextBoolean() ? AttendanceStatus.ABSENT : AttendanceStatus.LATE).build());
                }
            }
        }
    }

    private void savePeriods() {
        List<AcademicPeriod> periods = List.of(
                AcademicPeriod.builder()
                        .name("Первая четверть").schoolYear("2025-2026")
                        .startDate(LocalDate.of(2025, 9, 1)).endDate(LocalDate.of(2025, 10, 26))
                        .isClosed(true).build(),

                AcademicPeriod.builder()
                        .name("Вторая четверть").schoolYear("2025-2026")
                        .startDate(LocalDate.of(2025, 11, 5)).endDate(LocalDate.of(2025, 12, 28))
                        .isClosed(true).build(),

                AcademicPeriod.builder()
                        .name("Третья четверть").schoolYear("2025-2026")
                        .startDate(LocalDate.of(2026, 1, 9)).endDate(LocalDate.of(2026, 3, 31))
                        .isClosed(true).build(),

                AcademicPeriod.builder()
                        .name("Четвертая четверть").schoolYear("2025-2026")
                        .startDate(LocalDate.of(2026, 4, 6)).endDate(LocalDate.of(2026, 5, 25))
                        .isClosed(false).build()
        );

        academicPeriodRepository.saveAll(periods);
    }

    private int generateRealisticGrade() {
        int chance = rng.nextInt(100);
        if (chance < 40) return 5;
        if (chance < 80) return 4;
        if (chance < 95) return 3;
        return 2;
    }

    private record Slot(int number, String room, TeachingAssignment ta) {}
}
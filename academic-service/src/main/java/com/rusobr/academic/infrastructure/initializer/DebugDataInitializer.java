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
import java.util.*;
import java.util.stream.LongStream;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
@Deprecated
public class DebugDataInitializer implements CommandLineRunner {

    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final ScheduleLessonRepository scheduleLessonRepository;
    private final LessonInstanceRepository lessonInstanceRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final AcademicPeriodRepository academicPeriodRepository;

    // Ученики с 1 по 12 из User Microservice
    private static final List<Long> CLASS_STUDENTS_IDS = LongStream.rangeClosed(1, 12).boxed().toList();
    private final Random rng = new Random(42);

    @Override
    public void run(String... args) {
        log.info("=== Инициализация Академических Данных (2025-2026 Учебный год) ===");

        // 1. Предметы
        Map<String, Subject> S = new LinkedHashMap<>();
        for (String name : List.of("Алгебра", "Геометрия", "Русский язык", "Литература", "Физика",
                "Химия", "Биология", "История", "Информатика", "Английский язык", "Физкультура")) {
            S.put(name, subjectRepository.save(Subject.builder().name(name).build()));
        }

        // 2. Класс 8А
        SchoolClass c8a = SchoolClass.builder()
                .name("8А")
                .year("2025") // Текущий год поступления/обучения
                .classTeacherId(17L)
                .students(new HashSet<>())
                .build();

        CLASS_STUDENTS_IDS.forEach(sid -> {
            ClassStudent cs = ClassStudent.builder()
                    .schoolClass(c8a)
                    .studentId(sid)
                    .build();
            c8a.getStudents().add(cs);
        });
        schoolClassRepository.save(c8a);

        // 3. Назначения
        TeachingAssignment taAlg = saveTA(17L, c8a, S.get("Алгебра"));
        TeachingAssignment taGeo = saveTA(17L, c8a, S.get("Геометрия"));
        TeachingAssignment taRus = saveTA(18L, c8a, S.get("Русский язык"));
        TeachingAssignment taLit = saveTA(26L, c8a, S.get("Литература"));
        TeachingAssignment taPhy = saveTA(19L, c8a, S.get("Физика"));

        // 4. Расписание
        List<ScheduleLesson> mon = schedule(DayOfWeek.MONDAY, List.of(
                new Slot(1, "201", taRus), new Slot(2, "201", taLit), new Slot(3, "305", taAlg)
        ));
        List<ScheduleLesson> wed = schedule(DayOfWeek.WEDNESDAY, List.of(
                new Slot(1, "201", taRus), new Slot(2, "305", taAlg), new Slot(3, "215", taPhy)
        ));

        // 5. Генерация "прошлых" данных (за последние 3 недели от сегодня)
        // Чтобы в журнале уже были какие-то оценки при открытии
        LocalDate startDay = LocalDate.now().minusWeeks(3).with(DayOfWeek.MONDAY);
        for (int i = 0; i < 4; i++) {
            LocalDate week = startDay.plusWeeks(i);
            buildDay(week, mon, c8a);
            buildDay(week.plusDays(2), wed, c8a);
        }

        log.info("Создаем академические периоды для 2025-2026 года");

        AcademicPeriod periodOne = AcademicPeriod.builder()
                .name("Первая четверть")
                .schoolYear("2025-2026")    // ← было "2024-2025" (баг)
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 10, 26))  // ← было 2026-10-26 (опечатка)
                .build();

        AcademicPeriod periodTwo = AcademicPeriod.builder()
                .name("Вторая четверть")
                .schoolYear("2025-2026")    // ← без изменений, было правильно
                .startDate(LocalDate.of(2025, 11, 5))
                .endDate(LocalDate.of(2025, 12, 28))
                .build();

        AcademicPeriod periodThree = AcademicPeriod.builder()
                .name("Третья четверть")
                .schoolYear("2025-2026")
                .startDate(LocalDate.of(2026, 1, 9))
                .endDate(LocalDate.of(2026, 3, 31))   // ← без изменений
                .build();

        AcademicPeriod periodFour = AcademicPeriod.builder()
                .name("Четвертая четверть")
                .schoolYear("2025-2026")
                .startDate(LocalDate.of(2026, 4, 6))  // ← было 3/30 (каникулы ещё идут)
                .endDate(LocalDate.of(2026, 5, 25))   // ← было 5/30 (обычно раньше)
                .build();

        academicPeriodRepository.saveAll(Arrays.asList(periodOne, periodTwo, periodThree, periodFour));

        log.info("=== Инициализация завершена. Сегодня: {} ===", LocalDate.now());
    }

    private void buildDay(LocalDate date, List<ScheduleLesson> slots, SchoolClass schoolClass) {

        for (ScheduleLesson slot : slots) {
            LessonInstance li = lessonInstanceRepository.save(
                    LessonInstance.builder().date(date).scheduleLesson(slot).build()
            );

            // Итерируемся по списку студентов из сущности ClassStudent
            for (ClassStudent cs : schoolClass.getStudents()) {
                Long sid = cs.getStudentId();

                if (rng.nextInt(100) < 25) {
                    gradeRepository.save(Grade.builder()
                            .lessonInstance(li)
                            .studentId(sid)
                            .value(generateRealisticGrade())
                            .type(GradeType.values()[rng.nextInt(GradeType.values().length)])
                            .build());
                }

                if (rng.nextInt(100) < 4) {
                    attendanceRepository.save(Attendance.builder()
                            .lessonInstance(li)
                            .studentId(sid)
                            .status(generateRealisticAttendanceStatus())
                            .build());
                }
            }
        }
    }

    private int generateRealisticGrade() {
        int chance = rng.nextInt(100);
        if (chance < 40) return 5;
        if (chance < 75) return 4;
        if (chance < 95) return 3;
        return 2;
    }

    private AttendanceStatus generateRealisticAttendanceStatus() {
        int chance = rng.nextInt(100);
        if (chance < 50) return AttendanceStatus.ABSENT;
        if (chance < 80) return AttendanceStatus.LATE;
        return AttendanceStatus.EXCUSED;
    }

    private record Slot(int number, String room, TeachingAssignment ta) {
    }

    private List<ScheduleLesson> schedule(DayOfWeek day, List<Slot> slots) {
        List<ScheduleLesson> res = new ArrayList<>();
        for (Slot s : slots) {
            res.add(scheduleLessonRepository.save(ScheduleLesson.builder()
                    .dayOfWeek(day)
                    .lessonNumber(s.number())
                    .classRoom(s.room())
                    .teachingAssignment(s.ta())
                    .build()));
        }
        return res;
    }

    private TeachingAssignment saveTA(Long tid, SchoolClass sc, Subject sub) {
        return teachingAssignmentRepository.save(TeachingAssignment.builder()
                .teacherId(tid)
                .schoolClass(sc)
                .subject(sub)
                .build());
    }
}
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

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DebugDataInitializer implements CommandLineRunner {

    private final SubjectRepository            subjectRepository;
    private final SchoolClassRepository        schoolClassRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final ScheduleLessonRepository     scheduleLessonRepository;
    private final LessonInstanceRepository     lessonInstanceRepository;
    private final GradeRepository              gradeRepository;
    private final AttendanceRepository         attendanceRepository;

    private static final List<Long> ALL_STUDENTS = List.of(1L, 2L, 3L, 4L, 5L);

    private final Random rng = new Random(42);

    @Override
    public void run(String... args) {
        log.info("=== Инициализация: Школа №47 ===");

        // ── 1. Предметы ───────────────────────────────────────────────────────
        Map<String, Subject> S = new LinkedHashMap<>();
        for (String name : List.of(
                "Алгебра", "Геометрия", "Русский язык", "Литература",
                "Физика", "Химия", "Биология", "История",
                "Английский язык", "Информатика", "Физкультура", "Черчение")) {
            S.put(name, subjectRepository.save(Subject.builder().name(name).build()));
        }
        log.info("Предметы: {}", S.size());

        // ── 2. Классы ─────────────────────────────────────────────────────────
        SchoolClass c8a  = saveClass("8А",  "2024", 2L);
        c8a.getStudentIds().addAll(List.of(4L, 5L));
        SchoolClass c8b  = saveClass("8Б",  "2024", 3L);
        SchoolClass c9a  = saveClass("9А",  "2024", 4L);
        saveClass("9Б",  "2024", 4L);
        saveClass("10А", "2024", 5L);
        saveClass("11А", "2024", 6L);

        // ── 3. Назначения учителей ────────────────────────────────────────────
        // teacher_id: 10=математик, 11=словесник, 12=физик, 13=химик,
        //             14=историк, 15=англичанин, 16=информатик, 17=физрук, 18=чертёжник
        TeachingAssignment taAlgebra    = saveTA(10L, c8a, S.get("Алгебра"));
        TeachingAssignment taGeometry   = saveTA(10L, c8a, S.get("Геометрия"));
        TeachingAssignment taRussian    = saveTA(11L, c8a, S.get("Русский язык"));
        TeachingAssignment taLiterature = saveTA(11L, c8a, S.get("Литература"));
        TeachingAssignment taPhysics    = saveTA(12L, c8a, S.get("Физика"));
        TeachingAssignment taChemistry  = saveTA(13L, c8a, S.get("Химия"));
        TeachingAssignment taHistory    = saveTA(14L, c8a, S.get("История"));
        TeachingAssignment taEnglish    = saveTA(15L, c8a, S.get("Английский язык"));
        TeachingAssignment taIT         = saveTA(16L, c8a, S.get("Информатика"));
        TeachingAssignment taPE         = saveTA(17L, c8a, S.get("Физкультура"));
        TeachingAssignment taDrawing    = saveTA(18L, c8a, S.get("Черчение"));

        // 8Б и 9А — только назначения (без расписания)
        saveTA(10L, c8b, S.get("Алгебра"));    saveTA(11L, c8b, S.get("Русский язык"));
        saveTA(12L, c8b, S.get("Физика"));     saveTA(13L, c8b, S.get("Химия"));
        saveTA(14L, c8b, S.get("История"));    saveTA(15L, c8b, S.get("Английский язык"));
        saveTA(10L, c9a, S.get("Алгебра"));    saveTA(11L, c9a, S.get("Русский язык"));
        saveTA(12L, c9a, S.get("Физика"));     saveTA(13L, c9a, S.get("Биология"));
        saveTA(14L, c9a, S.get("История"));    saveTA(15L, c9a, S.get("Английский язык"));

        // ── 4. Расписание 8А — разное число уроков по дням ───────────────────
        //
        //  ПН — 6 уроков: насыщенный день (русский, лит, алгебра, физика, история, англ)
        //  ВТ — 5 уроков: (алгебра, физика, химия, русский, инф)
        //  СР — 4 урока:  короткий день (англ, геометрия, химия, физкультура)
        //  ЧТ — 6 уроков: (алгебра, русский, история, инф, англ, физика)
        //  ПТ — 3 урока:  самый короткий (черчение, физкультура, геометрия)

        List<ScheduleLesson> mon = schedule(DayOfWeek.MONDAY, List.of(
                new Slot(1, "201",      taRussian),
                new Slot(2, "201",      taLiterature),
                new Slot(3, "305",      taAlgebra),
                new Slot(4, "401",      taPhysics),
                new Slot(5, "112",      taHistory),
                new Slot(6, "203",      taEnglish)
        ));
        List<ScheduleLesson> tue = schedule(DayOfWeek.TUESDAY, List.of(
                new Slot(1, "305",      taAlgebra),
                new Slot(2, "401",      taPhysics),
                new Slot(3, "402",      taChemistry),
                new Slot(4, "201",      taRussian),
                new Slot(5, "116",      taIT)
        ));
        List<ScheduleLesson> wed = schedule(DayOfWeek.WEDNESDAY, List.of(
                new Slot(1, "203",       taEnglish),
                new Slot(2, "305",       taGeometry),
                new Slot(3, "402",       taChemistry),
                new Slot(4, "спортзал",  taPE)
        ));
        List<ScheduleLesson> thu = schedule(DayOfWeek.THURSDAY, List.of(
                new Slot(1, "305",      taAlgebra),
                new Slot(2, "201",      taRussian),
                new Slot(3, "112",      taHistory),
                new Slot(4, "116",      taIT),
                new Slot(5, "203",      taEnglish),
                new Slot(6, "401",      taPhysics)
        ));
        List<ScheduleLesson> fri = schedule(DayOfWeek.FRIDAY, List.of(
                new Slot(1, "313",       taDrawing),
                new Slot(2, "спортзал",  taPE),
                new Slot(3, "305",       taGeometry)
        ));

        log.info("Расписание 8А: ПН={}, ВТ={}, СР={}, ЧТ={}, ПТ={} уроков",
                mon.size(), tue.size(), wed.size(), thu.size(), fri.size());

        // ── 6. Четыре недели: уроки + оценки + посещаемость ──────────────────
        LocalDate firstMonday = LocalDate.of(2025, 3, 3);
        for (int week = 0; week < 4; week++) {
            LocalDate weekStart = firstMonday.plusWeeks(week);
            buildWeek(weekStart, mon, tue, wed, thu, fri);
        }

        log.info("=== Инициализация завершена ===");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  НЕДЕЛЯ
    // ══════════════════════════════════════════════════════════════════════════

    private void buildWeek(LocalDate monday,
                           List<ScheduleLesson> mon,
                           List<ScheduleLesson> tue,
                           List<ScheduleLesson> wed,
                           List<ScheduleLesson> thu,
                           List<ScheduleLesson> fri) {
        buildDay(monday,             mon);
        buildDay(monday.plusDays(1), tue);
        buildDay(monday.plusDays(2), wed);
        buildDay(monday.plusDays(3), thu);
        buildDay(monday.plusDays(4), fri);
        log.info("  Неделя {} заполнена", monday);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ДЕНЬ
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Создаёт LessonInstance для каждого слота и для каждого ученика
     * случайно решает: поставить оценку? отметить нарушение?
     */
    private void buildDay(LocalDate date, List<ScheduleLesson> slots) {
        for (ScheduleLesson slot : slots) {
            LessonInstance li = lessonInstanceRepository.save(
                    LessonInstance.builder()
                            .date(date)
                            .scheduleLesson(slot)
                            .build()
            );
            for (Long studentId : ALL_STUDENTS) {
                maybeGrade(studentId, li);
                maybeAttendance(studentId, li);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ПРОФИЛИ УЧЕНИКОВ
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Вероятность получить оценку на уроке ~25–35%.
     * Диапазон оценок зависит от профиля.
     */
    private void maybeGrade(Long studentId, LessonInstance li) {
        int chance;
        int minGrade, maxGrade;

        switch (studentId.intValue()) {
            case 1 -> { chance = 35; minGrade = 4; maxGrade = 5; } // отличник
            case 2 -> { chance = 30; minGrade = 3; maxGrade = 5; } // хорошист
            case 3 -> { chance = 28; minGrade = 2; maxGrade = 4; } // троечник
            case 4 -> { chance = 30; minGrade = 3; maxGrade = 5; } // середнячок А
            default -> { chance = 28; minGrade = 2; maxGrade = 4; } // середнячок Б
        }

        if (rng.nextInt(100) >= chance) return;

        int value    = minGrade + rng.nextInt(maxGrade - minGrade + 1);
        GradeType type = randomGradeType();

        gradeRepository.save(Grade.builder()
                .lessonInstance(li)
                .studentId(studentId)
                .value(value)
                .type(type.name())
                .build());
    }

    /**
     * Посещаемость записывается ТОЛЬКО при нарушении (норма — ученик присутствует).
     * Вероятность нарушения и его тип зависят от профиля.
     */
    private void maybeAttendance(Long studentId, LessonInstance li) {
        int chance;
        // веса для: ABSENT / LATE / EXCUSED
        int wAbsent, wLate, wExcused;

        switch (studentId.intValue()) {
            case 1 -> { chance = 3;  wAbsent = 1; wLate = 3; wExcused = 6; } // почти никогда
            case 2 -> { chance = 8;  wAbsent = 2; wLate = 5; wExcused = 3; } // чаще опаздывает
            case 3 -> { chance = 22; wAbsent = 7; wLate = 2; wExcused = 1; } // часто прогуливает
            case 4 -> { chance = 7;  wAbsent = 2; wLate = 2; wExcused = 6; } // уважительные причины
            default -> { chance = 12; wAbsent = 4; wLate = 4; wExcused = 2; } // прогулы и опоздания
        }

        if (rng.nextInt(100) >= chance) return;

        AttendanceStatus status = weightedPick(
                List.of(AttendanceStatus.ABSENT, AttendanceStatus.LATE, AttendanceStatus.EXCUSED),
                new int[]{wAbsent, wLate, wExcused}
        );

        attendanceRepository.save(Attendance.builder()
                .lessonInstance(li)
                .studentId(studentId)
                .status(status)
                .build());
    }

    /** HOMEWORK — 50%, TEST — 30%, CONTROL — 20% */
    private GradeType randomGradeType() {
        int r = rng.nextInt(10);
        if (r < 5) return GradeType.HOMEWORK;
        if (r < 8) return GradeType.TEST;
        return GradeType.CONTROL;
    }

    /** Взвешенный выбор из списка по массиву весов. */
    private <T> T weightedPick(List<T> options, int[] weights) {
        int total = Arrays.stream(weights).sum();
        int r = rng.nextInt(total);
        int cumulative = 0;
        for (int i = 0; i < options.size(); i++) {
            cumulative += weights[i];
            if (r < cumulative) return options.get(i);
        }
        return options.get(options.size() - 1);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ И ТИПЫ
    // ══════════════════════════════════════════════════════════════════════════

    /** DTO для описания слота расписания. */
    private record Slot(int number, String room, TeachingAssignment ta) {}

    private List<ScheduleLesson> schedule(DayOfWeek day, List<Slot> slots) {
        List<ScheduleLesson> result = new ArrayList<>();
        for (Slot s : slots) {
            result.add(scheduleLessonRepository.save(
                    ScheduleLesson.builder()
                            .dayOfWeek(day)
                            .lessonNumber(s.number())
                            .classRoom(s.room())
                            .teachingAssignment(s.ta())
                            .build()
            ));
        }
        return result;
    }

    private SchoolClass saveClass(String name, String year, Long classTeacherId) {
        return schoolClassRepository.save(SchoolClass.builder()
                .name(name).year(year).classTeacherId(classTeacherId).build());
    }

    private TeachingAssignment saveTA(Long teacherId, SchoolClass sc, Subject subject) {
        return teachingAssignmentRepository.save(TeachingAssignment.builder()
                .teacherId(teacherId).schoolClass(sc).subject(subject).build());
    }

}
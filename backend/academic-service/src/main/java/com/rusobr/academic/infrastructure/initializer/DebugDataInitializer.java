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

    // Первый понедельник 4-й четверти
    private static final LocalDate ANCHOR_DATE = LocalDate.of(2026, 4, 6);
    // Генерируем 6 недель данных
    private static final int WEEKS = 6;

    private final SubjectRepository              subjectRepository;
    private final SchoolClassRepository          schoolClassRepository;
    private final TeachingAssignmentRepository   teachingAssignmentRepository;
    private final ScheduleLessonRepository       scheduleLessonRepository;
    private final LessonInstanceRepository       lessonInstanceRepository;
    private final GradeRepository                gradeRepository;
    private final AttendanceRepository           attendanceRepository;
    private final AcademicPeriodRepository       academicPeriodRepository;
    private final HomeworkRepository             homeworkRepository;

    private final Random rng = new Random(42);

    // ── Вспомогательные структуры ─────────────────────────────────────────────

    /** ID учителей из user-service (порядок соответствует порядку createUser в DebugDataInitializer) */
    private enum Teachers {
        MATH1(17), MATH2(18), GEOM(19),
        PHYS1(20), PHYS2(21),
        IT1(22),   IT2(23),
        RUS1(24),  RUS2(25), LIT(26),
        HIST1(27), HIST2(28), SOC(29),
        ENG1(30),  ENG2(31), DE(32),
        CHEM(33),  BIO(34),  GEO(35),
        PE1(36),   PE2(37),  ART(38),
        MUSIC(39), TECH(40);

        final long id;
        Teachers(long id) { this.id = id; }
    }

    private record Slot(int number, String room, TeachingAssignment ta) {}

    // ── Entry point ───────────────────────────────────────────────────────────

    @Override
    public void run(String... args) {
        if (schoolClassRepository.count() > 0) {
            log.info("Данные уже инициализированы. Пропуск.");
            return;
        }

        log.info("=== Полная инициализация академических данных (8А, 8Б, 9А) ===");

        // ── Предметы ──────────────────────────────────────────────────────────
        Map<String, Subject> S = new LinkedHashMap<>();
        List.of(
                "Алгебра", "Геометрия", "Русский язык", "Литература",
                "Физика", "Химия", "Биология", "История", "Обществознание",
                "Информатика", "Английский язык", "Немецкий язык", "География",
                "Физкультура", "Изо", "Музыка", "Технология"
        ).forEach(n -> S.put(n, subjectRepository.save(Subject.builder().name(n).build())));

        // ── Классы + учебные назначения ───────────────────────────────────────
        ClassBundle c8a  = buildClass("8А",  "2025", 17L, range(1L,  28L),  S);
        ClassBundle c8b  = buildClass("8Б",  "2025", 26L, range(29L, 55L),  S);
        ClassBundle c9a  = buildClass("9А",  "2025", 27L, range(56L, 81L),  S);

        // ── Расписание 8А ─────────────────────────────────────────────────────
        Map<DayOfWeek, List<ScheduleLesson>> sched8a = buildSchedule8A(c8a.ta());
        // ── Расписание 8Б ─────────────────────────────────────────────────────
        Map<DayOfWeek, List<ScheduleLesson>> sched8b = buildSchedule8B(c8b.ta());
        // ── Расписание 9А ─────────────────────────────────────────────────────
        Map<DayOfWeek, List<ScheduleLesson>> sched9a = buildSchedule9A(c9a.ta());

        // ── Генерация занятий и оценок ────────────────────────────────────────
        for (int week = 0; week < WEEKS; week++) {
            LocalDate monday = ANCHOR_DATE.plusWeeks(week);
            generateWeek(monday, sched8a, c8a.schoolClass());
            generateWeek(monday, sched8b, c8b.schoolClass());
            generateWeek(monday, sched9a, c9a.schoolClass());
        }

        savePeriods();

        log.info("=== Инициализация завершена успешно ===");
    }

    // ── Классы ────────────────────────────────────────────────────────────────

    private record ClassBundle(SchoolClass schoolClass, Map<String, TeachingAssignment> ta) {}

    private ClassBundle buildClass(String name, String year, Long classTeacherId,
                                   List<Long> studentIds, Map<String, Subject> S) {
        SchoolClass sc = SchoolClass.builder()
                .name(name).year(year)
                .classTeacherId(classTeacherId)
                .students(new HashSet<>()).build();
        studentIds.forEach(sid ->
                sc.getStudents().add(ClassStudent.builder().schoolClass(sc).studentId(sid).build()));
        schoolClassRepository.save(sc);

        Map<String, TeachingAssignment> ta = new HashMap<>();

        // Общие назначения для всех трёх классов (разные учителя у разных классов)
        boolean is9 = name.startsWith("9");
        ta.put("Алг",   saveTA(Teachers.MATH1.id, sc, S.get("Алгебра")));
        ta.put("Гео",   saveTA(Teachers.GEOM.id,  sc, S.get("Геометрия")));
        ta.put("Рус",   saveTA(is9 ? Teachers.RUS2.id : Teachers.RUS1.id, sc, S.get("Русский язык")));
        ta.put("Лит",   saveTA(Teachers.LIT.id,   sc, S.get("Литература")));
        ta.put("Физ",   saveTA(is9 ? Teachers.PHYS2.id : Teachers.PHYS1.id, sc, S.get("Физика")));
        ta.put("Хим",   saveTA(Teachers.CHEM.id,  sc, S.get("Химия")));
        ta.put("Био",   saveTA(Teachers.BIO.id,   sc, S.get("Биология")));
        ta.put("Ист",   saveTA(Teachers.HIST1.id, sc, S.get("История")));
        ta.put("Общ",   saveTA(Teachers.SOC.id,   sc, S.get("Обществознание")));
        ta.put("Инф",   saveTA(is9 ? Teachers.IT2.id : Teachers.IT1.id, sc, S.get("Информатика")));
        ta.put("Анг",   saveTA(Teachers.ENG1.id,  sc, S.get("Английский язык")));
        ta.put("Нем",   saveTA(Teachers.DE.id,    sc, S.get("Немецкий язык")));
        ta.put("ГеоГр", saveTA(Teachers.GEO.id,   sc, S.get("География")));
        ta.put("Спорт", saveTA(is9 ? Teachers.PE2.id : Teachers.PE1.id, sc, S.get("Физкультура")));
        ta.put("Изо",   saveTA(Teachers.ART.id,   sc, S.get("Изо")));
        ta.put("Муз",   saveTA(Teachers.MUSIC.id, sc, S.get("Музыка")));
        ta.put("Тех",   saveTA(Teachers.TECH.id,  sc, S.get("Технология")));

        return new ClassBundle(sc, ta);
    }

    // ── Расписания ────────────────────────────────────────────────────────────

    private Map<DayOfWeek, List<ScheduleLesson>> buildSchedule8A(Map<String, TeachingAssignment> ta) {
        Map<DayOfWeek, List<ScheduleLesson>> map = new LinkedHashMap<>();
        map.put(DayOfWeek.MONDAY, scheduleDay(DayOfWeek.MONDAY, List.of(
                new Slot(1, "201", ta.get("Рус")),   new Slot(2, "201", ta.get("Лит")),
                new Slot(3, "305", ta.get("Алг")),   new Slot(4, "305", ta.get("Гео")),
                new Slot(5, "Зал", ta.get("Спорт")), new Slot(6, "114", ta.get("Муз"))
        )));
        map.put(DayOfWeek.TUESDAY, scheduleDay(DayOfWeek.TUESDAY, List.of(
                new Slot(1, "402", ta.get("Физ")),   new Slot(2, "402", ta.get("Хим")),
                new Slot(3, "104", ta.get("Анг")),   new Slot(4, "205", ta.get("Ист")),
                new Slot(5, "301", ta.get("Био")),   new Slot(6, "206", ta.get("Общ"))
        )));
        map.put(DayOfWeek.WEDNESDAY, scheduleDay(DayOfWeek.WEDNESDAY, List.of(
                new Slot(1, "305", ta.get("Алг")),   new Slot(2, "305", ta.get("Алг")),
                new Slot(3, "201", ta.get("Рус")),   new Slot(4, "ПК-1", ta.get("Инф")),
                new Slot(5, "ПК-1", ta.get("Инф")), new Slot(6, "113", ta.get("ГеоГр"))
        )));
        map.put(DayOfWeek.THURSDAY, scheduleDay(DayOfWeek.THURSDAY, List.of(
                new Slot(1, "104", ta.get("Анг")),   new Slot(2, "402", ta.get("Физ")),
                new Slot(3, "205", ta.get("Ист")),   new Slot(4, "305", ta.get("Гео")),
                new Slot(5, "201", ta.get("Лит")),   new Slot(6, "Нем", ta.get("Нем"))
        )));
        map.put(DayOfWeek.FRIDAY, scheduleDay(DayOfWeek.FRIDAY, List.of(
                new Slot(1, "301", ta.get("Био")),   new Slot(2, "402", ta.get("Хим")),
                new Slot(3, "305", ta.get("Алг")),   new Slot(4, "201", ta.get("Рус")),
                new Slot(5, "Зал", ta.get("Спорт")), new Slot(6, "Мас", ta.get("Тех"))
        )));
        return map;
    }

    private Map<DayOfWeek, List<ScheduleLesson>> buildSchedule8B(Map<String, TeachingAssignment> ta) {
        Map<DayOfWeek, List<ScheduleLesson>> map = new LinkedHashMap<>();
        map.put(DayOfWeek.MONDAY, scheduleDay(DayOfWeek.MONDAY, List.of(
                new Slot(1, "305", ta.get("Алг")),   new Slot(2, "305", ta.get("Гео")),
                new Slot(3, "402", ta.get("Физ")),   new Slot(4, "202", ta.get("Рус")),
                new Slot(5, "202", ta.get("Лит")),   new Slot(6, "Зал", ta.get("Спорт"))
        )));
        map.put(DayOfWeek.TUESDAY, scheduleDay(DayOfWeek.TUESDAY, List.of(
                new Slot(1, "205", ta.get("Ист")),   new Slot(2, "206", ta.get("Общ")),
                new Slot(3, "104", ta.get("Анг")),   new Slot(4, "ПК-2", ta.get("Инф")),
                new Slot(5, "ПК-2", ta.get("Инф")), new Slot(6, "113", ta.get("ГеоГр"))
        )));
        map.put(DayOfWeek.WEDNESDAY, scheduleDay(DayOfWeek.WEDNESDAY, List.of(
                new Slot(1, "402", ta.get("Хим")),   new Slot(2, "301", ta.get("Био")),
                new Slot(3, "305", ta.get("Алг")),   new Slot(4, "104", ta.get("Анг")),
                new Slot(5, "Нем", ta.get("Нем")),   new Slot(6, "114", ta.get("Муз"))
        )));
        map.put(DayOfWeek.THURSDAY, scheduleDay(DayOfWeek.THURSDAY, List.of(
                new Slot(1, "202", ta.get("Рус")),   new Slot(2, "305", ta.get("Гео")),
                new Slot(3, "402", ta.get("Физ")),   new Slot(4, "402", ta.get("Хим")),
                new Slot(5, "301", ta.get("Био")),   new Slot(6, "Мас", ta.get("Тех"))
        )));
        map.put(DayOfWeek.FRIDAY, scheduleDay(DayOfWeek.FRIDAY, List.of(
                new Slot(1, "305", ta.get("Алг")),   new Slot(2, "205", ta.get("Ист")),
                new Slot(3, "202", ta.get("Лит")),   new Slot(4, "Зал", ta.get("Спорт")),
                new Slot(5, "202", ta.get("Рус")),   new Slot(6, "Изо", ta.get("Изо"))
        )));
        return map;
    }

    private Map<DayOfWeek, List<ScheduleLesson>> buildSchedule9A(Map<String, TeachingAssignment> ta) {
        Map<DayOfWeek, List<ScheduleLesson>> map = new LinkedHashMap<>();
        map.put(DayOfWeek.MONDAY, scheduleDay(DayOfWeek.MONDAY, List.of(
                new Slot(1, "402", ta.get("Физ")),   new Slot(2, "402", ta.get("Физ")),
                new Slot(3, "305", ta.get("Алг")),   new Slot(4, "203", ta.get("Рус")),
                new Slot(5, "203", ta.get("Лит")),   new Slot(6, "Зал", ta.get("Спорт"))
        )));
        map.put(DayOfWeek.TUESDAY, scheduleDay(DayOfWeek.TUESDAY, List.of(
                new Slot(1, "ПК-1", ta.get("Инф")), new Slot(2, "ПК-1", ta.get("Инф")),
                new Slot(3, "402", ta.get("Хим")),   new Slot(4, "301", ta.get("Био")),
                new Slot(5, "205", ta.get("Ист")),   new Slot(6, "206", ta.get("Общ"))
        )));
        map.put(DayOfWeek.WEDNESDAY, scheduleDay(DayOfWeek.WEDNESDAY, List.of(
                new Slot(1, "305", ta.get("Алг")),   new Slot(2, "305", ta.get("Гео")),
                new Slot(3, "104", ta.get("Анг")),   new Slot(4, "104", ta.get("Нем")),
                new Slot(5, "113", ta.get("ГеоГр")), new Slot(6, "Зал", ta.get("Спорт"))
        )));
        map.put(DayOfWeek.THURSDAY, scheduleDay(DayOfWeek.THURSDAY, List.of(
                new Slot(1, "203", ta.get("Рус")),   new Slot(2, "305", ta.get("Алг")),
                new Slot(3, "402", ta.get("Физ")),   new Slot(4, "205", ta.get("Ист")),
                new Slot(5, "305", ta.get("Гео")),   new Slot(6, "Мас", ta.get("Тех"))
        )));
        map.put(DayOfWeek.FRIDAY, scheduleDay(DayOfWeek.FRIDAY, List.of(
                new Slot(1, "402", ta.get("Хим")),   new Slot(2, "301", ta.get("Био")),
                new Slot(3, "203", ta.get("Лит")),   new Slot(4, "104", ta.get("Анг")),
                new Slot(5, "203", ta.get("Рус")),   new Slot(6, "Изо", ta.get("Изо"))
        )));
        return map;
    }

    // ── Генерация занятий ─────────────────────────────────────────────────────

    private void generateWeek(LocalDate monday,
                              Map<DayOfWeek, List<ScheduleLesson>> schedule,
                              SchoolClass schoolClass) {
        schedule.forEach((day, lessons) -> {
            LocalDate date = monday.plusDays(day.getValue() - 1);
            buildDay(date, lessons, schoolClass);
        });
    }

    @Transactional
    void buildDay(LocalDate date, List<ScheduleLesson> slots, SchoolClass schoolClass) {
        for (ScheduleLesson slot : slots) {
            LessonInstance li = lessonInstanceRepository.save(
                    LessonInstance.builder().lessonDate(date).scheduleLesson(slot).build());

            homeworkRepository.save(Homework.builder()
                    .text(randomHomework(slot.getTeachingAssignment().getSubject().getName()))
                    .lessonInstance(li).build());

            for (ClassStudent cs : schoolClass.getStudents()) {
                Long sid = cs.getStudentId();
                // ~35% шанс оценки
                if (rng.nextInt(100) < 35) {
                    gradeRepository.save(Grade.builder()
                            .lessonInstance(li).studentId(sid)
                            .value(generateRealisticGrade()).weight(1)
                            .type(GradeType.HOMEWORK).build());
                }
                // ~6% шанс пропуска / опоздания
                if (rng.nextInt(100) < 6) {
                    AttendanceStatus status = switch (rng.nextInt(3)) {
                        case 0 -> AttendanceStatus.ABSENT;
                        case 1 -> AttendanceStatus.LATE;
                        default -> AttendanceStatus.EXCUSED;
                    };
                    attendanceRepository.save(Attendance.builder()
                            .lessonInstance(li).studentId(sid).status(status).build());
                }
            }
        }
    }

    // ── Периоды ───────────────────────────────────────────────────────────────

    private void savePeriods() {
        academicPeriodRepository.saveAll(List.of(
                AcademicPeriod.builder().name("Первая четверть").schoolYear("2025-2026")
                        .startDate(LocalDate.of(2025, 9, 1)).endDate(LocalDate.of(2025, 10, 26))
                        .isClosed(true).build(),
                AcademicPeriod.builder().name("Вторая четверть").schoolYear("2025-2026")
                        .startDate(LocalDate.of(2025, 11, 5)).endDate(LocalDate.of(2025, 12, 28))
                        .isClosed(true).build(),
                AcademicPeriod.builder().name("Третья четверть").schoolYear("2025-2026")
                        .startDate(LocalDate.of(2026, 1, 9)).endDate(LocalDate.of(2026, 3, 31))
                        .isClosed(true).build(),
                AcademicPeriod.builder().name("Четвертая четверть").schoolYear("2025-2026")
                        .startDate(LocalDate.of(2026, 4, 6)).endDate(LocalDate.of(2026, 5, 25))
                        .isClosed(false).build()
        ));
    }

    // ── Утилиты ───────────────────────────────────────────────────────────────

    private TeachingAssignment saveTA(long tid, SchoolClass sc, Subject sub) {
        return teachingAssignmentRepository.save(
                TeachingAssignment.builder().teacherId(tid).schoolClass(sc).subject(sub).build());
    }

    private List<ScheduleLesson> scheduleDay(DayOfWeek day, List<Slot> slots) {
        return slots.stream().map(s -> scheduleLessonRepository.save(
                ScheduleLesson.builder()
                        .dayOfWeek(day).lessonNumber(s.number())
                        .classRoom(s.room()).teachingAssignment(s.ta())
                        .validFrom(ANCHOR_DATE.minusMonths(1)).build()
        )).toList();
    }

    private int generateRealisticGrade() {
        int r = rng.nextInt(100);
        if (r < 38) return 5;
        if (r < 75) return 4;
        if (r < 93) return 3;
        return 2;
    }

    private String randomHomework(String subject) {
        List<String> tasks = switch (subject) {
            case "Алгебра"       -> List.of("§12, задачи 1–8", "Контрольные вопросы к §14", "Решить уравнения из упр. 234");
            case "Геометрия"     -> List.of("Доказать теорему §8", "Упр. 115–120, чертежи", "Построить треугольник по условию");
            case "Физика"        -> List.of("§18 читать, вопросы 1–5", "Решить задачи 14.3–14.7", "Написать конспект §19");
            case "Химия"         -> List.of("Выучить валентности элементов", "§21 читать, задачи 3–6", "Составить уравнения реакций");
            case "Биология"      -> List.of("§11 читать, заполнить таблицу", "Нарисовать схему клетки", "Выучить термины §12");
            case "История"       -> List.of("§15 читать, вопросы на с. 98", "Составить хронологию событий", "Написать эссе по теме");
            case "Русский язык"  -> List.of("Упр. 341, диктант", "Правила §18 выучить наизусть", "Сочинение-миниатюра");
            case "Литература"    -> List.of("Прочитать гл. 5–7", "Ответить на вопросы с. 78", "Выучить наизусть стихотворение");
            case "Английский язык" -> List.of("WB p.45 ex.3–5", "Vocabulary unit 7", "Прочитать текст, ответить на вопросы");
            case "Информатика"   -> List.of("Задание в Moodle (тема 9)", "Написать программу сортировки", "Презентация по теме ИИ");
            default              -> List.of("Изучить тему и выполнить упражнения в тетради");
        };
        return tasks.get(rng.nextInt(tasks.size()));
    }

    private static List<Long> range(long from, long to) {
        return java.util.stream.LongStream.rangeClosed(from, to).boxed().toList();
    }
}
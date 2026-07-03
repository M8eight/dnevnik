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

    private static final LocalDate ANCHOR = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    private static final int PAST_WEEKS = 4;

    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final ScheduleLessonRepository scheduleLessonRepository;
    private final LessonInstanceRepository lessonInstanceRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final HomeworkRepository homeworkRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final FinalGradeRepository finalGradeRepository;

    private final Random rng = new Random(42);
    private final PeriodGradeRepository periodGradeRepository;
    private final AcademicYearRepository academicYearRepository;

    private enum Teachers {
        MATH1(17), MATH2(4), GEOM(5),
        PHYS1(6), PHYS2(7),
        IT1(8), IT2(9),
        RUS1(10), RUS2(11), LIT(12),
        HIST1(13), HIST2(14), SOC(15),
        ENG1(16), ENG2(3), DE(18),
        CHEM(19), BIO(20), GEO(21),
        PE1(22), PE2(23), ART(24),
        MUSIC(25), TECH(26);

        final long id;

        Teachers(long id) {
            this.id = id;
        }
    }

    private record Slot(int number, String room, TeachingAssignment ta) {
    }

    @Override
    public void run(String... args) {
        if (schoolClassRepository.count() > 0) {
            log.info("Данные уже инициализированы. Пропуск.");
            return;
        }

        LocalDate from = ANCHOR.minusWeeks(PAST_WEEKS);
        log.info("=== Инициализация: {} недель начиная с {} ===", PAST_WEEKS, from);

        Map<String, Subject> S = new LinkedHashMap<>();
        List.of(
                "Алгебра", "Геометрия", "Русский язык", "Литература",
                "Физика", "Химия", "Биология", "История", "Обществознание",
                "Информатика", "Английский язык", "Немецкий язык", "География",
                "Физкультура", "Изо", "Музыка", "Технология"
        ).forEach(n -> S.put(n, subjectRepository.save(Subject.builder().name(n).build())));

        AcademicYear academicYear = academicYearRepository.save(AcademicYear.builder()
                .name("2025-2026")
                .description("Год")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 6, 26))
                .build());

        ClassBundle c8a = buildClass("8А", 24L, range(27L, 54L), S, academicYear);
        ClassBundle c8b = buildClass("8Б", 26L, range(55L, 81L), S, academicYear);
        ClassBundle c9a = buildClass("9А", 25L, range(82L, 107L), S, academicYear);

        Map<DayOfWeek, List<ScheduleLesson>> sched8a = buildSchedule8A(c8a.ta());
        Map<DayOfWeek, List<ScheduleLesson>> sched8b = buildSchedule8B(c8b.ta());
        Map<DayOfWeek, List<ScheduleLesson>> sched9a = buildSchedule9A(c9a.ta());

        // Только прошлые недели включая текущую (anchor)
        for (int week = 0; week <= PAST_WEEKS; week++) {
            LocalDate monday = from.plusWeeks(week);
            generateWeek(monday, sched8a, c8a.schoolClass());
            generateWeek(monday, sched8b, c8b.schoolClass());
            generateWeek(monday, sched9a, c9a.schoolClass());
        }

        savePeriods();

        saveTeacherSubjects(S);

        savePeriodGrades(c8a, c8b, c9a);

        saveFinalGrades(c8a, c8b, c9a);

        log.info("=== Инициализация завершена ===");
    }

    private record ClassBundle(SchoolClass schoolClass, Map<String, TeachingAssignment> ta) {
    }

    private ClassBundle buildClass(String name, Long classTeacherId,
                                   List<Long> studentIds, Map<String, Subject> S, AcademicYear academicYear) {
        SchoolClass sc = SchoolClass.builder()
                .name(name).academicYear(academicYear)
                .classTeacherId(classTeacherId)
                .students(new HashSet<>()).build();
        schoolClassRepository.save(sc);

        studentIds.forEach(sid ->
                sc.getStudents().add(
                        ClassStudent.builder().schoolClass(sc).studentId(sid).build()
                ));
        schoolClassRepository.save(sc);

        Map<String, TeachingAssignment> ta = new HashMap<>();
        boolean is9 = name.startsWith("9");
        ta.put("Алг", saveTA(Teachers.MATH1.id, sc, S.get("Алгебра")));
        ta.put("Гео", saveTA(Teachers.GEOM.id, sc, S.get("Геометрия")));
        ta.put("Рус", saveTA(is9 ? Teachers.RUS2.id : Teachers.RUS1.id, sc, S.get("Русский язык")));
        ta.put("Лит", saveTA(Teachers.LIT.id, sc, S.get("Литература")));
        ta.put("Физ", saveTA(is9 ? Teachers.PHYS2.id : Teachers.PHYS1.id, sc, S.get("Физика")));
        ta.put("Хим", saveTA(Teachers.CHEM.id, sc, S.get("Химия")));
        ta.put("Био", saveTA(Teachers.BIO.id, sc, S.get("Биология")));
        ta.put("Ист", saveTA(Teachers.HIST1.id, sc, S.get("История")));
        ta.put("Общ", saveTA(Teachers.SOC.id, sc, S.get("Обществознание")));
        ta.put("Инф", saveTA(is9 ? Teachers.IT2.id : Teachers.IT1.id, sc, S.get("Информатика")));
        ta.put("Анг", saveTA(Teachers.ENG1.id, sc, S.get("Английский язык")));
        ta.put("Нем", saveTA(Teachers.DE.id, sc, S.get("Немецкий язык")));
        ta.put("ГеоГр", saveTA(Teachers.GEO.id, sc, S.get("География")));
        ta.put("Спорт", saveTA(is9 ? Teachers.PE2.id : Teachers.PE1.id, sc, S.get("Физкультура")));
        ta.put("Изо", saveTA(Teachers.ART.id, sc, S.get("Изо")));
        ta.put("Муз", saveTA(Teachers.MUSIC.id, sc, S.get("Музыка")));
        ta.put("Тех", saveTA(Teachers.TECH.id, sc, S.get("Технология")));

        return new ClassBundle(sc, ta);
    }

    private Map<DayOfWeek, List<ScheduleLesson>> buildSchedule8A(Map<String, TeachingAssignment> ta) {
        Map<DayOfWeek, List<ScheduleLesson>> map = new LinkedHashMap<>();
        map.put(DayOfWeek.MONDAY, scheduleDay(DayOfWeek.MONDAY, List.of(
                new Slot(1, "201", ta.get("Рус")), new Slot(2, "201", ta.get("Лит")),
                new Slot(3, "305", ta.get("Алг")), new Slot(4, "305", ta.get("Гео")),
                new Slot(5, "Зал", ta.get("Спорт")), new Slot(6, "114", ta.get("Муз"))
        )));
        map.put(DayOfWeek.TUESDAY, scheduleDay(DayOfWeek.TUESDAY, List.of(
                new Slot(1, "402", ta.get("Физ")), new Slot(2, "402", ta.get("Хим")),
                new Slot(3, "104", ta.get("Анг")), new Slot(4, "205", ta.get("Ист")),
                new Slot(5, "301", ta.get("Био")), new Slot(6, "206", ta.get("Общ"))
        )));
        map.put(DayOfWeek.WEDNESDAY, scheduleDay(DayOfWeek.WEDNESDAY, List.of(
                new Slot(1, "305", ta.get("Алг")), new Slot(2, "305", ta.get("Алг")),
                new Slot(3, "201", ta.get("Рус")), new Slot(4, "ПК-1", ta.get("Инф")),
                new Slot(5, "ПК-1", ta.get("Инф")), new Slot(6, "113", ta.get("ГеоГр"))
        )));
        map.put(DayOfWeek.THURSDAY, scheduleDay(DayOfWeek.THURSDAY, List.of(
                new Slot(1, "104", ta.get("Анг")), new Slot(2, "402", ta.get("Физ")),
                new Slot(3, "205", ta.get("Ист")), new Slot(4, "305", ta.get("Гео")),
                new Slot(5, "201", ta.get("Лит")), new Slot(6, "Нем", ta.get("Нем"))
        )));
        map.put(DayOfWeek.FRIDAY, scheduleDay(DayOfWeek.FRIDAY, List.of(
                new Slot(1, "301", ta.get("Био")), new Slot(2, "402", ta.get("Хим")),
                new Slot(3, "305", ta.get("Алг")), new Slot(4, "201", ta.get("Рус")),
                new Slot(5, "Зал", ta.get("Спорт")), new Slot(6, "Мас", ta.get("Тех"))
        )));
        return map;
    }

    private Map<DayOfWeek, List<ScheduleLesson>> buildSchedule8B(Map<String, TeachingAssignment> ta) {
        Map<DayOfWeek, List<ScheduleLesson>> map = new LinkedHashMap<>();
        map.put(DayOfWeek.MONDAY, scheduleDay(DayOfWeek.MONDAY, List.of(
                new Slot(1, "305", ta.get("Алг")), new Slot(2, "305", ta.get("Гео")),
                new Slot(3, "402", ta.get("Физ")), new Slot(4, "202", ta.get("Рус")),
                new Slot(5, "202", ta.get("Лит")), new Slot(6, "Зал", ta.get("Спорт"))
        )));
        map.put(DayOfWeek.TUESDAY, scheduleDay(DayOfWeek.TUESDAY, List.of(
                new Slot(1, "205", ta.get("Ист")), new Slot(2, "206", ta.get("Общ")),
                new Slot(3, "104", ta.get("Анг")), new Slot(4, "ПК-2", ta.get("Инф")),
                new Slot(5, "ПК-2", ta.get("Инф")), new Slot(6, "113", ta.get("ГеоГр"))
        )));
        map.put(DayOfWeek.WEDNESDAY, scheduleDay(DayOfWeek.WEDNESDAY, List.of(
                new Slot(1, "402", ta.get("Хим")), new Slot(2, "301", ta.get("Био")),
                new Slot(3, "305", ta.get("Алг")), new Slot(4, "104", ta.get("Анг")),
                new Slot(5, "Нем", ta.get("Нем")), new Slot(6, "114", ta.get("Муз"))
        )));
        map.put(DayOfWeek.THURSDAY, scheduleDay(DayOfWeek.THURSDAY, List.of(
                new Slot(1, "202", ta.get("Рус")), new Slot(2, "305", ta.get("Гео")),
                new Slot(3, "402", ta.get("Физ")), new Slot(4, "402", ta.get("Хим")),
                new Slot(5, "301", ta.get("Био")), new Slot(6, "Мас", ta.get("Тех"))
        )));
        map.put(DayOfWeek.FRIDAY, scheduleDay(DayOfWeek.FRIDAY, List.of(
                new Slot(1, "305", ta.get("Алг")), new Slot(2, "205", ta.get("Ист")),
                new Slot(3, "202", ta.get("Лит")), new Slot(4, "Зал", ta.get("Спорт")),
                new Slot(5, "202", ta.get("Рус")), new Slot(6, "Изо", ta.get("Изо"))
        )));
        return map;
    }

    private Map<DayOfWeek, List<ScheduleLesson>> buildSchedule9A(Map<String, TeachingAssignment> ta) {
        Map<DayOfWeek, List<ScheduleLesson>> map = new LinkedHashMap<>();
        map.put(DayOfWeek.MONDAY, scheduleDay(DayOfWeek.MONDAY, List.of(
                new Slot(1, "402", ta.get("Физ")), new Slot(2, "402", ta.get("Физ")),
                new Slot(3, "305", ta.get("Алг")), new Slot(4, "203", ta.get("Рус")),
                new Slot(5, "203", ta.get("Лит")), new Slot(6, "Зал", ta.get("Спорт"))
        )));
        map.put(DayOfWeek.TUESDAY, scheduleDay(DayOfWeek.TUESDAY, List.of(
                new Slot(1, "ПК-1", ta.get("Инф")), new Slot(2, "ПК-1", ta.get("Инф")),
                new Slot(3, "402", ta.get("Хим")), new Slot(4, "301", ta.get("Био")),
                new Slot(5, "205", ta.get("Ист")), new Slot(6, "206", ta.get("Общ"))
        )));
        map.put(DayOfWeek.WEDNESDAY, scheduleDay(DayOfWeek.WEDNESDAY, List.of(
                new Slot(1, "305", ta.get("Алг")), new Slot(2, "305", ta.get("Гео")),
                new Slot(3, "104", ta.get("Анг")), new Slot(4, "104", ta.get("Нем")),
                new Slot(5, "113", ta.get("ГеоГр")), new Slot(6, "Зал", ta.get("Спорт"))
        )));
        map.put(DayOfWeek.THURSDAY, scheduleDay(DayOfWeek.THURSDAY, List.of(
                new Slot(1, "203", ta.get("Рус")), new Slot(2, "305", ta.get("Алг")),
                new Slot(3, "402", ta.get("Физ")), new Slot(4, "205", ta.get("Ист")),
                new Slot(5, "305", ta.get("Гео")), new Slot(6, "Мас", ta.get("Тех"))
        )));
        map.put(DayOfWeek.FRIDAY, scheduleDay(DayOfWeek.FRIDAY, List.of(
                new Slot(1, "402", ta.get("Хим")), new Slot(2, "301", ta.get("Био")),
                new Slot(3, "203", ta.get("Лит")), new Slot(4, "104", ta.get("Анг")),
                new Slot(5, "203", ta.get("Рус")), new Slot(6, "Изо", ta.get("Изо"))
        )));
        return map;
    }

    private void generateWeek(LocalDate monday,
                              Map<DayOfWeek, List<ScheduleLesson>> schedule,
                              SchoolClass schoolClass) {
        schedule.forEach((day, lessons) -> {
            LocalDate date = monday.with(TemporalAdjusters.nextOrSame(day));
            if (!date.isAfter(LocalDate.now())) {
                buildDay(date, lessons, schoolClass);
            }
        });
    }

    @Transactional
    void buildDay(LocalDate date, List<ScheduleLesson> slots, SchoolClass schoolClass) {
        for (ScheduleLesson slot : slots) {
            LessonInstance li = lessonInstanceRepository.save(
                    LessonInstance.builder().lessonDate(date).scheduleLesson(slot).build());

            if (rng.nextInt(100) < 40) {
                homeworkRepository.save(Homework.builder()
                        .text(randomHomework(slot.getTeachingAssignment().getSubject().getName()))
                        .lessonInstance(li).build());
            }

            for (ClassStudent cs : schoolClass.getStudents()) {
                Long sid = cs.getStudentId();

                if (rng.nextInt(100) < 25) {
                    GradeType type = switch (rng.nextInt(10)) {
                        case 0, 1 -> GradeType.TEST;       // 20% — контрольная
                        case 2 -> GradeType.CONTROL;    // 10% — проверочная
                        default -> GradeType.HOMEWORK;   // 70% — за ДЗ
                    };
                    int weight = type == GradeType.TEST ? 3
                            : type == GradeType.CONTROL ? 2
                              : 1;
                    gradeRepository.save(Grade.builder()
                            .lessonInstance(li).studentId(sid)
                            .value(generateRealisticGrade())
                            .weight(weight)
                            .type(type).build());
                }

                if (rng.nextInt(100) < 8) {
                    AttendanceStatus status = switch (rng.nextInt(10)) {
                        case 0, 1, 2 -> AttendanceStatus.EXCUSED; // 30% уважительная
                        case 3 -> AttendanceStatus.LATE;    // 10% опоздал
                        default -> AttendanceStatus.ABSENT;  // 60% просто прогул
                    };
                    attendanceRepository.save(Attendance.builder()
                            .lessonInstance(li).studentId(sid).status(status).build());
                }
            }
        }
    }

    private void savePeriods() {
        AcademicYear academicYear = academicYearRepository.getReferenceById(1L);

        academicPeriodRepository.saveAll(List.of(
                AcademicPeriod.builder().name("Первая четверть").academicYear(academicYear)
                        .startDate(LocalDate.of(2025, 9, 1)).endDate(LocalDate.of(2025, 10, 26))
                        .closed(true).build(),
                AcademicPeriod.builder().name("Вторая четверть").academicYear(academicYear)
                        .startDate(LocalDate.of(2025, 11, 5)).endDate(LocalDate.of(2025, 12, 28))
                        .closed(true).build(),
                AcademicPeriod.builder().name("Третья четверть").academicYear(academicYear)
                        .startDate(LocalDate.of(2026, 1, 9)).endDate(LocalDate.of(2026, 3, 31))
                        .closed(true).build(),
                AcademicPeriod.builder().name("Четвертая четверть").academicYear(academicYear)
                        .startDate(LocalDate.of(2026, 4, 6)).endDate(LocalDate.of(2026, 6, 25))
                        .closed(false).build()
        ));
    }

    private void saveTeacherSubjects(Map<String, Subject> S) {
        Map<Long, Set<String>> teacherToSubjects = new LinkedHashMap<>();

        // Математика / геометрия
        addTS(teacherToSubjects, Teachers.MATH1.id, "Алгебра");
        addTS(teacherToSubjects, Teachers.MATH2.id, "Алгебра");
        addTS(teacherToSubjects, Teachers.GEOM.id, "Геометрия");

        // Физика
        addTS(teacherToSubjects, Teachers.PHYS1.id, "Физика");
        addTS(teacherToSubjects, Teachers.PHYS2.id, "Физика");

        // Информатика
        addTS(teacherToSubjects, Teachers.IT1.id, "Информатика");
        addTS(teacherToSubjects, Teachers.IT2.id, "Информатика");

        // Русский / литература
        addTS(teacherToSubjects, Teachers.RUS1.id, "Русский язык");
        addTS(teacherToSubjects, Teachers.RUS2.id, "Русский язык");
        addTS(teacherToSubjects, Teachers.LIT.id, "Литература");

        // История / обществознание
        addTS(teacherToSubjects, Teachers.HIST1.id, "История");
        addTS(teacherToSubjects, Teachers.HIST2.id, "История");
        addTS(teacherToSubjects, Teachers.SOC.id, "Обществознание");

        // Иностранные языки
        addTS(teacherToSubjects, Teachers.ENG1.id, "Английский язык");
        addTS(teacherToSubjects, Teachers.ENG2.id, "Английский язык");
        addTS(teacherToSubjects, Teachers.DE.id, "Немецкий язык");

        // Естественные науки
        addTS(teacherToSubjects, Teachers.CHEM.id, "Химия");
        addTS(teacherToSubjects, Teachers.BIO.id, "Биология");
        addTS(teacherToSubjects, Teachers.GEO.id, "География");

        // Физкультура
        addTS(teacherToSubjects, Teachers.PE1.id, "Физкультура");
        addTS(teacherToSubjects, Teachers.PE2.id, "Физкультура");

        // Творческие
        addTS(teacherToSubjects, Teachers.ART.id, "Изо");
        addTS(teacherToSubjects, Teachers.MUSIC.id, "Музыка");
        addTS(teacherToSubjects, Teachers.TECH.id, "Технология");

        teacherToSubjects.forEach((teacherId, subjects) ->
                subjects.forEach(subjectName -> {
                    Subject subject = S.get(subjectName);
                    if (subject == null) return;
                    TeacherSubjectId tsId = TeacherSubjectId.builder()
                            .teacherId(teacherId)
                            .subjectId(subject.getId())
                            .build();
                    teacherSubjectRepository.save(
                            TeacherSubject.builder()
                                    .id(tsId)
                                    .subject(subject)
                                    .build()
                    );
                })
        );

        log.info("Создано teacher_subject связок: {}",
                teacherToSubjects.values().stream().mapToInt(Set::size).sum());
    }

    private void addTS(Map<Long, Set<String>> map, long teacherId, String subject) {
        map.computeIfAbsent(teacherId, k -> new LinkedHashSet<>()).add(subject);
    }

    private TeachingAssignment saveTA(long tid, SchoolClass sc, Subject sub) {
        return teachingAssignmentRepository.save(
                TeachingAssignment.builder().teacherId(tid).schoolClass(sc).subject(sub).build());
    }

    private List<ScheduleLesson> scheduleDay(DayOfWeek day, List<Slot> slots) {
        return slots.stream().map(s -> scheduleLessonRepository.save(
                ScheduleLesson.builder()
                        .dayOfWeek(day).lessonNumber(s.number())
                        .classRoom(s.room()).teachingAssignment(s.ta())
                        .validFrom(ANCHOR.minusWeeks(PAST_WEEKS))
                        .build()
        )).toList();
    }

    private int generateRealisticGrade() {
        int r = rng.nextInt(100);
        if (r < 38) return 5;
        if (r < 75) return 4;
        if (r < 93) return 3;
        return 2;
    }

    private void savePeriodGrades(ClassBundle... bundles) {
        List<AcademicPeriod> closedPeriods = academicPeriodRepository.findAll()
                .stream()
                .filter(AcademicPeriod::isClosed)
                .toList();

        List<PeriodGrade> toSave = new ArrayList<>();

        for (ClassBundle bundle : bundles) {
            for (ClassStudent cs : bundle.schoolClass().getStudents()) {
                Long studentId = cs.getStudentId();

                for (TeachingAssignment ta : bundle.ta().values()) {
                    for (AcademicPeriod period : closedPeriods) {
                        int value = generateRealisticGrade();

                        toSave.add(PeriodGrade.builder()
                                .studentId(studentId)
                                .teachingAssignment(ta)
                                .academicPeriod(period)
                                .value(value)
                                .description(generatePeriodGradeDescription(value))
                                .build());
                    }
                }
            }
        }


        periodGradeRepository.saveAll(toSave);
        log.info("Создано period_grades: {}", toSave.size());
    }

    private void saveFinalGrades(ClassBundle... bundles) {
        AcademicYear academicYear = academicYearRepository.getReferenceById(1L);

        List<FinalGrade> toSave = new ArrayList<>();

        for (ClassBundle bundle : bundles) {
            for (ClassStudent cs : bundle.schoolClass().getStudents()) {
                Long studentId = cs.getStudentId();

                for (TeachingAssignment ta : bundle.ta().values()) {
                    int value = generateRealisticGrade();

                    toSave.add(FinalGrade.builder()
                            .studentId(studentId)
                            .teachingAssignment(ta)
                            .academicYear(academicYear)
                            .value(value)
                            .description(generateFinalGradeDescription(value))
                            .build());
                }
            }
        }

        finalGradeRepository.saveAll(toSave);
        log.info("Создано final_grades: {}", toSave.size());
    }

    private String generateFinalGradeDescription(int value) {
        return switch (value) {
            case 5 -> rng.nextBoolean() ? "Отличный результат за год" : null;
            case 4 -> rng.nextBoolean() ? "Хорошая успеваемость за год" : null;
            case 3 -> rng.nextBoolean() ? "Удовлетворительно, рекомендуется подтянуть" : null;
            case 2 -> "Неудовлетворительно, необходима пересдача";
            default -> null;
        };
    }

    private String generatePeriodGradeDescription(int value) {
        return switch (value) {
            case 5 -> rng.nextBoolean() ? "Отличная работа в четверти" : null;
            case 4 -> rng.nextBoolean() ? "Хорошая успеваемость" : null;
            case 3 -> rng.nextBoolean() ? "Удовлетворительно, есть пробелы" : null;
            case 2 -> "Неудовлетворительно, необходима доработка";
            default -> null;
        };
    }

    private String randomHomework(String subject) {
        List<String> tasks = switch (subject) {
            case "Алгебра" -> List.of("§12, задачи 1–8", "Контрольные вопросы к §14", "Решить уравнения из упр. 234");
            case "Геометрия" ->
                    List.of("Доказать теорему §8", "Упр. 115–120, чертежи", "Построить треугольник по условию");
            case "Физика" -> List.of("§18 читать, вопросы 1–5", "Решить задачи 14.3–14.7", "Написать конспект §19");
            case "Химия" ->
                    List.of("Выучить валентности элементов", "§21 читать, задачи 3–6", "Составить уравнения реакций");
            case "Биология" ->
                    List.of("§11 читать, заполнить таблицу", "Нарисовать схему клетки", "Выучить термины §12");
            case "История" ->
                    List.of("§15 читать, вопросы на с. 98", "Составить хронологию событий", "Написать эссе по теме");
            case "Русский язык" -> List.of("Упр. 341, диктант", "Правила §18 выучить наизусть", "Сочинение-миниатюра");
            case "Литература" ->
                    List.of("Прочитать гл. 5–7", "Ответить на вопросы с. 78", "Выучить наизусть стихотворение");
            case "Английский язык" ->
                    List.of("WB p.45 ex.3–5", "Vocabulary unit 7", "Прочитать текст, ответить на вопросы");
            case "Информатика" ->
                    List.of("Задание в Moodle (тема 9)", "Написать программу сортировки", "Презентация по теме ИИ");
            default -> List.of("Изучить тему и выполнить упражнения в тетради");
        };
        return tasks.get(rng.nextInt(tasks.size()));
    }

    private static List<Long> range(long from, long to) {
        return java.util.stream.LongStream.rangeClosed(from, to).boxed().toList();
    }
}
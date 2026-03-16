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
@Deprecated
public class DebugDataInitializer implements CommandLineRunner {

    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final ScheduleLessonRepository scheduleLessonRepository;
    private final LessonInstanceRepository lessonInstanceRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;

    private static final List<Long> ALL_STUDENTS = List.of(1L, 2L, 3L, 4L, 5L);
    private final Random rng = new Random(42);

    @Override
    public void run(String... args) {
        log.info("=== Инициализация Академических Данных ===");

        // 1. Предметы
        Map<String, Subject> S = new LinkedHashMap<>();
        for (String name : List.of("Алгебра", "Геометрия", "Русский язык", "Физика", "Химия", "История", "Информатика", "Физкультура")) {
            S.put(name, subjectRepository.save(Subject.builder().name(name).build()));
        }

        // 2. Класс 8А
        SchoolClass c8a = saveClass("8А", "2024", 10L);
        c8a.getStudentIds().addAll(ALL_STUDENTS);
        schoolClassRepository.save(c8a);

        // 3. Назначения учителей (ID 10-17)
        TeachingAssignment taAlg = saveTA(10L, c8a, S.get("Алгебра"));
        TeachingAssignment taGeo = saveTA(10L, c8a, S.get("Геометрия"));
        TeachingAssignment taRus = saveTA(11L, c8a, S.get("Русский язык"));
        TeachingAssignment taPhy = saveTA(12L, c8a, S.get("Физика"));
        TeachingAssignment taChe = saveTA(13L, c8a, S.get("Химия"));
        TeachingAssignment taHis = saveTA(15L, c8a, S.get("История"));
        TeachingAssignment taIT  = saveTA(16L, c8a, S.get("Информатика"));
        TeachingAssignment taPE  = saveTA(17L, c8a, S.get("Физкультура"));

        // 4. Расписание
        List<ScheduleLesson> mon = schedule(DayOfWeek.MONDAY, List.of(new Slot(1, "201", taRus), new Slot(2, "305", taAlg)));
        List<ScheduleLesson> tue = schedule(DayOfWeek.TUESDAY, List.of(new Slot(1, "401", taPhy), new Slot(2, "116", taIT)));
        List<ScheduleLesson> wed = schedule(DayOfWeek.WEDNESDAY, List.of(new Slot(1, "402", taChe), new Slot(2, "Спортзал", taPE)));

        // 5. Данные за 2 недели
        LocalDate day = LocalDate.of(2025, 3, 3);
        for (int i = 0; i < 2; i++) {
            buildDay(day.plusWeeks(i), mon);
            buildDay(day.plusWeeks(i).plusDays(1), tue);
            buildDay(day.plusWeeks(i).plusDays(2), wed);
        }

        log.info("=== Академические данные готовы ===");
    }

    private void buildDay(LocalDate date, List<ScheduleLesson> slots) {
        for (ScheduleLesson slot : slots) {
            LessonInstance li = lessonInstanceRepository.save(LessonInstance.builder().date(date).scheduleLesson(slot).build());
            for (Long sid : ALL_STUDENTS) {
                if (rng.nextInt(100) < 40) {
                    gradeRepository.save(Grade.builder().lessonInstance(li).studentId(sid).value(3 + rng.nextInt(3)).type(GradeType.HOMEWORK.name()).build());
                }
                if (rng.nextInt(100) < 5) {
                    attendanceRepository.save(Attendance.builder().lessonInstance(li).studentId(sid).status(AttendanceStatus.ABSENT).build());
                }
            }
        }
    }

    private record Slot(int number, String room, TeachingAssignment ta) {}

    private List<ScheduleLesson> schedule(DayOfWeek day, List<Slot> slots) {
        List<ScheduleLesson> res = new ArrayList<>();
        for (Slot s : slots) {
            res.add(scheduleLessonRepository.save(ScheduleLesson.builder().dayOfWeek(day).lessonNumber(s.number()).classRoom(s.room()).teachingAssignment(s.ta()).build()));
        }
        return res;
    }

    private SchoolClass saveClass(String name, String year, Long teacherId) {
        // ИСПРАВЛЕНО: используем HashSet вместо ArrayList для соответствия типу Set<Long>
        return schoolClassRepository.save(SchoolClass.builder()
                .name(name)
                .year(year)
                .classTeacherId(teacherId)
                .studentIds(new HashSet<>())
                .build());
    }

    private TeachingAssignment saveTA(Long tid, SchoolClass sc, Subject sub) {
        return teachingAssignmentRepository.save(TeachingAssignment.builder().teacherId(tid).schoolClass(sc).subject(sub).build());
    }
}
package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.projection.ScheduleLessonProjection;
import com.rusobr.academic.infrastructure.persistence.projection.SchoolLessonProjection;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ScheduleLessonRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    ScheduleLessonRepository scheduleLessonRepository;

    private static final long STUDENT_ID = 11L;
    private static final long TEACHER_ID = 7L;
    private static final LocalDate NOW = LocalDate.of(2025, 9, 15);

    private TeachingAssignment assignment;
    private ScheduleLesson mondayLesson;
    private ScheduleLesson tuesdayLesson;
    private ScheduleLesson expiredLesson;

    private void setUpGraph() {
        var year = persist(TestData.academicYear("2025-2026"));
        SchoolClass schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
        persist(TestData.classStudent(schoolClass, STUDENT_ID));
        Subject subject = persist(TestData.subject("Математика"));
        Subject biology = persist(TestData.subject("Биология"));
        assignment = persist(TestData.assignment(TEACHER_ID, schoolClass, subject));
        TeachingAssignment otherAssignment = persist(TestData.assignment(TEACHER_ID, schoolClass, biology));

        mondayLesson = persist(ScheduleLesson.builder()
                .teachingAssignment(assignment)
                .dayOfWeek(DayOfWeek.MONDAY)
                .lessonNumber(1)
                .classRoom("101")
                .validFrom(TestData.SEPTEMBER)
                .build());
        tuesdayLesson = persist(ScheduleLesson.builder()
                .teachingAssignment(assignment)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .lessonNumber(2)
                .classRoom("102")
                .validFrom(TestData.SEPTEMBER)
                .build());
        expiredLesson = persist(ScheduleLesson.builder()
                .teachingAssignment(otherAssignment)
                .dayOfWeek(DayOfWeek.MONDAY)
                .lessonNumber(3)
                .classRoom("103")
                .validFrom(TestData.SEPTEMBER)
                .validTo(LocalDate.of(2025, 9, 10))
                .build());
    }

    @Nested
    @DisplayName("getScheduleByDate")
    class GetScheduleByDate {

        @Test
        @DisplayName("возвращает активные уроки расписания на день")
        void success() {
            setUpGraph();

            List<ScheduleLessonProjection> schedule = scheduleLessonRepository
                    .getScheduleByDate(STUDENT_ID, DayOfWeek.MONDAY, NOW);

            assertThat(schedule).hasSize(1);
            assertThat(schedule.get(0).getLessonNumber()).isEqualTo(1);
            assertThat(schedule.get(0).getSubjectName()).isEqualTo("Математика");
            assertThat(schedule.get(0).getClassRoom()).isEqualTo("101");
        }

        @Test
        @DisplayName("исключает устаревшие уроки и уроки других дней")
        void excludesExpiredAndOtherDays() {
            setUpGraph();

            List<ScheduleLessonProjection> schedule = scheduleLessonRepository
                    .getScheduleByDate(STUDENT_ID, DayOfWeek.TUESDAY, NOW);

            assertThat(schedule).hasSize(1);
            assertThat(schedule.get(0).getLessonNumber()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findAllByStudentId")
    class FindAllByStudentId {

        @Test
        @DisplayName("возвращает все уроки класса ученика")
        void success() {
            setUpGraph();

            List<SchoolLessonProjection> lessons = scheduleLessonRepository.findAllByStudentId(STUDENT_ID);

            assertThat(lessons).hasSize(3);
            assertThat(lessons).extracting(SchoolLessonProjection::getLessonNumber)
                    .containsExactlyInAnyOrder(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("findByTeachingAssignmentId")
    class FindByTeachingAssignmentId {

        @Test
        @DisplayName("фильтрует уроки по назначению")
        void success() {
            setUpGraph();

            List<ScheduleLesson> lessons = scheduleLessonRepository.findByTeachingAssignmentId(assignment.getId());

            assertThat(lessons).extracting(ScheduleLesson::getId)
                    .containsExactlyInAnyOrder(mondayLesson.getId(), tuesdayLesson.getId());
        }
    }

    @Nested
    @DisplayName("findClassSchedule")
    class FindClassSchedule {

        @Test
        @DisplayName("возвращает активные отсортированные уроки класса")
        void success() {
            setUpGraph();

            List<ScheduleLesson> lessons = scheduleLessonRepository.findClassSchedule(assignment.getSchoolClass().getId(), NOW);

            assertThat(lessons).extracting(ScheduleLesson::getLessonNumber).containsExactly(1, 2);
            assertThat(lessons).allSatisfy(sl -> assertThat(sl.getTeachingAssignment()).isNotNull());
        }
    }

    @Nested
    @DisplayName("findWithTeachingAssignmentById")
    class FindWithTeachingAssignmentById {

        @Test
        @DisplayName("загружает назначение урока")
        void success() {
            setUpGraph();
            clearAndFlush();

            Optional<ScheduleLesson> found = scheduleLessonRepository.findWithTeachingAssignmentById(mondayLesson.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getTeachingAssignment().getId()).isEqualTo(assignment.getId());
        }
    }

    @Nested
    @DisplayName("existsActiveByClassSlot")
    class ExistsActiveByClassSlot {

        @Test
        @DisplayName("проверяет занятость слота класса")
        void success() {
            setUpGraph();

            long classId = assignment.getSchoolClass().getId();
            assertThat(scheduleLessonRepository
                    .existsActiveByClassSlot(classId, DayOfWeek.MONDAY, 1, NOW)).isTrue();
            assertThat(scheduleLessonRepository
                    .existsActiveByClassSlot(classId, DayOfWeek.MONDAY, 5, NOW)).isFalse();
        }
    }

    @Nested
    @DisplayName("existsActiveByTeachingAssignmentSlot")
    class ExistsActiveByTeachingAssignmentSlot {

        @Test
        @DisplayName("проверяет занятость слота назначения")
        void success() {
            setUpGraph();

            assertThat(scheduleLessonRepository
                    .existsActiveByTeachingAssignmentSlot(assignment.getId(), DayOfWeek.MONDAY, 1, NOW)).isTrue();
            assertThat(scheduleLessonRepository
                    .existsActiveByTeachingAssignmentSlot(assignment.getId(), DayOfWeek.FRIDAY, 1, NOW)).isFalse();
        }
    }

    @Nested
    @DisplayName("findAllByClassIdAndPeriod")
    class FindAllByClassIdAndPeriod {

        @Test
        @DisplayName("возвращает уроки класса, пересекающие период")
        void success() {
            setUpGraph();

            List<ScheduleLesson> lessons = scheduleLessonRepository
                    .findAllByClassIdAndPeriod(assignment.getSchoolClass().getId(),
                            LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 30));

            assertThat(lessons).hasSize(3);
        }
    }

    @Nested
    @DisplayName("existsByTeacherSlot")
    class ExistsByTeacherSlot {

        @Test
        @DisplayName("проверяет занятость учителя в слоте")
        void success() {
            setUpGraph();

            assertThat(scheduleLessonRepository
                    .existsByTeacherSlot(TEACHER_ID, DayOfWeek.MONDAY, 1, NOW)).isTrue();
            assertThat(scheduleLessonRepository
                    .existsByTeacherSlot(TEACHER_ID, DayOfWeek.MONDAY, 5, NOW)).isFalse();
        }
    }

    @Nested
    @DisplayName("findDiaryScheduleByStudentId")
    class FindDiaryScheduleByStudentId {

        @Test
        @DisplayName("возвращает уроки ученика, пересекающие период")
        void success() {
            setUpGraph();

            List<ScheduleLesson> lessons = scheduleLessonRepository
                    .findDiaryScheduleByStudentId(STUDENT_ID,
                            LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 30));

            assertThat(lessons).extracting(ScheduleLesson::getLessonNumber).containsExactly(1, 2, 3);
            assertThat(lessons).allSatisfy(sl -> assertThat(sl.getTeachingAssignment()).isNotNull());
        }
    }

    @Nested
    @DisplayName("findTeacherScheduleByDate")
    class FindTeacherScheduleByDate {

        @Test
        @DisplayName("возвращает экземпляры уроков учителя на дату")
        void success() {
            setUpGraph();
            LessonInstance mondayInstance = persist(TestData.lessonInstance(mondayLesson, NOW));

            List<LessonInstance> instances = scheduleLessonRepository.findTeacherScheduleByDate(TEACHER_ID, NOW);

            assertThat(instances).extracting(LessonInstance::getId)
                    .containsExactly(mondayInstance.getId());
        }
    }

    @Nested
    @DisplayName("findTeacherScheduleByPeriod")
    class FindTeacherScheduleByPeriod {

        @Test
        @DisplayName("возвращает экземпляры уроков учителя за период")
        void success() {
            setUpGraph();
            LessonInstance mondayInstance = persist(TestData.lessonInstance(mondayLesson, NOW));
            persist(TestData.lessonInstance(tuesdayLesson, NOW.plusDays(1)));

            List<LessonInstance> instances = scheduleLessonRepository
                    .findTeacherScheduleByPeriod(TEACHER_ID, NOW, NOW.plusDays(7));

            assertThat(instances).hasSize(2);
            assertThat(instances).extracting(LessonInstance::getId)
                    .contains(mondayInstance.getId());
        }
    }

}

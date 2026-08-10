package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.projection.AttendanceStudentProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeJournalProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeStudentProjection;
import com.rusobr.academic.infrastructure.persistence.projection.LessonInstanceProjection;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LessonInstanceRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    LessonInstanceRepository lessonInstanceRepository;

    private static final long STUDENT_1 = 11L;
    private static final long STUDENT_2 = 12L;
    private static final long TEACHER_ID = 7L;

    private static final LocalDate D1 = LocalDate.of(2025, 9, 15);
    private static final LocalDate D2 = LocalDate.of(2025, 9, 16);
    private static final LocalDate D3 = LocalDate.of(2025, 9, 22);

    private TeachingAssignment assignment;
    private ScheduleLesson mondayLesson;
    private ScheduleLesson tuesdayLesson;
    private LessonInstance mondayInstance;
    private LessonInstance tuesdayInstance;

    private void setUpGraph() {
        var year = persist(TestData.academicYear("2025-2026"));
        SchoolClass schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
        persist(TestData.classStudent(schoolClass, STUDENT_1));
        persist(TestData.classStudent(schoolClass, STUDENT_2));
        Subject subject = persist(TestData.subject("Математика"));
        assignment = persist(TestData.assignment(TEACHER_ID, schoolClass, subject));

        mondayLesson = persist(TestData.scheduleLesson(assignment, DayOfWeek.MONDAY, 1));
        tuesdayLesson = persist(TestData.scheduleLesson(assignment, DayOfWeek.TUESDAY, 2));

        mondayInstance = persist(TestData.lessonInstance(mondayLesson, D1));
        tuesdayInstance = persist(TestData.lessonInstance(tuesdayLesson, D2));
        persist(TestData.lessonInstance(mondayLesson, D3));

        persist(TestData.grade(STUDENT_1, mondayInstance, 4, 2, GradeType.CONTROL));
        persist(TestData.attendance(STUDENT_1, mondayInstance, AttendanceStatus.ABSENT));
        persist(TestData.homework(mondayInstance, "Параграф 5"));
    }

    @Nested
    @DisplayName("findDiaryLessonsByStudentIdAndDateRange")
    class FindDiaryLessonsByStudentIdAndDateRange {

        @Test
        @DisplayName("возвращает уроки ученика за диапазон дат с оценками")
        void success() {
            setUpGraph();
            clearAndFlush();

            List<LessonInstance> lessons = lessonInstanceRepository
                    .findDiaryLessonsByStudentIdAndDateRange(STUDENT_1, D1, D3);

            assertThat(lessons).extracting(LessonInstance::getLessonDate)
                    .containsExactly(D1, D2, D3);
            LessonInstance first = lessons.stream()
                    .filter(li -> li.getLessonDate().equals(D1))
                    .findFirst().orElseThrow();
            assertThat(first.getGrades())
                    .extracting(Grade::getValue)
                    .containsExactly(4);
        }
    }

    @Nested
    @DisplayName("findGradesLessonsByStudentId")
    class FindGradesLessonsByStudentId {

        @Test
        @DisplayName("возвращает журнал оценок ученика")
        void success() {
            setUpGraph();
            persist(TestData.grade(STUDENT_1, tuesdayInstance, 5, 1, GradeType.TEST));

            List<GradeJournalProjection> journal = lessonInstanceRepository
                    .findGradesLessonsByStudentId(STUDENT_1, D1, D2);

            assertThat(journal)
                    .extracting(GradeJournalProjection::getLessonDate)
                    .containsExactly(D1, D2);
            assertThat(journal)
                    .extracting(GradeJournalProjection::getSubjectName)
                    .containsOnly("Математика");
            assertThat(journal)
                    .extracting(GradeJournalProjection::getValue)
                    .containsExactly(4, 5);
        }
    }

    @Nested
    @DisplayName("findLessonDatesByStudentId")
    class FindLessonDatesByStudentId {

        @Test
        @DisplayName("возвращает уникальные даты уроков ученика")
        void success() {
            setUpGraph();

            List<LocalDate> dates = lessonInstanceRepository
                    .findLessonDatesByStudentId(STUDENT_1, D1, D3);

            assertThat(dates).containsExactly(D1, D2, D3);
        }
    }

    @Nested
    @DisplayName("findLessonInstanceByTeachingAssignmentId")
    class FindLessonInstanceByTeachingAssignmentId {

        @Test
        @DisplayName("возвращает проекции уроков назначения")
        void success() {
            setUpGraph();

            List<LessonInstanceProjection> lessons = lessonInstanceRepository
                    .findLessonInstanceByTeachingAssignmentId(assignment.getId(), D1, D3);

            assertThat(lessons).hasSize(3);
            assertThat(lessons)
                    .extracting(LessonInstanceProjection::getLessonDate)
                    .containsExactly(D1, D2, D3);
        }
    }

    @Nested
    @DisplayName("findGradesByTeachingAssignment")
    class FindGradesByTeachingAssignment {

        @Test
        @DisplayName("возвращает оценки по назначению")
        void success() {
            setUpGraph();

            List<GradeStudentProjection> grades = lessonInstanceRepository
                    .findGradesByTeachingAssignment(assignment.getId(), D1, D3);

            assertThat(grades).hasSize(1);
            assertThat(grades.get(0).getStudentId()).isEqualTo(STUDENT_1);
            assertThat(grades.get(0).getValue()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("findAttendancesByTeachingAssignment")
    class FindAttendancesByTeachingAssignment {

        @Test
        @DisplayName("возвращает посещаемость по назначению")
        void success() {
            setUpGraph();

            List<AttendanceStudentProjection> attendances = lessonInstanceRepository
                    .findAttendancesByTeachingAssignment(assignment.getId(), D1, D3);

            assertThat(attendances).hasSize(1);
            assertThat(attendances.get(0).getStudentId()).isEqualTo(STUDENT_1);
            assertThat(attendances.get(0).getStatus()).isEqualTo(AttendanceStatus.ABSENT);
        }
    }

    @Nested
    @DisplayName("softDeleteFutureEmptyAfterDate")
    class SoftDeleteFutureEmptyAfterDate {

        @Test
        @DisplayName("удаляет только пустые будущие уроки")
        void success() {
            setUpGraph();
            LessonInstance futureEmpty = persist(TestData.lessonInstance(mondayLesson, D3.plusWeeks(1)));
            LessonInstance futureWithGrade = persist(TestData.lessonInstance(mondayLesson, D3.plusWeeks(2)));
            persist(TestData.grade(STUDENT_1, futureWithGrade, 5, 1, GradeType.TEST));

            lessonInstanceRepository.softDeleteFutureEmptyAfterDate(mondayLesson.getId(), D3);
            clearAndFlush();

            assertThat(lessonInstanceRepository.findById(futureEmpty.getId())).isNotPresent();
            assertThat(lessonInstanceRepository.findById(futureWithGrade.getId())).isPresent();
        }
    }

    @Nested
    @DisplayName("existsByScheduleLessonAndLessonDate")
    class ExistsByScheduleLessonAndLessonDate {

        @Test
        @DisplayName("проверяет уникальность урока на дату")
        void success() {
            setUpGraph();

            assertThat(lessonInstanceRepository
                    .existsByScheduleLessonAndLessonDate(mondayLesson, D1)).isTrue();
            assertThat(lessonInstanceRepository
                    .existsByScheduleLessonAndLessonDate(mondayLesson, LocalDate.of(2025, 9, 30))).isFalse();
        }
    }

    @Nested
    @DisplayName("findLessonInstancesByScheduleId")
    class FindLessonInstancesByScheduleId {

        @Test
        @DisplayName("возвращает уроки по идентификаторам расписаний")
        void success() {
            setUpGraph();

            List<LessonInstance> lessons = lessonInstanceRepository
                    .findLessonInstancesByScheduleId(List.of(mondayLesson.getId(), tuesdayLesson.getId()), D1, D3);

            assertThat(lessons).hasSize(3);
        }
    }

    @Nested
    @DisplayName("findLessonInstanceGradesByPeriodAndStudent")
    class FindLessonInstanceGradesByPeriodAndStudent {

        @Test
        @DisplayName("возвращает уроки с оценками только для указанного ученика")
        void success() {
            setUpGraph();
            persist(TestData.grade(STUDENT_2, tuesdayInstance, 3, 1, GradeType.HOMEWORK));

            List<LessonInstance> lessons = lessonInstanceRepository
                    .findLessonInstanceGradesByPeriodAndStudent(
                            List.of(mondayLesson.getId(), tuesdayLesson.getId()), D1, D2, STUDENT_1);

            assertThat(lessons).extracting(LessonInstance::getId)
                    .containsExactly(mondayInstance.getId());
        }
    }

    @Nested
    @DisplayName("findLessonInstanceAttendancesByPeriodAndStudent")
    class FindLessonInstanceAttendancesByPeriodAndStudent {

        @Test
        @DisplayName("возвращает уроки с посещаемостью только для указанного ученика")
        void success() {
            setUpGraph();
            persist(TestData.attendance(STUDENT_2, tuesdayInstance, AttendanceStatus.LATE));

            List<LessonInstance> lessons = lessonInstanceRepository
                    .findLessonInstanceAttendancesByPeriodAndStudent(
                            List.of(mondayLesson.getId(), tuesdayLesson.getId()), D1, D2, STUDENT_1);

            assertThat(lessons).extracting(LessonInstance::getId)
                    .containsExactly(mondayInstance.getId());
        }
    }

    @Nested
    @DisplayName("findLessonInstanceHomeworksByPeriodAndStudent")
    class FindLessonInstanceHomeworksByPeriodAndStudent {

        @Test
        @DisplayName("возвращает уроки с домашними заданиями (left join)")
        void success() {
            setUpGraph();
            clearAndFlush();

            List<LessonInstance> lessons = lessonInstanceRepository
                    .findLessonInstanceHomeworksByPeriodAndStudent(
                            List.of(mondayLesson.getId(), tuesdayLesson.getId()), D1, D2);

            assertThat(lessons).extracting(LessonInstance::getId)
                    .containsExactly(mondayInstance.getId(), tuesdayInstance.getId());
            LessonInstance withHomework = lessons.stream()
                    .filter(li -> li.getId().equals(mondayInstance.getId()))
                    .findFirst().orElseThrow();
            assertThat(withHomework.getHomeworks()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("isOwnedByTeacherAndHasStudent")
    class IsOwnedByTeacherAndHasStudent {

        @Test
        @DisplayName("проверяет принадлежность урока учителю и ученику")
        void success() {
            setUpGraph();

            assertThat(lessonInstanceRepository
                    .isOwnedByTeacherAndHasStudent(TEACHER_ID, mondayInstance.getId(), STUDENT_1)).isTrue();
            assertThat(lessonInstanceRepository
                    .isOwnedByTeacherAndHasStudent(99L, mondayInstance.getId(), STUDENT_1)).isFalse();
            assertThat(lessonInstanceRepository
                    .isOwnedByTeacherAndHasStudent(TEACHER_ID, mondayInstance.getId(), 999L)).isFalse();
        }
    }

    @Nested
    @DisplayName("isOwnedByTeacher")
    class IsOwnedByTeacher {

        @Test
        @DisplayName("проверяет принадлежность урока учителю")
        void success() {
            setUpGraph();

            assertThat(lessonInstanceRepository
                    .isOwnedByTeacher(TEACHER_ID, mondayInstance.getId())).isTrue();
            assertThat(lessonInstanceRepository
                    .isOwnedByTeacher(99L, mondayInstance.getId())).isFalse();
        }
    }

}

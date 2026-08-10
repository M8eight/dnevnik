package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.projection.GradeDetailProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeJournalItemProjection;
import com.rusobr.academic.infrastructure.persistence.projection.GradeWithSubjectNameProjection;
import com.rusobr.academic.infrastructure.persistence.projection.StudentAverageProjection;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class GradeRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    GradeRepository gradeRepository;

    private static final long STUDENT_1 = 11L;
    private static final long STUDENT_2 = 12L;
    private static final long TEACHER_ID = 7L;

    private static final LocalDate D1 = LocalDate.of(2025, 9, 15);
    private static final LocalDate D2 = LocalDate.of(2025, 9, 16);

    private TeachingAssignment assignment;
    private LessonInstance firstInstance;
    private LessonInstance secondInstance;
    private Grade studentOneFirst;
    private Grade studentTwoFirst;

    private void setUpGraph() {
        var year = persist(TestData.academicYear("2025-2026"));
        SchoolClass schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
        persist(TestData.classStudent(schoolClass, STUDENT_1));
        persist(TestData.classStudent(schoolClass, STUDENT_2));
        Subject subject = persist(TestData.subject("Математика"));
        assignment = persist(TestData.assignment(TEACHER_ID, schoolClass, subject));

        ScheduleLesson scheduleLesson = persist(TestData.scheduleLesson(assignment, DayOfWeek.MONDAY, 1));
        firstInstance = persist(TestData.lessonInstance(scheduleLesson, D1));
        secondInstance = persist(TestData.lessonInstance(scheduleLesson, D2));

        studentOneFirst = persist(TestData.grade(STUDENT_1, firstInstance, 4, 2, GradeType.CONTROL));
        persist(TestData.grade(STUDENT_1, secondInstance, 2, 1, GradeType.HOMEWORK));
        studentTwoFirst = persist(TestData.grade(STUDENT_2, firstInstance, 5, 1, GradeType.TEST));
    }

    @Nested
    @DisplayName("getClassGrades")
    class GetClassGrades {

        @Test
        @DisplayName("возвращает оценки назначения, отсортированные по ученику")
        void success() {
            setUpGraph();

            List<GradeJournalItemProjection> grades = gradeRepository.getClassGrades(assignment.getId());

            assertThat(grades).hasSize(3);
            assertThat(grades).extracting(GradeJournalItemProjection::getStudentId)
                    .containsExactly(STUDENT_1, STUDENT_1, STUDENT_2);
            assertThat(grades).extracting(GradeJournalItemProjection::getValue)
                    .containsExactlyInAnyOrder(4, 2, 5);
            assertThat(grades).extracting(GradeJournalItemProjection::getLessonDate)
                    .contains(D1, D2);
        }
    }

    @Nested
    @DisplayName("getAverageGrade")
    class GetAverageGrade {

        @Test
        @DisplayName("вычисляет средневзвешенный балл")
        void success() {
            setUpGraph();

            Double average = gradeRepository.getAverageGrade(STUDENT_1, D1, D2);

            assertThat(average).isCloseTo(3.3333, org.assertj.core.data.Offset.offset(0.001));
        }

        @Test
        @DisplayName("возвращает 0, если оценок в диапазоне нет")
        void returnsZeroWhenNoGradesInRange() {
            setUpGraph();

            Double average = gradeRepository.getAverageGrade(STUDENT_1,
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

            assertThat(average).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("findAllByDateAndStudentId")
    class FindAllByDateAndStudentId {

        @Test
        @DisplayName("возвращает оценки ученика за дату с названием предмета")
        void success() {
            setUpGraph();

            List<GradeWithSubjectNameProjection> grades =
                    gradeRepository.findAllByDateAndStudentId(STUDENT_1, D1);

            assertThat(grades).hasSize(1);
            assertThat(grades.get(0).getSubjectName()).isEqualTo("Математика");
            assertThat(grades.get(0).getValue()).isEqualTo(4);
            assertThat(grades.get(0).getGradeType()).isEqualTo(GradeType.CONTROL);
        }
    }

    @Nested
    @DisplayName("findAverageStudentsByTeachingAssignment")
    class FindAverageStudentsByTeachingAssignment {

        @Test
        @DisplayName("группирует средний балл по ученикам")
        void success() {
            setUpGraph();

            List<StudentAverageProjection> averages = gradeRepository
                    .findAverageStudentsByTeachingAssignment(assignment.getId(), D1, D2);

            assertThat(averages).hasSize(2);
            StudentAverageProjection studentOne = averages.stream()
                    .filter(a -> a.getStudentId().equals(STUDENT_1))
                    .findFirst().orElseThrow();
            StudentAverageProjection studentTwo = averages.stream()
                    .filter(a -> a.getStudentId().equals(STUDENT_2))
                    .findFirst().orElseThrow();
            assertThat(studentOne.getAverage()).isCloseTo(3.33, org.assertj.core.data.Offset.offset(0.01));
            assertThat(studentTwo.getAverage()).isEqualTo(5.0);
        }
    }

    @Nested
    @DisplayName("findDetailById")
    class FindDetailById {

        @Test
        @DisplayName("возвращает детали оценки с учителем")
        void success() {
            setUpGraph();

            Optional<GradeDetailProjection> detail = gradeRepository.findDetailById(studentOneFirst.getId());

            assertThat(detail).isPresent();
            assertThat(detail.get().getValue()).isEqualTo(4);
            assertThat(detail.get().getWeight()).isEqualTo(2);
            assertThat(detail.get().getGradeType()).isEqualTo(GradeType.CONTROL);
            assertThat(detail.get().getTeacherId()).isEqualTo(TEACHER_ID);
        }
    }

    @Nested
    @DisplayName("existsByIdAndStudentId")
    class ExistsByIdAndStudentId {

        @Test
        @DisplayName("проверяет принадлежность оценки ученику")
        void success() {
            setUpGraph();

            assertThat(gradeRepository.existsByIdAndStudentId(studentOneFirst.getId(), STUDENT_1)).isTrue();
            assertThat(gradeRepository.existsByIdAndStudentId(studentOneFirst.getId(), STUDENT_2)).isFalse();
        }
    }

    @Nested
    @DisplayName("isGradeOwnedByTeacher")
    class IsGradeOwnedByTeacher {

        @Test
        @DisplayName("возвращает true для учителя предмета и false для другого")
        void success() {
            setUpGraph();

            assertThat(gradeRepository.isGradeOwnedByTeacher(TEACHER_ID, studentOneFirst.getId())).isTrue();
            assertThat(gradeRepository.isGradeOwnedByTeacher(99L, studentOneFirst.getId())).isFalse();
            assertThat(gradeRepository.isGradeOwnedByTeacher(TEACHER_ID, studentTwoFirst.getId())).isTrue();
        }
    }

    @Nested
    @DisplayName("findWithLessonInstanceById")
    class FindWithLessonInstanceById {

        @Test
        @DisplayName("загружает урок оценки")
        void success() {
            setUpGraph();
            clearAndFlush();

            Optional<Grade> found = gradeRepository.findWithLessonInstanceById(studentOneFirst.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getLessonInstance()).isNotNull();
            assertThat(found.get().getLessonInstance().getLessonDate()).isEqualTo(D1);
        }
    }

}

package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.domain.model.PeriodGrade;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.repository.PeriodGradeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PeriodGradeRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    PeriodGradeRepository periodGradeRepository;

    private static final long STUDENT_ID = 11L;
    private static final long TEACHER_ID = 7L;

    private AcademicYear year;
    private AcademicPeriod firstPeriod;
    private AcademicPeriod secondPeriod;
    private TeachingAssignment assignment;
    private TeachingAssignment biologyAssignment;

    private void setUpGraph() {
        year = persist(TestData.academicYear("2025-2026"));
        firstPeriod = persist(TestData.academicPeriod(year, "1 четверть",
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 10, 31)));
        secondPeriod = persist(TestData.academicPeriod(year, "2 четверть",
                LocalDate.of(2025, 11, 10), LocalDate.of(2025, 12, 26)));

        SchoolClass schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
        Subject math = persist(TestData.subject("Математика"));
        Subject biology = persist(TestData.subject("Биология"));
        assignment = persist(TestData.assignment(TEACHER_ID, schoolClass, math));
        biologyAssignment = persist(TestData.assignment(TEACHER_ID, schoolClass, biology));
    }

    @Nested
    @DisplayName("findWithAcademicPeriodById")
    class FindWithAcademicPeriodById {

        @Test
        @DisplayName("загружает учебный период оценки")
        void success() {
            setUpGraph();
            PeriodGrade grade = persist(TestData.periodGrade(STUDENT_ID, firstPeriod, assignment, 4));
            clearAndFlush();

            Optional<PeriodGrade> found = periodGradeRepository.findWithAcademicPeriodById(grade.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getAcademicPeriod().getName()).isEqualTo("1 четверть");
        }
    }

    @Nested
    @DisplayName("findPeriodGradeByStudentId")
    class FindPeriodGradeByStudentId {

        @Test
        @DisplayName("возвращает оценки ученика, отсортированные по периоду и предмету")
        void success() {
            setUpGraph();
            persist(TestData.periodGrade(STUDENT_ID, secondPeriod, assignment, 5));
            persist(TestData.periodGrade(STUDENT_ID, firstPeriod, assignment, 4));
            persist(TestData.periodGrade(STUDENT_ID, firstPeriod, biologyAssignment, 3));

            List<PeriodGrade> grades = periodGradeRepository
                    .findPeriodGradeByStudentId(STUDENT_ID, year.getId());

            assertThat(grades).hasSize(3);
            assertThat(grades).extracting(PeriodGrade::getValue).containsExactly(3, 4, 5);
        }
    }

    @Nested
    @DisplayName("findPeriodGradesByTeachingAssignmentId")
    class FindPeriodGradesByTeachingAssignmentId {

        @Test
        @DisplayName("фильтрует оценки по назначению и году")
        void success() {
            setUpGraph();
            persist(TestData.periodGrade(STUDENT_ID, firstPeriod, assignment, 4));
            persist(TestData.periodGrade(STUDENT_ID, secondPeriod, assignment, 5));
            persist(TestData.periodGrade(STUDENT_ID, firstPeriod, biologyAssignment, 3));

            List<PeriodGrade> grades = periodGradeRepository
                    .findPeriodGradesByTeachingAssignmentId(assignment.getId(), year.getId());

            assertThat(grades).hasSize(2);
            assertThat(grades).extracting(PeriodGrade::getValue).containsExactly(4, 5);
        }
    }

    @Nested
    @DisplayName("isPeriodGradeOwnedByTeacher")
    class IsPeriodGradeOwnedByTeacher {

        @Test
        @DisplayName("возвращает true для учителя предмета и false для другого")
        void success() {
            setUpGraph();
            PeriodGrade grade = persist(TestData.periodGrade(STUDENT_ID, firstPeriod, assignment, 4));

            assertThat(periodGradeRepository.isPeriodGradeOwnedByTeacher(TEACHER_ID, grade.getId())).isTrue();
            assertThat(periodGradeRepository.isPeriodGradeOwnedByTeacher(99L, grade.getId())).isFalse();
        }
    }

}

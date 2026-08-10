package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.domain.model.FinalGrade;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.repository.FinalGradeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class FinalGradeRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    FinalGradeRepository finalGradeRepository;

    private static final long STUDENT_ID = 11L;
    private static final long TEACHER_ID = 7L;

    private AcademicYear year;
    private AcademicYear previousYear;
    private TeachingAssignment assignment;
    private TeachingAssignment biologyAssignment;

    private void setUpGraph() {
        year = persist(TestData.academicYear("2025-2026"));
        previousYear = persist(AcademicYear.builder()
                .name("2024-2025")
                .startDate(LocalDate.of(2024, 9, 1))
                .endDate(LocalDate.of(2025, 5, 31))
                .build());
        SchoolClass schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
        Subject math = persist(TestData.subject("Математика"));
        Subject biology = persist(TestData.subject("Биология"));
        assignment = persist(TestData.assignment(TEACHER_ID, schoolClass, math));
        biologyAssignment = persist(TestData.assignment(TEACHER_ID, schoolClass, biology));
    }

    @Nested
    @DisplayName("findFinalGradesByStudentId")
    class FindFinalGradesByStudentId {

        @Test
        @DisplayName("возвращает итоговые оценки ученика за год, отсортированные по предмету")
        void success() {
            setUpGraph();
            persist(TestData.finalGrade(STUDENT_ID, year, assignment, 5));
            persist(TestData.finalGrade(STUDENT_ID, year, biologyAssignment, 3));
            persist(TestData.finalGrade(STUDENT_ID, previousYear, assignment, 4));

            List<FinalGrade> grades = finalGradeRepository
                    .findFinalGradesByStudentId(STUDENT_ID, year.getId());

            assertThat(grades).hasSize(2);
            assertThat(grades).extracting(FinalGrade::getValue).containsExactly(3, 5);
            assertThat(grades.get(0).getTeachingAssignment().getSubject().getName()).isEqualTo("Биология");
            assertThat(grades.get(0).getAcademicYear().getName()).isEqualTo("2025-2026");
        }
    }

    @Nested
    @DisplayName("findFinalGradesByTeachingAssignmentId")
    class FindFinalGradesByTeachingAssignmentId {

        @Test
        @DisplayName("возвращает оценки по назначению за указанный год")
        void success() {
            setUpGraph();
            persist(TestData.finalGrade(STUDENT_ID, year, assignment, 5));
            persist(TestData.finalGrade(STUDENT_ID, previousYear, assignment, 4));

            List<FinalGrade> grades = finalGradeRepository
                    .findFinalGradesByTeachingAssignmentId(assignment.getId(), year.getId());

            assertThat(grades).hasSize(1);
            assertThat(grades.get(0).getValue()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("findWithAcademicYearById")
    class FindWithAcademicYearById {

        @Test
        @DisplayName("загружает учебный год оценки")
        void success() {
            setUpGraph();
            FinalGrade grade = persist(TestData.finalGrade(STUDENT_ID, year, assignment, 5));
            clearAndFlush();

            Optional<FinalGrade> found = finalGradeRepository.findWithAcademicYearById(grade.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getAcademicYear().getName()).isEqualTo("2025-2026");
        }
    }

    @Nested
    @DisplayName("isFinalGradeOwnedByTeacher")
    class IsFinalGradeOwnedByTeacher {

        @Test
        @DisplayName("возвращает true для учителя предмета и false для другого")
        void success() {
            setUpGraph();
            FinalGrade grade = persist(TestData.finalGrade(STUDENT_ID, year, assignment, 5));

            assertThat(finalGradeRepository.isFinalGradeOwnedByTeacher(TEACHER_ID, grade.getId())).isTrue();
            assertThat(finalGradeRepository.isFinalGradeOwnedByTeacher(99L, grade.getId())).isFalse();
        }
    }

}

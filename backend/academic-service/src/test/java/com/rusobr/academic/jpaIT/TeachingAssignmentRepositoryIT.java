package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.projection.TeachingAssignmentDetailsProjection;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TeachingAssignmentRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    TeachingAssignmentRepository teachingAssignmentRepository;

    @Nested
    @DisplayName("findByIdWithClassId")
    class FindByIdWithClassId {

        @Test
        @DisplayName("возвращает id класса назначения")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
            var subject = persist(TestData.subject("Математика"));
            var assignment = persist(TestData.assignment(7L, schoolClass, subject));

            Optional<Long> classId = teachingAssignmentRepository.findByIdWithClassId(assignment.getId());

            assertThat(classId).hasValue(schoolClass.getId());
        }
    }

    @Nested
    @DisplayName("findTeachingAssignmentDetailByTeacherId")
    class FindTeachingAssignmentDetailByTeacherId {

        @Test
        @DisplayName("возвращает детали назначений, отсортированные по предмету и классу")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var classA = persist(TestData.schoolClass(year, "5Б", 42L));
            var classB = persist(TestData.schoolClass(year, "5А", 42L));
            var algebra = persist(TestData.subject("Алгебра"));
            var physics = persist(TestData.subject("Физика"));
            persist(TestData.assignment(7L, classA, physics));
            persist(TestData.assignment(7L, classB, algebra));
            persist(TestData.assignment(8L, classA, algebra));

            List<TeachingAssignmentDetailsProjection> details =
                    teachingAssignmentRepository.findTeachingAssignmentDetailByTeacherId(7L);

            assertThat(details).hasSize(2);
            assertThat(details)
                    .extracting(TeachingAssignmentDetailsProjection::getSubjectName)
                    .containsExactly("Алгебра", "Физика");
            assertThat(details)
                    .extracting(TeachingAssignmentDetailsProjection::getSchoolClassName)
                    .containsExactly("5А", "5Б");
            assertThat(details.get(0).getSchoolClassId()).isEqualTo(classB.getId());
            assertThat(details.get(0).getSubjectId()).isEqualTo(algebra.getId());
        }
    }

    @Nested
    @DisplayName("findBySubjectIdAndSchoolClassIdAndTeacherId")
    class FindBySubjectIdAndSchoolClassIdAndTeacherId {

        @Test
        @DisplayName("находит точное назначение по предмету, классу и учителю")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
            var subject = persist(TestData.subject("Математика"));
            persist(TestData.assignment(7L, schoolClass, subject));

            Optional<TeachingAssignment> found = teachingAssignmentRepository
                    .findBySubjectIdAndSchoolClassIdAndTeacherId(subject.getId(), schoolClass.getId(), 7L);

            assertThat(found).isPresent();
            assertThat(teachingAssignmentRepository
                    .findBySubjectIdAndSchoolClassIdAndTeacherId(subject.getId(), schoolClass.getId(), 8L)).isNotPresent();
        }
    }

    @Nested
    @DisplayName("findStudentIdsByTeachingAssignmentId")
    class FindStudentIdsByTeachingAssignmentId {

        @Test
        @DisplayName("возвращает id учеников класса назначения")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
            persist(TestData.classStudent(schoolClass, 11L));
            persist(TestData.classStudent(schoolClass, 12L));
            var subject = persist(TestData.subject("Математика"));
            var assignment = persist(TestData.assignment(7L, schoolClass, subject));

            List<Long> studentIds = teachingAssignmentRepository
                    .findStudentIdsByTeachingAssignmentId(assignment.getId());

            assertThat(studentIds).containsExactlyInAnyOrder(11L, 12L);
        }
    }

    @Nested
    @DisplayName("findByTeacherId")
    class FindByTeacherId {

        @Test
        @DisplayName("загружает предмет и класс назначения")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
            var subject = persist(TestData.subject("Математика"));
            persist(TestData.assignment(7L, schoolClass, subject));
            clearAndFlush();

            List<TeachingAssignment> assignments = teachingAssignmentRepository.findByTeacherId(7L);

            assertThat(assignments).hasSize(1);
            assertThat(assignments.get(0).getSubject().getName()).isEqualTo("Математика");
            assertThat(assignments.get(0).getSchoolClass().getName()).isEqualTo("5А");
        }
    }

    @Nested
    @DisplayName("isTeacherOwnedAssignment")
    class IsTeacherOwnedAssignment {

        @Test
        @DisplayName("возвращает true для учителя назначения и false для другого")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
            var subject = persist(TestData.subject("Математика"));
            var assignment = persist(TestData.assignment(7L, schoolClass, subject));

            assertThat(teachingAssignmentRepository.isTeacherOwnedAssignment(7L, assignment.getId())).isTrue();
            assertThat(teachingAssignmentRepository.isTeacherOwnedAssignment(8L, assignment.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("isOwnedByTeacherWithStudent")
    class IsOwnedByTeacherWithStudent {

        @Test
        @DisplayName("проверяет принадлежность назначения учителю и ученику")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
            persist(TestData.classStudent(schoolClass, 11L));
            persist(TestData.classStudent(schoolClass, 12L));
            var subject = persist(TestData.subject("Математика"));
            var assignment = persist(TestData.assignment(7L, schoolClass, subject));

            assertThat(teachingAssignmentRepository
                    .isOwnedByTeacherWithStudent(7L, assignment.getId(), 11L)).isTrue();
            assertThat(teachingAssignmentRepository
                    .isOwnedByTeacherWithStudent(8L, assignment.getId(), 11L)).isFalse();
            assertThat(teachingAssignmentRepository
                    .isOwnedByTeacherWithStudent(7L, assignment.getId(), 99L)).isFalse();
        }
    }

}

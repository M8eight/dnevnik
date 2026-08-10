package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SchoolClassRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    SchoolClassRepository schoolClassRepository;

    @Nested
    @DisplayName("findStudentIdsFromSchoolClasses")
    class FindStudentIdsFromSchoolClasses {

        @Test
        @DisplayName("возвращает id учеников класса")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
            persist(TestData.classStudent(schoolClass, 11L));
            persist(TestData.classStudent(schoolClass, 12L));

            List<Long> studentIds = schoolClassRepository.findStudentIdsFromSchoolClasses(schoolClass.getId());

            assertThat(studentIds).containsExactlyInAnyOrder(11L, 12L);
        }
    }

    @Nested
    @DisplayName("findSchoolClassByStudentId")
    class FindSchoolClassByStudentId {

        @Test
        @DisplayName("находит класс ученика")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
            persist(TestData.classStudent(schoolClass, 11L));

            Optional<SchoolClass> found = schoolClassRepository.findSchoolClassByStudentId(11L);

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("5А");
            assertThat(found.get().getAcademicYear()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findStudentsIdsByTeachingAssignment")
    class FindStudentsIdsByTeachingAssignment {

        @Test
        @DisplayName("возвращает id учеников по назначению")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
            persist(TestData.classStudent(schoolClass, 11L));
            persist(TestData.classStudent(schoolClass, 12L));
            var subject = persist(TestData.subject("Математика"));
            var assignment = persist(TestData.assignment(7L, schoolClass, subject));

            List<Long> studentIds = schoolClassRepository
                    .findStudentsIdsByTeachingAssignment(assignment.getId());

            assertThat(studentIds).containsExactlyInAnyOrder(11L, 12L);
        }
    }

    @Nested
    @DisplayName("findAllByAcademicYearIdOrderByNameAsc")
    class FindAllByAcademicYearIdOrderByNameAsc {

        @Test
        @DisplayName("фильтрует по году и сортирует по имени")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var otherYear = persist(AcademicYear.builder()
                    .name("2024-2025")
                    .startDate(LocalDate.of(2024, 9, 1))
                    .endDate(LocalDate.of(2025, 5, 31))
                    .build());
            persist(TestData.schoolClass(year, "5Б", 42L));
            persist(TestData.schoolClass(year, "5А", 43L));
            persist(TestData.schoolClass(otherYear, "5В", 44L));

            List<SchoolClass> classes = schoolClassRepository
                    .findAllByAcademicYearIdOrderByNameAsc(year.getId());

            assertThat(classes).extracting(SchoolClass::getName).containsExactly("5А", "5Б");
        }
    }

    @Nested
    @DisplayName("findAllByOrderByNameAsc")
    class FindAllByOrderByNameAsc {

        @Test
        @DisplayName("загружает учебный год и сортирует по имени")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            persist(TestData.schoolClass(year, "5Б", 42L));
            persist(TestData.schoolClass(year, "5А", 43L));

            List<SchoolClass> classes = schoolClassRepository.findAllByOrderByNameAsc();

            assertThat(classes).extracting(SchoolClass::getName).containsExactly("5А", "5Б");
            assertThat(classes).allSatisfy(sc -> assertThat(sc.getAcademicYear()).isNotNull());
        }
    }

    @Nested
    @DisplayName("existsByNameAndAcademicYearId")
    class ExistsByNameAndAcademicYearId {

        @Test
        @DisplayName("проверяет уникальность имени в пределах года")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            persist(TestData.schoolClass(year, "5А", 42L));

            assertThat(schoolClassRepository.existsByNameAndAcademicYearId("5А", year.getId())).isTrue();
            assertThat(schoolClassRepository.existsByNameAndAcademicYearId("5Б", year.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByNameAndIdNot")
    class ExistsByNameAndIdNot {

        @Test
        @DisplayName("игнорирует собственный id при проверке")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
            persist(TestData.schoolClass(year, "5Б", 43L));

            assertThat(schoolClassRepository.existsByNameAndIdNot("5А", schoolClass.getId())).isFalse();
            assertThat(schoolClassRepository.existsByNameAndIdNot("5Б", schoolClass.getId())).isTrue();
            assertThat(schoolClassRepository.existsByNameAndIdNot("5А", 999_999L)).isTrue();
        }
    }

    @Nested
    @DisplayName("existsByNameAndAcademicYearIdAndIdNot")
    class ExistsByNameAndAcademicYearIdAndIdNot {

        @Test
        @DisplayName("комбинирует имя, год и исключение собственного id")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));

            assertThat(schoolClassRepository
                    .existsByNameAndAcademicYearIdAndIdNot("5А", year.getId(), schoolClass.getId())).isFalse();
            assertThat(schoolClassRepository
                    .existsByNameAndAcademicYearIdAndIdNot("5А", year.getId(), 999_999L)).isTrue();
        }
    }

    @Nested
    @DisplayName("findWithClassStudentById")
    class FindWithClassStudentById {

        @Test
        @DisplayName("загружает учеников класса")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
            persist(TestData.classStudent(schoolClass, 11L));
            clearAndFlush();

            Optional<SchoolClass> found = schoolClassRepository.findWithClassStudentById(schoolClass.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getStudents())
                    .extracting(cs -> cs.getStudentId())
                    .containsExactlyInAnyOrder(11L);
        }
    }

    @Nested
    @DisplayName("findWithAcademicYearById")
    class FindWithAcademicYearById {

        @Test
        @DisplayName("загружает учебный год класса")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
            clearAndFlush();

            Optional<SchoolClass> found = schoolClassRepository.findWithAcademicYearById(schoolClass.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getAcademicYear().getName()).isEqualTo("2025-2026");
        }
    }

    @Nested
    @DisplayName("findSchoolClassesBySchoolClassTeacherId")
    class FindSchoolClassesBySchoolClassTeacherId {

        @Test
        @DisplayName("возвращает классы классного руководителя")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            persist(TestData.schoolClass(year, "5А", 42L));
            persist(TestData.schoolClass(year, "5Б", 42L));
            persist(TestData.schoolClass(year, "5В", 43L));

            List<SchoolClass> classes = schoolClassRepository.findSchoolClassesBySchoolClassTeacherId(42L);

            assertThat(classes).extracting(SchoolClass::getName).containsExactly("5А", "5Б");
        }
    }

    @Nested
    @DisplayName("findSchoolClassesTeacherId")
    class FindSchoolClassesTeacherId {

        @Test
        @DisplayName("возвращает уникальные классы учителя")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var classA = persist(TestData.schoolClass(year, "5А", 42L));
            var classB = persist(TestData.schoolClass(year, "5Б", 42L));
            var subject = persist(TestData.subject("Математика"));
            var subject2 = persist(TestData.subject("Физика"));
            persist(TestData.assignment(7L, classA, subject));
            persist(TestData.assignment(7L, classA, subject2));
            persist(TestData.assignment(7L, classB, subject));

            List<SchoolClass> classes = schoolClassRepository.findSchoolClassesTeacherId(7L);

            assertThat(classes).extracting(SchoolClass::getName).containsExactly("5А", "5Б");
        }
    }

}

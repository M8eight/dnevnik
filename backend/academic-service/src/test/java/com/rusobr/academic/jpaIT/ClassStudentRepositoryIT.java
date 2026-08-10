package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.model.ClassStudent;
import com.rusobr.academic.infrastructure.persistence.repository.ClassStudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassStudentRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    ClassStudentRepository classStudentRepository;

    @Nested
    @DisplayName("existsByStudentId")
    class ExistsByStudentId {

        @Test
        @DisplayName("возвращает true, если ученик состоит в классе")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
            persist(TestData.classStudent(schoolClass, 11L));

            assertThat(classStudentRepository.existsByStudentId(11L)).isTrue();
            assertThat(classStudentRepository.existsByStudentId(99L)).isFalse();
        }
    }

    @Nested
    @DisplayName("findBySchoolClassIdAndStudentId")
    class FindBySchoolClassIdAndStudentId {

        @Test
        @DisplayName("находит запись ученика в классе")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
            persist(TestData.classStudent(schoolClass, 11L));

            Optional<ClassStudent> found = classStudentRepository
                    .findBySchoolClassIdAndStudentId(schoolClass.getId(), 11L);

            assertThat(found).isPresent();
            assertThat(found.get().getSchoolClass().getId()).isEqualTo(schoolClass.getId());
            assertThat(classStudentRepository
                    .findBySchoolClassIdAndStudentId(schoolClass.getId(), 12L)).isNotPresent();
        }
    }

    @Nested
    @DisplayName("findAllStudentIds")
    class FindAllStudentIds {

        @Test
        @DisplayName("собирает id всех учеников по классам")
        void success() {
            var year = persist(TestData.academicYear("2025-2026"));
            var classA = persist(TestData.schoolClass(year, "5А", 42L));
            var classB = persist(TestData.schoolClass(year, "5Б", 43L));
            persist(TestData.classStudent(classA, 11L));
            persist(TestData.classStudent(classA, 12L));
            persist(TestData.classStudent(classB, 13L));

            Set<Long> ids = classStudentRepository.findAllStudentIds();

            assertThat(ids).containsExactlyInAnyOrder(11L, 12L, 13L);
        }
    }

}

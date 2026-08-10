package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.infrastructure.persistence.projection.SubjectResponseProjection;
import com.rusobr.academic.infrastructure.persistence.repository.SubjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SubjectRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    SubjectRepository subjectRepository;

    @Nested
    @DisplayName("findAllByOrderByNameAsc")
    class FindAllByOrderByNameAsc {

        @Test
        @DisplayName("возвращает предметы, отсортированные по имени")
        void success() {
            persist(TestData.subject("Химия"));
            persist(TestData.subject("Алгебра"));

            Pageable pageable = PageRequest.of(0, 10);
            Page<SubjectResponseProjection> page = subjectRepository.findAllByOrderByNameAsc(pageable);

            assertThat(page.getContent())
                    .extracting(SubjectResponseProjection::getName)
                    .containsExactly("Алгебра", "Химия");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("возвращает предмет по id")
        void success() {
            Subject subject = persist(TestData.subject("Физика"));

            Optional<Subject> found = subjectRepository.findById(subject.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Физика");
        }
    }

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("исключает удалённый предмет из выборок")
        void success() {
            Subject subject = persist(TestData.subject("История"));

            subjectRepository.deleteById(subject.getId());
            clearAndFlush();

            assertThat(subjectRepository.findById(subject.getId())).isNotPresent();
            assertThat(subjectRepository.findAll()).isEmpty();
        }
    }

}

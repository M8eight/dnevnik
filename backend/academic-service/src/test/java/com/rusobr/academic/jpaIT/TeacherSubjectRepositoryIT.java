package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeacherSubject;
import com.rusobr.academic.domain.model.TeacherSubjectId;
import com.rusobr.academic.infrastructure.persistence.repository.TeacherSubjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TeacherSubjectRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    TeacherSubjectRepository teacherSubjectRepository;

    @Nested
    @DisplayName("findByTeacherId")
    class FindByTeacherId {

        @Test
        @DisplayName("возвращает предметы учителя, отсортированные по имени")
        void success() {
            Subject algebra = persist(TestData.subject("Алгебра"));
            Subject physics = persist(TestData.subject("Физика"));
            persist(TestData.teacherSubject(7L, physics));
            persist(TestData.teacherSubject(7L, algebra));
            persist(TestData.teacherSubject(8L, algebra));

            List<TeacherSubject> teacherSubjects = teacherSubjectRepository.findByTeacherId(7L);

            assertThat(teacherSubjects)
                    .extracting(ts -> ts.getSubject().getName())
                    .containsExactly("Алгебра", "Физика");
        }
    }

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("помечает запись удалённой и исключает её из активных запросов")
        void success() {
            Subject algebra = persist(TestData.subject("Алгебра"));
            TeacherSubject teacherSubject = persist(TestData.teacherSubject(7L, algebra));
            TeacherSubjectId id = teacherSubject.getId();

            teacherSubjectRepository.softDelete(id.getSubjectId(), id.getTeacherId());
            clearAndFlush();

            assertThat(teacherSubjectRepository.findById(id)).isNotPresent();
            assertThat(teacherSubjectRepository.findByTeacherId(7L)).isEmpty();

            Optional<TeacherSubject> withDeleted = teacherSubjectRepository
                    .findByIdWithDeleted(id.getSubjectId(), id.getTeacherId());
            assertThat(withDeleted).isPresent();
            assertThat(withDeleted.get().getDeletedAt()).isNotNull();
        }
    }

}

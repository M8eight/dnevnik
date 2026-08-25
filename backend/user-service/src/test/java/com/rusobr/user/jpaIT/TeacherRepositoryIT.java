package com.rusobr.user.jpaIT;

import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.persistence.repository.TeacherRepository;
import com.rusobr.user.infrastructure.persistence.repository.projection.UserProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
public class TeacherRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    TeacherRepository teacherRepository;

    private Teacher persistTeacher(String username, String firstName, String lastName) {
        User user = persist(TestData.user(username, firstName, lastName));
        return persist(TestData.teacher(user, username + "@mail.ru", "+79990001122"));
    }

    @Nested
    @DisplayName("findWithUserById")
    class FindWithUserById {

        @Test
        @DisplayName("возвращает учителя вместе с пользователем")
        void success() {
            Teacher teacher = persistTeacher("ivanova", "Ирина", "Иванова");

            Optional<Teacher> found = teacherRepository.findWithUserById(teacher.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getUser().getUsername()).isEqualTo("ivanova");
            assertThat(found.get().getEmail()).isEqualTo("ivanova@mail.ru");
        }

        @Test
        @DisplayName("возвращает пустой Optional, если учитель не найден")
        void notFound() {
            Optional<Teacher> found = teacherRepository.findWithUserById(999L);

            assertThat(found).isNotPresent();
        }
    }

    @Nested
    @DisplayName("findByIdWithDeleted")
    class FindByIdWithDeleted {

        @Test
        @DisplayName("возвращает учителя, даже если он мягко удалён")
        void success() {
            Teacher teacher = persistTeacher("ivanova", "Ирина", "Иванова");

            teacherRepository.deleteById(teacher.getId());
            clearAndFlush();

            Optional<Teacher> withDeleted = teacherRepository.findByIdWithDeleted(teacher.getId());

            assertThat(withDeleted).isPresent();
            assertThat(withDeleted.get().getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findAllTeachersByIds")
    class FindAllTeachersByIds {

        @Test
        @DisplayName("возвращает учителей по id, отсортированных по фамилии")
        void success() {
            persistTeacher("sidorova", "Анна", "Сидорова");
            persistTeacher("ivanova", "Ирина", "Иванова");
            Teacher kozlova = persistTeacher("kozlova", "Ольга", "Козлова");

            List<UserProjection> teachers = teacherRepository.findAllTeachersByIds(List.of(kozlova.getId()));

            assertThat(teachers)
                    .extracting(UserProjection::getLastName)
                    .containsExactly("Козлова");
        }

        @Test
        @DisplayName("возвращает пустой список, если ни один id не найден")
        void notFound() {
            List<UserProjection> teachers = teacherRepository.findAllTeachersByIds(List.of(999L));

            assertThat(teachers).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTeacherSimpleById")
    class GetTeacherSimpleById {

        @Test
        @DisplayName("возвращает проекцию учителя по id")
        void success() {
            Teacher teacher = persistTeacher("ivanova", "Ирина", "Иванова");

            UserProjection projection = teacherRepository.getTeacherSimpleById(teacher.getId());

            assertThat(projection).isNotNull();
            assertThat(projection.getId()).isEqualTo(teacher.getId());
            assertThat(projection.getFirstName()).isEqualTo("Ирина");
            assertThat(projection.getLastName()).isEqualTo("Иванова");
            assertThat(projection.getUsername()).isEqualTo("ivanova");
        }
    }

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("исключает удалённого учителя из выборок")
        void success() {
            Teacher teacher = persistTeacher("ivanova", "Ирина", "Иванова");
            persistTeacher("kozova", "Ольга", "Козлова");

            teacherRepository.deleteById(teacher.getId());
            clearAndFlush();

            assertThat(teacherRepository.findById(teacher.getId())).isNotPresent();
            assertThat(teacherRepository.findWithUserById(teacher.getId())).isNotPresent();
            assertThat(teacherRepository.findAllTeachersByIds(List.of(teacher.getId()))).isEmpty();
        }
    }

}

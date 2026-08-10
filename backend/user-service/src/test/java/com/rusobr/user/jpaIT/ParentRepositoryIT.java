package com.rusobr.user.jpaIT;

import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.persistence.repository.ParentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ParentRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    ParentRepository parentRepository;

    private Parent persistParent(String username) {
        User user = persist(TestData.user(username, "Ольга", "Сидорова"));
        return persist(TestData.parent(user));
    }

    @Nested
    @DisplayName("findWithUserById")
    class FindWithUserById {

        @Test
        @DisplayName("возвращает родителя вместе с пользователем и детьми")
        void success() {
            Parent parent = persistParent("parent1");
            User childUser = persist(TestData.user("child1", "Иван", "Сидоров"));
            Student child = persist(TestData.student(childUser, "math", parent));
            clearAndFlush();

            Optional<Parent> found = parentRepository.findWithUserById(parent.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getUser().getUsername()).isEqualTo("parent1");
            assertThat(found.get().getChildren())
                    .extracting(Student::getId)
                    .containsExactly(child.getId());
        }

        @Test
        @DisplayName("возвращает родителя с пользователем, даже если детей нет")
        void withoutChildren() {
            Parent parent = persistParent("parent1");

            Optional<Parent> found = parentRepository.findWithUserById(parent.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getUser().getUsername()).isEqualTo("parent1");
            assertThat(found.get().getChildren()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdWithDeleted")
    class FindByIdWithDeleted {

        @Test
        @DisplayName("возвращает родителя, даже если он мягко удалён")
        void success() {
            Parent parent = persistParent("parent1");

            parentRepository.deleteById(parent.getId());
            clearAndFlush();

            Optional<Parent> withDeleted = parentRepository.findByIdWithDeleted(parent.getId());

            assertThat(withDeleted).isPresent();
            assertThat(withDeleted.get().getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findParentInfoById")
    class FindParentInfoById {

        @Test
        @DisplayName("возвращает родителя вместе с детьми и их пользователями")
        void success() {
            Parent parent = persistParent("parent1");
            User childUser = persist(TestData.user("child1", "Иван", "Сидоров"));
            Student child = persist(TestData.student(childUser, "math", parent));
            clearAndFlush();

            Optional<Parent> found = parentRepository.findParentInfoById(parent.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getChildren())
                    .extracting(Student::getId)
                    .containsExactly(child.getId());
            assertThat(found.get().getChildren())
                    .extracting(s -> s.getUser().getUsername())
                    .containsExactly("child1");
        }
    }

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("исключает удалённого родителя из выборок")
        void success() {
            Parent parent = persistParent("parent1");

            parentRepository.deleteById(parent.getId());
            clearAndFlush();

            assertThat(parentRepository.findById(parent.getId())).isNotPresent();
            assertThat(parentRepository.findWithUserById(parent.getId())).isNotPresent();
        }
    }

}

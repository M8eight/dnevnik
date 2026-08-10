package com.rusobr.user.jpaIT;

import com.rusobr.common.enums.UserRole;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.infrastructure.specification.UserSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    UserRepository userRepository;

    @Nested
    @DisplayName("findByKeycloakId")
    class FindByKeycloakId {

        @Test
        @DisplayName("возвращает пользователя по keycloak id")
        void success() {
            persist(TestData.user("ivan", "Иван", "Иванов", "kc-123"));

            Optional<User> found = userRepository.findByKeycloakId("kc-123");

            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("ivan");
        }

        @Test
        @DisplayName("возвращает пустой Optional, если пользователь не найден")
        void notFound() {
            Optional<User> found = userRepository.findByKeycloakId("missing");

            assertThat(found).isNotPresent();
        }
    }

    @Nested
    @DisplayName("existsByUsername")
    class ExistsByUsername {

        @Test
        @DisplayName("возвращает true, если username занят")
        void taken() {
            persist(TestData.user("ivan", "Иван", "Иванов"));

            boolean exists = userRepository.existsByUsername("ivan");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("возвращает false, если username свободен")
        void free() {
            persist(TestData.user("ivan", "Иван", "Иванов"));

            boolean exists = userRepository.existsByUsername("petr");

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByUsernameAndIdNot")
    class ExistsByUsernameAndIdNot {

        @Test
        @DisplayName("возвращает true, если username занят другим пользователем")
        void takenByOther() {
            User ivan = persist(TestData.user("ivan", "Иван", "Иванов"));
            persist(TestData.user("petr", "Пётр", "Петров"));

            boolean exists = userRepository.existsByUsernameAndIdNot("petr", ivan.getId());

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("возвращает false, если username занят тем же пользователем")
        void takenBySame() {
            User ivan = persist(TestData.user("ivan", "Иван", "Иванов"));

            boolean exists = userRepository.existsByUsernameAndIdNot("ivan", ivan.getId());

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findByUsername")
    class FindByUsername {

        @Test
        @DisplayName("возвращает пользователя по username")
        void success() {
            persist(TestData.user("ivan", "Иван", "Иванов"));

            Optional<User> found = userRepository.findByUsername("ivan");

            assertThat(found).isPresent();
            assertThat(found.get().getFirstName()).isEqualTo("Иван");
        }

        @Test
        @DisplayName("возвращает пустой Optional, если пользователь не найден")
        void notFound() {
            Optional<User> found = userRepository.findByUsername("petr");

            assertThat(found).isNotPresent();
        }
    }

    @Nested
    @DisplayName("findWithRolesById")
    class FindWithRolesById {

        @Test
        @DisplayName("возвращает пользователя с ролями")
        void success() {
            User user = persist(TestData.user("ivan", "Иван", "Иванов", Set.of(UserRole.STUDENT, UserRole.PARENT)));

            Optional<User> found = userRepository.findWithRolesById(user.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getRoles())
                    .containsExactlyInAnyOrder(UserRole.STUDENT, UserRole.PARENT);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("фильтрует пользователей по роли")
        void byRole() {
            persist(TestData.user("teacher1", "Анна", "Иванова", Set.of(UserRole.TEACHER)));
            persist(TestData.user("student1", "Иван", "Петров", Set.of(UserRole.STUDENT)));

            Specification<User> specification = UserSpecification.findByRole(UserRole.STUDENT)
                    .and(UserSpecification.findByFullNameFuzzy(null));

            Page<User> page = userRepository.findAll(specification, PageRequest.of(0, 10));

            assertThat(page.getContent())
                    .extracting(User::getUsername)
                    .containsExactly("student1");
        }

        @Test
        @DisplayName("фильтрует пользователей по части ФИО")
        void byFullName() {
            persist(TestData.user("ivanov", "Иван", "Иванов"));
            persist(TestData.user("petrov", "Пётр", "Петров"));

            Specification<User> specification = UserSpecification.findByRole(null)
                    .and(UserSpecification.findByFullNameFuzzy("Иван"));

            Page<User> page = userRepository.findAll(specification, PageRequest.of(0, 10));

            assertThat(page.getContent())
                    .extracting(User::getUsername)
                    .containsExactly("ivanov");
        }

        @Test
        @DisplayName("комбинирует фильтры по роли и ФИО")
        void byRoleAndFullName() {
            persist(TestData.user("student1", "Иван", "Петров", Set.of(UserRole.STUDENT)));
            persist(TestData.user("teacher1", "Иван", "Сидоров", Set.of(UserRole.TEACHER)));

            Specification<User> specification = UserSpecification.findByRole(UserRole.STUDENT)
                    .and(UserSpecification.findByFullNameFuzzy("Иван"));

            Page<User> page = userRepository.findAll(specification, PageRequest.of(0, 10));

            assertThat(page.getContent())
                    .extracting(User::getUsername)
                    .containsExactly("student1");
        }
    }

    @Nested
    @DisplayName("setKeycloakId")
    class SetKeycloakId {

        @Test
        @DisplayName("обновляет keycloakId пользователя")
        void success() {
            User user = persist(TestData.user("ivan", "Иван", "Иванов"));

            userRepository.setKeycloakId("kc-456", user.getId());
            clearAndFlush();

            Optional<User> found = userRepository.findByKeycloakId("kc-456");

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(user.getId());
        }
    }

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("исключает удалённого пользователя из выборок")
        void success() {
            User user = persist(TestData.user("ivan", "Иван", "Иванов"));

            userRepository.deleteById(user.getId());
            clearAndFlush();

            assertThat(userRepository.findById(user.getId())).isNotPresent();
            assertThat(userRepository.findByUsername("ivan")).isNotPresent();
            assertThat(userRepository.existsByUsername("ivan")).isFalse();
            assertThat(userRepository.findAll()).isEmpty();
        }
    }

}

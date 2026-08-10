package com.rusobr.user.jpaIT;

import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.persistence.repository.StudentRepository;
import com.rusobr.user.infrastructure.persistence.repository.projection.UserProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class StudentRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    StudentRepository studentRepository;

    private Student persistStudent(String username, String firstName, String lastName) {
        User user = persist(TestData.user(username, firstName, lastName));
        return persist(TestData.student(user, "math"));
    }


    @Nested
    @DisplayName("findAllStudentsByIds")
    class FindAllStudentsByIds {

        @Test
        @DisplayName("возвращает студентов по id, отсортированных по фамилии")
        void success() {
            persistStudent("sidorov", "Алексей", "Сидоров");
            persistStudent("ivanov", "Иван", "Иванов");
            Student petrov = persistStudent("petrov", "Пётр", "Петров");

            List<UserProjection> students = studentRepository.findAllStudentsByIds(List.of(petrov.getId()));

            assertThat(students)
                    .extracting(UserProjection::getLastName)
                    .containsExactly("Петров");
        }

        @Test
        @DisplayName("возвращает пустой список, если ни один id не найден")
        void notFound() {
            List<UserProjection> students = studentRepository.findAllStudentsByIds(List.of(999L));

            assertThat(students).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllStudentsExcludeAssigned")
    class FindAllStudentsExcludeAssigned {

        @Test
        @DisplayName("исключает студентов с переданными user id")
        void success() {
            Student ivanov = persistStudent("ivanov", "Иван", "Иванов");
            Student sidorov = persistStudent("sidorov", "Алексей", "Сидоров");
            Student petrov = persistStudent("petrov", "Пётр", "Петров");

            List<UserProjection> students = studentRepository
                    .findAllStudentsExcludeAssigned(List.of(ivanov.getUser().getId(), sidorov.getUser().getId()));

            assertThat(students)
                    .extracting(UserProjection::getLastName)
                    .containsExactly("Петров");
        }

        @Test
        @DisplayName("возвращает всех студентов, если список исключений пуст")
        void emptyExcludeList() {
            Student ivanov = persistStudent("ivanov", "Иван", "Иванов");
            persistStudent("petrov", "Пётр", "Петров");

            List<UserProjection> students = studentRepository.findAllStudentsExcludeAssigned(List.of());

            assertThat(students)
                    .extracting(UserProjection::getLastName)
                    .containsExactly("Иванов", "Петров");
        }
    }

    @Nested
    @DisplayName("findWithUserAllStudents")
    class FindWithUserAllStudents {

        @Test
        @DisplayName("возвращает всех студентов с данными пользователя, отсортированных по фамилии")
        void success() {
            persistStudent("sidorov", "Алексей", "Сидоров");
            persistStudent("ivanov", "Иван", "Иванов");

            List<UserProjection> students = studentRepository.findWithUserAllStudents();

            assertThat(students)
                    .extracting(UserProjection::getLastName)
                    .containsExactly("Иванов", "Сидоров");
        }
    }

    @Nested
    @DisplayName("findWithUserById")
    class FindWithUserById {

        @Test
        @DisplayName("возвращает студента вместе с пользователем")
        void success() {
            Student student = persistStudent("ivanov", "Иван", "Иванов");

            Optional<Student> found = studentRepository.findWithUserById(student.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getUser().getUsername()).isEqualTo("ivanov");
            assertThat(found.get().getStudyProfile()).isEqualTo("math");
        }

        @Test
        @DisplayName("возвращает пустой Optional, если студент не найден")
        void notFound() {
            Optional<Student> found = studentRepository.findWithUserById(999L);

            assertThat(found).isNotPresent();
        }
    }

    @Nested
    @DisplayName("findStudentInfoById")
    class FindStudentInfoById {

        @Test
        @DisplayName("возвращает студента вместе с пользователем и родителем")
        void success() {
            Parent parent = persist(TestData.parent(persist(TestData.user("parent1", "Ольга", "Сидорова"))));
            User childUser = persist(TestData.user("child1", "Иван", "Сидоров"));
            Student student = persist(TestData.student(childUser, "math", parent));

            Optional<Student> found = studentRepository.findStudentInfoById(student.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getUser().getUsername()).isEqualTo("child1");
            assertThat(found.get().getParent().getUser().getUsername()).isEqualTo("parent1");
        }

        @Test
        @DisplayName("возвращает студента с пользователем, даже если родитель не назначен")
        void withoutParent() {
            Student student = persistStudent("ivanov", "Иван", "Иванов");

            Optional<Student> found = studentRepository.findStudentInfoById(student.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getUser().getUsername()).isEqualTo("ivanov");
            assertThat(found.get().getParent()).isNull();
        }
    }

    @Nested
    @DisplayName("findByIdWithDeleted")
    class FindByIdWithDeleted {

        @Test
        @DisplayName("возвращает студента, даже если он мягко удалён")
        void success() {
            Student student = persistStudent("ivanov", "Иван", "Иванов");

            studentRepository.deleteById(student.getId());
            clearAndFlush();

            Optional<Student> withDeleted = studentRepository.findByIdWithDeleted(student.getId());

            assertThat(withDeleted).isPresent();
            assertThat(withDeleted.get().getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("исключает удалённого студента из выборок")
        void success() {
            Student student = persistStudent("ivanov", "Иван", "Иванов");
            persistStudent("petrov", "Пётр", "Петров");

            studentRepository.deleteById(student.getId());
            clearAndFlush();

            assertThat(studentRepository.findById(student.getId())).isNotPresent();
            assertThat(studentRepository.findWithUserById(student.getId())).isNotPresent();
            assertThat(studentRepository.findWithUserAllStudents())
                    .extracting(UserProjection::getLastName)
                    .containsExactly("Петров");
        }
    }

}

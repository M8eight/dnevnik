package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.model.Homework;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.projection.HomeworkWithSubjectProjection;
import com.rusobr.academic.infrastructure.persistence.repository.HomeworkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class HomeworkRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    HomeworkRepository homeworkRepository;

    private static final long STUDENT_ID = 11L;
    private static final long TEACHER_ID = 7L;

    private TeachingAssignment assignment;
    private LessonInstance lessonInstance;
    private Homework homework;

    private void setUpGraph() {
        var year = persist(TestData.academicYear("2025-2026"));
        SchoolClass schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
        persist(TestData.classStudent(schoolClass, STUDENT_ID));
        Subject subject = persist(TestData.subject("Математика"));
        assignment = persist(TestData.assignment(TEACHER_ID, schoolClass, subject));
        ScheduleLesson scheduleLesson = persist(TestData.scheduleLesson(assignment, DayOfWeek.MONDAY, 1));
        lessonInstance = persist(TestData.lessonInstance(scheduleLesson, LocalDate.of(2025, 9, 15)));
        homework = persist(TestData.homework(lessonInstance, "Параграф 5, упражнения 1-3"));
    }

    @Nested
    @DisplayName("findHomeworksByDate")
    class FindHomeworksByDate {

        @Test
        @DisplayName("возвращает домашние задания ученика за дату")
        void success() {
            setUpGraph();

            List<HomeworkWithSubjectProjection> homeworks = homeworkRepository
                    .findHomeworksByDate(LocalDate.of(2025, 9, 15), STUDENT_ID);

            assertThat(homeworks).hasSize(1);
            assertThat(homeworks.get(0).getText()).isEqualTo("Параграф 5, упражнения 1-3");
            assertThat(homeworks.get(0).getSubjectName()).isEqualTo("Математика");
        }

        @Test
        @DisplayName("возвращает пусто для другой даты или ученика")
        void returnsEmptyForOtherDateOrStudent() {
            setUpGraph();

            assertThat(homeworkRepository
                    .findHomeworksByDate(LocalDate.of(2025, 9, 16), STUDENT_ID)).isEmpty();
            assertThat(homeworkRepository
                    .findHomeworksByDate(LocalDate.of(2025, 9, 15), 999L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findHomeworksByTeachingAssignmentId")
    class FindHomeworksByTeachingAssignmentId {

        @Test
        @DisplayName("возвращает домашние задания назначения постранично")
        void success() {
            setUpGraph();

            Page<Homework> page = homeworkRepository
                    .findHomeworksByTeachingAssignmentId(assignment.getId(), PageRequest.of(0, 10));

            assertThat(page.getTotalElements()).isEqualTo(1);
            assertThat(page.getContent()).extracting(Homework::getText)
                    .containsExactly("Параграф 5, упражнения 1-3");
        }
    }

    @Nested
    @DisplayName("findWithLessonInstanceById")
    class FindWithLessonInstanceById {

        @Test
        @DisplayName("загружает урок домашнего задания")
        void success() {
            setUpGraph();
            clearAndFlush();

            Optional<Homework> found = homeworkRepository.findWithLessonInstanceById(homework.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getLessonInstance()).isNotNull();
            assertThat(found.get().getLessonInstance().getLessonDate())
                    .isEqualTo(LocalDate.of(2025, 9, 15));
        }
    }

    @Nested
    @DisplayName("isHomeworkOwnedByTeacher")
    class IsHomeworkOwnedByTeacher {

        @Test
        @DisplayName("возвращает true для учителя предмета и false для другого")
        void success() {
            setUpGraph();

            assertThat(homeworkRepository.isHomeworkOwnedByTeacher(TEACHER_ID, homework.getId())).isTrue();
            assertThat(homeworkRepository.isHomeworkOwnedByTeacher(99L, homework.getId())).isFalse();
        }
    }

}

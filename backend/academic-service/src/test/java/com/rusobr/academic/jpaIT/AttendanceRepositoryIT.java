package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.model.Attendance;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.repository.AttendanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AttendanceRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    AttendanceRepository attendanceRepository;

    private static final long STUDENT_ID = 11L;
    private static final long TEACHER_ID = 7L;

    private LessonInstance lessonInstance;
    private Attendance attendance;

    private void setUpGraph() {
        var year = persist(TestData.academicYear("2025-2026"));
        SchoolClass schoolClass = persist(TestData.schoolClass(year, "5А", 42L));
        persist(TestData.classStudent(schoolClass, STUDENT_ID));
        Subject subject = persist(TestData.subject("Математика"));
        TeachingAssignment assignment = persist(TestData.assignment(TEACHER_ID, schoolClass, subject));
        ScheduleLesson scheduleLesson = persist(TestData.scheduleLesson(assignment, DayOfWeek.MONDAY, 1));
        lessonInstance = persist(TestData.lessonInstance(scheduleLesson, LocalDate.of(2025, 9, 15)));
        attendance = persist(TestData.attendance(STUDENT_ID, lessonInstance, AttendanceStatus.ABSENT));
    }

    @Nested
    @DisplayName("findByStudentIdAndLessonInstanceId")
    class FindByStudentIdAndLessonInstanceId {

        @Test
        @DisplayName("находит посещаемость ученика на уроке")
        void success() {
            setUpGraph();

            Optional<Attendance> found = attendanceRepository
                    .findByStudentIdAndLessonInstanceId(STUDENT_ID, lessonInstance.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getStatus()).isEqualTo(AttendanceStatus.ABSENT);
        }

        @Test
        @DisplayName("возвращает пусто, если посещаемость отсутствует")
        void returnsEmptyWhenAbsent() {
            setUpGraph();

            assertThat(attendanceRepository
                    .findByStudentIdAndLessonInstanceId(999L, lessonInstance.getId())).isNotPresent();
        }
    }

    @Nested
    @DisplayName("isAttendanceOwnedByTeacher")
    class IsAttendanceOwnedByTeacher {

        @Test
        @DisplayName("возвращает true для учителя предмета и false для другого")
        void success() {
            setUpGraph();

            assertThat(attendanceRepository
                    .isAttendanceOwnedByTeacher(TEACHER_ID, attendance.getId())).isTrue();
            assertThat(attendanceRepository
                    .isAttendanceOwnedByTeacher(99L, attendance.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("findWithLessonInstanceById")
    class FindWithLessonInstanceById {

        @Test
        @DisplayName("загружает урок посещаемости")
        void success() {
            setUpGraph();
            clearAndFlush();

            Optional<Attendance> found = attendanceRepository.findWithLessonInstanceById(attendance.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getLessonInstance()).isNotNull();
            assertThat(found.get().getLessonInstance().getLessonDate())
                    .isEqualTo(LocalDate.of(2025, 9, 15));
        }
    }

}

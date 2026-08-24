package com.rusobr.user.mapper;

import com.rusobr.common.enums.UserRole;
import com.rusobr.user.application.mapper.StudentMapper;
import com.rusobr.user.application.mapper.StudentMapperImpl;
import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.web.dto.feign.AcademicYearResponse;
import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.student.StudentWithClassResponse;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import com.rusobr.user.web.dto.user.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StudentMapperTest {

    private final StudentMapper mapper = new StudentMapperImpl();

    private SchoolClassResponse schoolClass() {
        return new SchoolClassResponse(
                10L,
                "10A",
                new AcademicYearResponse(1L, "2026-2027", null, null, null, true),
                1L
        );
    }

    private TeacherResponse classTeacher() {
        return new TeacherResponse(
                UserResponse.builder().id(1L).build(),
                new TeacherDetails("t@mail.com", "+7999")
        );
    }

    private User studentUser() {
        return User.builder()
                .id(2L)
                .username("petr")
                .firstName("Petr")
                .lastName("Petrov")
                .keycloakId("kc-2")
                .roles(Set.of(UserRole.STUDENT))
                .build();
    }

    @Nested
    @DisplayName("toStudentDetailResponse")
    class ToStudentDetailResponse {

        @Test
        @DisplayName("маппит данные студента и вкладывает класс с классным руководителем")
        void mapsAllFields() {
            Student student = Student.builder()
                    .id(2L)
                    .user(studentUser())
                    .studyProfile("physics")
                    .build();
            SchoolClassResponse schoolClass = schoolClass();
            TeacherResponse teacher = classTeacher();

            StudentWithClassResponse result = mapper.toStudentDetailResponse(student, schoolClass, teacher);

            assertThat(result.id()).isEqualTo(2L);
            assertThat(result.firstName()).isEqualTo("Petr");
            assertThat(result.lastName()).isEqualTo("Petrov");
            assertThat(result.studyProfile()).isEqualTo("physics");
            assertThat(result.schoolClass()).isSameAs(schoolClass);
            assertThat(result.schoolClassTeacher()).isSameAs(teacher);
        }

        @Test
        @DisplayName("null student с переданными классом и учителем — возвращает непустой ответ")
        void nullStudent_stillBuildsResponse() {
            SchoolClassResponse schoolClass = schoolClass();
            TeacherResponse teacher = classTeacher();

            StudentWithClassResponse result = mapper.toStudentDetailResponse(null, schoolClass, teacher);

            assertThat(result.id()).isNull();
            assertThat(result.schoolClass()).isSameAs(schoolClass);
            assertThat(result.schoolClassTeacher()).isSameAs(teacher);
        }

        @Test
        @DisplayName("все аргументы null — возвращает null")
        void nullArgs_returnsNull() {
            assertThat(mapper.toStudentDetailResponse(null, null, null)).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("связывает пользователя и копирует studyProfile")
        void mapsAllFields() {
            User user = User.builder().id(2L).build();
            StudentDetails details = new StudentDetails("math");

            Student result = mapper.toEntity(user, details);

            assertThat(result.getId()).isNull();
            assertThat(result.getUser()).isSameAs(user);
            assertThat(result.getStudyProfile()).isEqualTo("math");
        }

        @Test
        @DisplayName("null user и null details — возвращает null")
        void nullArgs_returnsNull() {
            assertThat(mapper.toEntity(null, null)).isNull();
        }
    }

    @Nested
    @DisplayName("toStudentDetails")
    class ToStudentDetails {

        @Test
        @DisplayName("копирует studyProfile")
        void mapsAllFields() {
            Student student = Student.builder().studyProfile("physics").build();

            StudentDetails result = mapper.toStudentDetails(student);

            assertThat(result.studyProfile()).isEqualTo("physics");
        }

        @Test
        @DisplayName("null — возвращает null")
        void nullStudent_returnsNull() {
            assertThat(mapper.toStudentDetails(null)).isNull();
        }
    }

}

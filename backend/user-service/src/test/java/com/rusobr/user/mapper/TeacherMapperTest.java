package com.rusobr.user.mapper;

import com.rusobr.common.enums.UserRole;
import com.rusobr.user.application.mapper.TeacherMapper;
import com.rusobr.user.application.mapper.TeacherMapperImpl;
import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import com.rusobr.user.web.dto.user.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TeacherMapperTest {

    private final TeacherMapper mapper = new TeacherMapperImpl();

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("связывает пользователя и копирует детали")
        void mapsAllFields() {
            User user = User.builder().id(1L).build();
            TeacherDetails details = new TeacherDetails("t@mail.com", "+79991112233");

            Teacher result = mapper.toEntity(user, details);

            assertThat(result.getId()).isNull();
            assertThat(result.getUser()).isSameAs(user);
            assertThat(result.getEmail()).isEqualTo("t@mail.com");
            assertThat(result.getPhoneNumber()).isEqualTo("+79991112233");
        }

        @Test
        @DisplayName("null details — остальные поля остаются null")
        void nullDetails_returnsTeacherWithUser() {
            User user = User.builder().id(1L).build();

            Teacher result = mapper.toEntity(user, null);

            assertThat(result.getUser()).isSameAs(user);
            assertThat(result.getEmail()).isNull();
            assertThat(result.getPhoneNumber()).isNull();
        }

        @Test
        @DisplayName("null user и null details — возвращает null")
        void nullArgs_returnsNull() {
            assertThat(mapper.toEntity(null, null)).isNull();
        }
    }

    @Nested
    @DisplayName("toTeacherDetails")
    class ToTeacherDetails {

        @Test
        @DisplayName("копирует email и phoneNumber")
        void mapsAllFields() {
            Teacher teacher = Teacher.builder()
                    .email("t@mail.com")
                    .phoneNumber("+79991112233")
                    .build();

            TeacherDetails result = mapper.toTeacherDetails(teacher);

            assertThat(result.email()).isEqualTo("t@mail.com");
            assertThat(result.phoneNumber()).isEqualTo("+79991112233");
        }

        @Test
        @DisplayName("null — возвращает null")
        void nullTeacher_returnsNull() {
            assertThat(mapper.toTeacherDetails(null)).isNull();
        }
    }

    @Nested
    @DisplayName("toTeacherResponse")
    class ToTeacherResponse {

        @Test
        @DisplayName("маппит пользователя в user и детали в details")
        void mapsAllFields() {
            User user = User.builder()
                    .id(1L)
                    .username("ivan")
                    .firstName("Ivan")
                    .lastName("Ivanov")
                    .keycloakId("kc-1")
                    .roles(Set.of(UserRole.TEACHER))
                    .build();
            Teacher teacher = Teacher.builder()
                    .user(user)
                    .email("t@mail.com")
                    .phoneNumber("+79991112233")
                    .build();

            TeacherResponse result = mapper.toTeacherResponse(teacher);

            assertThat(result.details().email()).isEqualTo("t@mail.com");
            assertThat(result.details().phoneNumber()).isEqualTo("+79991112233");
            assertThat(result.user()).isEqualTo(UserResponse.builder()
                    .id(1L)
                    .username("ivan")
                    .firstName("Ivan")
                    .lastName("Ivanov")
                    .keycloakId("kc-1")
                    .roles(Set.of(UserRole.TEACHER))
                    .build());
        }

        @Test
        @DisplayName("null — возвращает null")
        void nullTeacher_returnsNull() {
            assertThat(mapper.toTeacherResponse(null)).isNull();
        }

        @Test
        @DisplayName("null user — user остаётся null")
        void nullUser_returnsDetailsOnly() {
            Teacher teacher = Teacher.builder()
                    .email("t@mail.com")
                    .phoneNumber("+79991112233")
                    .build();

            TeacherResponse result = mapper.toTeacherResponse(teacher);

            assertThat(result.user()).isNull();
            assertThat(result.details()).isNotNull();
        }
    }
}

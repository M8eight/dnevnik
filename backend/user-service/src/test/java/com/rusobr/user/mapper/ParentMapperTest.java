package com.rusobr.user.mapper;

import com.rusobr.common.enums.UserRole;
import com.rusobr.user.application.mapper.ParentMapper;
import com.rusobr.user.application.mapper.ParentMapperImpl;
import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.parent.ParentInfoResponse;
import com.rusobr.user.web.dto.parent.ParentResponse;
import com.rusobr.user.web.dto.user.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ParentMapperTest {

    private final ParentMapper mapper = new ParentMapperImpl();

    private User parentUser() {
        return User.builder()
                .id(1L)
                .username("mama")
                .firstName("Elena")
                .lastName("Petrova")
                .keycloakId("kc-1")
                .roles(Set.of(UserRole.PARENT))
                .build();
    }

    private Student child(long id, String username) {
        return Student.builder()
                .id(id)
                .user(User.builder()
                        .id(id)
                        .username(username)
                        .firstName("Child")
                        .lastName("Petrov")
                        .keycloakId("kc-child-" + id)
                        .roles(Set.of(UserRole.STUDENT))
                        .build())
                .build();
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("связывает пользователя")
        void mapsAllFields() {
            User user = User.builder().id(1L).build();

            Parent result = mapper.toEntity(user, new ParentDetails());

            assertThat(result.getId()).isNull();
            assertThat(result.getUser()).isSameAs(user);
        }

        @Test
        @DisplayName("null user и null details — возвращает null")
        void nullArgs_returnsNull() {
            assertThat(mapper.toEntity(null, null)).isNull();
        }
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("маппит родителя и каждого ребёнка в user-ответы")
        void mapsAllFields() {
            Parent parent = Parent.builder()
                    .user(parentUser())
                    .children(List.of(child(2L, "petr"), child(3L, "masha")))
                    .build();

            ParentResponse result = mapper.toResponse(parent);

            assertThat(result.user()).isEqualTo(UserResponse.builder()
                    .id(1L)
                    .username("mama")
                    .firstName("Elena")
                    .lastName("Petrova")
                    .keycloakId("kc-1")
                    .roles(Set.of(UserRole.PARENT))
                    .build());
            assertThat(result.children()).hasSize(2)
                    .extracting(UserResponse::id)
                    .containsExactlyInAnyOrder(2L, 3L);
            assertThat(result.children())
                    .extracting(UserResponse::username)
                    .containsExactlyInAnyOrder("petr", "masha");
        }

        @Test
        @DisplayName("без детей — children остаётся null")
        void noChildren_returnsNullChildren() {
            Parent parent = Parent.builder().user(parentUser()).build();

            ParentResponse result = mapper.toResponse(parent);

            assertThat(result.children()).isNull();
            assertThat(result.user()).isNotNull();
        }

        @Test
        @DisplayName("null — возвращает null")
        void nullParent_returnsNull() {
            assertThat(mapper.toResponse(null)).isNull();
        }
    }

    @Nested
    @DisplayName("toUserResponse")
    class ToUserResponse {

        @Test
        @DisplayName("маппит поля пользователя студента")
        void mapsAllFields() {
            Student student = child(2L, "petr");

            UserResponse result = mapper.toUserResponse(student);

            assertThat(result.id()).isEqualTo(2L);
            assertThat(result.username()).isEqualTo("petr");
            assertThat(result.firstName()).isEqualTo("Child");
            assertThat(result.lastName()).isEqualTo("Petrov");
            assertThat(result.keycloakId()).isEqualTo("kc-child-2");
            assertThat(result.roles()).containsExactly(UserRole.STUDENT);
        }

        @Test
        @DisplayName("null — возвращает null")
        void nullStudent_returnsNull() {
            assertThat(mapper.toUserResponse((Student) null)).isNull();
        }
    }

    @Nested
    @DisplayName("toParentDetails")
    class ToParentDetails {

        @Test
        @DisplayName("возвращает пустые детали")
        void returnsEmptyDetails() {
            Parent parent = Parent.builder().build();

            ParentDetails result = mapper.toParentDetails(parent);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("null — возвращает null")
        void nullParent_returnsNull() {
            assertThat(mapper.toParentDetails(null)).isNull();
        }
    }

    @Nested
    @DisplayName("toParentInfoResponse")
    class ToParentInfoResponse {

        @Test
        @DisplayName("маппит детей в список user-ответов")
        void mapsAllFields() {
            Parent parent = Parent.builder()
                    .children(List.of(child(2L, "petr"), child(3L, "masha")))
                    .build();

            ParentInfoResponse result = mapper.toParentInfoResponse(parent);

            assertThat(result.children()).hasSize(2)
                    .extracting(UserResponse::id)
                    .containsExactlyInAnyOrder(2L, 3L);
        }

        @Test
        @DisplayName("без детей — children остаётся null")
        void noChildren_returnsNullChildren() {
            Parent parent = Parent.builder().build();

            ParentInfoResponse result = mapper.toParentInfoResponse(parent);

            assertThat(result.children()).isNull();
        }

        @Test
        @DisplayName("null — возвращает null")
        void nullParent_returnsNull() {
            assertThat(mapper.toParentInfoResponse(null)).isNull();
        }
    }
}

package com.rusobr.user.mapper;

import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.common.enums.UserRole;
import com.rusobr.user.application.mapper.UserMapper;
import com.rusobr.user.application.mapper.UserMapperImpl;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.persistence.repository.projection.UserProjection;
import com.rusobr.user.web.dto.user.UserDataDto;
import com.rusobr.user.web.dto.user.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapperImpl();

    @Nested
    @DisplayName("toUser")
    class ToUser {

        @Test
        @DisplayName("маппит поля из UserDataDto и задаёт роли")
        void mapsAllFields() {
            UserDataDto dto = new UserDataDto("ivan", "secret1", "Ivan", "Ivanov");
            Set<UserRole> roles = Set.of(UserRole.TEACHER, UserRole.STUDENT);

            User result = mapper.toUser(dto, roles);

            assertThat(result.getId()).isNull();
            assertThat(result.getUsername()).isEqualTo("ivan");
            assertThat(result.getFirstName()).isEqualTo("Ivan");
            assertThat(result.getLastName()).isEqualTo("Ivanov");
            assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.TEACHER, UserRole.STUDENT);
        }

        @Test
        @DisplayName("пароль не маппится в сущность")
        void passwordIgnored() {
            UserDataDto dto = new UserDataDto("ivan", "secret1", "Ivan", "Ivanov");

            User result = mapper.toUser(dto, Set.of(UserRole.ADMIN));

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("копирует роли в новый набор, не переиспользуя исходный")
        void rolesCopiedToNewSet() {
            UserDataDto dto = new UserDataDto("ivan", "secret1", "Ivan", "Ivanov");
            Set<UserRole> roles = new LinkedHashSet<>(Set.of(UserRole.ADMIN));

            User result = mapper.toUser(dto, roles);

            assertThat(result.getRoles()).isNotSameAs(roles).containsExactly(UserRole.ADMIN);
        }

        @Test
        @DisplayName("null dto и null роли — возвращает null")
        void nullArgs_returnsNull() {
            assertThat(mapper.toUser(null, null)).isNull();
        }

        @Test
        @DisplayName("null роли — остаются пустыми (дефолт билдера)")
        void nullRoles_returnsEmptyRoles() {
            UserDataDto dto = new UserDataDto("ivan", "secret1", "Ivan", "Ivanov");

            User result = mapper.toUser(dto, null);

            assertThat(result.getUsername()).isEqualTo("ivan");
            assertThat(result.getRoles()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toCreateUserResponse")
    class ToCreateUserResponse {

        @Test
        @DisplayName("маппит все поля пользователя")
        void mapsAllFields() {
            User user = User.builder()
                    .id(5L)
                    .username("ivan")
                    .firstName("Ivan")
                    .lastName("Ivanov")
                    .keycloakId("kc-1")
                    .roles(Set.of(UserRole.TEACHER))
                    .build();

            UserResponse result = mapper.toCreateUserResponse(user);

            assertThat(result.id()).isEqualTo(5L);
            assertThat(result.username()).isEqualTo("ivan");
            assertThat(result.firstName()).isEqualTo("Ivan");
            assertThat(result.lastName()).isEqualTo("Ivanov");
            assertThat(result.keycloakId()).isEqualTo("kc-1");
            assertThat(result.roles()).containsExactly(UserRole.TEACHER);
        }

        @Test
        @DisplayName("null — возвращает null")
        void nullUser_returnsNull() {
            assertThat(mapper.toCreateUserResponse(null)).isNull();
        }
    }

    @Nested
    @DisplayName("toUserResponse")
    class ToUserResponse {

        @Test
        @DisplayName("маппит все поля пользователя")
        void mapsAllFields() {
            User user = User.builder()
                    .id(7L)
                    .username("petr")
                    .firstName("Petr")
                    .lastName("Petrov")
                    .keycloakId("kc-2")
                    .roles(Set.of(UserRole.PARENT))
                    .build();

            UserResponse result = mapper.toUserResponse(user);

            assertThat(result.id()).isEqualTo(7L);
            assertThat(result.username()).isEqualTo("petr");
            assertThat(result.firstName()).isEqualTo("Petr");
            assertThat(result.lastName()).isEqualTo("Petrov");
            assertThat(result.keycloakId()).isEqualTo("kc-2");
            assertThat(result.roles()).containsExactly(UserRole.PARENT);
        }

        @Test
        @DisplayName("null — возвращает null")
        void nullUser_returnsNull() {
            assertThat(mapper.toUserResponse(null)).isNull();
        }

        @Test
        @DisplayName("null роли — остаются пустыми")
        void nullRoles_remainEmpty() {
            User user = User.builder().id(1L).build();

            UserResponse result = mapper.toUserResponse(user);

            assertThat(result.roles()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toUserFeignResponse")
    class ToUserFeignResponse {

        @Test
        @DisplayName("маппит все поля проекции")
        void mapsAllFields() {
            UserProjection projection = new TestUserProjection(3L, "Anna", "Smirnova", "anna", "kc-3");

            UserFeignResponse result = mapper.toUserFeignResponse(projection);

            assertThat(result.id()).isEqualTo(3L);
            assertThat(result.firstName()).isEqualTo("Anna");
            assertThat(result.lastName()).isEqualTo("Smirnova");
            assertThat(result.username()).isEqualTo("anna");
            assertThat(result.keycloakId()).isEqualTo("kc-3");
        }

        @Test
        @DisplayName("null — возвращает null")
        void nullProjection_returnsNull() {
            assertThat(mapper.toUserFeignResponse(null)).isNull();
        }
    }

    private record TestUserProjection(Long id, String firstName, String lastName,
                                      String username, String keycloakId) implements UserProjection {
        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getFirstName() {
            return firstName;
        }

        @Override
        public String getLastName() {
            return lastName;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getKeycloakId() {
            return keycloakId;
        }
    }
}

package com.rusobr.user.service;

import com.rusobr.user.application.mapper.UserMapper;
import com.rusobr.user.application.service.user.UserDbService;
import com.rusobr.user.application.service.user.UserService;
import com.rusobr.user.domain.enums.UserRole;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.user.UserCreateRequest;
import com.rusobr.user.web.dto.user.UserDataDto;
import com.rusobr.user.web.dto.user.UserResponse;
import com.rusobr.user.web.dto.user.UserRoleStrategy;
import com.rusobr.user.web.dto.user.update.UserUpdateData;
import com.rusobr.user.web.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDbServiceTest {

    @Mock private UserService userService;
    @Mock private UserMapper userMapper;
    @Mock private UserRepository userRepository;
    @Mock private UserRoleStrategy studentStrategy;
    @Mock private UserRoleStrategy teacherStrategy;

    @InjectMocks private UserDbService service;

    private static final Long USER_ID = 1L;
    private static final String KEYCLOAK_ID = "kc-123";

    @BeforeEach
    void initStrategies() {
        when(studentStrategy.getRole()).thenReturn(UserRole.STUDENT);
        when(teacherStrategy.getRole()).thenReturn(UserRole.TEACHER);

        // Инжектим strategies через поле вручную, т.к. List<UserRoleStrategy> не мокается через @InjectMocks
        service = new UserDbService(userService, List.of(studentStrategy, teacherStrategy), userMapper, userRepository);
        service.init();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("успешно создаёт пользователя и вызывает стратегию")
        void success() {
            StudentDetails details = new StudentDetails("math");
            UserDataDto userDataDto = mock(UserDataDto.class);
            UserCreateRequest<StudentDetails> request = new UserCreateRequest<>(userDataDto, UserRole.STUDENT, details);
            UserResponse userResponse = UserResponse.builder().id(USER_ID).build();

            when(userService.create(userDataDto, UserRole.STUDENT)).thenReturn(userResponse);

            UserResponse result = service.create(request);

            assertThat(result).isEqualTo(userResponse);
            verify(userService).create(userDataDto, UserRole.STUDENT);
            verify(studentStrategy).save(USER_ID, details);
        }

        @Test
        @DisplayName("роль не найдена среди стратегий — бросает ConflictException")
        void unknownRole_throwsConflict() {
            UserDataDto userDataDto = mock(UserDataDto.class);
            UserCreateRequest<StudentDetails> request = new UserCreateRequest<>(
                    userDataDto, UserRole.ADMIN, new StudentDetails("math")
            );
            UserResponse userResponse = UserResponse.builder().id(USER_ID).build();

            when(userService.create(userDataDto, UserRole.ADMIN)).thenReturn(userResponse);

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Invalid user role");

            verify(studentStrategy, never()).save(any(), any());
            verify(teacherStrategy, never()).save(any(), any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("успешно обновляет username, firstName, lastName")
        void success_updatesFields() {
            User user = User.builder().id(USER_ID)
                    .username("old").firstName("Old").lastName("Name")
                    .roles(new HashSet<>(Set.of(UserRole.STUDENT)))
                    .build();

            UserUpdateData newData = UserUpdateData.builder()
                    .username("new").firstName("New").lastName("Updated").build();

            StudentDetails details = new StudentDetails("physics");
            UserResponse expected = UserResponse.builder().id(USER_ID).build();

            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserResponse(user)).thenReturn(expected);

            UserResponse result = service.update(user, newData, Set.of(UserRole.STUDENT), Map.of(UserRole.STUDENT, details));

            assertThat(user.getUsername()).isEqualTo("new");
            assertThat(user.getFirstName()).isEqualTo("New");
            assertThat(user.getLastName()).isEqualTo("Updated");
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("null-поля в UserUpdateData не перезаписывают существующие значения")
        void nullFields_doNotOverwrite() {
            User user = User.builder().id(USER_ID)
                    .username("old").firstName("Old").lastName("Name")
                    .roles(new HashSet<>(Set.of(UserRole.STUDENT)))
                    .build();

            UserUpdateData newData = UserUpdateData.builder().build(); // все null
            StudentDetails details = new StudentDetails("physics");

            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserResponse(user)).thenReturn(UserResponse.builder().id(USER_ID).build());

            service.update(user, newData, Set.of(UserRole.STUDENT), Map.of(UserRole.STUDENT, details));

            assertThat(user.getUsername()).isEqualTo("old");
            assertThat(user.getFirstName()).isEqualTo("Old");
            assertThat(user.getLastName()).isEqualTo("Name");
        }

        @Test
        @DisplayName("роль убрана из новых — вызывает delete у стратегии")
        void removedRole_callsDelete() {
            User user = User.builder().id(USER_ID)
                    .roles(new HashSet<>(Set.of(UserRole.STUDENT, UserRole.TEACHER)))
                    .build();

            UserUpdateData newData = UserUpdateData.builder().build();
            StudentDetails details = new StudentDetails("math");

            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserResponse(user)).thenReturn(UserResponse.builder().id(USER_ID).build());

            // Оставляем только STUDENT, убираем TEACHER
            service.update(user, newData, Set.of(UserRole.STUDENT), Map.of(UserRole.STUDENT, details));

            verify(teacherStrategy).delete(USER_ID);
            verify(studentStrategy, never()).delete(any());
        }

        @Test
        @DisplayName("роль добавлена в новые — вызывает save у стратегии")
        void addedRole_callsSave() {
            User user = User.builder().id(USER_ID)
                    .roles(new HashSet<>(Set.of(UserRole.STUDENT)))
                    .build();

            UserUpdateData newData = UserUpdateData.builder().build();
            StudentDetails studentDetails = new StudentDetails("math");
            StudentDetails teacherDetailsAsStudent = new StudentDetails(null); // просто заглушка

            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserResponse(user)).thenReturn(UserResponse.builder().id(USER_ID).build());

            // Добавляем TEACHER
            service.update(user, newData,
                    Set.of(UserRole.STUDENT, UserRole.TEACHER),
                    Map.of(UserRole.STUDENT, studentDetails, UserRole.TEACHER, teacherDetailsAsStudent));

            verify(teacherStrategy).save(USER_ID, teacherDetailsAsStudent);
            verify(studentStrategy, never()).save(any(), any());
        }

        @Test
        @DisplayName("роль осталась в новых — вызывает update у стратегии")
        void unchangedRole_callsUpdate() {
            User user = User.builder().id(USER_ID)
                    .roles(new HashSet<>(Set.of(UserRole.STUDENT)))
                    .build();

            UserUpdateData newData = UserUpdateData.builder().build();
            StudentDetails details = new StudentDetails("physics");

            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserResponse(user)).thenReturn(UserResponse.builder().id(USER_ID).build());

            service.update(user, newData, Set.of(UserRole.STUDENT), Map.of(UserRole.STUDENT, details));

            verify(studentStrategy).update(USER_ID, details);
        }

        @Test
        @DisplayName("роли пользователя заменяются на новые после обновления")
        void rolesAreReplacedAfterUpdate() {
            User user = User.builder().id(USER_ID)
                    .roles(new HashSet<>(Set.of(UserRole.STUDENT)))
                    .build();

            UserUpdateData newData = UserUpdateData.builder().build();

            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserResponse(user)).thenReturn(UserResponse.builder().id(USER_ID).build());

            service.update(user, newData, Set.of(UserRole.TEACHER),
                    Map.of(UserRole.TEACHER, new StudentDetails(null)));

            assertThat(user.getRoles()).containsExactly(UserRole.TEACHER);
        }
    }
}
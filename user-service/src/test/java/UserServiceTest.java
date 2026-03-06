import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.enums.UserRoles;
import com.rusobr.user.infrastructure.mapper.UserMapper;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.infrastructure.service.UserService;
import com.rusobr.user.infrastructure.webClient.KeycloackRestClient;
import com.rusobr.user.web.dto.keycloack.KeycloackUserRequest;
import com.rusobr.user.web.dto.keycloack.KeycloackUserResponse;
import com.rusobr.user.web.dto.keycloack.role.AssignRoleToUserRequest;
import com.rusobr.user.web.dto.keycloack.role.KeycloackRoleDto;
import com.rusobr.user.web.dto.user.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private KeycloackRestClient keycloackRestClient;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Должен возвращать страницу пользователей")
    void getAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        User user =  new User();
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toUserResponse(user)).thenReturn(new UserResponse("Иван", "Иванов", "key-1", 1L));

        // WHEN
        Page<UserResponse> result = userService.findAll(pageable);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(pageable);
        verify(userMapper).toUserResponse(any());
    }

    @Test
    void shouldReturnUser() {
        User user = User.builder().id(1L).firstName("Татьяна").lastName("Михайловна").keycloackId("abc-123").build();
        UserResponse userResponse = new UserResponse("Татьяна", "Михайловна", "abc-123", 1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user))
                .thenReturn(userResponse);

        UserResponse res = userService.findUserDbById(1L);
        assertEquals("Татьяна", res.firstName());
    }

    @Test
    void shouldCreateUser() {
        KeycloackUserRequest req = new KeycloackUserRequest(
                "AlexK",
                "123456",
                "Алексей",
                "Кочетыгов"
        );

        when(keycloackRestClient.createKeyCloackUser(any()))
                .thenReturn("abc-123");

        User savedUser = User.builder()
                .id(1L)
                .firstName("Алексей")
                .lastName("Кочетыгов")
                .username("AlexK")
                .keycloackId("abc-123")
                .build();

        KeycloackUserResponse userResp = KeycloackUserResponse.builder()
                .id(1L)
                .username("AlexK")
                .firstName("Алексей")
                .lastName("Кочетыгов")
                .keycloackId("abc-123")
                .build();

        when(userRepository.save(any()))
                .thenReturn(savedUser);

        when(userMapper.toUser(any()))
            .thenReturn(savedUser);

        when(userMapper.toKeycloackUserResponse(any()))
                .thenReturn(userResp);

        when(keycloackRestClient.createKeyCloackUser(any()))
                .thenReturn("abc-123");

        KeycloackUserResponse userResponse = userService.createUser(req);

        assertEquals("Алексей", userResponse.firstName());
        verify(keycloackRestClient).createKeyCloackUser(any());
        verify(userRepository).save(any());
    }

    @Test
    void shouldDeleteKeycloakUserIfDatabaseFails() {
        // GIVEN
        KeycloackUserRequest req = new KeycloackUserRequest("AlexK", "123", "Alex", "K");
        String generatedId = "abc-123";

        when(keycloackRestClient.createKeyCloackUser(any())).thenReturn(generatedId);
        when(userMapper.toUser(any())).thenReturn(new User());
        // Имитируем падение базы данных
        when(userRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(req));

        assertEquals("Keycloack could not be created", exception.getMessage());
        // ПРОВЕРКА: вызвался ли метод удаления из Keycloak при ошибке в БД?
        verify(keycloackRestClient).deleteKeyCloackUser(generatedId);
    }

    @Test
    @DisplayName("Должен возвращать список всех ролей из Keycloak")
    void shouldReturnAllRolesFromKeycloak() {
        // GIVEN
        List<KeycloackRoleDto> roles = List.of(new KeycloackRoleDto("role-123", "ADMIN"));
        when(keycloackRestClient.getAllKeycloackRoles()).thenReturn(roles);

        // WHEN
        List<KeycloackRoleDto> result = userService.getAllRoles();

        // THEN
        assertEquals(1, result.size());
        assertEquals("ADMIN", result.get(0).name());
        verify(keycloackRestClient).getAllKeycloackRoles();
    }

    @Test
    void shouldAssignRoleToUser() {
        // GIVEN
        String kId = "user-uuid";
        AssignRoleToUserRequest assignReq = new AssignRoleToUserRequest("user-uuid", UserRoles.ADMIN, "role-123");
        User user = User.builder()
                .keycloackId(kId)
                .roles(new HashSet<>())
                .id(1L)
                .username("AlexK")
                .firstName("Алексей")
                .lastName("Кочетыгов")
                .build();

        doNothing().when(keycloackRestClient).assignRoleToUser(any());
        when(userRepository.findByKeycloackId(kId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        // WHEN
        userService.assignRoleToUser(assignReq);

        System.out.println(user);

        // THEN
        verify(keycloackRestClient).assignRoleToUser(assignReq);
        verify(userRepository).save(user);
        assertTrue(user.getRoles().contains(UserRoles.ADMIN));
    }

    @Test
    @DisplayName("Должен успешно удалять роль у пользователя")
    void shouldDeleteRoleFromUserSuccessfully() {
        // GIVEN
        String kId = "user-uuid";
        AssignRoleToUserRequest req = new AssignRoleToUserRequest(kId, UserRoles.ADMIN, "role-123");

        // Создаем пользователя, у которого уже есть роль ADMIN
        Set<UserRoles> roles = new HashSet<>();
        roles.add(UserRoles.ADMIN);
        User user = User.builder().keycloackId(kId).roles(roles).build();

        when(userRepository.findByKeycloackId(kId)).thenReturn(Optional.of(user));

        // WHEN
        userService.deleteRoleFromUser(req);

        // THEN
        assertFalse(user.getRoles().contains(UserRoles.ADMIN), "Роль должна быть удалена из сета");
        verify(keycloackRestClient).deleteRoleFromUser(req);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Должен выбрасывать исключение, если пользователь не найден при получении по ID")
    void shouldThrowExceptionWhenUserByIdNotFound() {
        // GIVEN
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.findUserDbById(userId));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при удалении роли, если пользователь не найден в БД")
    void shouldThrowExceptionWhenUserNotFoundDuringRoleDeletion() {
        // GIVEN
        AssignRoleToUserRequest req = new AssignRoleToUserRequest("not-exist", UserRoles.ADMIN, "role-123");
        when(userRepository.findByKeycloackId(anyString())).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> userService.deleteRoleFromUser(req));
        // Проверяем, что запрос к Keycloak все равно ушел (согласно логике метода)
        verify(keycloackRestClient).deleteRoleFromUser(req);
    }

    @Test
    void shouldDeleteUserFromBothSystems() {
        // GIVEN
        String kId = "user-uuid";
        User user = User.builder().keycloackId(kId).build();
        when(userRepository.findByKeycloackId(kId)).thenReturn(Optional.of(user));

        // WHEN
        userService.deleteUser(kId);

        // THEN
        verify(keycloackRestClient).deleteKeyCloackUser(kId);
        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByKId() {
        // GIVEN
        String kId = "wrong-id";
        when(userRepository.findByKeycloackId(kId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> userService.deleteUser(kId));
    }

}

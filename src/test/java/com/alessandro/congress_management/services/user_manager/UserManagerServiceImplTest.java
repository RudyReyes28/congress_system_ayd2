package com.alessandro.congress_management.services.user_manager;
import com.alessandro.congress_management.dto.user_manager.*;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.services.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class UserManagerServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserManagerServiceImpl userManagerService;

    private static final String ADMIN_ROLE_NAME = "ADMIN_SYSTEM";

    // ---------------------- CREATE USER BY ADMIN TESTS -------------------

    @Test
    void testCreateUserByAdmin_success() throws DuplicatedEntityException {
        // Arrange
        CreateUserRequest request = createUserRequest();
        UserEntity createdUser = createUser(1L, "johndoe", "PARTICIPANT");

        when(userService.createUser(any(CreateUserCommand.class))).thenReturn(createdUser);

        // Act
        UserResponse response = userManagerService.createUserByAdmin(request);

        // Assert
        assertAll(
                () -> assertNotNull(response, "Response no debe ser null"),
                () -> assertEquals(1L, response.getIdUser(), "ID debe coincidir"),
                () -> assertEquals("johndoe", response.getUsername(), "Username debe coincidir"),
                () -> assertEquals("johndoe@example.com", response.getEmail(), "Email debe coincidir")
        );

        // Verificar que se llamó al servicio con el comando correcto
        verify(userService).createUser(argThat(command ->
                command.username().equals("johndoe") &&
                        command.email().equals("john@example.com") &&
                        command.password().equals("password123") &&
                        command.fullName().equals("John Doe") &&
                        command.phoneNumber().equals("555-1234") &&
                        command.organization().equals("USAC") &&
                        command.identificationNumber().equals("12345678") &&
                        command.roleName().equals("PARTICIPANT")
        ));
    }

    @Test
    void testCreateUserByAdmin_shouldDelegateToUserService() throws DuplicatedEntityException {
        // Arrange
        CreateUserRequest request = createUserRequest();
        UserEntity createdUser = createUser(1L, "johndoe", "PARTICIPANT");

        when(userService.createUser(any(CreateUserCommand.class))).thenReturn(createdUser);

        // Act
        userManagerService.createUserByAdmin(request);

        // Assert - Verificar que DELEGA al UserService
        verify(userService, times(1)).createUser(any(CreateUserCommand.class));
    }

    @Test
    void testCreateUserByAdmin_whenUserServiceThrowsException_shouldPropagateException()
            throws DuplicatedEntityException {
        // Arrange
        CreateUserRequest request = createUserRequest();

        when(userService.createUser(any(CreateUserCommand.class)))
                .thenThrow(new DuplicatedEntityException("Username ya existe"));

        // Act & Assert
        DuplicatedEntityException exception = assertThrows(
                DuplicatedEntityException.class,
                () -> userManagerService.createUserByAdmin(request)
        );

        assertEquals("Username ya existe", exception.getMessage());
        verify(userService).createUser(any(CreateUserCommand.class));
    }

    @Test
    void testCreateUserByAdmin_shouldConvertRequestToCommand() throws DuplicatedEntityException {
        // Arrange
        CreateUserRequest request = createUserRequest();
        UserEntity createdUser = createUser(1L, "johndoe", "PARTICIPANT");

        when(userService.createUser(any(CreateUserCommand.class))).thenReturn(createdUser);

        // Act
        userManagerService.createUserByAdmin(request);

        // Assert - Verificar que los datos del Request se pasan correctamente al Command
        verify(userService).createUser(argThat(command -> {
            return command.username().equals(request.getUsername()) &&
                    command.email().equals(request.getEmail()) &&
                    command.password().equals(request.getPassword()) &&
                    command.fullName().equals(request.getFullName()) &&
                    command.phoneNumber().equals(request.getPhoneNumber()) &&
                    command.organization().equals(request.getOrganization()) &&
                    command.identificationNumber().equals(request.getIdentificationNumber()) &&
                    command.roleName().equals(request.getRoleName());
        }));
    }

    // ---------------------- GET ALL USERS TESTS -------------------

    @Test
    void testGetAllUsers_success() {
        // Arrange
        List<UserEntity> users = Arrays.asList(
                createUser(1L, "user1", "ADMIN_SYSTEM"),
                createUser(2L, "user2", "ADMIN_CONGRESS"),
                createUser(3L, "user3", "PARTICIPANT")
        );

        when(userService.getAllUsers()).thenReturn(users);

        // Act
        List<UserResponse> response = userManagerService.getAllUsers();

        // Assert
        assertAll(
                () -> assertNotNull(response, "Response no debe ser null"),
                () -> assertEquals(3, response.size(), "Debe retornar 3 usuarios"),
                () -> assertEquals("user1", response.get(0).getUsername()),
                () -> assertEquals("user2", response.get(1).getUsername()),
                () -> assertEquals("user3", response.get(2).getUsername())
        );

        verify(userService).getAllUsers();
    }

    @Test
    void testGetAllUsers_shouldDelegateToUserService() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(Arrays.asList());

        // Act
        userManagerService.getAllUsers();

        // Assert - Verificar que DELEGA al UserService
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetAllUsers_whenEmpty_shouldReturnEmptyList() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(Arrays.asList());

        // Act
        List<UserResponse> response = userManagerService.getAllUsers();

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void testGetAllUsers_shouldConvertEntitiesToResponses() {
        // Arrange
        UserEntity user = createUser(1L, "johndoe", "PARTICIPANT");
        user.setEmail("john@example.com");
        user.setFullName("John Doe");

        when(userService.getAllUsers()).thenReturn(Arrays.asList(user));

        // Act
        List<UserResponse> response = userManagerService.getAllUsers();

        // Assert
        UserResponse userResponse = response.get(0);
        assertAll(
                () -> assertEquals(user.getIdUser(), userResponse.getIdUser()),
                () -> assertEquals(user.getUsername(), userResponse.getUsername()),
                () -> assertEquals(user.getEmail(), userResponse.getEmail()),
                () -> assertEquals(user.getFullName(), userResponse.getFullName())
        );
    }

    // ---------------------- SET USER ACTIVE STATUS TESTS -------------------

    @Test
    void testSetUserActiveStatus_deactivateNonAdmin_success()
            throws DuplicatedEntityException, NotFoundException {
        // Arrange
        Long userId = 1L;
        UpdateStatusUser statusUpdate = new UpdateStatusUser(false);
        UserEntity user = createUser(userId, "participant", "PARTICIPANT");

        when(userRepository.countByRole_RoleNameAndIsActive(ADMIN_ROLE_NAME, true)).thenReturn(2);
        doNothing().when(userService).setUserActiveStatus(userId, false);

        // Act
        userManagerService.setUserActiveStatus(userId, statusUpdate);

        // Assert
        verify(userService).setUserActiveStatus(userId, false);
        verify(userRepository).countByRole_RoleNameAndIsActive(ADMIN_ROLE_NAME, true);
    }

    @Test
    void testSetUserActiveStatus_deactivateLastAdmin_shouldThrowException()
            throws NotFoundException {
        // Arrange
        Long userId = 1L;
        UpdateStatusUser statusUpdate = new UpdateStatusUser(false);
        UserEntity adminUser = createUser(userId, "admin", ADMIN_ROLE_NAME);
        adminUser.setIsActive(true);

        // Solo hay 1 admin activo
        when(userRepository.countByRole_RoleNameAndIsActive(ADMIN_ROLE_NAME, true)).thenReturn(1);
        when(userService.getUserById(userId)).thenReturn(adminUser);

        // Act & Assert
        DuplicatedEntityException exception = assertThrows(
                DuplicatedEntityException.class,
                () -> userManagerService.setUserActiveStatus(userId, statusUpdate)
        );

        assertEquals("No se puede eliminar el único administrador activo", exception.getMessage());

        // Verificar que NO se llamó a setUserActiveStatus
        verify(userService, never()).setUserActiveStatus(anyLong(), anyBoolean());
    }

    @Test
    void testSetUserActiveStatus_deactivateOneOfManyAdmins_success()
            throws DuplicatedEntityException, NotFoundException {
        // Arrange
        Long userId = 1L;
        UpdateStatusUser statusUpdate = new UpdateStatusUser(false);
        UserEntity adminUser = createUser(userId, "admin1", ADMIN_ROLE_NAME);

        // Hay 3 admins activos
        when(userRepository.countByRole_RoleNameAndIsActive(ADMIN_ROLE_NAME, true)).thenReturn(3);
        doNothing().when(userService).setUserActiveStatus(userId, false);

        // Act
        userManagerService.setUserActiveStatus(userId, statusUpdate);

        // Assert - Debe permitir desactivar porque hay otros admins
        verify(userService).setUserActiveStatus(userId, false);
    }

    @Test
    void testSetUserActiveStatus_activateUser_success()
            throws DuplicatedEntityException, NotFoundException {
        // Arrange
        Long userId = 1L;
        UpdateStatusUser statusUpdate = new UpdateStatusUser(true);

        doNothing().when(userService).setUserActiveStatus(userId, true);

        // Act
        userManagerService.setUserActiveStatus(userId, statusUpdate);

        // Assert - Activar siempre debe funcionar
        verify(userService).setUserActiveStatus(userId, true);
        // No debe verificar conteo de admins cuando se activa
        verify(userRepository, never()).countByRole_RoleNameAndIsActive(anyString(), anyBoolean());
    }

    @Test
    void testSetUserActiveStatus_deactivateLastAdminButNotActive_success()
            throws DuplicatedEntityException, NotFoundException {
        // Arrange
        Long userId = 1L;
        UpdateStatusUser statusUpdate = new UpdateStatusUser(false);
        UserEntity adminUser = createUser(userId, "admin", ADMIN_ROLE_NAME);
        adminUser.setIsActive(false); // Ya está inactivo

        when(userRepository.countByRole_RoleNameAndIsActive(ADMIN_ROLE_NAME, true)).thenReturn(1);
        when(userService.getUserById(userId)).thenReturn(adminUser);
        doNothing().when(userService).setUserActiveStatus(userId, false);

        // Act
        userManagerService.setUserActiveStatus(userId, statusUpdate);

        // Assert - Debe permitir porque el admin ya está inactivo
        verify(userService).setUserActiveStatus(userId, false);
    }

    @Test
    void testSetUserActiveStatus_whenUserNotFound_shouldPropagateException()
            throws NotFoundException {
        // Arrange
        Long userId = 999L;
        UpdateStatusUser statusUpdate = new UpdateStatusUser(false);

        when(userRepository.countByRole_RoleNameAndIsActive(ADMIN_ROLE_NAME, true)).thenReturn(1);
        when(userService.getUserById(userId)).thenThrow(new NotFoundException("Usuario no encontrado"));

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> userManagerService.setUserActiveStatus(userId, statusUpdate)
        );
    }

    // ---------------------- UPDATE USER BY ADMIN TESTS -------------------

    @Test
    void testUpdateUserByAdmin_success() throws DuplicatedEntityException, NotFoundException {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest updateRequest = createUpdateRequest();
        UserEntity updatedUser = createUser(userId, "newusername", "PARTICIPANT");
        updatedUser.setEmail("newemail@example.com");

        when(userService.updateUser(userId, updateRequest)).thenReturn(updatedUser);

        // Act
        UserResponse response = userManagerService.updateUserByAdmin(userId, updateRequest);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(userId, response.getIdUser()),
                () -> assertEquals("newusername", response.getUsername()),
                () -> assertEquals("newemail@example.com", response.getEmail())
        );

        verify(userService).updateUser(userId, updateRequest);
    }

    @Test
    void testUpdateUserByAdmin_shouldDelegateToUserService()
            throws DuplicatedEntityException, NotFoundException {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest updateRequest = createUpdateRequest();
        UserEntity updatedUser = createUser(userId, "johndoe", "PARTICIPANT");

        when(userService.updateUser(userId, updateRequest)).thenReturn(updatedUser);

        // Act
        userManagerService.updateUserByAdmin(userId, updateRequest);

        // Assert - Verificar que DELEGA al UserService
        verify(userService, times(1)).updateUser(userId, updateRequest);
    }

    @Test
    void testUpdateUserByAdmin_whenUserNotFound_shouldPropagateException()
            throws DuplicatedEntityException, NotFoundException {
        // Arrange
        Long userId = 999L;
        UpdateUserRequest updateRequest = createUpdateRequest();

        when(userService.updateUser(userId, updateRequest))
                .thenThrow(new NotFoundException("Usuario no encontrado"));

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> userManagerService.updateUserByAdmin(userId, updateRequest)
        );
    }

    @Test
    void testUpdateUserByAdmin_whenDuplicatedData_shouldPropagateException()
            throws DuplicatedEntityException, NotFoundException {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest updateRequest = createUpdateRequest();

        when(userService.updateUser(userId, updateRequest))
                .thenThrow(new DuplicatedEntityException("Email ya existe"));

        // Act & Assert
        assertThrows(
                DuplicatedEntityException.class,
                () -> userManagerService.updateUserByAdmin(userId, updateRequest)
        );
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Test
    void testChangeUserPasswordByAdmin_success() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        UpdateUserPassword passwordUpdate = new UpdateUserPassword("newPassword123");

        doNothing().when(userService).changeUserPassword(userId, "newPassword123");

        // Act
        userManagerService.changeUserPasswordByAdmin(userId, passwordUpdate);

        // Assert
        verify(userService).changeUserPassword(userId, "newPassword123");
    }

    @Test
    void testChangeUserPasswordByAdmin_shouldDelegateToUserService() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        UpdateUserPassword passwordUpdate = new UpdateUserPassword("newPassword123");

        doNothing().when(userService).changeUserPassword(anyLong(), anyString());

        // Act
        userManagerService.changeUserPasswordByAdmin(userId, passwordUpdate);

        // Assert - Verificar que DELEGA al UserService
        verify(userService, times(1)).changeUserPassword(userId, "newPassword123");
    }

    @Test
    void testChangeUserPasswordByAdmin_whenUserNotFound_shouldPropagateException()
            throws NotFoundException {
        // Arrange
        Long userId = 999L;
        UpdateUserPassword passwordUpdate = new UpdateUserPassword("newPassword123");

        doThrow(new NotFoundException("Usuario no encontrado"))
                .when(userService).changeUserPassword(userId, "newPassword123");

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> userManagerService.changeUserPasswordByAdmin(userId, passwordUpdate)
        );
    }

    @Test
    void testChangeUserPasswordByAdmin_shouldPassCorrectPassword() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        String newPassword = "superSecurePassword456";
        UpdateUserPassword passwordUpdate = new UpdateUserPassword(newPassword);

        doNothing().when(userService).changeUserPassword(anyLong(), anyString());

        // Act
        userManagerService.changeUserPasswordByAdmin(userId, passwordUpdate);

        // Assert - Verificar que se pasa el password correcto
        verify(userService).changeUserPassword(eq(userId), eq(newPassword));
    }

    // ------------------------ Helper Methods -------------------

    private CreateUserRequest createUserRequest() {
        CreateUserRequest request = new CreateUserRequest(
                "john@example.com",
                "John Doe",
                "555-1234",
                "USAC",
                "johndoe",
                "password123",
                "12345678",
                "PARTICIPANT"
        );
        return request;
    }

    private UpdateUserRequest createUpdateRequest() {
        return new UpdateUserRequest(
                "newusername",
                "newemail@example.com",
                "Jane Doe",
                "555-5678",
                "UMG",
                "87654321"
        );
    }

    private UserEntity createUser(Long id, String username, String roleName) {
        UserEntity user = new UserEntity();
        user.setIdUser(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("$2a$10$hashedPassword");
        user.setFullName("Test User");
        user.setIdentificationNumber("12345678");
        user.setPhoneNumber("555-1234");
        user.setOrganization("USAC");
        user.setIsActive(true);

        RoleEntity role = new RoleEntity();
        role.setIdRole(1);
        role.setRoleName(roleName);
        role.setDescription("Test role");
        user.setRole(role);

        return user;
    }
}

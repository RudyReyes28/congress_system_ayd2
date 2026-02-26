package com.alessandro.congress_management.services.user;

import com.alessandro.congress_management.dto.user_manager.CreateUserCommand;
import com.alessandro.congress_management.dto.user_manager.UpdateUserRequest;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.repositories.authenticate.RoleRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    // ---------------------- CREATE USER TESTS --------------------

    @Test
    void testCreateUser_success() throws DuplicatedEntityException {
        // Arrange
        CreateUserCommand command = createUserCommand();
        RoleEntity role = createRole("PARTICIPANT");
        String hashedPassword = "$2a$10$hashedPassword";

        when(roleRepository.findByRoleName("PARTICIPANT")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn(hashedPassword);
        when(userRepository.existsByUsernameAndIdUserNot("johndoe", null)).thenReturn(false);
        when(userRepository.existsByEmailAndIdUserNot("john@example.com", null)).thenReturn(false);
        when(userRepository.existsByIdentificationNumberAndIdUserNot("12345678", null)).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setIdUser(1L);
            return user;
        });

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        // Act
        UserEntity result = userService.createUser(command);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        UserEntity capturedUser = userCaptor.getValue();

        assertAll(
                () -> assertNotNull(result, "El usuario creado no debe ser null"),
                () -> assertEquals("johndoe", capturedUser.getUsername(), "Username debe coincidir"),
                () -> assertEquals("john@example.com", capturedUser.getEmail(), "Email debe coincidir"),
                () -> assertEquals(hashedPassword, capturedUser.getPassword(), "Password debe estar hasheado"),
                () -> assertEquals("John Doe", capturedUser.getFullName(), "Full name debe coincidir"),
                () -> assertEquals("12345678", capturedUser.getIdentificationNumber(), "ID debe coincidir"),
                () -> assertEquals("555-1234", capturedUser.getPhoneNumber(), "Teléfono debe coincidir"),
                () -> assertEquals("USAC", capturedUser.getOrganization(), "Organización debe coincidir"),
                () -> assertEquals(role, capturedUser.getRole(), "Rol debe coincidir"),
                () -> assertTrue(capturedUser.getIsActive(), "Usuario debe estar activo por defecto")
        );

        verify(passwordEncoder).encode("password123");
        verify(roleRepository).findByRoleName("PARTICIPANT");
    }

    @Test
    void testCreateUser_whenUsernameExists_shouldThrowException() {
        // Arrange
        CreateUserCommand command = createUserCommand();

        when(userRepository.existsByUsernameAndIdUserNot("johndoe", null)).thenReturn(true);

        // Act & Assert
        DuplicatedEntityException exception = assertThrows(
                DuplicatedEntityException.class,
                () -> userService.createUser(command)
        );

        assertEquals("El username ya está en uso", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUser_whenEmailExists_shouldThrowException() {
        // Arrange
        CreateUserCommand command = createUserCommand();

        when(userRepository.existsByUsernameAndIdUserNot("johndoe", null)).thenReturn(false);
        when(userRepository.existsByEmailAndIdUserNot("john@example.com", null)).thenReturn(true);

        // Act & Assert
        DuplicatedEntityException exception = assertThrows(
                DuplicatedEntityException.class,
                () -> userService.createUser(command)
        );

        assertEquals("El email ya está registrado", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUser_whenIdentificationNumberExists_shouldThrowException() {
        // Arrange
        CreateUserCommand command = createUserCommand();

        when(userRepository.existsByUsernameAndIdUserNot("johndoe", null)).thenReturn(false);
        when(userRepository.existsByEmailAndIdUserNot("john@example.com", null)).thenReturn(false);
        when(userRepository.existsByIdentificationNumberAndIdUserNot("12345678", null)).thenReturn(true);

        // Act & Assert
        DuplicatedEntityException exception = assertThrows(
                DuplicatedEntityException.class,
                () -> userService.createUser(command)
        );

        assertEquals("El número de identificación ya está registrado", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUser_whenRoleNotFound_shouldThrowException() {
        // Arrange
        CreateUserCommand command = createUserCommand();

        when(userRepository.existsByUsernameAndIdUserNot(anyString(), any())).thenReturn(false);
        when(userRepository.existsByEmailAndIdUserNot(anyString(), any())).thenReturn(false);
        when(userRepository.existsByIdentificationNumberAndIdUserNot(anyString(), any())).thenReturn(false);
        when(roleRepository.findByRoleName("PARTICIPANT")).thenReturn(Optional.empty());

        // Act & Assert
        DuplicatedEntityException exception = assertThrows(
                DuplicatedEntityException.class,
                () -> userService.createUser(command)
        );

        assertEquals("El rol especificado no existe", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUser_shouldHashPassword() throws DuplicatedEntityException {
        // Arrange
        CreateUserCommand command = createUserCommand();
        RoleEntity role = createRole("PARTICIPANT");
        String rawPassword = "password123";
        String hashedPassword = "$2a$10$hashedPassword";

        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));
        when(userRepository.existsByUsernameAndIdUserNot(anyString(), any())).thenReturn(false);
        when(userRepository.existsByEmailAndIdUserNot(anyString(), any())).thenReturn(false);
        when(userRepository.existsByIdentificationNumberAndIdUserNot(anyString(), any())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        // Act
        userService.createUser(command);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        UserEntity capturedUser = userCaptor.getValue();

        assertAll(
                () -> verify(passwordEncoder).encode(rawPassword),
                () -> assertEquals(hashedPassword, capturedUser.getPassword()),
                () -> assertNotEquals(rawPassword, capturedUser.getPassword(),
                        "Password no debe estar en texto plano")
        );
    }

    // ---------------------- GET USER BY ID TESTS --------------------

    @Test
    void testGetUserById_whenUserExists_shouldReturnUser() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        UserEntity user = createUser(userId, "johndoe");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserEntity result = userService.getUserById(userId);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(userId, result.getIdUser()),
                () -> assertEquals("johndoe", result.getUsername())
        );

        verify(userRepository).findById(userId);
    }

    @Test
    void testGetUserById_whenUserNotFound_shouldThrowException() {
        // Arrange
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUserById(userId)
        );

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        assertTrue(exception.getMessage().contains("999"));
    }

    // ---------------------- GET ALL USERS TESTS --------------------

    @Test
    void testGetAllUsers_shouldReturnAllUsers() {
        // Arrange
        List<UserEntity> users = Arrays.asList(
                createUser(1L, "user1"),
                createUser(2L, "user2"),
                createUser(3L, "user3")
        );

        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserEntity> result = userService.getAllUsers();

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(3, result.size()),
                () -> assertEquals("user1", result.get(0).getUsername()),
                () -> assertEquals("user2", result.get(1).getUsername()),
                () -> assertEquals("user3", result.get(2).getUsername())
        );

        verify(userRepository).findAll();
    }

    @Test
    void testGetAllUsers_whenNoUsers_shouldReturnEmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<UserEntity> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAll();
    }

    //---------------------- SET USER ACTIVE STATUS TESTS --------------------

    @Test
    void testSetUserActiveStatus_shouldActivateUser() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        UserEntity user = createUser(userId, "johndoe");
        user.setIsActive(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        // Act
        userService.setUserActiveStatus(userId, true);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        UserEntity capturedUser = userCaptor.getValue();

        assertTrue(capturedUser.getIsActive(), "Usuario debe estar activo");
    }

    @Test
    void testSetUserActiveStatus_shouldDeactivateUser() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        UserEntity user = createUser(userId, "johndoe");
        user.setIsActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        // Act
        userService.setUserActiveStatus(userId, false);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        UserEntity capturedUser = userCaptor.getValue();

        assertFalse(capturedUser.getIsActive(), "Usuario debe estar inactivo");
    }

    @Test
    void testSetUserActiveStatus_whenUserNotFound_shouldThrowException() {
        // Arrange
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> userService.setUserActiveStatus(userId, true)
        );

        verify(userRepository, never()).save(any());
    }

    // ---------------------- UPDATE USER TESTS --------------------

    @Test
    void testUpdateUser_success() throws NotFoundException, DuplicatedEntityException {
        // Arrange
        Long userId = 1L;
        UserEntity existingUser = createUser(userId, "oldusername");
        UpdateUserRequest updateRequest = createUpdateRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsernameAndIdUserNot("newusername", userId)).thenReturn(false);
        when(userRepository.existsByEmailAndIdUserNot("newemail@example.com", userId)).thenReturn(false);
        when(userRepository.existsByIdentificationNumberAndIdUserNot("87654321", userId)).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        // Act
        UserEntity result = userService.updateUser(userId, updateRequest);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        UserEntity capturedUser = userCaptor.getValue();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("newusername", capturedUser.getUsername()),
                () -> assertEquals("newemail@example.com", capturedUser.getEmail()),
                () -> assertEquals("Jane Doe", capturedUser.getFullName()),
                () -> assertEquals("87654321", capturedUser.getIdentificationNumber()),
                () -> assertEquals("555-5678", capturedUser.getPhoneNumber()),
                () -> assertEquals("UMG", capturedUser.getOrganization())
        );
    }

    @Test
    void testUpdateUser_whenUserNotFound_shouldThrowException() {
        // Arrange
        Long userId = 999L;
        UpdateUserRequest updateRequest = createUpdateRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(userId, updateRequest)
        );

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUser_whenNewUsernameExists_shouldThrowException() {
        // Arrange
        Long userId = 1L;
        UserEntity existingUser = createUser(userId, "oldusername");
        UpdateUserRequest updateRequest = createUpdateRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsernameAndIdUserNot("newusername", userId)).thenReturn(true);

        // Act & Assert
        DuplicatedEntityException exception = assertThrows(
                DuplicatedEntityException.class,
                () -> userService.updateUser(userId, updateRequest)
        );

        assertEquals("El username ya está en uso", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUser_whenNewEmailExists_shouldThrowException() {
        // Arrange
        Long userId = 1L;
        UserEntity existingUser = createUser(userId, "oldusername");
        UpdateUserRequest updateRequest = createUpdateRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsernameAndIdUserNot(anyString(), eq(userId))).thenReturn(false);
        when(userRepository.existsByEmailAndIdUserNot("newemail@example.com", userId)).thenReturn(true);

        // Act & Assert
        DuplicatedEntityException exception = assertThrows(
                DuplicatedEntityException.class,
                () -> userService.updateUser(userId, updateRequest)
        );

        assertEquals("El email ya está registrado", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUser_shouldNotChangePassword() throws NotFoundException, DuplicatedEntityException {
        // Arrange
        Long userId = 1L;
        UserEntity existingUser = createUser(userId, "johndoe");
        String originalPassword = existingUser.getPassword();
        UpdateUserRequest updateRequest = createUpdateRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsernameAndIdUserNot(anyString(), eq(userId))).thenReturn(false);
        when(userRepository.existsByEmailAndIdUserNot(anyString(), eq(userId))).thenReturn(false);
        when(userRepository.existsByIdentificationNumberAndIdUserNot(anyString(), eq(userId))).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        // Act
        userService.updateUser(userId, updateRequest);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        UserEntity capturedUser = userCaptor.getValue();

        assertEquals(originalPassword, capturedUser.getPassword(),
                "El password no debe cambiar en un update normal");
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ---------------------- CHANGE USER PASSWORD TESTS --------------------

    @Test
    void testChangeUserPassword_success() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        String newPassword = "newPassword456";
        String hashedPassword = "$2a$10$newHashedPassword";
        UserEntity user = createUser(userId, "johndoe");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        // Act
        userService.changeUserPassword(userId, newPassword);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        UserEntity capturedUser = userCaptor.getValue();

        assertAll(
                () -> verify(passwordEncoder).encode(newPassword),
                () -> assertEquals(hashedPassword, capturedUser.getPassword()),
                () -> assertNotEquals(newPassword, capturedUser.getPassword(),
                        "Password debe estar hasheado")
        );
    }

    @Test
    void testChangeUserPassword_whenUserNotFound_shouldThrowException() {
        // Arrange
        Long userId = 999L;
        String newPassword = "newPassword456";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> userService.changeUserPassword(userId, newPassword)
        );

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testChangeUserPassword_shouldHashNewPassword() throws NotFoundException {
        // Arrange
        Long userId = 1L;
        String rawPassword = "plainTextPassword";
        String hashedPassword = "$2a$10$hashedVersion";
        UserEntity user = createUser(userId, "johndoe");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        // Act
        userService.changeUserPassword(userId, rawPassword);

        // Assert
        verify(userRepository).save(userCaptor.capture());

        assertEquals(hashedPassword, userCaptor.getValue().getPassword());
        assertNotEquals(rawPassword, userCaptor.getValue().getPassword());
    }

    //-------------- TESTS FOR FIND ACTIVE USERS --------------------
    @Test
    void testFindActiveUsers_shouldReturnOnlyActiveUsers() {
        //Arrange
        UserEntity activeUser1 = createUser(1L, "activeUser1");
        activeUser1.setIsActive(true);
        UserEntity activeUser2 = createUser(2L, "activeUser2");
        activeUser2.setIsActive(true);
        UserEntity inactiveUser = createUser(3L, "inactiveUser");
        inactiveUser.setIsActive(false);

        when(userRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(activeUser1, activeUser2));

        //Act
        List<UserEntity> result = userService.findActiveUsers();
        //Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.stream().anyMatch(u -> u.getUsername().equals("activeUser1")), "Debe contener activeUser1"),
                () -> assertTrue(result.stream().anyMatch(u -> u.getUsername().equals("activeUser2")), "Debe contener activeUser2")
        );
        verify(userRepository).findByIsActiveTrue();
    }

    @Test
    void testFindActiveUsers_whenNoActiveUsers_shouldReturnEmptyList() {
        //Arrange
        when(userRepository.findByIsActiveTrue()).thenReturn(Arrays.asList());

        //Act
        List<UserEntity> result = userService.findActiveUsers();

        //Assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "La lista de usuarios activos debe estar vacía");
        verify(userRepository).findByIsActiveTrue();
    }

    // ---------------------- HELPER METHODS --------------------

    private CreateUserCommand createUserCommand() {
        return new CreateUserCommand(
                "johndoe",
                "john@example.com",
                "password123",
                "John Doe",
                "555-1234",
                "USAC",
                "12345678",
                "PARTICIPANT"
        );
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

    private UserEntity createUser(Long id, String username) {
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
        user.setRole(createRole("PARTICIPANT"));
        return user;
    }

    private RoleEntity createRole(String roleName) {
        RoleEntity role = new RoleEntity();
        role.setIdRole(1);
        role.setRoleName(roleName);
        role.setDescription("Test role");
        return role;
    }
}
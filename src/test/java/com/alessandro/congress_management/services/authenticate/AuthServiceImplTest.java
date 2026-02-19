package com.alessandro.congress_management.services.authenticate;

import com.alessandro.congress_management.dto.authenticate.AuthResponse;
import com.alessandro.congress_management.dto.authenticate.LoginRequest;
import com.alessandro.congress_management.dto.authenticate.RefreshTokenRequest;
import com.alessandro.congress_management.dto.authenticate.RegisterRequest;
import com.alessandro.congress_management.dto.user_manager.CreateUserCommand;
import com.alessandro.congress_management.dto.user_manager.CreateUserRequest;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.InvalidCredentialsException;
import com.alessandro.congress_management.exceptions.InvalidTokenException;
import com.alessandro.congress_management.models.authentication_and_users.RefreshTokenEntity;
import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.repositories.authenticate.RoleRepository;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.security.JwtTokenProvider;
import com.alessandro.congress_management.services.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FULL_NAME = "Test User";
    private static final String TEST_ID_NUMBER = "12345678";
    private static final String TEST_PHONE = "1234567890";
    private static final String TEST_ORG = "Test Organization";
    private static final String TEST_HASHED_PASSWORD = "$2a$10$hashedpassword";
    private static final String TEST_ACCESS_TOKEN = "access.token.here";
    private static final String TEST_REFRESH_TOKEN = "refresh-token-uuid";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    //----------------- Register Tests -----------------
    @Test
    void testRegister_success() throws DuplicatedEntityException {
        // Arrange
        RegisterRequest request = createRegisterRequest();
        UserEntity createdUser = createTestUser();

        when(userService.createUser(any(CreateUserCommand.class)))
                .thenReturn(createdUser);

        when(jwtTokenProvider.generateToken(TEST_USERNAME, 1L))
                .thenReturn(TEST_ACCESS_TOKEN);

        when(jwtTokenProvider.getExpirationMs())
                .thenReturn(3600000L);

        when(refreshTokenService.createRefreshToken(createdUser))
                .thenReturn(createRefreshToken());

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(TEST_ACCESS_TOKEN, response.getAccessToken()),
                () -> assertEquals(TEST_REFRESH_TOKEN, response.getRefreshToken()),
                () -> assertEquals("Bearer", response.getTokenType()),
                () -> assertEquals(TEST_USERNAME, response.getUser().getUsername())
        );

        // Verificar que construyó bien el command
        verify(userService).createUser(argThat(command ->
                command.username().equals(TEST_USERNAME) &&
                        command.email().equals(TEST_EMAIL) &&
                        command.password().equals(TEST_PASSWORD) &&
                        command.roleName().equals("PARTICIPANT")
        ));

        verify(jwtTokenProvider).generateToken(TEST_USERNAME, 1L);
        verify(refreshTokenService).createRefreshToken(createdUser);
    }

    @Test
    void testRegister_whenUserServiceThrows_shouldPropagateException()
            throws DuplicatedEntityException {

        RegisterRequest request = createRegisterRequest();

        when(userService.createUser(any(CreateUserCommand.class)))
                .thenThrow(new DuplicatedEntityException("Username ya existe"));

        assertThrows(
                DuplicatedEntityException.class,
                () -> authService.register(request)
        );

        verify(userService).createUser(any(CreateUserCommand.class));
    }

    // ----------------- Login Tests -----------------

    @Test
    void testLogin_success() throws InvalidCredentialsException {
        // Arrange
        LoginRequest request = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        UserEntity user = createTestUser();

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_HASHED_PASSWORD)).thenReturn(true);
        when(jwtTokenProvider.generateToken(TEST_USERNAME, 1L)).thenReturn(TEST_ACCESS_TOKEN);
        when(jwtTokenProvider.getExpirationMs()).thenReturn(3600000L);
        when(refreshTokenService.createRefreshToken(user)).thenReturn(createRefreshToken());

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(TEST_ACCESS_TOKEN, response.getAccessToken()),
                () -> assertEquals(TEST_REFRESH_TOKEN, response.getRefreshToken()),
                () -> assertEquals("Bearer", response.getTokenType()),
                () -> assertNotNull(response.getUser()),
                () -> assertEquals(TEST_USERNAME, response.getUser().getUsername())
        );

        verify(passwordEncoder).matches(TEST_PASSWORD, TEST_HASHED_PASSWORD);
        verify(jwtTokenProvider).generateToken(TEST_USERNAME, 1L);
        verify(refreshTokenService).createRefreshToken(user);
    }

    @Test
    void testLogin_whenUserNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLogin_whenUserInactive() {
        // Arrange
        LoginRequest request = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        UserEntity inactiveUser = createTestUser();
        inactiveUser.setIsActive(false);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(inactiveUser));

        // Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Usuario inactivo", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLogin_whenPasswordIncorrect() {
        // Arrange
        LoginRequest request = new LoginRequest(TEST_USERNAME, "wrongpassword");
        UserEntity user = createTestUser();

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", TEST_HASHED_PASSWORD)).thenReturn(false);

        // Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong());
    }

    // ------------------- Refresh Token Tests -----------------

    @Test
    void testRefreshToken_success() throws InvalidTokenException {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest(TEST_REFRESH_TOKEN);
        UserEntity user = createTestUser();
        RefreshTokenEntity refreshToken = createRefreshToken();
        refreshToken.setUser(user);

        when(refreshTokenService.validateRefreshToken(TEST_REFRESH_TOKEN))
                .thenReturn(refreshToken);
        when(jwtTokenProvider.generateToken(TEST_USERNAME, 1L)).thenReturn(TEST_ACCESS_TOKEN);
        when(jwtTokenProvider.getExpirationMs()).thenReturn(3600000L);

        // Act
        AuthResponse response = authService.refreshToken(request);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(TEST_ACCESS_TOKEN, response.getAccessToken()),
                () -> assertEquals(TEST_REFRESH_TOKEN, response.getRefreshToken()),
                () -> assertNotNull(response.getUser()),
                () -> assertEquals(TEST_USERNAME, response.getUser().getUsername())
        );

        verify(refreshTokenService).validateRefreshToken(TEST_REFRESH_TOKEN);
        verify(jwtTokenProvider).generateToken(TEST_USERNAME, 1L);
    }

    @Test
    void testRefreshToken_whenTokenInvalid() throws InvalidTokenException {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");

        when(refreshTokenService.validateRefreshToken("invalid-token"))
                .thenThrow(new InvalidTokenException("Token inválido"));

        // Assert
        assertThrows(InvalidTokenException.class,
                () -> authService.refreshToken(request));

        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong());
    }

    @Test
    void testRefreshToken_whenUserInactive() throws InvalidTokenException {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest(TEST_REFRESH_TOKEN);
        UserEntity inactiveUser = createTestUser();
        inactiveUser.setIsActive(false);
        RefreshTokenEntity refreshToken = createRefreshToken();
        refreshToken.setUser(inactiveUser);

        when(refreshTokenService.validateRefreshToken(TEST_REFRESH_TOKEN))
                .thenReturn(refreshToken);

        // Assert
        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> authService.refreshToken(request)
        );

        assertEquals("Usuario inactivo", exception.getMessage());
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong());
    }

    // -------------------- Logout Tests -----------------

    @Test
    void testLogout_success() {
        // Arrange
        UserEntity user = createTestUser();

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // Act
        authService.logout(TEST_USERNAME);

        // Assert
        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(refreshTokenService).deleteByUser(user);
    }

    @Test
    void testLogout_whenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // Act
        authService.logout(TEST_USERNAME);

        // Assert
        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(refreshTokenService, never()).deleteByUser(any());
    }

    // -------------------- Helper Methods -----------------

    private RegisterRequest createRegisterRequest() {
        return new RegisterRequest(
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_EMAIL,
                TEST_FULL_NAME,
                TEST_ID_NUMBER,
                TEST_PHONE,
                TEST_ORG,
                null
        );
    }

    private UserEntity createTestUser() {
        UserEntity user = new UserEntity();
        user.setIdUser(1L);
        user.setRole(createRoleEntity());
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_HASHED_PASSWORD);
        user.setEmail(TEST_EMAIL);
        user.setFullName(TEST_FULL_NAME);
        user.setIdentificationNumber(TEST_ID_NUMBER);
        user.setPhoneNumber(TEST_PHONE);
        user.setOrganization(TEST_ORG);
        user.setIsActive(true);
        return user;
    }

    private RefreshTokenEntity createRefreshToken() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setIdToken(1L);
        token.setToken(TEST_REFRESH_TOKEN);
        token.setExpiryDate(LocalDateTime.now().plusDays(7));
        return token;
    }

    private RoleEntity createRoleEntity() {
        RoleEntity role = new RoleEntity();
        role.setIdRole(1);
        role.setRoleName("PARTICIPANT");
        return role;
    }

}

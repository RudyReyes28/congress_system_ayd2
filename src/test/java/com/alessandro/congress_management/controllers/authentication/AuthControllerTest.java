package com.alessandro.congress_management.controllers.authentication;

import com.alessandro.congress_management.dto.authenticate.*;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.InvalidCredentialsException;
import com.alessandro.congress_management.exceptions.InvalidTokenException;
import com.alessandro.congress_management.services.authenticate.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // ----------------------------------- REGISTER TESTS ------------------------------

    @Test
    void testRegister_success() throws Exception {
        // Arrange
        RegisterRequest request = createRegisterRequest();
        AuthResponse response = createAuthResponse();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void testRegister_whenUsernameExists() throws Exception {
        // Arrange
        RegisterRequest request = createRegisterRequest();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicatedEntityException("El username ya está en uso"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El username ya está en uso"))
                .andExpect(jsonPath("$.status").value(409));

        verify(authService).register(any(RegisterRequest.class));
    }



    // -------------------------------------- LOGIN TESTS ------------------------------

    @Test
    void testLogin_success() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password123");
        AuthResponse response = createAuthResponse();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_whenInvalidCredentials() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Credenciales inválidas"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"))
                .andExpect(jsonPath("$.status").value(401));

        verify(authService).login(any(LoginRequest.class));
    }



    // -------------------------------------- REFRESH TOKEN TESTS ------------------------------

    @Test
    void testRefreshToken_success() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        AuthResponse response = createAuthResponse();

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    void testRefreshToken_whenTokenInvalid() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");

        when(authService.refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new InvalidTokenException("Token inválido"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token inválido"))
                .andExpect(jsonPath("$.status").value(401));

        verify(authService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    void testRefreshToken_whenTokenExpired() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("expired-token");

        when(authService.refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new InvalidTokenException("Refresh token expirado"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token expirado"));

        verify(authService).refreshToken(any(RefreshTokenRequest.class));
    }

    // -------------------------------------- LOGOUT TESTS ------------------------------

    @Test
    void testLogout_success() throws Exception {
        // Arrange
        String username = "testuser";

        doNothing().when(authService).logout(username);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/logout")
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sesión cerrada exitosamente"));

        verify(authService).logout(username);
    }

    // -------------------------------- HELPER METHODS ------------------------------

    private RegisterRequest createRegisterRequest() {
        return new RegisterRequest(
                "testuser",
                "password123",
                "test@example.com",
                "Test User",
                "12345678",
                "1234567890",
                "Test Organization",
                null
        );
    }

    private AuthResponse createAuthResponse() {
        UserInfoResponse userInfo = new UserInfoResponse(
                1L,
                "testuser",
                "test@example.com",
                "Test User",
                "12345678",
                "1234567890",
                "Test Organization",
                null,
                BigDecimal.ZERO,
                1,
                "PARTICIPANT"

        );

        return new AuthResponse(
                "access-token",
                "refresh-token",
                3600L,
                userInfo
        );
    }
}

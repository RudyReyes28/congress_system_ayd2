package com.alessandro.congress_management.controllers.user_manager;


import com.alessandro.congress_management.dto.user_manager.*;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.RoleEntity;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.security.JwtAuthenticationFilter;
import com.alessandro.congress_management.security.JwtTokenProvider;
import com.alessandro.congress_management.services.user_manager.UserManagerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserManagerController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserManagerService userManagerService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;


    // ---------------------- CREATE USER TESTS -------------------

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testCreateUser_success() throws Exception {
        // Arrange
        CreateUserRequest request = createUserRequest();
        UserResponse response = createUserResponse(1L, "johndoe", "PARTICIPANT");

        when(userManagerService.createUserByAdmin(any(CreateUserRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUser").value(1))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("johndoe@example.com"))
                .andExpect(jsonPath("$.roleName").value("PARTICIPANT"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userManagerService).createUserByAdmin(any(CreateUserRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testCreateUser_whenDuplicatedUsername_shouldReturn409() throws Exception {
        // Arrange
        CreateUserRequest request = createUserRequest();

        when(userManagerService.createUserByAdmin(any(CreateUserRequest.class)))
                .thenThrow(new DuplicatedEntityException("El username ya está en uso"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(userManagerService).createUserByAdmin(any(CreateUserRequest.class));
    }


    // ---------------------- GET ALL USERS TESTS -------------------

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testGetAllUsers_success() throws Exception {
        // Arrange
        List<UserResponse> users = Arrays.asList(
                createUserResponse(1L, "user1", "ADMIN_SYSTEM"),
                createUserResponse(2L, "user2", "ADMIN_CONGRESS"),
                createUserResponse(3L, "user3", "PARTICIPANT")
        );

        when(userManagerService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"))
                .andExpect(jsonPath("$[2].username").value("user3"));

        verify(userManagerService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testGetAllUsers_whenEmpty_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(userManagerService.getAllUsers()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    // ---------------------- UPDATE USER STATUS TESTS -------------------

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateUserStatus_deactivate_success() throws Exception {
        // Arrange
        Long userId = 1L;
        UpdateStatusUser statusUpdate = new UpdateStatusUser(false);

        doNothing().when(userManagerService).setUserActiveStatus(eq(userId), any(UpdateStatusUser.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{idUser}/activation", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isAccepted());

        verify(userManagerService).setUserActiveStatus(eq(userId), any(UpdateStatusUser.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateUserStatus_activate_success() throws Exception {
        // Arrange
        Long userId = 1L;
        UpdateStatusUser statusUpdate = new UpdateStatusUser(true);

        doNothing().when(userManagerService).setUserActiveStatus(eq(userId), any(UpdateStatusUser.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{idUser}/activation", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isAccepted());

        verify(userManagerService).setUserActiveStatus(eq(userId), any(UpdateStatusUser.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateUserStatus_whenUserNotFound_shouldReturn404() throws Exception {
        // Arrange
        Long userId = 999L;
        UpdateStatusUser statusUpdate = new UpdateStatusUser(false);

        doThrow(new NotFoundException("Usuario no encontrado"))
                .when(userManagerService).setUserActiveStatus(eq(userId), any(UpdateStatusUser.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{idUser}/activation", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateUserStatus_whenLastAdmin_shouldReturn409() throws Exception {
        // Arrange
        Long userId = 1L;
        UpdateStatusUser statusUpdate = new UpdateStatusUser(false);

        doThrow(new DuplicatedEntityException("No se puede eliminar el único administrador activo"))
                .when(userManagerService).setUserActiveStatus(eq(userId), any(UpdateStatusUser.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{idUser}/activation", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isConflict());
    }

    // ---------------------- UPDATE USER DETAILS TESTS -------------------
    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateUserById_success() throws Exception {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest updateRequest = createUpdateRequest();
        UserResponse updatedUser = createUserResponse(userId, "updateduser", "PARTICIPANT");

        when(userManagerService.updateUserByAdmin(eq(userId), any(UpdateUserRequest.class)))
                .thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{idUser}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUser").value(userId))
                .andExpect(jsonPath("$.username").value("updateduser"));

        verify(userManagerService).updateUserByAdmin(eq(userId), any(UpdateUserRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateUserById_whenUserNotFound_shouldReturn404() throws Exception {
        // Arrange
        Long userId = 999L;
        UpdateUserRequest updateRequest = createUpdateRequest();

        when(userManagerService.updateUserByAdmin(eq(userId), any(UpdateUserRequest.class)))
                .thenThrow(new NotFoundException("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{idUser}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testUpdateUserById_whenDuplicatedEmail_shouldReturn409() throws Exception {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest updateRequest = createUpdateRequest();

        when(userManagerService.updateUserByAdmin(eq(userId), any(UpdateUserRequest.class)))
                .thenThrow(new DuplicatedEntityException("El email ya está registrado"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{idUser}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());
    }


    // ---------------------- CHANGE USER PASSWORD TESTS -------------------

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testChangeUserPasswordById_success() throws Exception {
        // Arrange
        Long userId = 1L;
        UpdateUserPassword passwordUpdate = new UpdateUserPassword("newPassword123");

        doNothing().when(userManagerService).changeUserPasswordByAdmin(eq(userId), any(UpdateUserPassword.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{idUser}/password", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordUpdate)))
                .andExpect(status().isNoContent());

        verify(userManagerService).changeUserPasswordByAdmin(eq(userId), any(UpdateUserPassword.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testChangeUserPasswordById_whenUserNotFound_shouldReturn404() throws Exception {
        // Arrange
        Long userId = 999L;
        UpdateUserPassword passwordUpdate = new UpdateUserPassword("newPassword123");

        doThrow(new NotFoundException("Usuario no encontrado"))
                .when(userManagerService).changeUserPasswordByAdmin(eq(userId), any(UpdateUserPassword.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{idUser}/password", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordUpdate)))
                .andExpect(status().isNotFound());
    }


    // ---------------------- CREATE CONGRESS ADMIN TESTS -------------------

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testCreateCongressAdmin_success() throws Exception {
        // Arrange
        CreateCongressAdminRequest request = createCongressAdminRequest();
        UserCongressAdminResponse response = createCongressAdminResponse();

        when(userManagerService.createCongressAdmin(any(CreateCongressAdminRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/congress-admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("admin_congress"))
                .andExpect(jsonPath("$.email").value("admin@usac.edu"))
                .andExpect(jsonPath("$.institutionName").value("USAC"));

        verify(userManagerService).createCongressAdmin(any(CreateCongressAdminRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testCreateCongressAdmin_whenUserNotFound_shouldReturn404() throws Exception {
        // Arrange
        CreateCongressAdminRequest request = createCongressAdminRequest();

        when(userManagerService.createCongressAdmin(any(CreateCongressAdminRequest.class)))
                .thenThrow(new NotFoundException("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/congress-admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN_SYSTEM")
    void testCreateCongressAdmin_whenInstitutionNotFound_shouldReturn404() throws Exception {
        // Arrange
        CreateCongressAdminRequest request = createCongressAdminRequest();

        when(userManagerService.createCongressAdmin(any(CreateCongressAdminRequest.class)))
                .thenThrow(new NotFoundException("Institution not found"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/congress-admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }



    // ---------------------- HELPER METHODS -------------------

    private CreateUserRequest createUserRequest() {
        return new CreateUserRequest(
                "john@example.com",
                "John Doe",
                "555-1234",
                "USAC",
                "johndoe",
                "password123",
                "12345678",
                "PARTICIPANT"
        );
    }

    private UpdateUserRequest createUpdateRequest() {
        return new UpdateUserRequest(
                "updateduser",
                "updated@example.com",
                "Updated User",
                "555-5678",
                "UMG",
                "87654321"
        );
    }

    private CreateCongressAdminRequest createCongressAdminRequest() {
        return new CreateCongressAdminRequest(
                "admin_congress",
                "admin@usac.edu",
                "securePassword123",
                "Admin Congress",
                "555-9999",
                "USAC Admin",
                "99999999",
                1L  // institutionId
        );
    }

    private UserResponse createUserResponse(Long id, String username, String roleName) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setIdRole(1);
        roleEntity.setRoleName(roleName);

        UserEntity userEntity = new UserEntity();
        userEntity.setIdUser(id);
        userEntity.setUsername(username);
        userEntity.setEmail(username + "@example.com");
        userEntity.setFullName("Test User");
        userEntity.setPhoneNumber("555-1234");
        userEntity.setOrganization("USAC");
        userEntity.setIdentificationNumber("12345678");
        userEntity.setRole(roleEntity);
        userEntity.setIsActive(true);
        return new UserResponse(
                userEntity
        );
    }

    private UserCongressAdminResponse createCongressAdminResponse() {
        return new UserCongressAdminResponse(
                "admin@usac.edu",
                "Admin Congress",
                "555-9999",
                "USAC Admin",
                "admin_congress",
                "99999999",
                "USAC"
        );
    }
}
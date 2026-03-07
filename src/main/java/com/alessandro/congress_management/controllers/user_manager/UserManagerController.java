package com.alessandro.congress_management.controllers.user_manager;


import com.alessandro.congress_management.dto.user_manager.*;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.DuplicatedEntityException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.services.user_manager.UserManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Manager Controller", description = "Controller for managing users in the system. This controller provides endpoints for creating, listing, updating, and deleting users. Only administrators have access to these endpoints.")
@Slf4j
public class UserManagerController {
    private final UserManagerService userManagerService;

    @Autowired
    public UserManagerController(UserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Create a new user", description = "Creates a new user in the system. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Username, email or identification already registered")
    })
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) throws DuplicatedEntityException, NotFoundException {
        UserResponse userResponse = userManagerService.createUserByAdmin(createUserRequest);
        return ResponseEntity.status(201).body(userResponse);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "List all users", description = "Retrieves a list of all users in the system. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userManagerService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{idUser}/activation")
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Delete a user by ID", description = "Deletes a user from the system based on their ID. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long idUser,@Valid @RequestBody UpdateStatusUser updateStatus) throws DuplicatedEntityException, NotFoundException {
        userManagerService.setUserActiveStatus(idUser, updateStatus);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/{idUser}")
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Update a user by ID", description = "Updates the details of a user based on their ID. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<UserResponse> updateUserById(@PathVariable Long idUser,@Valid @RequestBody UpdateUserRequest updateUserRequest) throws DuplicatedEntityException, NotFoundException {
        UserResponse updatedUser = userManagerService.updateUserByAdmin(idUser, updateUserRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{idUser}/password")
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Change a user's password by ID", description = "Changes the password of a user based on their ID. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> changeUserPasswordById(@PathVariable Long idUser,@Valid @RequestBody UpdateUserPassword updatePassword) throws NotFoundException {
        userManagerService.changeUserPasswordByAdmin(idUser, updatePassword);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/congress-admin")
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Create a new congress administrator", description = "Creates a new congress administrator in the system. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Congress administrator created successfully", content = @Content(schema = @Schema(implementation = UserCongressAdminResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Username, email or identification already registered")
    })
    public ResponseEntity<UserCongressAdminResponse> createCongressAdmin(@Valid @RequestBody CreateCongressAdminRequest createCongressAdminRequest) throws DuplicatedEntityException, NotFoundException {
        UserCongressAdminResponse congressAdminResponse = userManagerService.createCongressAdmin(createCongressAdminRequest);
        return ResponseEntity.status(201).body(congressAdminResponse);
    }

    @PostMapping("/{idUser}/resend-invitation")
    @PreAuthorize("hasRole('ADMIN_SYSTEM')")
    @Operation(summary = "Resend invitation email to a user", description = "Resends the invitation email to a user based on their ID. Only administrators can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation email resent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> resend(@PathVariable Long idUser) throws BusinessRuleException, NotFoundException {
        userManagerService.resendInvitation(idUser);
        return ResponseEntity.ok().build();
    }

}

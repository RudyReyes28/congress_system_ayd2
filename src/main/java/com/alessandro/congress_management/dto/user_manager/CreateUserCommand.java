package com.alessandro.congress_management.dto.user_manager;

public record CreateUserCommand(
        String username,
        String email,
        String password,
        String fullName,
        String phoneNumber,
        String organization,
        String identificationNumber,
        String roleName
) {}

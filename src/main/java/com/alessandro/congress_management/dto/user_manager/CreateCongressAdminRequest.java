package com.alessandro.congress_management.dto.user_manager;

import jakarta.validation.constraints.*;
import lombok.Value;

@Value
public class CreateCongressAdminRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    String email;
    @NotBlank(message = "Full name cannot be blank")
    String fullName;
    @NotBlank(message = "Phone number cannot be blank")
    String phoneNumber;
    @NotBlank(message = "Organization cannot be blank")
    String organization;
    @NotBlank(message = "Username cannot be blank")
    String username;
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password;
    @NotBlank(message = "Identification number cannot be blank")
    String identificationNumber;
    @NotNull(message = "Institution ID cannot be null")
    Long idInstitution;
}

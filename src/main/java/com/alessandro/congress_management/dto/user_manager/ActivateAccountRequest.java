package com.alessandro.congress_management.dto.user_manager;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class ActivateAccountRequest {
    @NotBlank
    String token;
    @NotBlank
    @Size(min = 8, message = "The password must be at least 8 characters long")
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}

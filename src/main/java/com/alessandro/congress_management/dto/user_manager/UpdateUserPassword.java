package com.alessandro.congress_management.dto.user_manager;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UpdateUserPassword {
    @NotBlank(message = "User ID cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String newPassword;
}

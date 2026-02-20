package com.alessandro.congress_management.dto.authenticate;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class LoginRequest {
    @NotBlank(message = "Username cannot be blank")
    String username;
    @NotBlank(message = "Password cannot be blank")
    String password;
}

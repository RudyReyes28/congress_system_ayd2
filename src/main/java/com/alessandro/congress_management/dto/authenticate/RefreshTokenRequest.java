package com.alessandro.congress_management.dto.authenticate;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token cannot be blank")
    String refreshToken;
}

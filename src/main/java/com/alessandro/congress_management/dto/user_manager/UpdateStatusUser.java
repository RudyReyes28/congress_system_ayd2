package com.alessandro.congress_management.dto.user_manager;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class UpdateStatusUser {
    @NotNull
    boolean active;
}
